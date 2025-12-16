import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { PixService, UserInfo } from '../../services/pix.service';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-pix',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './pix.html',
  styleUrls: ['./pix.css']
})
export class Pix implements OnInit {
  // Injetar o Sanitizer para confiar nos SVGs
  private sanitizer = inject(DomSanitizer);
  private router = inject(Router);
  private pixService = inject(PixService);
  private accountService = inject(AccountService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);
  
  // Formulário de transferência
  transferForm!: FormGroup;
  isLoading = false;
  isSearching = false;
  userFound: UserInfo | null = null;
  currentBalance = 0;
  userId: number | null = null;
  userAccountId: number | null = null;
  showBalance = true;
  
  // Ícones como strings SVG
  icons = {
    qr: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/><path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/><rect x="7" y="7" width="3" height="3"/><rect x="14" y="7" width="3" height="3"/><rect x="7" y="14" width="3" height="3"/><path d="M14 14h3v3h-3z"/></svg>`,
    copy: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M15 2H9a1 1 0 0 0-1 1v2a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1Z"/></svg>`,
    transfer: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    key: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg>`
  };

  pixActions = [
    { label: 'Ler QR Code', icon: this.icons.qr, highlight: false },
    { label: 'Pix Copia e Cola', icon: this.icons.copy, highlight: true },
    { label: 'Transferir', icon: this.icons.transfer, highlight: false },
    { label: 'Minhas Chaves', icon: this.icons.key, highlight: false },
  ];

  contacts = [
    { firstName: 'João', lastName: 'Silva', initials: 'JS', isNew: false },
    { firstName: 'Maria', lastName: 'Alice', initials: 'MA', isNew: false },
    { firstName: 'Pedro', lastName: 'Lucas', initials: 'PL', isNew: false },
    { firstName: 'Thiago', lastName: 'Reis', initials: 'TR', isNew: false },
    { firstName: 'Novo', lastName: '', initials: '', isNew: true },
  ];

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Inicializa o formulário
    this.transferForm = this.fb.group({
      destinationCpf: ['', [Validators.required, this.cpfValidator.bind(this)]],
      amount: ['', [Validators.required, this.amountValidator.bind(this)]],
      description: ['']
    });

    const userIdStr = localStorage.getItem('userId');
    if (userIdStr) {
      this.userId = Number(userIdStr);
      this.loadBalance();
    } else {
      this.router.navigate(['/login']);
    }
  }

  // Carrega o saldo da conta ativa (mesma conta usada em todas as páginas)
  loadBalance(): void {
    if (!this.userId) return;

    this.accountService.getActiveAccountByUserId(this.userId).subscribe({
      next: (activeAccount) => {
        if (activeAccount) {
          this.currentBalance = activeAccount.balance || 0;
          this.userAccountId = activeAccount.accountId;
        } else {
          this.currentBalance = 0;
          this.userAccountId = null;
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erro ao carregar saldo:', error);
        this.currentBalance = 0;
        this.userAccountId = null;
        this.cdr.detectChanges();
      }
    });
  }

  // Busca usuário por CPF
  searchUserByCpf(): void {
    const cpf = this.transferForm.get('destinationCpf')?.value;
    if (!cpf) {
      this.userFound = null;
      return;
    }

    const cleanCpf = cpf.replace(/[^\d]/g, '');
    if (cleanCpf.length !== 11) {
      this.userFound = null;
      return;
    }

    this.isSearching = true;
    this.userFound = null;

    this.pixService.getUserByCpf(cpf).subscribe({
      next: (user) => {
        this.userFound = user;
        this.isSearching = false;
        
        // Preenche descrição sugerida se estiver vazia
        if (!this.transferForm.get('description')?.value) {
          this.transferForm.patchValue({
            description: `Transferência PIX para ${user.firstName} ${user.lastName}`
          });
        }
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.userFound = null;
        this.isSearching = false;
        console.error('Usuário não encontrado:', error);
        this.cdr.detectChanges();
      }
    });
  }

  // Formata CPF enquanto digita
  formatCpfInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    
    if (value.length <= 11) {
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
      
      this.transferForm.patchValue({ destinationCpf: value }, { emitEvent: false });
      
      // Busca usuário automaticamente quando CPF estiver completo
      if (value.length === 14) { // CPF formatado tem 14 caracteres
        setTimeout(() => this.searchUserByCpf(), 500);
      } else {
        this.userFound = null;
      }
    }
  }

  // Formata valor monetário
  formatAmountInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/[^\d,]/g, '');
    
    // Limita a 2 casas decimais
    if (value.includes(',')) {
      const parts = value.split(',');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + ',' + parts[1].substring(0, 2);
      }
    }
    
    this.transferForm.patchValue({ amount: value }, { emitEvent: false });
  }

  // Validador de CPF
  cpfValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Deixa o required validator tratar
    }

    const cleanCpf = control.value.replace(/[^\d]/g, '');
    
    if (cleanCpf.length !== 11) {
      return { invalidCpf: true };
    }
    
    // Validação básica (pode usar validação completa se necessário)
    if (!this.pixService.validateCPF(control.value)) {
      return { invalidCpf: true };
    }
    
    return null;
  }

  // Validador de valor
  amountValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Deixa o required validator tratar
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
      return { invalidAmount: true };
    }
    
    // Valida saldo suficiente
    if (value > this.currentBalance) {
      return { insufficientBalance: true };
    }
    
    return null;
  }

  // Verifica se um campo é inválido
  isFieldInvalid(fieldName: string): boolean {
    const field = this.transferForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // Submete o formulário de transferência
  onSubmit(): void {
    if (this.transferForm.invalid || !this.userFound || !this.userAccountId) {
      this.transferForm.markAllAsTouched();
      if (!this.userFound) {
        alert('Por favor, busque e encontre o destinatário antes de transferir.');
      }
      return;
    }

    if (!confirm(`Confirmar transferência de R$ ${this.getFormattedAmount()} para ${this.userFound.firstName} ${this.userFound.lastName}?`)) {
      return;
    }

    this.isLoading = true;
    const formValue = this.transferForm.value;
    
    // Formata o valor para string (formato: "100.00")
    let amountStr = String(formValue.amount).trim();
    amountStr = amountStr.replace(/R\$\s*/gi, '')
                        .replace(/\$\s*/g, '')
                        .replace(/€\s*/g, '')
                        .replace(/£\s*/g, '')
                        .trim();
    
    if (amountStr.includes(',')) {
      amountStr = amountStr.replace(/\./g, '').replace(',', '.');
    }
    
    const amountNum = parseFloat(amountStr);
    if (isNaN(amountNum) || amountNum <= 0) {
      alert('Valor inválido');
      this.isLoading = false;
      return;
    }
    
    // Formata para 2 casas decimais
    const formattedAmount = amountNum.toFixed(2);

    this.pixService.transferPixByCpf(
      this.userAccountId!,
      formValue.destinationCpf,
      formattedAmount,
      formValue.description || undefined
    ).subscribe({
      next: (response) => {
        alert('Transferência realizada com sucesso!');
        console.log('Transferência:', response);
        this.transferForm.reset();
        this.userFound = null;
        this.isLoading = false;
        // Recarrega o saldo após um delay para garantir que o backend atualizou
        setTimeout(() => {
          this.loadBalance();
        }, 300);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erro na transferência:', error);
        let errorMessage = 'Erro ao realizar transferência PIX';
        if (error.message) {
          errorMessage = error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        }
        alert(`Erro: ${errorMessage}`);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Obtém o valor formatado para exibição
  getFormattedAmount(): string {
    const amount = this.transferForm.get('amount')?.value || '';
    if (!amount) return '0,00';
    
    let valueStr = String(amount).trim();
    valueStr = valueStr.replace(/R\$\s*/gi, '')
                      .replace(/\$\s*/g, '')
                      .replace(/€\s*/g, '')
                      .replace(/£\s*/g, '')
                      .trim();
    
    if (valueStr.includes(',')) {
      valueStr = valueStr.replace(/\./g, '').replace(',', '.');
    }
    
    const value = parseFloat(valueStr);
    if (isNaN(value)) return '0,00';
    
    return value.toFixed(2).replace('.', ',');
  }

  // Formata o saldo para exibição
  getFormattedBalance(): string {
    if (!this.showBalance) {
      return '••••••';
    }
    return this.currentBalance.toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  }

  // Alterna visibilidade do saldo
  toggleBalance(): void {
    this.showBalance = !this.showBalance;
  }

  // Volta para a home
  goBack(): void {
    this.router.navigate(['/nav/Home']);
  }

  /**
   * Navega para a rota correspondente ao atalho.
   */
  async onActionClick(label: string) {
    if (label === 'Transferir') {
      // Scroll para o formulário de transferência
      const formElement = document.querySelector('.transfer-section');
      if (formElement) {
        formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
      return;
    }

    const candidates = [
      label,
      label.replace(/\s+/g, '-'),
      label.replace(/\s+/g, '').replace(/[^a-zA-Z0-9\-]/g, ''),
      label.toLowerCase(),
      label.toLowerCase().replace(/\s+/g, '-'),
      label.split(' ')[0]
    ];

    for (const cand of candidates) {
      if (!cand) continue;
      try {
        const success = await this.router.navigate([`/nav/${cand}`]);
        if (success) return;
      } catch (e) {
        // ignore and try next
      }
    }
  }

  getSafeIcon(iconString: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(iconString);
  }
}