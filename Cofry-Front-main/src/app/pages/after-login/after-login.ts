import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { UserService } from '../../services/user.service';
import { AccountService, Account } from '../../services/account.service';
import { TransactionCardComponent } from '../../shared/transaction-card/transaction-card';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-after-login',
  standalone: true,
  imports: [CommonModule, TransactionCardComponent, RouterLink, FormsModule],
  templateUrl: './after-login.html',
  styleUrls: ['./after-login.css']
})
export class AfterLogin implements OnInit {
  // Inicialização do array de transações
  transactions: Transaction[] = [];
  accountsList: Account[] = [];

  // Injeção de dependências
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private userService = inject(UserService);
  private accountService = inject(AccountService);
  private platformId = inject(PLATFORM_ID);

  // Propriedades de estado do componente
  showBalance = true;
  userData: any = null;
  balance = '0,00'; // Valor padrão inicializado dinamico

  // Account Modal & Form State
  showAccountModal = false;
  isEditingAccount = false;
  editingAccountId: number | null = null;
  newAccount = {
    instituicao: '',
    saldo: 0
  };

  constructor() {
    console.log('AfterLogin - Constructor chamado');
  }

  ngOnInit(): void {
    console.log('AfterLogin - ngOnInit iniciado');
    
    if (!isPlatformBrowser(this.platformId)) {
      console.log('AfterLogin - SSR detectado, retornando');
      return; // Não faz nada no SSR
    }

    const userEmail = localStorage.getItem('userEmail');
    const storedUserData = localStorage.getItem('userData');

    console.log('AfterLogin - Dados do usuário:', { userEmail, storedUserData });

    if (storedUserData) {
      try {
        this.userData = JSON.parse(storedUserData);
        console.log('AfterLogin - userData parseado:', this.userData);
      } catch (error) {
        console.error('AfterLogin - Erro ao parsear dados do usuário:', error);
      }
    }

    if (userEmail) {
      this.loadUserData(userEmail);
      this.loadAccounts();
    }

    // Carregar transações (chamada principal) - com tratamento de erro para não quebrar a página
    try {
      this.loadTransactions();
    } catch (error) {
      console.error('AfterLogin - Erro ao inicializar transações:', error);
      this.transactions = [];
    }
    
    console.log('AfterLogin - ngOnInit concluído');
  }

  loadAccounts(): void {
    this.accountService.getAccountsByUser().subscribe({
      next: (accs) => {
        this.accountsList = accs;
        let total = 0;
        accs.forEach(acc => {
          if (acc.saldo) {
            total += Number(acc.saldo);
          }
        });
        this.balance = total.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      },
      error: (err) => console.error('Erro ao buscar contas:', err)
    });
  }

  openAccountModal(account?: Account) {
    if (account) {
      this.isEditingAccount = true;
      this.editingAccountId = account.idConta ?? null;
      this.newAccount = {
        instituicao: account.instituicao,
        saldo: account.saldo
      };
    } else {
      this.isEditingAccount = false;
      this.editingAccountId = null;
      this.newAccount = {
        instituicao: '',
        saldo: 0
      };
    }
    this.showAccountModal = true;
  }

  closeAccountModal() {
    this.showAccountModal = false;
  }

  saveAccount(event: Event) {
    event.preventDefault();
    const userId = Number(localStorage.getItem('userId'));
    if (!userId) {
      alert('Usuário não autenticado.');
      return;
    }

    const payload: Account = {
      idUsuario: userId,
      instituicao: this.newAccount.instituicao,
      saldo: Number(this.newAccount.saldo)
    };

    if (this.isEditingAccount && this.editingAccountId !== null) {
      payload.idConta = this.editingAccountId;
      this.accountService.updateAccount(this.editingAccountId, payload).subscribe({
        next: () => {
          this.closeAccountModal();
          this.loadAccounts();
        },
        error: (err) => {
          console.error('Erro ao atualizar conta:', err);
          alert('Erro ao atualizar conta.');
        }
      });
    } else {
      this.accountService.createAccount(payload).subscribe({
        next: () => {
          this.closeAccountModal();
          this.loadAccounts();
        },
        error: (err) => {
          console.error('Erro ao criar conta:', err);
          alert('Erro ao criar conta.');
        }
      });
    }
  }

  deleteAccount(id: number, event: Event) {
    event.stopPropagation();
    if (confirm('Tem certeza que deseja excluir esta conta? Isso removerá as transações associadas.')) {
      this.accountService.deleteAccount(id).subscribe({
        next: () => {
          this.loadAccounts();
          this.loadTransactions(); // Recarrega transações pois podem ter sido deletadas em cascata
        },
        error: (err) => {
          console.error('Erro ao excluir conta:', err);
          alert('Erro ao excluir conta.');
        }
      });
    }
  }

  loadUserData(email: string): void {
    this.userService.fetch<any>(email).subscribe({
      next: (res) => {
        console.log('Dados completos do usuário carregados:', res);
        if (res && res.data) {
          const accounts = res.data.accounts;
          if (Array.isArray(accounts) && accounts.length > 0) {
            let total = 0;
            accounts.forEach((acc: any) => {
              if (acc.saldo) {
                total += Number(acc.saldo);
              }
            });
            this.balance = total.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
          } else {
            this.balance = '0,00';
          }
        }
      },
      error: (err) => {
        console.error('Erro ao carregar dados do usuário:', err);
      }
    });
  }

  loadTransactions(): void {
    this.transactionService.getTransactionsByUser(5, 0).subscribe({
      next: (data) => {
        console.log('Transações carregadas:', data);
        if (Array.isArray(data)) {
          this.transactions = data;
        } else {
          console.warn('Dados de transações não são um array:', data);
          this.transactions = [];
        }
      },
      error: (error) => {
        console.error('Erro ao carregar transações:', error);
        // Se der erro, simplesmente mantém o array vazio para não quebrar a página
        this.transactions = [];
      }
    });
  }

  toggleBalance(): void {
    this.showBalance = !this.showBalance;
  }
}