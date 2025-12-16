import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../../services/transaction.service';
import { Transaction } from '../../models/transaction.model';
import { AuthService } from '../../services/auth.service';
import { formatDate } from '@angular/common';
import { CommonModule } from '@angular/common';
import { TransactionCardComponent } from "../../shared/transaction-card/transaction-card";
import { Router } from '@angular/router';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, TransactionCardComponent],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit {
  transactions: Transaction[] = [];
  userId: number = 0;
  groupedTransactions: any = {};
  filter: string = 'ALL';  // Filtro inicial

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = Number(this.authService.getUserId());
    this.loadTransactions();
  }

  loadTransactions() {
    this.transactionService.getTransactionsByUser().subscribe({
        next: data => this.transactions = data,
        error: err => console.error(err)
    });
  }

  groupTransactionsByDate() {
    const today = formatDate(new Date(), 'yyyy-MM-dd', 'en-US');
    const yesterday = formatDate(new Date(Date.now() - 86400000), 'yyyy-MM-dd', 'en-US');

    this.groupedTransactions = {
      today: this.filterTransactions(this.transactions.filter(t => formatDate(t.data_hora, 'yyyy-MM-dd', 'en-US') === today)),
      yesterday: this.filterTransactions(this.transactions.filter(t => formatDate(t.data_hora, 'yyyy-MM-dd', 'en-US') === yesterday)),
      older: this.filterTransactions(this.transactions.filter(t => ![today, yesterday].includes(formatDate(t.data_hora, 'yyyy-MM-dd', 'en-US'))))
    };
  }

  filterTransactions(transactions: Transaction[]) {
    if (this.filter === 'ALL') {
      return transactions;
    }
    return transactions.filter(t => t.tipo === this.filter);
  }

  setFilter(type: string) {
    this.filter = type;
    this.groupTransactionsByDate();  // Refiltra após mudança
  }

  // Abre o formulário para editar transação (redireciona para a página de extrato ou after-login)
  openEditTransactionForm(transaction: Transaction): void {
    // Redireciona para a página de extrato onde o usuário pode editar
    this.router.navigate(['/nav/Extrato']);
    // Alternativamente, poderia abrir um modal ou formulário inline aqui
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
        this.loadTransactions(); // Recarrega a lista
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
}