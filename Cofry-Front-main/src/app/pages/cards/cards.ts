import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { Transaction } from '../../models/transaction.model';
import { TransactionCardComponent } from '../../shared/transaction-card/transaction-card';
import { AccountService } from '../../services/account.service';
import { CardService, Card, CardType } from '../../services/card.service';

@Component({
  selector: 'app-cards',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TransactionCardComponent],
  templateUrl: './cards.html',
  styleUrl: './cards.css',
})
export class Cards implements OnInit {
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private accountService = inject(AccountService);
  private cardService = inject(CardService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  transactions: Transaction[] = [];
  isLoading = false;
  userId: number | null = null;
  
  // Estados para cartões
  cards: Card[] = [];
  isLoadingCards = false;
  showCardForm = false;
  isEditingCard = false;
  editingCardId: number | null = null;
  cardForm!: FormGroup;
  cardTypes: CardType[] = [];
  userAccounts: any[] = [];
  canScrollLeft = false;
  canScrollRight = false;
  
  @ViewChild('cardsContainer', { static: false }) cardsContainer!: ElementRef<HTMLDivElement>;
  
  // Estados para formulário de transação
  showTransactionForm = false;
  isEditingTransaction = false;
  editingTransactionId: number | null = null;
  transactionForm!: FormGroup;
  userAccountId: number | null = null;
  
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

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Inicializa o formulário de transação
    this.transactionForm = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      categoria: ['OUTROS', [Validators.required]],
      categoriaOutros: [''],
      forma_pagamento: ['PIX', [Validators.required]],
      isIncome: [false],
      valor: ['', [Validators.required, this.customValueValidator.bind(this)]],
      dataOption: ['today'],
      data_hora: [new Date().toISOString().slice(0, 16)]
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

    // Escuta mudanças na opção de data
    this.transactionForm.get('dataOption')?.valueChanges.subscribe(dataOption => {
      const dataHoraControl = this.transactionForm.get('data_hora');
      if (dataOption === 'other') {
        dataHoraControl?.setValidators([Validators.required]);
      } else {
        dataHoraControl?.clearValidators();
        dataHoraControl?.setValue(new Date().toISOString().slice(0, 16));
      }
      dataHoraControl?.updateValueAndValidity();
    });

    // Inicializa o formulário de cartão
    this.cardForm = this.fb.group({
      cardNumber: ['', [Validators.required, this.cardNumberValidator.bind(this)]],
      cardHolderName: ['', [Validators.required, Validators.minLength(3)]],
      expiryDate: ['', [Validators.required, this.expiryDateValidator.bind(this)]],
      cvv: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(4)]],
      cardType: ['CREDIT', [Validators.required]],
      brand: [''],
      accountId: [null],
      limitAmount: ['']
    });

    // Validação condicional: limitAmount obrigatório apenas para CREDIT
    this.cardForm.get('cardType')?.valueChanges.subscribe(cardType => {
      const limitAmountControl = this.cardForm.get('limitAmount');
      if (cardType === 'CREDIT') {
        limitAmountControl?.setValidators([Validators.required, this.limitAmountValidator.bind(this)]);
      } else {
        limitAmountControl?.clearValidators();
        limitAmountControl?.setValue('');
      }
      limitAmountControl?.updateValueAndValidity();
    });

    const userIdStr = localStorage.getItem('userId');
    if (userIdStr) {
      this.userId = Number(userIdStr);
      this.loadUserAccount(Number(userIdStr));
      this.loadCardTypes();
      this.loadCards();
      this.loadTransactions();
    } else {
      this.router.navigate(['/login']);
      return;
    }
  }

  // Carrega a conta ativa do usuário (mesma conta usada em todas as páginas)
  loadUserAccount(userId: number): void {
    // Carrega todas as contas para o select de cartões
    this.accountService.getAccountsByUserId(userId).subscribe({
      next: (accounts) => {
        this.userAccounts = accounts;
      },
      error: (error) => {
        console.error('Erro ao carregar contas do usuário:', error);
      }
    });

    // Carrega a conta ativa para transações (mesma conta usada em todas as páginas)
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

  // Carrega os tipos de cartão
  loadCardTypes(): void {
    this.cardService.getCardTypes().subscribe({
      next: (types) => {
        this.cardTypes = types;
      },
      error: (error) => {
        console.error('Erro ao carregar tipos de cartão:', error);
      }
    });
  }

  // Carrega os cartões do usuário
  loadCards(): void {
    if (!this.userId) {
      console.error('Cards: userId não encontrado');
      return;
    }

    console.log('Cards: Carregando cartões para userId:', this.userId);
    this.isLoadingCards = true;
    this.cardService.getCardsByUserId(this.userId).subscribe({
      next: (cards) => {
        console.log('Cards: Cartões recebidos:', cards);
        this.cards = cards || [];
        this.isLoadingCards = false;
        this.cdr.detectChanges();
        // Verifica scroll após carregar
        setTimeout(() => this.checkScrollButtons(), 100);
      },
      error: (error) => {
        console.error('Cards: Erro ao carregar cartões:', error);
        console.error('Cards: Status:', error.status);
        console.error('Cards: Erro detalhado:', error.error);
        this.cards = [];
        this.isLoadingCards = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Verifica se pode rolar para os lados
  checkScrollButtons(): void {
    if (!this.cardsContainer) {
      return;
    }
    const container = this.cardsContainer.nativeElement;
    this.canScrollLeft = container.scrollLeft > 0;
    this.canScrollRight = container.scrollLeft < (container.scrollWidth - container.clientWidth);
    this.cdr.detectChanges();
  }

  // Rola para a esquerda
  scrollLeft(): void {
    if (this.cardsContainer) {
      const container = this.cardsContainer.nativeElement;
      container.scrollBy({ left: -400, behavior: 'smooth' });
      setTimeout(() => this.checkScrollButtons(), 300);
    }
  }

  // Rola para a direita
  scrollRight(): void {
    if (this.cardsContainer) {
      const container = this.cardsContainer.nativeElement;
      container.scrollBy({ left: 400, behavior: 'smooth' });
      setTimeout(() => this.checkScrollButtons(), 300);
    }
  }

  // Abre o formulário para criar cartão
  openCreateCardForm(): void {
    this.isEditingCard = false;
    this.editingCardId = null;
    this.cardForm.reset({
      cardNumber: '',
      cardHolderName: '',
      expiryDate: '',
      cvv: '',
      cardType: 'CREDIT',
      brand: '',
      accountId: this.userAccountId || null,
      limitAmount: ''
    });
    this.showCardForm = true;
    this.cdr.detectChanges();
  }

  // Abre o formulário para editar cartão
  openEditCardForm(card: Card): void {
    if (!card || !card.cardId) {
      alert('Erro: cartão inválido. Não é possível editar.');
      return;
    }

    this.isEditingCard = true;
    this.editingCardId = card.cardId;
    
    // Converte a data de expiração para MM/YY
    let expiryDate = '';
    if (card.expiryDate) {
      const date = new Date(card.expiryDate);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = String(date.getFullYear()).slice(-2);
      expiryDate = `${month}/${year}`;
    }
    
    this.cardForm.patchValue({
      cardNumber: card.cardNumber.replace(/\*/g, ''), // Remove máscara para edição
      cardHolderName: card.cardHolderName,
      expiryDate: expiryDate,
      cvv: '', // CVV não é retornado por segurança
      cardType: card.cardType,
      brand: card.brand || '',
      accountId: card.accountId || null,
      limitAmount: card.limitAmount ? String(card.limitAmount) : ''
    });
    
    this.showCardForm = true;
    this.cdr.detectChanges();
  }

  // Fecha o formulário de cartão
  closeCardForm(): void {
    this.showCardForm = false;
    this.isEditingCard = false;
    this.editingCardId = null;
    this.cardForm.reset();
    this.cdr.detectChanges();
  }

  // Salva o cartão (criar ou editar)
  saveCard(): void {
    if (this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      return;
    }

    if (!this.userId) {
      alert('Erro: usuário não identificado. Faça login novamente.');
      return;
    }

    const formValue = this.cardForm.value;
    
    // Valida o número do cartão (conta apenas dígitos, mas aceita espaços)
    const cleanCardNumber = String(formValue.cardNumber || '').replace(/\s/g, '');
    if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
      const errorMsg = `Número do cartão inválido. Deve ter entre 13 e 19 dígitos. Você digitou ${cleanCardNumber.length} dígitos.`;
      alert(errorMsg);
      this.cardForm.get('cardNumber')?.setErrors({ invalidLength: true });
      this.cardForm.get('cardNumber')?.markAsTouched();
      return;
    }
    
    // Valida data de expiração (formato MM/YY)
    const expiryRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!expiryRegex.test(formValue.expiryDate)) {
      alert('Data de expiração inválida. Use o formato MM/AA (ex: 12/25)');
      this.cardForm.get('expiryDate')?.setErrors({ invalidFormat: true });
      this.cardForm.get('expiryDate')?.markAsTouched();
      return;
    }
    
    // Valida limite para cartão de crédito
    if (formValue.cardType === 'CREDIT') {
      if (!formValue.limitAmount || String(formValue.limitAmount).trim() === '') {
        alert('Limite de crédito é obrigatório para cartões de crédito');
        this.cardForm.get('limitAmount')?.setErrors({ required: true });
        this.cardForm.get('limitAmount')?.markAsTouched();
        return;
      }
    }
    
    // Prepara os dados do cartão conforme a API espera
    // O backend aceita espaços no cardNumber e remove automaticamente
    const cardData: any = {
      userId: this.userId,
      cardNumber: formValue.cardNumber.trim(), // Pode ter espaços, o backend remove
      cardHolderName: formValue.cardHolderName.toUpperCase().trim(),
      expiryDate: formValue.expiryDate.trim(), // Formato MM/YY
      cardType: formValue.cardType
    };

    // Adiciona CVV se fornecido (opcional)
    if (formValue.cvv && formValue.cvv.trim()) {
      cardData.cvv = formValue.cvv.trim();
    }

    // Adiciona brand apenas se fornecido (opcional)
    if (formValue.brand && formValue.brand.trim()) {
      cardData.brand = formValue.brand.trim();
    }

    // Adiciona accountId apenas se fornecido (opcional)
    if (formValue.accountId) {
      cardData.accountId = formValue.accountId;
    }

    // Adiciona limitAmount se for cartão de crédito (formato string conforme documentação)
    if (formValue.cardType === 'CREDIT' && formValue.limitAmount) {
      let limitStr = String(formValue.limitAmount).trim();
      // Remove formatação de moeda
      limitStr = limitStr.replace(/R\$\s*/gi, '')
                        .replace(/\$\s*/g, '')
                        .replace(/€\s*/g, '')
                        .replace(/£\s*/g, '')
                        .trim();
      // Converte vírgula para ponto
      if (limitStr.includes(',')) {
        limitStr = limitStr.replace(/\./g, '').replace(',', '.');
      }
      // Valida se é um número válido
      const limitNum = parseFloat(limitStr);
      if (!isNaN(limitNum) && isFinite(limitNum) && limitNum > 0) {
        // Envia como string (sem casas decimais fixas, pode ser "500000" ou "5000.00")
        cardData.limitAmount = limitStr; // Mantém o formato original (string)
      } else {
        alert('Limite de crédito inválido. Deve ser um número maior que zero.');
        this.cardForm.get('limitAmount')?.setErrors({ invalid: true });
        this.cardForm.get('limitAmount')?.markAsTouched();
        return;
      }
    }

    console.log('Cards: Dados do cartão a serem enviados:', cardData);

    if (this.isEditingCard && this.editingCardId) {
      // Atualiza cartão existente
      const updateData: any = {
        cardHolderName: cardData.cardHolderName,
        expiryDate: cardData.expiryDate
      };
      
      if (formValue.limitAmount) {
        updateData.limitAmount = cardData.limitAmount;
      }
      
      this.cardService.updateCard(this.editingCardId, updateData).subscribe({
        next: (card) => {
          if (card) {
            alert('Cartão atualizado com sucesso!');
            this.closeCardForm();
            this.loadCards();
          } else {
            alert('Erro ao atualizar cartão. Tente novamente.');
          }
        },
        error: (error) => {
          console.error('Erro ao atualizar cartão:', error);
          alert('Erro ao atualizar cartão. Tente novamente.');
        }
      });
    } else {
      // Cria novo cartão
      this.cardService.createCard(cardData).subscribe({
        next: (card) => {
          if (card) {
            alert('Cartão criado com sucesso!');
            this.closeCardForm();
            this.loadCards();
          } else {
            alert('Erro ao criar cartão. Tente novamente.');
          }
        },
        error: (error) => {
          console.error('Erro ao criar cartão:', error);
          console.error('Status:', error.status);
          console.error('Erro completo:', error.error);
          
          let errorMessage = 'Erro ao criar cartão. Tente novamente.';
          
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            } else if (Array.isArray(error.error) && error.error.length > 0) {
              errorMessage = error.error.map((e: any) => e.message || e).join(', ');
            }
          }
          
          alert(`Erro ao criar cartão:\n${errorMessage}`);
        }
      });
    }
  }

  // Exclui um cartão
  deleteCard(card: Card): void {
    if (!card || !card.cardId) {
      alert('Erro: cartão inválido. Não é possível excluir.');
      return;
    }

    if (!confirm(`Tem certeza que deseja excluir o cartão terminado em ${card.cardNumber.slice(-4)}?`)) {
      return;
    }

    this.cardService.deleteCard(card.cardId).subscribe({
      next: (success) => {
        if (success) {
          alert('Cartão excluído com sucesso!');
          this.loadCards();
        } else {
          alert('Erro ao excluir cartão. Tente novamente.');
        }
      },
      error: (error) => {
        console.error('Erro ao excluir cartão:', error);
        alert('Erro ao excluir cartão. Tente novamente.');
      }
    });
  }

  // Validador para data de expiração (MM/YY)
  // O backend aceita qualquer ano de 2 dígitos (00-99) e converte automaticamente
  expiryDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { required: true };
    }

    const value = String(control.value).trim();
    // Valida formato MM/YY (aceita qualquer ano de 2 dígitos)
    const regex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    
    if (!regex.test(value)) {
      return { invalidFormat: true };
    }

    return null;
  }

  // Validador customizado para número do cartão (conta apenas dígitos)
  cardNumberValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { required: true };
    }

    const value = String(control.value);
    // Remove espaços e outros caracteres não numéricos
    const digitsOnly = value.replace(/\D/g, '');
    
    if (digitsOnly.length < 13) {
      return { minlength: { requiredLength: 13, actualLength: digitsOnly.length } };
    }
    
    if (digitsOnly.length > 19) {
      return { maxlength: { requiredLength: 19, actualLength: digitsOnly.length } };
    }
    
    return null;
  }

  // Validador para limite de crédito
  limitAmountValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { required: true };
    }

    let valueStr = String(control.value).trim();
    valueStr = valueStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    if (valueStr.includes(',')) {
      valueStr = valueStr.replace(/\./g, '').replace(',', '.');
    }
    
    const value = parseFloat(valueStr);
    
    if (isNaN(value) || !isFinite(value) || value <= 0) {
      return { invalid: true };
    }
    
    return null;
  }

  // Verifica se um campo do formulário de cartão é inválido
  isCardFieldInvalid(fieldName: string): boolean {
    const field = this.cardForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // Formata o número do cartão (adiciona espaços a cada 4 dígitos)
  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\s/g, ''); // Remove espaços existentes
    value = value.replace(/\D/g, ''); // Remove não-dígitos
    
    // Limita a 19 dígitos (não conta espaços)
    if (value.length > 19) {
      value = value.slice(0, 19);
    }
    
    // Adiciona espaços a cada 4 dígitos para visualização
    value = value.match(/.{1,4}/g)?.join(' ') || value;
    
    this.cardForm.patchValue({ cardNumber: value }, { emitEvent: false });
  }

  // Formata a data de expiração (MM/YY)
  formatExpiryDate(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, ''); // Remove não-dígitos
    
    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2, 4);
    }
    
    if (value.length > 5) {
      value = value.slice(0, 5);
    }
    
    this.cardForm.patchValue({ expiryDate: value }, { emitEvent: false });
  }

  // Formata o CVV (apenas números, máximo 4 dígitos)
  formatCVV(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, ''); // Remove não-dígitos
    
    if (value.length > 4) {
      value = value.slice(0, 4);
    }
    
    this.cardForm.patchValue({ cvv: value }, { emitEvent: false });
  }

  // Obtém os últimos 4 dígitos do cartão
  getLastFourDigits(cardNumber: string): string {
    const digits = cardNumber.replace(/\D/g, '');
    return digits.slice(-4);
  }

  // Obtém a imagem do cartão baseado no tipo/brand
  getCardImage(card: Card): string {
    // Por enquanto, retorna imagens padrão baseado no tipo
    if (card.brand?.toLowerCase().includes('visa')) {
      return '/cartdev.png';
    }
    return '/cartPablo.png';
  }

  // Carrega as transações
  loadTransactions(): void {
    if (!this.userId) {
      return;
    }

    this.isLoading = true;
    this.transactionService.getTransactionsByUser().subscribe({
      next: (data) => {
        // Pega apenas as últimas 5 transações e ordena por data (mais recente primeiro)
        this.transactions = data
          .sort((a, b) => {
            const dateA = new Date(a.data_hora || a.transactionDate || 0);
            const dateB = new Date(b.data_hora || b.transactionDate || 0);
            return dateB.getTime() - dateA.getTime();
          })
          .slice(0, 5);
        
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erro ao carregar transações:', error);
        this.transactions = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Abre o formulário para editar transação
  openEditTransactionForm(transaction: Transaction): void {
    if (!transaction || !transaction.id) {
      alert('Erro: transação inválida. Não é possível editar.');
      return;
    }

    this.isEditingTransaction = true;
    this.editingTransactionId = transaction.id;
    
    const dateTime = new Date(transaction.data_hora || transaction.transactionDate || new Date());
    const formattedDateTime = new Date(dateTime.getTime() - dateTime.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);
    
    let formaPagamento = transaction.forma_pagamento;
    if (!formaPagamento && transaction.tipo) {
      const tipoMap: { [key: string]: string } = {
        'PIX': 'PIX',
        'DEBITO': 'Débito',
        'CREDITO': 'Crédito'
      };
      formaPagamento = tipoMap[transaction.tipo] || 'PIX';
    }
    
    let categoriaOutros = '';
    let descricaoPrincipal = transaction.descricao;
    
    if (transaction.categoria === 'OUTROS') {
      const descParts = transaction.descricao.split(' - ');
      if (descParts.length > 1) {
        descricaoPrincipal = descParts[0];
        categoriaOutros = descParts.slice(1).join(' - ');
      }
    }
    
    const isIncome = transaction.tipo === 'CREDITO' && transaction.valor > 0;
    const transactionDate = new Date(transaction.data_hora || transaction.transactionDate || new Date());
    const today = new Date();
    const isToday = transactionDate.toDateString() === today.toDateString();
    
    this.transactionForm.patchValue({
      descricao: descricaoPrincipal,
      categoria: transaction.categoria,
      categoriaOutros: categoriaOutros,
      forma_pagamento: formaPagamento,
      isIncome: isIncome,
      valor: Math.abs(transaction.valor),
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

  // Salva a transação
  saveTransaction(): void {
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    const formValue = this.transactionForm.value;
    const userId = localStorage.getItem('userId');
    
    if (!userId || !this.userAccountId) {
      alert('Erro: dados do usuário não encontrados.');
      return;
    }

    let valorStr = String(formValue.valor || '').trim();
    valorStr = valorStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    if (valorStr.includes(',')) {
      valorStr = valorStr.replace(/\./g, '').replace(',', '.');
    }
    
    const valor = parseFloat(valorStr);
    
    if (isNaN(valor) || !isFinite(valor) || valor <= 0) {
      alert('O valor da transação deve ser maior que zero!');
      return;
    }
    
    const valorFinal = Number(valor.toFixed(2));

    let descricaoFinal = formValue.descricao.trim();
    if (formValue.categoria === 'OUTROS' && formValue.categoriaOutros?.trim()) {
      descricaoFinal = `${descricaoFinal} - ${formValue.categoriaOutros.trim()}`;
    }

    let transactionDate: string;
    if (formValue.dataOption === 'today') {
      transactionDate = new Date().toISOString();
    } else {
      transactionDate = new Date(formValue.data_hora).toISOString();
    }

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

    if (this.isEditingTransaction && this.editingTransactionId) {
      this.transactionService.updateTransaction(this.editingTransactionId, transactionData).subscribe({
        next: () => {
          alert('Movimentação atualizada com sucesso!');
          this.closeTransactionForm();
          this.loadTransactions();
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Erro ao atualizar transação:', error);
          alert('Erro ao atualizar movimentação. Tente novamente.');
        }
      });
    }
  }

  // Exclui uma transação
  deleteTransaction(transaction: Transaction): void {
    if (!transaction || !transaction.id) {
      alert('Erro: transação inválida. Não é possível excluir.');
      return;
    }

    if (!confirm(`Tem certeza que deseja excluir a movimentação "${transaction.descricao}"?`)) {
      return;
    }

    this.transactionService.deleteTransaction(transaction.id).subscribe({
      next: () => {
        alert('Movimentação excluída com sucesso!');
        this.loadTransactions();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erro ao excluir transação:', error);
        alert('Erro ao excluir movimentação. Tente novamente.');
      }
    });
  }

  // Validador customizado
  customValueValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { required: true };
    }

    let valueStr = String(control.value).trim();
    valueStr = valueStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    if (valueStr.includes(',')) {
      valueStr = valueStr.replace(/\./g, '').replace(',', '.');
    }
    
    const value = parseFloat(valueStr);
    
    if (isNaN(value) || !isFinite(value) || value <= 0) {
      return { invalid: true };
    }
    
    return null;
  }

  // Verifica se um campo é inválido
  isFieldInvalid(fieldName: string): boolean {
    const field = this.transactionForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // Formata o input de valor
  formatCurrencyInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value;
    value = value.replace(/[^\d,.]/g, '');
    
    const commaIndex = value.indexOf(',');
    const dotIndex = value.indexOf('.');
    
    if (commaIndex !== -1 && dotIndex !== -1) {
      if (commaIndex < dotIndex) {
        value = value.replace(/\./g, '');
      } else {
        value = value.replace(/,/g, '');
      }
    }
    
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
