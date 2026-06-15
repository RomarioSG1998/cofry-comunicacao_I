import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Transaction } from '../models/transaction.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

interface BackendTransaction {
  idTrans: number;
  idUsuario: number;
  valor: number;
  data: string;
  comprovanteUrl?: string;
  idCategoria?: number;
  idConta?: number;
  idCartao?: number;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {

  private apiUrl = `${environment.apiUrl}/api/transactions`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getTransactionsByUser(limit?: number, offset?: number): Observable<Transaction[]> {
    const userId = this.authService.getUserId();

    if (!userId) {
      return this.http.get<ApiResponse<BackendTransaction[]>>(`${this.apiUrl}`).pipe(
        map(res => this.mapBackendTransactions(res))
      );
    }

    let url = `${this.apiUrl}/user/${userId}`;
    if (limit !== undefined && offset !== undefined) {
      url += `?limit=${limit}&offset=${offset}`;
    }

    return this.http.get<ApiResponse<BackendTransaction[]>>(url).pipe(
      map(res => this.mapBackendTransactions(res))
    );
  }

  createTransaction(transaction: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, transaction);
  }

  updateTransaction(id: number, transaction: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, transaction);
  }

  deleteTransaction(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  // CORRIGIDO: era /api/categories, o endpoint correto é /api/transaction-categories
  getCategories(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/api/transaction-categories`).pipe(
      map(res => res.data || [])
    );
  }

  getAccountsByUser(): Observable<any[]> {
    const userId = this.authService.getUserId();
    return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/api/accounts/user/${userId}`).pipe(
      map(res => res.data || [])
    );
  }

  private mapBackendTransactions(res: ApiResponse<BackendTransaction[]>): Transaction[] {
    if (!res || !res.data || !Array.isArray(res.data)) {
      return [];
    }

    return res.data.map(t => {
      let desc = 'Transação';
      let cat: 'MERCADO' | 'STREAMING' | 'TRANSPORTE' | 'OUTROS' = 'OUTROS';
      let tipo: 'PIX' | 'DEBITO' | 'CREDITO' = 'DEBITO';
      let forma = 'Outros';

      if (t.idCategoria === 1) {
        desc = 'Mercado / Alimentação';
        cat = 'MERCADO';
        tipo = 'DEBITO';
        forma = 'Cartão de Débito';
      } else if (t.idCategoria === 2) {
        desc = 'Recebimento de Salário';
        cat = 'OUTROS';
        tipo = 'PIX';
        forma = 'Transferência Pix';
      } else if (t.idCategoria === 3) {
        desc = 'Uber / Transporte';
        cat = 'TRANSPORTE';
        tipo = 'DEBITO';
        forma = 'Cartão de Débito';
      } else if (t.idCategoria === 4) {
        desc = 'Assinatura / Streaming';
        cat = 'STREAMING';
        tipo = 'CREDITO';
        forma = 'Cartão de Crédito';
      } else if (t.idCategoria === 7) {
        desc = 'Rendimento de Investimento';
        cat = 'OUTROS';
        tipo = 'PIX';
        forma = 'Transferência Pix';
      }

      let val = t.valor;
      if ((t.idCategoria === 1 || t.idCategoria === 3 || t.idCategoria === 4 || t.idCategoria === 5 || t.idCategoria === 6 || t.idCategoria === 8) && val > 0) {
        val = -val;
      }

      return {
        id: t.idTrans,
        descricao: desc,
        tipo: tipo,
        categoria: cat,
        forma_pagamento: forma,
        valor: val,
        data_hora: t.data + 'T12:00:00'
      };
    });
  }
}
