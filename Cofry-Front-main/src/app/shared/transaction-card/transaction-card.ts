import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-transaction-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-card.html'
})
export class TransactionCardComponent {
  @Input() transaction!: Transaction;
  @Output() delete = new EventEmitter<number>();

  onDelete(event: Event) {
    event.stopPropagation();
    this.delete.emit(this.transaction.id);
  }

  getIconConfig() {
    // PIX
    if (this.transaction.tipo === 'PIX') {
      return this.transaction.valor > 0
        ? { icon: 'pix-in', bg: 'bg-green-100', color: 'text-green-600' }
        : { icon: 'pix-out', bg: 'bg-red-100', color: 'text-red-600' };
    }

    // Categorias
    switch (this.transaction.categoria) {
      case 'MERCADO':
        return { icon: 'market', bg: 'bg-gray-100', color: 'text-gray-700' };

      case 'STREAMING':
        return { icon: 'streaming', bg: 'bg-blue-100', color: 'text-blue-600' };

      case 'TRANSPORTE':
        return { icon: 'transport', bg: 'bg-yellow-100', color: 'text-yellow-600' };

      default:
        return { icon: 'default', bg: 'bg-gray-100', color: 'text-gray-600' };
    }
  }
}
