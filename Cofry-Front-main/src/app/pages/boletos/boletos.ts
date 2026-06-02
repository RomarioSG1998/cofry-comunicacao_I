import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BoletoService, BoletoDDA } from '../../services/boleto.service';

@Component({
  selector: 'app-boletos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './boletos.html',
  styleUrls: ['./boletos.css']
})
export class Boletos implements OnInit {
  private router = inject(Router);
  private boletoService = inject(BoletoService);

  boletos: BoletoDDA[] = [];
  userId: number = 0;

  showForm = false;
  isEdit = false;

  boletoForm = {
    idBoleto: undefined as number | undefined,
    codBarras: '',
    vencimento: '',
    status: 'pendente'
  };

  ngOnInit() {
    const storedUserId = localStorage.getItem('userId');
    if (!storedUserId) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = Number(storedUserId);
    this.loadBoletos();
  }

  loadBoletos() {
    this.boletoService.getBoletosByUser(this.userId).subscribe({
      next: (data) => {
        this.boletos = data;
      },
      error: (err) => console.error('Erro ao buscar boletos:', err)
    });
  }

  formatBarcode(event: Event) {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, ''); // Remove non-digits
    
    // Apply standard boleto layout masking (47 digits for bank slip, 48 digits for concessions)
    if (value.length > 48) {
      value = value.substring(0, 48);
    }

    if (value.length <= 47) {
      // 47-digit format: 00000.00000 00000.000000 00000.000000 0 00000000000000
      let formatted = '';
      if (value.length > 0) formatted += value.substring(0, 5);
      if (value.length > 5) formatted += '.' + value.substring(5, 10);
      if (value.length > 10) formatted += ' ' + value.substring(10, 15);
      if (value.length > 15) formatted += '.' + value.substring(15, 21);
      if (value.length > 21) formatted += ' ' + value.substring(21, 26);
      if (value.length > 26) formatted += '.' + value.substring(26, 32);
      if (value.length > 32) formatted += ' ' + value.substring(32, 33);
      if (value.length > 33) formatted += ' ' + value.substring(33, 47);
      input.value = formatted;
    } else {
      // 48-digit format: 00000000000-0 00000000000-0 00000000000-0 00000000000-0
      let formatted = '';
      if (value.length > 0) formatted += value.substring(0, 11);
      if (value.length > 11) formatted += '-' + value.substring(11, 12);
      if (value.length > 12) formatted += ' ' + value.substring(12, 23);
      if (value.length > 23) formatted += '-' + value.substring(23, 24);
      if (value.length > 24) formatted += ' ' + value.substring(24, 35);
      if (value.length > 35) formatted += '-' + value.substring(35, 36);
      if (value.length > 36) formatted += ' ' + value.substring(36, 47);
      if (value.length > 47) formatted += '-' + value.substring(47, 48);
      input.value = formatted;
    }

    this.boletoForm.codBarras = input.value;
  }

  openAddForm() {
    this.isEdit = false;
    this.showForm = true;
    this.boletoForm = {
      idBoleto: undefined,
      codBarras: '',
      vencimento: '',
      status: 'pendente'
    };
  }

  openEditForm(boleto: BoletoDDA) {
    this.isEdit = true;
    this.showForm = true;
    this.boletoForm = {
      idBoleto: boleto.idBoleto,
      codBarras: boleto.codBarras,
      vencimento: boleto.vencimento,
      status: boleto.status
    };
  }

  closeForm() {
    this.showForm = false;
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (!this.boletoForm.codBarras.trim()) {
      alert('Informe o código de barras.');
      return;
    }
    if (!this.boletoForm.vencimento) {
      alert('Informe a data de vencimento.');
      return;
    }

    const payload: BoletoDDA = {
      idBoleto: this.boletoForm.idBoleto,
      idUsuario: this.userId,
      codBarras: this.boletoForm.codBarras,
      vencimento: this.boletoForm.vencimento,
      status: this.boletoForm.status
    };

    if (this.isEdit) {
      this.boletoService.updateBoleto(payload).subscribe({
        next: () => {
          alert('Boleto atualizado com sucesso!');
          this.closeForm();
          this.loadBoletos();
        },
        error: err => {
          console.error(err);
          alert('Erro ao atualizar boleto.');
        }
      });
    } else {
      this.boletoService.createBoleto(payload).subscribe({
        next: () => {
          alert('Boleto cadastrado com sucesso!');
          this.closeForm();
          this.loadBoletos();
        },
        error: err => {
          console.error(err);
          alert('Erro ao cadastrar boleto.');
        }
      });
    }
  }

  payBoleto(boleto: BoletoDDA) {
    if (confirm('Deseja marcar este boleto como pago?')) {
      const payload: BoletoDDA = {
        ...boleto,
        status: 'pago'
      };
      this.boletoService.updateBoleto(payload).subscribe({
        next: () => {
          alert('Boleto marcado como PAGO!');
          this.loadBoletos();
        },
        error: err => {
          console.error(err);
          alert('Erro ao atualizar status do boleto.');
        }
      });
    }
  }

  deleteBoleto(idBoleto: number) {
    if (confirm('Tem certeza que deseja remover este boleto DDA?')) {
      this.boletoService.deleteBoleto(idBoleto).subscribe({
        next: () => {
          alert('Boleto removido com sucesso!');
          this.loadBoletos();
        },
        error: err => {
          console.error(err);
          alert('Erro ao remover boleto.');
        }
      });
    }
  }
}
