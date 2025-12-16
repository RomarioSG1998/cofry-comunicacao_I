import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Transaction } from '../models/transaction.model';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TransactionService {

  private apiUrl = 'http://localhost:8081/api/transactions';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getTransactionsByUser(): Observable<Transaction[]> {
    const userId = this.authService.getUserId();

    if (!userId) {
      return this.http.get<any>(`${this.apiUrl}`).pipe(
        map(response => response.data || [])
      );
    }

    return this.http.get<any>(`${this.apiUrl}/user/${userId}`).pipe(
      map(response => {
        const transactions = response.data || [];
        // Mapear do formato do backend para o formato do frontend
        return transactions.map((t: any) => this.mapBackendToFrontend(t));
      })
    );
  }

  createTransaction(transaction: Partial<Transaction>): Observable<any> {
    const userId = this.authService.getUserId();
    if (!userId) {
      throw new Error('User not authenticated');
    }

    // Mapear do formato do frontend para o formato do backend
    const backendTransaction = this.mapFrontendToBackend(transaction, userId);
    
    return this.http.post<any>(`${this.apiUrl}`, backendTransaction).pipe(
      map(response => response.data)
    );
  }

  updateTransaction(id: number, transaction: Partial<Transaction>): Observable<any> {
    const userId = this.authService.getUserId();
    if (!userId) {
      throw new Error('User not authenticated');
    }

    const backendTransaction = this.mapFrontendToBackend(transaction, userId);
    
    return this.http.put<any>(`${this.apiUrl}/${id}`, backendTransaction).pipe(
      map(response => response.data)
    );
  }

  deleteTransaction(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  // Mapear do backend para frontend
  private mapBackendToFrontend(backend: any): Transaction {
    return {
      id: backend.idTrans || backend.id,
      descricao: backend.descricao || `Transação ${backend.idTrans}`,
      tipo: this.inferTipo(backend),
      categoria: this.mapCategoria(backend.idCategoria),
      forma_pagamento: this.inferFormaPagamento(backend),
      valor: backend.valor ? parseFloat(backend.valor.toString()) : 0,
      data_hora: backend.data ? `${backend.data}T00:00:00` : new Date().toISOString()
    };
  }

  // Mapear do frontend para backend
  private mapFrontendToBackend(frontend: Partial<Transaction>, userId: string | null): any {
    const backend: any = {
      idUsuario: userId ? parseInt(userId) : null,
      valor: frontend.valor || 0,
      data: frontend.data_hora ? frontend.data_hora.split('T')[0] : new Date().toISOString().split('T')[0]
    };

    if (frontend.id) {
      // Para atualização, incluir o ID
      backend.idTrans = frontend.id;
    }

    // Mapear categoria se fornecida
    if (frontend.categoria) {
      backend.idCategoria = this.mapCategoriaToId(frontend.categoria);
    }

    // Mapear forma de pagamento
    if (frontend.forma_pagamento) {
      if (frontend.forma_pagamento.toLowerCase().includes('cartao')) {
        // Se for cartão, precisaria do idCartao
        // Por enquanto, deixamos null
      } else if (frontend.forma_pagamento.toLowerCase().includes('conta')) {
        // Se for conta, precisaria do idConta
        // Por enquanto, deixamos null
      }
    }

    return backend;
  }

  private inferTipo(backend: any): 'PIX' | 'DEBITO' | 'CREDITO' {
    // Inferir tipo baseado nos dados disponíveis
    if (backend.idCartao) {
      return 'CREDITO';
    }
    return 'DEBITO';
  }

  private inferFormaPagamento(backend: any): string {
    if (backend.idCartao) {
      return 'Cartão';
    }
    if (backend.idConta) {
      return 'Conta';
    }
    return 'PIX';
  }

  private mapCategoria(categoriaId: number | null): 'MERCADO' | 'STREAMING' | 'TRANSPORTE' | 'OUTROS' {
    // Mapear IDs de categoria para nomes
    // Isso pode ser ajustado conforme necessário
    if (!categoriaId) return 'OUTROS';
    // Por enquanto, retornar OUTROS
    // Em produção, buscar a categoria do banco
    return 'OUTROS';
  }

  private mapCategoriaToId(categoria: string): number | null {
    // Mapear nomes de categoria para IDs
    // Isso pode ser ajustado conforme necessário
    return null;
  }
}
