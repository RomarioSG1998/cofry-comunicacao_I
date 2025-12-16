import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PixService, PixTransferRequest, PixTransferResponse } from '../../services/pix.service';
import { UserService, User } from '../../services/user.service';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-transferir',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transferir.html',
  styleUrls: ['./transferir.css']
})
export class Transferir implements OnInit {
  private router = inject(Router);
  private pixService = inject(PixService);
  private userService = inject(UserService);
  private accountService = inject(AccountService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private cdr = inject(ChangeDetectorRef);

  transferForm: FormGroup;
  isLoading = false;
  isSearching = false;
  destinationUser: User | null = null;
  currentBalance = 0;
  userId: number | null = null;
  userAccountId: number | null = null;

  constructor() {
    this.transferForm = this.fb.group({
      destination: ['', [Validators.required]], // CPF ou Email
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

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
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error: unknown) => {
        console.error('Erro ao carregar saldo:', error);
        this.currentBalance = 0;
        this.userAccountId = null;
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  searchUser(): void {
    const destination = this.transferForm.get('destination')?.value?.trim();
    if (!destination) {
      alert('Por favor, informe o CPF ou email do destinatário.');
      return;
    }

    this.isSearching = true;
    this.destinationUser = null;

    // Tenta buscar por CPF primeiro usando o PixService
    const cleanDestination = destination.replace(/[^\d]/g, '');
    if (cleanDestination.length === 11) {
      // É um CPF, usa o método do PixService
      this.pixService.getUserByCpf(destination).subscribe({
        next: (userInfo) => {
          // Converte UserInfo para User
          this.destinationUser = {
            userId: userInfo.userId,
            planId: 0, // Não temos essa informação aqui
            firstName: userInfo.firstName,
            lastName: userInfo.lastName,
            taxId: userInfo.cpf,
            email: userInfo.email,
            phoneNumber: userInfo.phoneNumber || undefined,
            dateOfBirth: '',
            isActive: userInfo.isActive,
            createdAt: '',
            updatedAt: ''
          };
          this.isSearching = false;
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          // Se não encontrar por CPF, tenta buscar por email
          this.searchUserByEmail(destination);
        }
      });
    } else {
      // É um email, busca diretamente
      this.searchUserByEmail(destination);
    }
  }

  searchUserByEmail(email: string): void {
    // Busca todos os usuários e filtra por email
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        const user = users.find(u => 
          u.email?.toLowerCase() === email.toLowerCase()
        );

        if (user) {
          this.destinationUser = user;
        } else {
          alert('Usuário não encontrado. Verifique o CPF ou email informado.');
        }
        this.isSearching = false;
        this.cdr.detectChanges(); // Força detecção de mudanças
      },
      error: (error: unknown) => {
        console.error('Erro ao buscar usuário:', error);
        alert('Erro ao buscar usuário. Tente novamente.');
        this.isSearching = false;
        this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
      }
    });
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    if (!this.destinationUser) {
      alert('Por favor, busque e selecione um destinatário antes de continuar.');
      return;
    }

    if (!this.userId) {
      alert('Erro: usuário não identificado. Faça login novamente.');
      return;
    }

    const amount = parseFloat(this.transferForm.get('amount')?.value);
    
    if (amount > this.currentBalance) {
      alert('Saldo insuficiente para realizar esta transferência.');
      return;
    }

    if (amount <= 0) {
      alert('O valor da transferência deve ser maior que zero.');
      return;
    }

    if (!this.userAccountId) {
      alert('Erro: conta não encontrada. Por favor, verifique suas contas.');
      return;
    }

    if (confirm(`Confirmar transferência de R$ ${amount.toFixed(2).replace('.', ',')} para ${this.destinationUser.firstName} ${this.destinationUser.lastName}?`)) {
      this.isLoading = true;

      // Formata o valor para string com 2 casas decimais
      const formattedAmount = amount.toFixed(2);

      const transferData: PixTransferRequest = {
        sourceAccountId: this.userAccountId,
        destinationUserId: this.destinationUser.userId,
        amount: formattedAmount,
        description: this.transferForm.get('description')?.value?.trim() || 'Transferência PIX'
      };

      this.pixService.transferPix(transferData).subscribe({
        next: (response: PixTransferResponse) => {
          this.isLoading = false;
          alert('Transferência realizada com sucesso!');
          this.transferForm.reset();
          this.destinationUser = null;
          // Aguarda um pequeno delay para garantir que o backend atualizou o saldo antes de recarregar
          setTimeout(() => {
            this.loadBalance(); // Atualiza o saldo
            this.cdr.detectChanges(); // Força detecção de mudanças
          }, 300);
        },
        error: (error: any) => {
          this.isLoading = false;
          console.error('Erro ao realizar transferência:', error);
          
          let errorMessage = 'Erro ao realizar transferência. Tente novamente.';
          
          // Tratamento específico para erro de saldo insuficiente (400 Bad Request)
          if (error.status === 400) {
            if (error.error?.error) {
              errorMessage = error.error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            } else if (error.message) {
              errorMessage = error.message;
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
            } else if (error.message) {
              errorMessage = error.message;
            }
            alert(errorMessage);
          }
          
          this.cdr.detectChanges(); // Força detecção mesmo em caso de erro
        }
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.transferForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.transferForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName === 'destination' ? 'Destinatário' : fieldName === 'amount' ? 'Valor' : 'Descrição'} é obrigatório`;
      if (field.errors['min']) return 'Valor mínimo é R$ 0,01';
      if (field.errors['minlength']) return 'Descrição deve ter no mínimo 3 caracteres';
    }
    return '';
  }

  formatCurrency(value: string): string {
    // Remove tudo que não é número
    const numbers = value.replace(/\D/g, '');
    if (!numbers) return '';
    
    // Converte para centavos e depois para reais
    const amount = parseFloat(numbers) / 100;
    return amount.toFixed(2).replace('.', ',');
  }

  getAmountValue(): number {
    const value = this.transferForm.get('amount')?.value;
    return value ? parseFloat(value) : 0;
  }

  goBack(): void {
    this.router.navigate(['/nav/Home']);
  }
}
