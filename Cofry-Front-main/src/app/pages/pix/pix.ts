import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AccountService, Account } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-pix',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pix.html',
  styleUrls: ['./pix.css']
})
export class Pix implements OnInit {
  // Injetar o Sanitizer para confiar nos SVGs
  private sanitizer = inject(DomSanitizer);
  private router = inject(Router);
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);

  balance = '0,00';
  accounts: Account[] = [];

  pixForm = {
    chave: '',
    valor: null as number | null,
    idConta: null as number | null
  };
  
  // Ícones como strings SVG
  icons = {
    qr: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/><path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/><rect x="7" y="7" width="3" height="3"/><rect x="14" y="7" width="3" height="3"/><rect x="7" y="14" width="3" height="3"/><path d="M14 14h3v3h-3z"/></svg>`,
    copy: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M15 2H9a1 1 0 0 0-1 1v2a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1Z"/></svg>`,
    transfer: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    key: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg>`
  };

  pixActions = [
    { label: 'Ler QR Code', icon: this.icons.qr, highlight: false },
    { label: 'Pix Copia e Cola', icon: this.icons.copy, highlight: true },
    { label: 'Transferir', icon: this.icons.transfer, highlight: false },
    { label: 'Minhas Chaves', icon: this.icons.key, highlight: false },
  ];

  /**
   * Navega para a rota correspondente ao atalho.
   * Tenta alguns formatos de caminho (ex.: 'Transferir' -> '/Transferir', 'minhas-chaves', 'minhaschaves').
   */
  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.accountService.getAccountsByUser().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.pixForm.idConta = accs[0].idConta ?? null;
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

  async onActionClick(label: string) {
    const candidates = [
      label, // try exact (case-sensitive) path
      label.replace(/\s+/g, '-'), // dashed
      label.replace(/\s+/g, '').replace(/[^a-zA-Z0-9\-]/g, ''), // joined
      label.toLowerCase(),
      label.toLowerCase().replace(/\s+/g, '-'),
      label.split(' ')[0] // first word fallback
    ];

    for (const cand of candidates) {
      if (!cand) continue;
      try {
        const success = await this.router.navigate([`/nav/${cand}`]);
        if (success) return;
      } catch (e) {
        // ignore and try next
      }
    }

    // Se não navegou para nenhum, mostrar aviso
    alert('Rota não encontrada para: ' + label);
  }

  formatPixKey(event: any) {
    const input = event.target.value;
    if (input.includes('@')) {
      this.pixForm.chave = input;
      return;
    }
    
    let clean = input.replace(/\D/g, '');
    if (clean.length > 11) {
      clean = clean.substring(0, 11);
    }
    
    if (clean.length === 11) {
      if (clean.charAt(2) === '9') {
        this.pixForm.chave = `(${clean.substring(0, 2)}) ${clean.substring(2, 7)}-${clean.substring(7, 11)}`;
      } else {
        this.pixForm.chave = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6, 9)}-${clean.substring(9, 11)}`;
      }
    } else if (clean.length === 10) {
      this.pixForm.chave = `(${clean.substring(0, 2)}) ${clean.substring(2, 6)}-${clean.substring(6, 10)}`;
    } else if (clean.length > 9) {
      this.pixForm.chave = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6, 9)}-${clean.substring(9)}`;
    } else if (clean.length > 6) {
      this.pixForm.chave = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6)}`;
    } else if (clean.length > 3) {
      this.pixForm.chave = `${clean.substring(0, 3)}.${clean.substring(3)}`;
    } else {
      this.pixForm.chave = clean;
    }
    
    event.target.value = this.pixForm.chave;
  }

  sendPix(event: Event) {
    event.preventDefault();
    const userId = Number(localStorage.getItem('userId'));
    if (!userId) {
      alert('Usuário não logado.');
      return;
    }
    if (!this.pixForm.valor || this.pixForm.valor <= 0) {
      alert('Por favor, informe um valor válido.');
      return;
    }
    if (!this.pixForm.idConta) {
      alert('Selecione uma conta de origem.');
      return;
    }

    const sourceAccount = this.accounts.find(acc => acc.idConta === Number(this.pixForm.idConta));
    if (sourceAccount && Number(sourceAccount.saldo) < this.pixForm.valor) {
      alert(`Saldo insuficiente na conta selecionada. Saldo disponível: R$ ${Number(sourceAccount.saldo).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      return;
    }

    const payload = {
      idUsuario: userId,
      valor: -Math.abs(this.pixForm.valor), // Debitando valor
      data: formatDate(new Date(), 'yyyy-MM-dd', 'en-US'),
      idCategoria: 2, // Recebimento de Salário / Transferência Pix categoria
      idConta: Number(this.pixForm.idConta)
    };

    this.transactionService.createTransaction(payload).subscribe({
      next: () => {
        alert('Pix realizado com sucesso!');
        this.router.navigate(['/nav/home']);
      },
      error: err => {
        console.error(err);
        alert('Erro ao realizar Pix.');
      }
    });
  }

  contacts = [
    { firstName: 'João', lastName: 'Silva', initials: 'JS', isNew: false },
    { firstName: 'Maria', lastName: 'Alice', initials: 'MA', isNew: false },
    { firstName: 'Pedro', lastName: 'Lucas', initials: 'PL', isNew: false },
    { firstName: 'Thiago', lastName: 'Reis', initials: 'TR', isNew: false },
    { firstName: 'Novo', lastName: '', initials: '', isNew: true },
  ];

  getSafeIcon(iconString: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(iconString);
  }
}