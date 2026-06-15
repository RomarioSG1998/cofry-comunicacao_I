import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
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
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const storedUserData = localStorage.getItem('userData');
    if (storedUserData) {
      try {
        this.userData = JSON.parse(storedUserData);
      } catch (error) {
        console.error('AfterLogin - Erro ao parsear dados do usuário:', error);
      }
    }

    // Carrega contas (calcula saldo total) e transações recentes
    this.loadAccounts();
    this.loadTransactions();
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


  loadTransactions(): void {
    this.transactionService.getTransactionsByUser(5, 0).subscribe({
      next: (data) => {
        this.transactions = Array.isArray(data) ? data : [];
      },
      error: () => {
        this.transactions = [];
      }
    });
  }

  toggleBalance(): void {
    this.showBalance = !this.showBalance;
  }
}