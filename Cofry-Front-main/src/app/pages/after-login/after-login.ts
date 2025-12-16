import { Component, OnInit, OnDestroy, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { UserService } from '../../services/user.service';
import { AccountService } from '../../services/account.service';
import { TransactionCardComponent } from '../../shared/transaction-card/transaction-card';
import { Transaction } from '../../models/transaction.model';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-after-login',
  standalone: true,
  imports: [CommonModule, TransactionCardComponent, ReactiveFormsModule],
  templateUrl: './after-login.html',
  styleUrls: ['./after-login.css']
})
export class AfterLogin implements OnInit, OnDestroy {
  // Inicialização do array de transações
  transactions: Transaction[] = [];

  // Injeção de dependências
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private userService = inject(UserService);
  private accountService = inject(AccountService);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);
  private fb = inject(FormBuilder);
  private routerSubscription?: Subscription;

  // Propriedades de estado do componente
  showBalance = true;
  userData: any = null;
  balance = '0,00'; // Valor padrão, será carregado da API (formato string para exibição)
  balanceValue = 0; // Valor numérico do saldo (para validações)
  isLoadingBalance = false;
  
  // Estados para formulário de transação
  showTransactionForm = false;
  isEditingTransaction = false;
  editingTransactionId: number | null = null;
  transactionForm!: FormGroup;
  userAccountId: number | null = null; // ID da conta ativa do usuário
  
  // Opções para o formulário
  transactionCategories = [
    { value: 'MERCADO', label: 'Mercado' },
    { value: 'STREAMING', label: 'Streaming' },
    { value: 'TRANSPORTE', label: 'Transporte' },
    { value: 'OUTROS', label: 'Outros' }
  ];
  
  paymentMethods = [
    { value: 'PIX', label: 'PIX' },
    { value: 'Débito', label: 'Débito' },
    { value: 'Crédito', label: 'Crédito' },
    { value: 'Dinheiro', label: 'Dinheiro' },
    { value: 'Transferência', label: 'Transferência' }
  ];

  ngOnInit(): void {
    // Inicializa o formulário de transação
    this.transactionForm = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      categoria: ['OUTROS', [Validators.required]],
      categoriaOutros: [''], // Campo para descrição quando categoria é OUTROS
      forma_pagamento: ['PIX', [Validators.required]],
      isIncome: [false], // false = saída (padrão), true = entrada
      valor: ['', [Validators.required, this.customValueValidator.bind(this)]],
      dataOption: ['today'], // 'today' ou 'other'
      data_hora: [new Date().toISOString().slice(0, 16)] // Só valida se dataOption for 'other'
    });

    // Escuta mudanças na categoria para validar campo "categoriaOutros"
    this.transactionForm.get('categoria')?.valueChanges.subscribe(categoria => {
      const categoriaOutrosControl = this.transactionForm.get('categoriaOutros');
      if (categoria === 'OUTROS') {
        categoriaOutrosControl?.setValidators([Validators.required, Validators.minLength(3)]);
      } else {
        categoriaOutrosControl?.clearValidators();
        categoriaOutrosControl?.setValue('');
      }
      categoriaOutrosControl?.updateValueAndValidity();
    });

    // Escuta mudanças na opção de data para validar campo "data_hora"
    this.transactionForm.get('dataOption')?.valueChanges.subscribe(dataOption => {
      const dataHoraControl = this.transactionForm.get('data_hora');
      if (dataOption === 'other') {
        dataHoraControl?.setValidators([Validators.required]);
      } else {
        dataHoraControl?.clearValidators();
        // Define data de hoje quando seleciona "today"
        dataHoraControl?.setValue(new Date().toISOString().slice(0, 16));
      }
      dataHoraControl?.updateValueAndValidity();
    });
    if (!isPlatformBrowser(this.platformId)) {
      return; // Não faz nada no SSR
    }

    const userId = localStorage.getItem('userId');
    const storedUserData = localStorage.getItem('userData');

    if (storedUserData) {
      try {
        this.userData = JSON.parse(storedUserData);
        this.cdr.detectChanges(); // Força detecção após carregar dados do localStorage
      } catch (error) {
        console.error('Erro ao parsear dados do usuário:', error);
      }
    }

    if (userId) {
      // Carrega saldo primeiro para garantir que apareça rapidamente
      this.loadAccountBalance(Number(userId));
      this.loadUserData(Number(userId));
      // Carrega a conta ativa para obter o sourceAccountId
      this.loadUserAccount(Number(userId));
    }

    // Carregar transações (chamada principal)
    console.log('Iniciando carregamento de transações no ngOnInit...');
    this.loadTransactions();
    
    // Escuta mudanças de rota para recarregar dados quando necessário
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        const userId = localStorage.getItem('userId');
        if (userId && this.router.url.includes('/Home')) {
          // Recarrega o saldo quando volta para a página Home
          this.loadAccountBalance(Number(userId));
        }
      });
    
    // Força uma detecção de mudanças final após inicializar tudo
    setTimeout(() => {
      this.cdr.detectChanges();
    }, 100);
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  loadUserData(userId: number): void {
    this.userService.getUserById(userId).subscribe({
      next: (user) => {
        this.userData = user;
        localStorage.setItem('userData', JSON.stringify(user));
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error) => {
        console.error('Erro ao carregar dados do usuário:', error);
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  // Carrega a conta ativa do usuário para obter o accountId
  loadUserAccount(userId: number): void {
    this.accountService.getAccountsByUserId(userId).subscribe({
      next: (accounts) => {
        if (accounts && accounts.length > 0) {
          // Pega a primeira conta ativa ou a primeira conta disponível
          const activeAccount = accounts.find(acc => acc.status === 'ACTIVE') || accounts[0];
          this.userAccountId = activeAccount.accountId;
        }
      },
      error: (error) => {
        console.error('Erro ao carregar conta do usuário:', error);
      }
    });
  }

  loadAccountBalance(userId: number): void {
    this.isLoadingBalance = true;
    this.cdr.detectChanges(); // Atualiza para mostrar "Carregando..."
    
    this.accountService.getActiveAccountByUserId(userId).subscribe({
      next: (activeAccount) => {
        let balanceValue = 0;
        
        if (activeAccount) {
          balanceValue = activeAccount.balance || 0;
          this.userAccountId = activeAccount.accountId; // Armazena o ID da conta ativa
        }
        
        // Armazena o valor numérico para validações
        this.balanceValue = balanceValue;
        
        // Formata o saldo sempre, mesmo se for 0
        this.balance = balanceValue.toLocaleString('pt-BR', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });
        
        this.isLoadingBalance = false;
        this.cdr.detectChanges(); // Força detecção de mudanças
        
        // Força uma segunda detecção após um pequeno delay para garantir
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 50);
      },
      error: (error) => {
        console.error('Erro ao carregar saldo:', error);
        this.balance = '0,00'; // Define valor padrão em caso de erro
        this.isLoadingBalance = false;
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
        
        // Força uma segunda detecção após um pequeno delay
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 50);
      }
    });
  }

  // FUNÇÃO AJUSTADA PARA ORDENAR E LIMITAR AS TRANSAÇÕES
  loadTransactions(): void {
    console.log('Carregando transações...');
    this.transactionService.getTransactionsByUser().subscribe({
      next: (data) => {
        console.log('Transações recebidas da API:', data);
        console.log('Total de transações recebidas:', data.length);
        
        // REMOVIDO: Filtro que removia transações com "teste" na descrição
        // Agora exibe todas as transações retornadas pela API
        const filteredData = data; // Não filtra mais

        console.log('Transações após processamento:', filteredData);
        console.log('Total de transações após processamento:', filteredData.length);

        // 1. Ordena as transações pela data de criação (mais recente primeiro)
        const sortedData = filteredData.sort((a, b) => {
          const dateA = new Date(a.data_hora || a.transactionDate || 0);
          const dateB = new Date(b.data_hora || b.transactionDate || 0);
          return dateB.getTime() - dateA.getTime();
        });

        console.log('Transações ordenadas:', sortedData);
        console.log('Total de transações ordenadas:', sortedData.length);

        // 2. Exibe todas as transações (sem limite, mas ordenadas)
        this.transactions = sortedData;
        console.log('Transações atribuídas ao componente:', this.transactions);
        console.log('Total de transações no componente:', this.transactions.length);
        this.cdr.detectChanges(); // Força detecção de mudanças
        
        // Força uma segunda detecção após um pequeno delay
        setTimeout(() => {
          this.cdr.detectChanges();
          console.log('Detecção de mudanças forçada novamente');
        }, 50);
      },
      error: (error) => {
        console.error('Erro ao carregar transações:', error);
        console.error('Detalhes do erro:', {
          message: error.message,
          status: error.status,
          error: error.error
        });
        this.transactions = []; // Limpa a lista em caso de erro
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }
  goToPix(): void {
    this.router.navigate(['/nav/Pix']);
  }

  goToPagar(): void {
    this.router.navigate(['/nav/Pagar']);
  }

  goToTransferir(): void {
    this.router.navigate(['/nav/Transferir']);
  }

  goToExtrato(): void {
    this.router.navigate(['/nav/Extrato']);
  }

  goToCards(): void {
    this.router.navigate(['/nav/Cards']);
  }

  goToInvest(): void {
    this.router.navigate(['/nav/Invest']);
  }

  goToBoletos(): void {
    this.router.navigate(['/nav/Dda']);
  }

  goToProfile(): void {
    this.router.navigate(['/nav/Profile']);
  }

  toggleBalance(): void {
    this.showBalance = !this.showBalance;
  }

  // Abre o formulário para criar nova transação
  openNewTransactionForm(): void {
    this.isEditingTransaction = false;
    this.editingTransactionId = null;
    this.transactionForm.reset({
      descricao: '',
      categoria: 'OUTROS',
      categoriaOutros: '',
      forma_pagamento: 'PIX',
      isIncome: false, // Padrão: saída de dinheiro
      valor: '',
      dataOption: 'today', // Padrão: data de hoje
      data_hora: new Date().toISOString().slice(0, 16)
    });
    this.showTransactionForm = true;
    this.cdr.detectChanges();
  }

  // Abre o formulário para editar transação existente
  openEditTransactionForm(transaction: Transaction): void {
    // Valida se a transação tem ID
    if (!transaction || !transaction.id) {
      alert('Erro: transação inválida. Não é possível editar.');
      console.error('Transaction sem ID:', transaction);
      return;
    }

    this.isEditingTransaction = true;
    this.editingTransactionId = transaction.id;
    
    // Converte a data para o formato do input datetime-local
    const dateTime = new Date(transaction.data_hora);
    const formattedDateTime = new Date(dateTime.getTime() - dateTime.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);
    
    // Mapeia o tipo antigo para forma de pagamento se necessário
    let formaPagamento = transaction.forma_pagamento;
    if (!formaPagamento && transaction.tipo) {
      // Se não houver forma_pagamento, usa o tipo como fallback
      const tipoMap: { [key: string]: string } = {
        'PIX': 'PIX',
        'DEBITO': 'Débito',
        'CREDITO': 'Crédito'
      };
      formaPagamento = tipoMap[transaction.tipo] || 'PIX';
    }
    
    // Se a categoria for OUTROS e a descrição contiver informação adicional, extrai ela
    let categoriaOutros = '';
    let descricaoPrincipal = transaction.descricao;
    
    if (transaction.categoria === 'OUTROS') {
      // Tenta extrair a descrição adicional se estiver no formato "descrição - categoriaOutros"
      const descParts = transaction.descricao.split(' - ');
      if (descParts.length > 1) {
        descricaoPrincipal = descParts[0]; // Primeira parte é a descrição principal
        categoriaOutros = descParts.slice(1).join(' - '); // Pega tudo após o primeiro " - "
      }
    }
    
    // Determina isIncome baseado no tipo da transação
    // Se o valor for positivo e tipo for CREDITO, provavelmente é entrada
    // Caso contrário, é saída
    const isIncome = transaction.tipo === 'CREDITO' && transaction.valor > 0;
    
    // Verifica se a data é de hoje ou outra data
    const transactionDate = new Date(transaction.data_hora);
    const today = new Date();
    const isToday = transactionDate.toDateString() === today.toDateString();
    
    this.transactionForm.patchValue({
      descricao: descricaoPrincipal,
      categoria: transaction.categoria,
      categoriaOutros: categoriaOutros,
      forma_pagamento: formaPagamento,
      isIncome: isIncome,
      valor: transaction.valor,
      dataOption: isToday ? 'today' : 'other',
      data_hora: formattedDateTime
    });
    
    this.showTransactionForm = true;
    this.cdr.detectChanges();
  }

  // Fecha o formulário
  closeTransactionForm(): void {
    this.showTransactionForm = false;
    this.isEditingTransaction = false;
    this.editingTransactionId = null;
    this.transactionForm.reset();
    this.cdr.detectChanges();
  }

  // Salva a transação (criar ou editar)
  saveTransaction(): void {
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    const formValue = this.transactionForm.value;
    const userId = localStorage.getItem('userId');
    
    if (!userId) {
      alert('Erro: usuário não identificado. Faça login novamente.');
      return;
    }

    // Valida e converte o valor
    // Trata diferentes formatos: número, string com ponto, string com vírgula
    let valorStr = String(formValue.valor || '').trim();
    
    // Remove formatação de moeda se houver
    valorStr = valorStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    // Remove pontos (separadores de milhar) mas mantém o último ponto/vírgula como decimal
    // Se tiver vírgula, substitui por ponto
    if (valorStr.includes(',')) {
      // Remove pontos (separadores de milhar) e substitui vírgula por ponto
      valorStr = valorStr.replace(/\./g, '').replace(',', '.');
    }
    
    // Converte para número
    const valor = parseFloat(valorStr);
    
    // Validação rigorosa
    if (isNaN(valor) || !isFinite(valor) || valor <= 0) {
      alert('O valor da transação deve ser maior que zero!');
      this.transactionForm.get('valor')?.setErrors({ invalid: true });
      this.transactionForm.get('valor')?.markAsTouched();
      return;
    }
    
    // Garante que o valor seja um número válido (não string)
    const valorFinal = Number(valor.toFixed(2)); // Arredonda para 2 casas decimais

    // Valida se há conta ativa
    if (!this.userAccountId) {
      alert('Erro: nenhuma conta ativa encontrada. Por favor, crie uma conta primeiro.');
      return;
    }

    // Validação de saldo no frontend (melhora UX, mas o backend também valida)
    // Só valida para transações de saída (isIncome = false)
    if (!formValue.isIncome && !this.isEditingTransaction) {
      // Para criar nova transação de saída, verifica saldo
      if (valorFinal > this.balanceValue) {
        alert(`Saldo insuficiente!\nSaldo atual: R$ ${this.balance}\nValor necessário: R$ ${valorFinal.toFixed(2).replace('.', ',')}`);
        return;
      }
    }

    // Se categoria for OUTROS, adiciona a descrição adicional à descrição principal
    let descricaoFinal = formValue.descricao.trim();
    if (formValue.categoria === 'OUTROS' && formValue.categoriaOutros?.trim()) {
      descricaoFinal = `${descricaoFinal} - ${formValue.categoriaOutros.trim()}`;
    }

    // Obtém a data baseado na opção selecionada
    let transactionDate: string;
    if (formValue.dataOption === 'today') {
      // Se for hoje, usa a data/hora atual
      transactionDate = new Date().toISOString();
    } else {
      // Se for outra data, usa a data selecionada
      transactionDate = new Date(formValue.data_hora).toISOString();
    }

    // Prepara os dados da transação conforme nova API do backend
    // O backend determina automaticamente o transactionType baseado em:
    // 1. destinationAccountId (se tiver → TRANSFER)
    // 2. isIncome = true (→ DEPOSIT)
    // 3. isIncome = false ou omitido (→ PAYMENT)
    const transactionData: any = {
      sourceAccountId: this.userAccountId, // Obrigatório
      amount: valorFinal, // Obrigatório - número válido
      description: descricaoFinal, // Obrigatório
      isIncome: formValue.isIncome || false, // false = saída, true = entrada
      transactionDate: transactionDate, // Opcional - data da transação
      // Não enviamos transactionType - o backend determina automaticamente
      // Mantemos campos antigos para compatibilidade (serão ignorados pelo backend)
      descricao: descricaoFinal,
      valor: valorFinal,
      categoria: formValue.categoria,
      forma_pagamento: formValue.forma_pagamento,
      data_hora: transactionDate
    };
    
    // Validação final antes de enviar
    if (!transactionData.amount || transactionData.amount <= 0 || !isFinite(transactionData.amount)) {
      alert('Erro: valor inválido. O valor deve ser um número maior que zero.');
      return;
    }
    
    // Debug: log dos dados antes de enviar (pode remover em produção)
    console.log('Dados da transação a serem enviados:', {
      ...transactionData,
      amount_tipo: typeof transactionData.amount,
      amount_valor: transactionData.amount,
      amount_é_número: typeof transactionData.amount === 'number'
    });

    if (this.isEditingTransaction && this.editingTransactionId) {
      // Atualiza transação existente
      this.transactionService.updateTransaction(this.editingTransactionId, transactionData).subscribe({
        next: () => {
          alert('Movimentação atualizada com sucesso!');
          this.closeTransactionForm();
          // Recarrega as transações e força detecção de mudanças
          this.loadTransactions();
          // Aguarda um pequeno delay para garantir que o backend atualizou o saldo antes de recarregar
          setTimeout(() => {
            this.loadAccountBalance(Number(userId));
            this.cdr.detectChanges();
          }, 300);
        },
        error: (error) => {
          console.error('Erro ao atualizar transação:', error);
          let errorMessage = 'Erro ao atualizar movimentação. Tente novamente.';
          
          // Tratamento específico para erro de saldo insuficiente
          if (error.status === 400) {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            // Se a mensagem contém "saldo insuficiente", destaca isso
            if (errorMessage.toLowerCase().includes('saldo insuficiente')) {
              alert(`⚠️ ${errorMessage}\n\nPor favor, verifique seu saldo e tente novamente.`);
            } else {
              alert(errorMessage);
            }
          } else {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            alert(errorMessage);
          }
        }
      });
    } else {
      // Cria nova transação
      this.transactionService.createTransaction(transactionData).subscribe({
        next: () => {
          alert('Movimentação criada com sucesso!');
          this.closeTransactionForm();
          // Recarrega as transações e força detecção de mudanças
          this.loadTransactions();
          // Aguarda um pequeno delay para garantir que o backend atualizou o saldo antes de recarregar
          // O backend atualiza o saldo automaticamente, mas precisamos aguardar a atualização
          setTimeout(() => {
            this.loadAccountBalance(Number(userId));
            // Força detecção de mudanças após um pequeno delay para garantir que a lista seja atualizada
            setTimeout(() => {
              this.cdr.detectChanges();
            }, 100);
          }, 300);
        },
        error: (error) => {
          console.error('Erro ao criar transação:', error);
          let errorMessage = 'Erro ao criar movimentação. Tente novamente.';
          
          // Tratamento específico para erro de saldo insuficiente (400 Bad Request)
          if (error.status === 400) {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            // Se a mensagem contém "saldo insuficiente", destaca isso
            if (errorMessage.toLowerCase().includes('saldo insuficiente')) {
              alert(`⚠️ ${errorMessage}\n\nPor favor, verifique seu saldo e tente novamente.`);
            } else {
              alert(errorMessage);
            }
          } else {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
            alert(errorMessage);
          }
        }
      });
    }
  }

  // Exclui uma transação
  deleteTransaction(transaction: Transaction): void {
    // Valida se a transação tem ID
    if (!transaction || !transaction.id) {
      alert('Erro: transação inválida. Não é possível excluir.');
      console.error('Transaction sem ID:', transaction);
      return;
    }

    if (!confirm(`Tem certeza que deseja excluir a movimentação "${transaction.descricao}"?`)) {
      return;
    }

    this.transactionService.deleteTransaction(transaction.id).subscribe({
      next: () => {
        alert('Movimentação excluída com sucesso!');
        // Recarrega as transações e força detecção de mudanças
        this.loadTransactions();
        const userId = localStorage.getItem('userId');
        if (userId) {
          // Aguarda um pequeno delay para garantir que o backend atualizou o saldo antes de recarregar
          setTimeout(() => {
            this.loadAccountBalance(Number(userId));
            // Força detecção de mudanças após um pequeno delay
            setTimeout(() => {
              this.cdr.detectChanges();
            }, 100);
          }, 300);
        }
      },
      error: (error) => {
        console.error('Erro ao excluir transação:', error);
        let errorMessage = 'Erro ao excluir movimentação. Tente novamente.';
        if (error.error?.error) {
          errorMessage = error.error.error;
        }
        alert(errorMessage);
      }
    });
  }

  // Verifica se um campo do formulário é inválido
  isFieldInvalid(fieldName: string): boolean {
    const field = this.transactionForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // Validador customizado para valores monetários
  customValueValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { required: true };
    }
    
    let valueStr = String(control.value).trim();
    
    // Remove formatação de moeda
    valueStr = valueStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    // Remove pontos (separadores de milhar) e substitui vírgula por ponto
    if (valueStr.includes(',')) {
      valueStr = valueStr.replace(/\./g, '').replace(',', '.');
    }
    
    const value = parseFloat(valueStr);
    
    if (isNaN(value) || !isFinite(value) || value <= 0) {
      return { invalid: true };
    }
    
    return null;
  }

  // Formata o input de valor enquanto o usuário digita
  formatCurrencyInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value;
    
    // Remove tudo que não é número, vírgula ou ponto
    value = value.replace(/[^\d,.]/g, '');
    
    // Se tiver mais de uma vírgula ou ponto, mantém apenas o primeiro
    const commaIndex = value.indexOf(',');
    const dotIndex = value.indexOf('.');
    
    if (commaIndex !== -1 && dotIndex !== -1) {
      // Se tiver ambos, mantém o que aparecer primeiro
      if (commaIndex < dotIndex) {
        value = value.replace(/\./g, '');
      } else {
        value = value.replace(/,/g, '');
      }
    }
    
    // Limita a 2 casas decimais
    if (commaIndex !== -1) {
      const parts = value.split(',');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + ',' + parts[1].substring(0, 2);
      }
    } else if (dotIndex !== -1) {
      const parts = value.split('.');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + '.' + parts[1].substring(0, 2);
      }
    }
    
    // Atualiza o valor do input
    input.value = value;
    this.transactionForm.patchValue({ valor: value }, { emitEvent: true });
  }
}