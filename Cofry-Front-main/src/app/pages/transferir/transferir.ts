import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService, Account } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-transferir',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transferir.html',
  styleUrls: ['./transferir.css'],
})
export class Transferir implements OnInit {
  private router = inject(Router);
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);

  balance = '0,00';
  accounts: Account[] = [];

  transferForm = {
    destinatario: '',
    banco: '',
    agencia: '',
    conta: '',
    valor: null as number | null,
    idConta: null as number | null
  };

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.accountService.getAccountsByUser().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.transferForm.idConta = accs[0].idConta ?? null;
        }
        let total = 0;
        accs.forEach(acc => total += Number(acc.saldo));
        this.balance = total.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      },
      error: err => console.error(err)
    });
  }

  goHome() {
    this.router.navigate(['/nav/home']);
  }

  sendTransfer(event: Event) {
    event.preventDefault();
    const userId = Number(localStorage.getItem('userId'));
    if (!userId) {
      alert('Usuário não logado.');
      return;
    }
    if (!this.transferForm.valor || this.transferForm.valor <= 0) {
      alert('Por favor, informe um valor válido.');
      return;
    }
    if (!this.transferForm.idConta) {
      alert('Selecione uma conta de origem.');
      return;
    }

    const sourceAccount = this.accounts.find(acc => acc.idConta === Number(this.transferForm.idConta));
    if (sourceAccount && Number(sourceAccount.saldo) < this.transferForm.valor) {
      alert(`Saldo insuficiente na conta selecionada. Saldo disponível: R$ ${Number(sourceAccount.saldo).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      return;
    }

    const payload = {
      idUsuario: userId,
      valor: -Math.abs(this.transferForm.valor), // Debitando
      data: formatDate(new Date(), 'yyyy-MM-dd', 'en-US'),
      idCategoria: 2, // Categoria Transferência
      idConta: Number(this.transferForm.idConta)
    };

    this.transactionService.createTransaction(payload).subscribe({
      next: () => {
        alert('Transferência realizada com sucesso!');
        this.router.navigate(['/nav/home']);
      },
      error: err => {
        console.error(err);
        alert('Erro ao realizar transferência.');
      }
    });
  }
}
