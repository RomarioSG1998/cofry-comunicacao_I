import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService, Account } from '../../services/account.service';
import { PixKeyService, PixKey } from '../../services/pix-key.service';

@Component({
  selector: 'app-pix-keys',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pix-keys.component.html',
  styleUrls: ['./pix-keys.component.css']
})
export class PixKeysComponent implements OnInit {
  private router = inject(Router);
  private accountService = inject(AccountService);
  private pixKeyService = inject(PixKeyService);

  accounts: Account[] = [];
  pixKeys: PixKey[] = [];
  userId: number = 0;

  keyForm = {
    tipoChave: 'CPF',
    valorChave: '',
    idConta: null as number | null
  };

  ngOnInit() {
    const storedUserId = localStorage.getItem('userId');
    if (!storedUserId) {
      alert('Usuário não logado.');
      this.router.navigate(['/login']);
      return;
    }
    this.userId = Number(storedUserId);
    this.loadAccounts();
    this.loadPixKeys();
  }

  loadAccounts() {
    this.accountService.getAccountsByUser().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.keyForm.idConta = accs[0].idConta ?? null;
        }
      },
      error: err => console.error('Erro ao carregar contas:', err)
    });
  }

  loadPixKeys() {
    this.pixKeyService.getPixKeysByUser(this.userId).subscribe({
      next: (keys) => {
        this.pixKeys = keys;
      },
      error: err => console.error('Erro ao carregar chaves:', err)
    });
  }

  generateRandomKey() {
    const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
    this.keyForm.tipoChave = 'ALEATORIA';
    this.keyForm.valorChave = uuid;
  }

  formatKeyInput(event: any) {
    const type = this.keyForm.tipoChave;
    const input = event.target.value;

    if (type === 'CPF') {
      let clean = input.replace(/\D/g, '');
      if (clean.length > 11) clean = clean.substring(0, 11);
      
      if (clean.length > 9) {
        this.keyForm.valorChave = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6, 9)}-${clean.substring(9)}`;
      } else if (clean.length > 6) {
        this.keyForm.valorChave = `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6)}`;
      } else if (clean.length > 3) {
        this.keyForm.valorChave = `${clean.substring(0, 3)}.${clean.substring(3)}`;
      } else {
        this.keyForm.valorChave = clean;
      }
    } else if (type === 'TELEFONE') {
      let clean = input.replace(/\D/g, '');
      if (clean.length > 11) clean = clean.substring(0, 11);

      if (clean.length === 11) {
        this.keyForm.valorChave = `(${clean.substring(0, 2)}) ${clean.substring(2, 7)}-${clean.substring(7, 11)}`;
      } else if (clean.length === 10) {
        this.keyForm.valorChave = `(${clean.substring(0, 2)}) ${clean.substring(2, 6)}-${clean.substring(6, 10)}`;
      } else if (clean.length > 2) {
        this.keyForm.valorChave = `(${clean.substring(0, 2)}) ${clean.substring(2)}`;
      } else {
        this.keyForm.valorChave = clean;
      }
    } else {
      this.keyForm.valorChave = input;
    }
    event.target.value = this.keyForm.valorChave;
  }

  onTypeChange() {
    this.keyForm.valorChave = '';
  }

  registerKey(event: Event) {
    event.preventDefault();
    if (!this.keyForm.valorChave.trim()) {
      alert('Por favor, informe o valor da chave.');
      return;
    }
    if (!this.keyForm.idConta) {
      alert('Selecione uma conta vinculada.');
      return;
    }

    const payload: PixKey = {
      idUsuario: this.userId,
      tipoChave: this.keyForm.tipoChave,
      valorChave: this.keyForm.valorChave,
      idConta: Number(this.keyForm.idConta)
    };

    this.pixKeyService.createPixKey(payload).subscribe({
      next: () => {
        alert('Chave Pix cadastrada com sucesso!');
        this.keyForm.valorChave = '';
        this.loadPixKeys();
      },
      error: (err) => {
        console.error(err);
        if (err.status === 409) {
          alert('Esta chave Pix já está cadastrada.');
        } else {
          alert('Erro ao cadastrar chave Pix.');
        }
      }
    });
  }

  deleteKey(idChave: number) {
    if (confirm('Tem certeza que deseja remover esta chave Pix?')) {
      this.pixKeyService.deletePixKey(idChave).subscribe({
        next: () => {
          alert('Chave Pix removida com sucesso!');
          this.loadPixKeys();
        },
        error: err => {
          console.error(err);
          alert('Erro ao remover chave Pix.');
        }
      });
    }
  }

  getAccountLabel(idConta: number): string {
    const acc = this.accounts.find(a => a.idConta === idConta);
    return acc ? `${acc.instituicao}` : `Conta #${idConta}`;
  }

  goBack() {
    this.router.navigate(['/nav/Pix']);
  }
}
