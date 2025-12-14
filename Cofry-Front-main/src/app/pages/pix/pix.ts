import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pix',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pix.html',
  styleUrls: ['./pix.css']
})
export class Pix {
  // Injetar o Sanitizer para confiar nos SVGs
  private sanitizer = inject(DomSanitizer);
  private router = inject(Router);
  
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
        const success = await this.router.navigate([`/${cand}`]);
        if (success) return;
      } catch (e) {
        // ignore and try next
      }
    }

    // Se não navegou para nenhum, mostrar aviso
    alert('Rota não encontrada para: ' + label);
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