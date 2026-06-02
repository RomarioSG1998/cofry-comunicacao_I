import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService, Account } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-pagar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagar.html',
  styleUrl: './pagar.css',
})
export class Pagar implements OnInit {
  private router = inject(Router);
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);

  balance = '0,00';
  accounts: Account[] = [];

  pagarForm = {
    codigoBarras: '',
    beneficiario: '',
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
          this.pagarForm.idConta = accs[0].idConta ?? null;
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

  formatBarcode(event: any) {
    const input = event.target.value;
    let clean = input.replace(/\D/g, '');
    
    if (clean.length > 48) {
      clean = clean.substring(0, 48);
    }
    
    if (clean.length > 47) {
      this.pagarForm.codigoBarras = `${clean.substring(0, 11)}-${clean.substring(11, 12)} ${clean.substring(12, 23)}-${clean.substring(23, 24)} ${clean.substring(24, 35)}-${clean.substring(35, 36)} ${clean.substring(36, 47)}-${clean.substring(47)}`;
    } else if (clean.length > 32) {
      this.pagarForm.codigoBarras = `${clean.substring(0, 5)}.${clean.substring(5, 10)} ${clean.substring(10, 15)}.${clean.substring(15, 21)} ${clean.substring(21, 26)}.${clean.substring(26, 32)} ${clean.substring(32, 33)} ${clean.substring(33)}`;
    } else if (clean.length > 21) {
      this.pagarForm.codigoBarras = `${clean.substring(0, 5)}.${clean.substring(5, 10)} ${clean.substring(10, 15)}.${clean.substring(15, 21)} ${clean.substring(21)}`;
    } else if (clean.length > 10) {
      this.pagarForm.codigoBarras = `${clean.substring(0, 5)}.${clean.substring(5, 10)} ${clean.substring(10)}`;
    } else if (clean.length > 5) {
      this.pagarForm.codigoBarras = `${clean.substring(0, 5)}.${clean.substring(5)}`;
    } else {
      this.pagarForm.codigoBarras = clean;
    }
    
    event.target.value = this.pagarForm.codigoBarras;
  }

  sendPayment(event: Event) {
    event.preventDefault();
    const userId = Number(localStorage.getItem('userId'));
    if (!userId) {
      alert('Usuário não logado.');
      return;
    }
    if (!this.pagarForm.valor || this.pagarForm.valor <= 0) {
      alert('Por favor, informe um valor válido.');
      return;
    }
    if (!this.pagarForm.idConta) {
      alert('Selecione uma conta de origem.');
      return;
    }

    const sourceAccount = this.accounts.find(acc => acc.idConta === Number(this.pagarForm.idConta));
    if (sourceAccount && Number(sourceAccount.saldo) < this.pagarForm.valor) {
      alert(`Saldo insuficiente na conta selecionada. Saldo disponível: R$ ${Number(sourceAccount.saldo).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      return;
    }

    const payload = {
      idUsuario: userId,
      valor: -Math.abs(this.pagarForm.valor), // Debitando
      data: formatDate(new Date(), 'yyyy-MM-dd', 'en-US'),
      idCategoria: 3, // Categoria Pagamentos/Boletos
      idConta: Number(this.pagarForm.idConta)
    };

    this.transactionService.createTransaction(payload).subscribe({
      next: () => {
        alert('Pagamento realizado com sucesso!');
        this.router.navigate(['/nav/home']);
      },
      error: err => {
        console.error(err);
        alert('Erro ao realizar pagamento.');
      }
    });
  }

  handleOption(option: string): void {
    if (option === 'pix') {
      this.router.navigate(['/nav/Pix']);
    } else {
      alert('Opção em breve!');
    }
  }
}
