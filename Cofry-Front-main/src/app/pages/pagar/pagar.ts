import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-pagar',
  imports: [CommonModule],
  templateUrl: './pagar.html',
  styleUrl: './pagar.css',
})
export class Pagar {
// Evento para avisar o componente pai para voltar
  @Output() goBack = new EventEmitter<void>();

  onBack(): void {
    this.goBack.emit();
  }

  // Ações dos botões (exemplo)
  handleOption(option: string): void {
    console.log('Opção selecionada:', option);
  }
}
