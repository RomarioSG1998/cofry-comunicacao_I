import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { TransactionCardComponent } from '../../shared/transaction-card/transaction-card';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-after-login',
  standalone: true,
  imports: [CommonModule, TransactionCardComponent],
  templateUrl: './after-login.html',
  styleUrls: ['./after-login.css']
})
export class AfterLogin implements OnInit {
  // Inicialização do array de transações
  transactions: Transaction[] = [];

  // Injeção de dependências
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private platformId = inject(PLATFORM_ID);

  // Propriedades de estado do componente
  showBalance = true;
  userData: any = null;
  balance = '24.500,00'; // Valor padrão, pode ser carregado da API

  constructor() {
    console.log('AfterLogin - Constructor chamado');
  }

  ngOnInit(): void {
    console.log('AfterLogin - ngOnInit iniciado');
    
    if (!isPlatformBrowser(this.platformId)) {
      console.log('AfterLogin - SSR detectado, retornando');
      return; // Não faz nada no SSR
    }

    const userEmail = localStorage.getItem('userEmail');
    const storedUserData = localStorage.getItem('userData');

    console.log('AfterLogin - Dados do usuário:', { userEmail, storedUserData });

    if (storedUserData) {
      try {
        this.userData = JSON.parse(storedUserData);
        console.log('AfterLogin - userData parseado:', this.userData);
      } catch (error) {
        console.error('AfterLogin - Erro ao parsear dados do usuário:', error);
      }
    }

    if (userEmail) {
      this.loadUserData(userEmail);
    }

    // Carregar transações (chamada principal) - com tratamento de erro para não quebrar a página
    try {
      this.loadTransactions();
    } catch (error) {
      console.error('AfterLogin - Erro ao inicializar transações:', error);
      this.transactions = [];
    }
    
    console.log('AfterLogin - ngOnInit concluído');
  }

  loadUserData(email: string): void {
    // Implementação para carregar dados adicionais do usuário
  }

  // FUNÇÃO AJUSTADA PARA ORDENAR E LIMITAR AS TRANSAÇÕES
  loadTransactions(): void {
    this.transactionService.getTransactionsByUser().subscribe({
      next: (data) => {
        console.log('Transações carregadas:', data);
        // Verifica se data é um array válido
        if (Array.isArray(data)) {
          // 1. Ordena as transações pela data de criação (mais recente primeiro)
          const sortedData = data.sort((a, b) => {
            const dateA = a.data_hora ? new Date(a.data_hora).getTime() : 0;
            const dateB = b.data_hora ? new Date(b.data_hora).getTime() : 0;
            return dateB - dateA;
          });

          // 2. Limita para exibir apenas as 5 transações mais recentes no dashboard
          this.transactions = sortedData.slice(0, 5);
        } else {
          console.warn('Dados de transações não são um array:', data);
          this.transactions = [];
        }
      },
      error: (error) => {
        console.error('Erro ao carregar transações:', error);
        // Se der erro, simplesmente mantém o array vazio para não quebrar a página
        this.transactions = [];
      }
    });
  }

  toggleBalance(): void {
    this.showBalance = !this.showBalance;
  }
}