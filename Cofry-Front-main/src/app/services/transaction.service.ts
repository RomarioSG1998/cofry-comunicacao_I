import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Transaction, TransactionResponse, ApiResponse } from '../models/transaction.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface TransactionFilters {
  accountId?: number;
  userId?: number;
  type?: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT';
  categoryId?: number;
  startDate?: string; // YYYY-MM-DD
  endDate?: string; // YYYY-MM-DD
}

// Mapeamento de categoryId para categoria (pode ser expandido com chamada à API)
const categoryIdMap: { [key: number]: 'MERCADO' | 'STREAMING' | 'TRANSPORTE' | 'OUTROS' } = {
  1: 'MERCADO',
  2: 'STREAMING',
  3: 'TRANSPORTE',
  4: 'OUTROS'
};

// Mapeamento de transactionType para tipo antigo
const typeMap: { [key: string]: 'PIX' | 'DEBITO' | 'CREDITO' } = {
  'DEPOSIT': 'CREDITO',
  'WITHDRAWAL': 'DEBITO',
  'PAYMENT': 'DEBITO',
  'TRANSFER': 'PIX'
};

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private apiUrl = 'http://localhost:8080/api/transactions';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Converte TransactionResponse para Transaction (compatibilidade)
   */
  private mapTransactionResponse(response: TransactionResponse | any): Transaction {
    // Trata caso onde transactionId pode não estar presente (usa sourceAccountId como fallback temporário)
    const transactionId = response.transactionId || response.id || 0;
    const categoryId = response.categoryId || 4; // Default: OUTROS
    const categoria = categoryIdMap[categoryId] || 'OUTROS';
    const tipo = typeMap[response.transactionType] || 'DEBITO';
    
    // Converte transactionDate para data_hora (adiciona hora se necessário)
    let data_hora = response.transactionDate || response.data_hora;
    if (!data_hora) {
      // Se não tiver data, usa createdAt
      data_hora = response.createdAt || new Date().toISOString();
    }
    
    if (!data_hora.includes('T')) {
      // Se não tiver hora, usa a hora de criação ou 00:00:00
      if (response.createdAt) {
        const createdDate = new Date(response.createdAt);
        const hours = createdDate.getHours().toString().padStart(2, '0');
        const minutes = createdDate.getMinutes().toString().padStart(2, '0');
        data_hora = `${response.transactionDate}T${hours}:${minutes}:00`;
      } else {
        data_hora = `${response.transactionDate}T00:00:00`;
      }
    }
    
    return {
      id: transactionId,
      transactionId: transactionId,
      descricao: response.description || response.descricao || '',
      tipo: tipo,
      categoria: categoria,
      forma_pagamento: response.transactionType === 'TRANSFER' ? 'PIX' : 
                       response.transactionType === 'DEPOSIT' ? 'Crédito' : 'Débito',
      valor: response.transactionType === 'DEPOSIT' ? response.amount : -response.amount, // Negativo para saídas
      data_hora: data_hora,
      transactionDate: response.transactionDate,
      transactionType: response.transactionType,
      categoryId: response.categoryId,
      isRecurring: response.isRecurring || false,
      installmentCurrent: response.installmentCurrent || null,
      installmentTotal: response.installmentTotal || null
    };
  }

  /**
   * Lista transações com filtros opcionais
   */
  getTransactions(filters?: TransactionFilters): Observable<Transaction[]> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.accountId) params = params.set('accountId', filters.accountId.toString());
      if (filters.userId) params = params.set('userId', filters.userId.toString());
      if (filters.type) params = params.set('type', filters.type);
      if (filters.categoryId) params = params.set('categoryId', filters.categoryId.toString());
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
    }

    console.log('Fazendo requisição GET para:', this.apiUrl, 'com params:', params.toString());
    
    return this.http.get<TransactionResponse[] | ApiResponse<TransactionResponse[]>>(this.apiUrl, { params }).pipe(
      map(response => {
        console.log('Resposta bruta da API:', response);
        
        // Verifica se a resposta é um array direto ou um objeto com success/data
        let transactions: TransactionResponse[];
        
        if (Array.isArray(response)) {
          // Resposta direta como array (formato atual da API)
          console.log('Resposta é um array direto com', response.length, 'transações');
          transactions = response;
        } else if (response && 'success' in response && response.success && response.data) {
          // Resposta com wrapper {success: true, data: [...]}
          console.log('Resposta tem wrapper success/data');
          transactions = response.data;
        } else {
          // Resposta vazia ou inválida
          console.warn('Resposta vazia ou inválida:', response);
          return [];
        }
        
        console.log('Mapeando', transactions.length, 'transações...');
        const mapped = transactions.map(t => this.mapTransactionResponse(t));
        console.log('Transações mapeadas:', mapped);
        return mapped;
      })
    );
  }

  /**
   * Busca transações do usuário atual
   */
  getTransactionsByUser(): Observable<Transaction[]> {
    // Tenta buscar userId do localStorage primeiro (mais confiável)
    const storedUserId = localStorage.getItem('userId');
    if (storedUserId) {
      console.log('Usando userId do localStorage:', storedUserId);
      return this.getTransactions({ userId: Number(storedUserId) });
    }
    
    // Fallback: tenta buscar do authService
    const userId = this.authService.getUserId();
    if (userId) {
      console.log('Usando userId do authService:', userId);
      return this.getTransactions({ userId: Number(userId) });
    }
    
    // Se não tiver userId, retorna array vazio
    console.warn('Nenhum userId encontrado. Retornando array vazio.');
    return new Observable(observer => {
      observer.next([]);
      observer.complete();
    });
  }

  /**
   * Busca uma transação por ID
   */
  getTransactionById(id: number): Observable<Transaction> {
    return this.http.get<TransactionResponse | ApiResponse<TransactionResponse>>(`${this.apiUrl}/${id}`).pipe(
      map(response => {
        // Verifica se a resposta é direta ou tem wrapper
        let transaction: TransactionResponse;
        
        if ('transactionId' in response) {
          // Resposta direta
          transaction = response as TransactionResponse;
        } else if (response && 'success' in response && response.success && response.data) {
          // Resposta com wrapper
          transaction = response.data;
        } else {
          throw new Error('Transação não encontrada');
        }
        
        return this.mapTransactionResponse(transaction);
      })
    );
  }

  /**
   * Cria uma nova transação
   */
  createTransaction(transaction: any): Observable<Transaction> {
    return this.http.post<Transaction>(this.apiUrl, transaction);
  }

  /**
   * Atualiza uma transação
   */
  updateTransaction(id: number, transaction: Partial<Transaction>): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.apiUrl}/${id}`, transaction);
  }

  /**
   * Deleta uma transação
   */
  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
