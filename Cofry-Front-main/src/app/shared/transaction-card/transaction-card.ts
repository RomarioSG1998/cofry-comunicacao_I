import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Transaction } from '../../models/transaction.model';

interface Tag {
  label: string;
  class: string;
}

@Component({
  selector: 'app-transaction-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-card.html'
})
export class TransactionCardComponent {
  @Input() transaction!: Transaction;

  // Mapeamento de categorias para labels em português
  private categoryLabels: { [key: string]: string } = {
    'MERCADO': 'Alimentação',
    'STREAMING': 'Serviços',
    'TRANSPORTE': 'Transporte',
    'OUTROS': 'Outros'
  };

  // Formata a data no formato "01 OUT"
  getFormattedDate(): string {
    // Usa transactionDate se disponível, senão usa data_hora
    const dateStr = this.transaction.transactionDate || this.transaction.data_hora;
    if (!dateStr) return '';
    
    const date = new Date(dateStr);
    const day = date.getDate().toString().padStart(2, '0');
    
    const months = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'];
    const month = months[date.getMonth()];
    
    return `${day} ${month}`;
  }

  // Retorna o label da categoria
  getCategoryLabel(): string {
    return this.categoryLabels[this.transaction.categoria] || this.transaction.categoria;
  }

  // Formata o valor
  getFormattedAmount(): string {
    // Usa amount se disponível, senão usa valor
    const valor = Math.abs(this.transaction.valor || 0);
    return `R$ ${valor.toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.')}`;
  }

  // Verifica se há tags para exibir
  hasTags(): boolean {
    return !!(this.transaction.isRecurring || 
              (this.transaction.installmentCurrent && this.transaction.installmentTotal));
  }

  // Retorna as tags da transação
  getTags(): Tag[] {
    const tags: Tag[] = [];
    
    // Tag de recorrente
    if (this.transaction.isRecurring) {
      tags.push({ label: 'Recorrente', class: 'bg-slate-400' });
    }
    
    // Tag de parcela
    if (this.transaction.installmentCurrent && this.transaction.installmentTotal) {
      tags.push({ 
        label: `Parcela ${this.transaction.installmentCurrent}/${this.transaction.installmentTotal}`, 
        class: 'bg-emerald-500' 
      });
    }
    
    return tags;
  }
}
