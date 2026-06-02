import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../../services/transaction.service';
import { Transaction } from '../../models/transaction.model';
import { AuthService } from '../../services/auth.service';
import { formatDate } from '@angular/common';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionCardComponent } from "../../shared/transaction-card/transaction-card";

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, TransactionCardComponent, FormsModule],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit {
  transactions: Transaction[] = [];
  userId: number = 0;
  groupedTransactions: any = {};
  filter: string = 'ALL';  // Filtro inicial

  // Pagination State
  pageSize: number = 10;
  offset: number = 0;
  hasMore: boolean = true;

  // Modal & Form State
  showModal = false;
  categories: any[] = [];
  accounts: any[] = [];
  newTx = {
    valor: 0,
    data: formatDate(new Date(), 'yyyy-MM-dd', 'en-US'),
    idCategoria: null,
    idConta: null
  };

  constructor(
    private transactionService: TransactionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userId = Number(this.authService.getUserId());
    this.loadTransactions();
    this.loadFormData();
  }

  loadTransactions(append: boolean = false) {
    if (!append) {
      this.offset = 0;
      this.hasMore = true;
    }
    this.transactionService.getTransactionsByUser(this.pageSize, this.offset).subscribe({
        next: data => {
          if (data.length < this.pageSize) {
            this.hasMore = false;
          }
          if (append) {
            this.transactions = [...this.transactions, ...data];
          } else {
            this.transactions = data;
          }
          this.groupTransactionsByDate();
        },
        error: err => console.error(err)
    });
  }

  loadMore() {
    this.offset += this.pageSize;
    this.loadTransactions(true);
  }

  loadFormData() {
    // Carregar categorias e contas dinamicamente para os selects
    this.transactionService.getCategories().subscribe({
      next: cats => {
        this.categories = cats;
        if (cats.length > 0) {
          this.newTx.idCategoria = cats[0].idCategoria;
        }
      },
      error: err => console.error('Erro ao carregar categorias:', err)
    });

    this.transactionService.getAccountsByUser().subscribe({
      next: accs => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.newTx.idConta = accs[0].idConta;
        }
      },
      error: err => console.error('Erro ao carregar contas:', err)
    });
  }

  openModal() {
    this.showModal = true;
    this.loadFormData(); // recarrega contas/categorias mais recentes
  }

  closeModal() {
    this.showModal = false;
  }

  saveTransaction(event: Event) {
    event.preventDefault();
    if (!this.userId) {
      alert('Usuário não autenticado.');
      return;
    }

    const payload = {
      idUsuario: this.userId,
      valor: this.newTx.valor,
      data: this.newTx.data,
      idCategoria: Number(this.newTx.idCategoria),
      idConta: Number(this.newTx.idConta)
    };

    this.transactionService.createTransaction(payload).subscribe({
      next: () => {
        this.closeModal();
        this.loadTransactions();
        // Reset form
        this.newTx.valor = 0;
        this.newTx.data = formatDate(new Date(), 'yyyy-MM-dd', 'en-US');
      },
      error: err => {
        console.error('Erro ao salvar transação:', err);
        alert('Erro ao salvar transação. Verifique os dados.');
      }
    });
  }

  deleteTransaction(id: number) {
    if (confirm('Tem certeza que deseja excluir esta transação?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: () => {
          this.loadTransactions();
        },
        error: err => {
          console.error('Erro ao deletar transação:', err);
          alert('Erro ao excluir a transação.');
        }
      });
    }
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
}