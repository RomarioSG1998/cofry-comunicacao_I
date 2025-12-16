import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { TransactionService, TransactionFilters } from '../../services/transaction.service';
import { Transaction } from '../../models/transaction.model';
import { TransactionCardComponent } from '../../shared/transaction-card/transaction-card';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-extrato',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TransactionCardComponent],
  templateUrl: './extrato.html',
  styleUrls: ['./extrato.css']
})
export class Extrato implements OnInit {
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private accountService = inject(AccountService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  filterForm: FormGroup;
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  isLoading = false;
  userId: number | null = null;
  
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
    { value: 'Dinheiro', label: 'Dinheiro' }
  ];

  // Opções de filtro
  transactionTypes = [
    { value: '', label: 'Todos' },
    { value: 'PIX', label: 'PIX' },
    { value: 'DEBITO', label: 'Débito' },
    { value: 'CREDITO', label: 'Crédito' }
  ];

  categories = [
    { value: '', label: 'Todas' },
    { value: 'MERCADO', label: 'Mercado' },
    { value: 'STREAMING', label: 'Streaming' },
    { value: 'TRANSPORTE', label: 'Transporte' },
    { value: 'OUTROS', label: 'Outros' }
  ];

  constructor() {
    this.filterForm = this.fb.group({
      startDate: [''],
      endDate: [''],
      type: [''],
      category: ['']
    });
  }

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

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

    const userIdStr = localStorage.getItem('userId');
    if (userIdStr) {
      this.userId = Number(userIdStr);
      // Carrega a conta ativa para obter o sourceAccountId
      this.loadUserAccount(Number(userIdStr));
      // Carrega transações imediatamente ao inicializar
      this.loadTransactions();
    } else {
      this.router.navigate(['/login']);
      return;
    }
  }
  
  // Carrega a conta ativa do usuário (mesma conta usada em todas as páginas)
  loadUserAccount(userId: number): void {
    this.accountService.getActiveAccountByUserId(userId).subscribe({
      next: (activeAccount) => {
        if (activeAccount) {
          this.userAccountId = activeAccount.accountId;
        } else {
          this.userAccountId = null;
        }
      },
      error: (error) => {
        console.error('Erro ao carregar conta ativa do usuário:', error);
        this.userAccountId = null;
      }
    });
  }

  loadTransactions(): void {
    if (!this.userId) {
      console.error('UserId não encontrado');
      return;
    }

    console.log('Extrato: Carregando transações para userId:', this.userId);
    this.isLoading = true;
    
    // Usa o mesmo método que funciona na página after-login
    this.transactionService.getTransactionsByUser().subscribe({
      next: (data) => {
        console.log('Extrato: Transações recebidas da API:', data);
        console.log('Extrato: Total de transações:', data.length);
        
        // REMOVIDO: Filtro que removia transações com "teste" na descrição
        // Agora exibe todas as transações retornadas pela API
        const filteredData = data; // Não filtra mais

        // Ordena por data (mais recente primeiro)
        this.transactions = filteredData.sort((a, b) => {
          const dateA = new Date(a.data_hora || a.transactionDate || 0);
          const dateB = new Date(b.data_hora || b.transactionDate || 0);
          return dateB.getTime() - dateA.getTime();
        });
        
        console.log('Extrato: Transações ordenadas:', this.transactions.length);
        
        // Aplica filtros adicionais (data, tipo, categoria)
        this.applyFilters();
        
        console.log('Extrato: Transações após aplicar filtros:', this.filteredTransactions.length);
        
        this.isLoading = false;
        this.cdr.detectChanges(); // Força detecção de mudanças
        
        // Força uma segunda detecção após um pequeno delay
        setTimeout(() => {
          this.cdr.detectChanges();
          console.log('Extrato: Detecção de mudanças forçada novamente');
        }, 50);
      },
      error: (error) => {
        console.error('Extrato: Erro ao carregar transações:', error);
        this.transactions = []; // Limpa a lista em caso de erro
        this.filteredTransactions = [];
        this.isLoading = false;
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
        
        // Tenta usar o método alternativo com userId direto
        const filters: TransactionFilters = { userId: this.userId ?? undefined };
        console.log('Extrato: Tentando método alternativo com filters:', filters);
        this.transactionService.getTransactions(filters).subscribe({
          next: (data) => {
            console.log('Extrato: Transações recebidas (método alternativo):', data);
            this.transactions = data.sort((a, b) => {
              const dateA = new Date(a.data_hora || a.transactionDate || 0);
              const dateB = new Date(b.data_hora || b.transactionDate || 0);
              return dateB.getTime() - dateA.getTime();
            });
            this.applyFilters();
            this.isLoading = false;
            this.cdr.detectChanges();
            setTimeout(() => {
              this.cdr.detectChanges();
            }, 50);
          },
          error: (err) => {
            console.error('Extrato: Erro ao carregar transações (método alternativo):', err);
            alert('Erro ao carregar extrato. Tente novamente.');
          }
        });
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.transactions];

    // Filtro por data
    const startDate = this.filterForm.get('startDate')?.value;
    const endDate = this.filterForm.get('endDate')?.value;
    
    if (startDate) {
      const start = new Date(startDate);
      start.setHours(0, 0, 0, 0);
      filtered = filtered.filter(t => {
        const transactionDate = new Date(t.data_hora);
        transactionDate.setHours(0, 0, 0, 0);
        return transactionDate >= start;
      });
    }
    
    if (endDate) {
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999);
      filtered = filtered.filter(t => {
        const transactionDate = new Date(t.data_hora);
        transactionDate.setHours(23, 59, 59, 999);
        return transactionDate <= end;
      });
    }

    // Filtro por tipo (se o campo tipo ainda existir no modelo)
    const type = this.filterForm.get('type')?.value;
    if (type) {
      // Tenta filtrar por tipo se disponível
      filtered = filtered.filter(t => {
        // Verifica se o tipo da transação corresponde ao filtro
        // Como o backend pode ter mudado, verificamos tanto 'tipo' quanto 'transactionType'
        const transactionType = (t as any).tipo || (t as any).transactionType;
        if (type === 'PIX') return transactionType === 'PIX' || transactionType === 'TRANSFER';
        if (type === 'DEBITO') return transactionType === 'DEBITO' || transactionType === 'WITHDRAWAL' || transactionType === 'PAYMENT';
        if (type === 'CREDITO') return transactionType === 'CREDITO' || transactionType === 'DEPOSIT';
        return true;
      });
    }

    // Filtro por categoria
    const category = this.filterForm.get('category')?.value;
    if (category) {
      filtered = filtered.filter(t => t.categoria === category);
    }

    this.filteredTransactions = filtered;
    this.cdr.detectChanges(); // Força detecção de mudanças após aplicar filtros
    
    // Força uma segunda detecção após um pequeno delay
    setTimeout(() => {
      this.cdr.detectChanges();
    }, 50);
  }

  clearFilters(): void {
    this.filterForm.reset({
      startDate: '',
      endDate: '',
      type: '',
      category: ''
    });
    this.loadTransactions();
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getTotalAmount(): number {
    return this.filteredTransactions.reduce((sum, t) => {
      // Verifica se é entrada (crédito/depósito) ou saída (débito/pagamento)
      const transactionType = (t as any).tipo || (t as any).transactionType;
      const isIncome = (t as any).isIncome || transactionType === 'CREDITO' || transactionType === 'DEPOSIT';
      
      if (isIncome) {
        return sum + (t.valor || 0);
      } else {
        return sum - (t.valor || 0);
      }
    }, 0);
  }

  goBack(): void {
    this.router.navigate(['/nav/Home']);
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
    const dateTime = new Date(transaction.data_hora || transaction.transactionDate || new Date());
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
    const transactionDate = new Date(transaction.data_hora || transaction.transactionDate || new Date());
    const today = new Date();
    const isToday = transactionDate.toDateString() === today.toDateString();
    
    this.transactionForm.patchValue({
      descricao: descricaoPrincipal,
      categoria: transaction.categoria,
      categoriaOutros: categoriaOutros,
      forma_pagamento: formaPagamento,
      isIncome: isIncome,
      valor: Math.abs(transaction.valor), // Usa valor absoluto
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

  // Salva a transação (editar)
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
    let valorStr = String(formValue.valor || '').trim();
    
    // Remove formatação de moeda se houver
    valorStr = valorStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    // Remove pontos (separadores de milhar) mas mantém o último ponto/vírgula como decimal
    if (valorStr.includes(',')) {
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
    
    // Garante que o valor seja um número válido
    const valorFinal = Number(valor.toFixed(2));

    // Valida se há conta ativa
    if (!this.userAccountId) {
      alert('Erro: nenhuma conta ativa encontrada. Por favor, crie uma conta primeiro.');
      return;
    }

    // Se categoria for OUTROS, adiciona a descrição adicional à descrição principal
    let descricaoFinal = formValue.descricao.trim();
    if (formValue.categoria === 'OUTROS' && formValue.categoriaOutros?.trim()) {
      descricaoFinal = `${descricaoFinal} - ${formValue.categoriaOutros.trim()}`;
    }

    // Obtém a data baseado na opção selecionada
    let transactionDate: string;
    if (formValue.dataOption === 'today') {
      transactionDate = new Date().toISOString();
    } else {
      transactionDate = new Date(formValue.data_hora).toISOString();
    }

    // Prepara os dados da transação
    const transactionData: any = {
      sourceAccountId: this.userAccountId,
      amount: valorFinal,
      description: descricaoFinal,
      isIncome: formValue.isIncome || false,
      transactionDate: transactionDate,
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

    if (this.isEditingTransaction && this.editingTransactionId) {
      // Atualiza transação existente
      this.transactionService.updateTransaction(this.editingTransactionId, transactionData).subscribe({
        next: () => {
          alert('Movimentação atualizada com sucesso!');
          this.closeTransactionForm();
          // Recarrega as transações
          this.loadTransactions();
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Erro ao atualizar transação:', error);
          let errorMessage = 'Erro ao atualizar movimentação. Tente novamente.';
          
          if (error.status === 400) {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
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
        // Recarrega as transações
        this.loadTransactions();
        this.cdr.detectChanges();
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

  // Verifica se um campo do formulário é inválido
  isFieldInvalid(fieldName: string): boolean {
    const field = this.transactionForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
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
    if (value.includes(',')) {
      const parts = value.split(',');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + ',' + parts[1].substring(0, 2);
      }
    } else if (value.includes('.')) {
      const parts = value.split('.');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + '.' + parts[1].substring(0, 2);
      }
    }
    
    this.transactionForm.patchValue({ valor: value }, { emitEvent: false });
  }
}
