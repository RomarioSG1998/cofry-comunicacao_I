import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InvestService, Investment } from '../../services/invest.service';

@Component({
  selector: 'app-invest',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invest.html',
  styleUrls: ['./invest.css']
})
export class Invest implements OnInit {
  private router = inject(Router);
  private investService = inject(InvestService);

  investments: Investment[] = [];
  userId: number = 0;
  totalPatrimonio: number = 0;

  // Pie chart calculation
  rendaFixaTotal = 0;
  acoesTotal = 0;
  criptoTotal = 0;
  outrosTotal = 0;

  showForm = false;
  isEdit = false;

  investmentForm = {
    idInvest: undefined as number | undefined,
    tipoAtivo: 'Renda Fixa',
    valorAplicado: null as number | null,
    roiAtual: null as number | null
  };

  ngOnInit() {
    const storedUserId = localStorage.getItem('userId');
    if (!storedUserId) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = Number(storedUserId);
    this.loadInvestments();
  }

  loadInvestments() {
    this.investService.getInvestmentsByUser(this.userId).subscribe({
      next: (data) => {
        this.investments = data;
        this.calculateMetrics();
      },
      error: (err) => console.error('Erro ao buscar investimentos:', err)
    });
  }

  calculateMetrics() {
    this.totalPatrimonio = 0;
    this.rendaFixaTotal = 0;
    this.acoesTotal = 0;
    this.criptoTotal = 0;
    this.outrosTotal = 0;

    this.investments.forEach(inv => {
      const val = Number(inv.valorAplicado) || 0;
      this.totalPatrimonio += val;

      const type = (inv.tipoAtivo || '').toUpperCase();
      if (type.includes('FIXA')) {
        this.rendaFixaTotal += val;
      } else if (type.includes('AÇÕES') || type.includes('ACAO') || type.includes('AÇÃO')) {
        this.acoesTotal += val;
      } else if (type.includes('CRIPTO') || type.includes('BITCOIN')) {
        this.criptoTotal += val;
      } else {
        this.outrosTotal += val;
      }
    });
  }

  getPercent(value: number): number {
    if (this.totalPatrimonio === 0) return 0;
    return Math.round((value / this.totalPatrimonio) * 100);
  }

  getDonutGradient(): string {
    const rf = this.getPercent(this.rendaFixaTotal);
    const ac = this.getPercent(this.acoesTotal);
    const cr = this.getPercent(this.criptoTotal);
    
    // conic-gradient(#10b981 0% 65%, #3b82f6 65% 90%, #f59e0b 90% 100%)
    const p1 = rf;
    const p2 = p1 + ac;
    const p3 = p2 + cr;

    return `conic-gradient(#10b981 0% ${p1}%, #3b82f6 ${p1}% ${p2}%, #f59e0b ${p2}% ${p3}%, #6b7280 ${p3}% 100%)`;
  }

  openAddForm() {
    this.isEdit = false;
    this.showForm = true;
    this.investmentForm = {
      idInvest: undefined,
      tipoAtivo: 'Renda Fixa',
      valorAplicado: null,
      roiAtual: null
    };
  }

  openEditForm(inv: Investment) {
    this.isEdit = true;
    this.showForm = true;
    this.investmentForm = {
      idInvest: inv.idInvest,
      tipoAtivo: inv.tipoAtivo,
      valorAplicado: inv.valorAplicado,
      roiAtual: inv.roiAtual ?? null
    };
  }

  closeForm() {
    this.showForm = false;
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (!this.investmentForm.tipoAtivo.trim()) {
      alert('Informe o tipo do ativo.');
      return;
    }
    if (this.investmentForm.valorAplicado == null || this.investmentForm.valorAplicado <= 0) {
      alert('Valor aplicado inválido.');
      return;
    }

    const payload: Investment = {
      idInvest: this.investmentForm.idInvest,
      idUsuario: this.userId,
      tipoAtivo: this.investmentForm.tipoAtivo,
      valorAplicado: Number(this.investmentForm.valorAplicado),
      roiAtual: this.investmentForm.roiAtual != null ? Number(this.investmentForm.roiAtual) : undefined
    };

    if (this.isEdit) {
      this.investService.updateInvestment(payload).subscribe({
        next: () => {
          alert('Investimento atualizado com sucesso!');
          this.closeForm();
          this.loadInvestments();
        },
        error: err => {
          console.error(err);
          alert('Erro ao atualizar investimento.');
        }
      });
    } else {
      this.investService.createInvestment(payload).subscribe({
        next: () => {
          alert('Investimento criado com sucesso!');
          this.closeForm();
          this.loadInvestments();
        },
        error: err => {
          console.error(err);
          alert('Erro ao cadastrar investimento.');
        }
      });
    }
  }

  deleteInvestment(idInvest: number) {
    if (confirm('Tem certeza que deseja remover este investimento?')) {
      this.investService.deleteInvestment(idInvest).subscribe({
        next: () => {
          alert('Investimento removido com sucesso!');
          this.loadInvestments();
        },
        error: err => {
          console.error(err);
          alert('Erro ao remover investimento.');
        }
      });
    }
  }
}
