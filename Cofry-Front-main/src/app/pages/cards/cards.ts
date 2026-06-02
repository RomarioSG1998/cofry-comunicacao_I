import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CardService, CreditCard } from '../../services/card.service';

@Component({
  selector: 'app-cards',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cards.html',
  styleUrls: ['./cards.css']
})
export class Cards implements OnInit {
  private router = inject(Router);
  private cardService = inject(CardService);

  cards: CreditCard[] = [];
  userId: number = 0;
  totalLimite: number = 0;

  showForm = false;
  isEdit = false;

  cardForm = {
    idCartao: undefined as number | undefined,
    limite: null as number | null,
    diaVencimento: 10
  };

  ngOnInit() {
    const storedUserId = localStorage.getItem('userId');
    if (!storedUserId) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = Number(storedUserId);
    this.loadCards();
  }

  loadCards() {
    this.cardService.getCreditCardsByUser(this.userId).subscribe({
      next: (data) => {
        this.cards = data;
        this.calculateTotalLimit();
      },
      error: (err) => console.error('Erro ao buscar cartões:', err)
    });
  }

  calculateTotalLimit() {
    this.totalLimite = this.cards.reduce((sum, card) => sum + (Number(card.limite) || 0), 0);
  }

  openAddForm() {
    this.isEdit = false;
    this.showForm = true;
    this.cardForm = {
      idCartao: undefined,
      limite: null,
      diaVencimento: 10
    };
  }

  openEditForm(card: CreditCard) {
    this.isEdit = true;
    this.showForm = true;
    this.cardForm = {
      idCartao: card.idCartao,
      limite: card.limite,
      diaVencimento: card.diaVencimento
    };
  }

  closeForm() {
    this.showForm = false;
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.cardForm.limite == null || this.cardForm.limite <= 0) {
      alert('Limite do cartão inválido.');
      return;
    }
    if (this.cardForm.diaVencimento < 1 || this.cardForm.diaVencimento > 31) {
      alert('Dia do vencimento inválido.');
      return;
    }

    const payload: CreditCard = {
      idCartao: this.cardForm.idCartao,
      idUsuario: this.userId,
      limite: Number(this.cardForm.limite),
      diaVencimento: Number(this.cardForm.diaVencimento)
    };

    if (this.isEdit) {
      this.cardService.updateCreditCard(payload).subscribe({
        next: () => {
          alert('Cartão de crédito atualizado com sucesso!');
          this.closeForm();
          this.loadCards();
        },
        error: err => {
          console.error(err);
          alert('Erro ao atualizar cartão.');
        }
      });
    } else {
      this.cardService.createCreditCard(payload).subscribe({
        next: () => {
          alert('Cartão de crédito criado com sucesso!');
          this.closeForm();
          this.loadCards();
        },
        error: err => {
          console.error(err);
          alert('Erro ao cadastrar cartão.');
        }
      });
    }
  }

  deleteCard(idCartao: number) {
    if (confirm('Tem certeza que deseja remover este cartão de crédito?')) {
      this.cardService.deleteCreditCard(idCartao).subscribe({
        next: () => {
          alert('Cartão removido com sucesso!');
          this.loadCards();
        },
        error: err => {
          console.error(err);
          alert('Erro ao remover cartão.');
        }
      });
    }
  }
}
