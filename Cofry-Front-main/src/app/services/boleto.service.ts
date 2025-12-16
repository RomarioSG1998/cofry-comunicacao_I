import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Boleto {
  id: number;
  title: string;
  amount: number;
  formattedAmount: string;
  dueDate: string;
  status: 'OPEN' | 'OVERDUE' | 'PAID';
  statusLabel: string;
  bankCode: string;
  walletCode: string;
  ourNumber: string;
  boletoCode: string;
  userId: number;
  paidAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBoletoRequest {
  userId: number;
  title: string;
  amount: number;
  dueDate: string; // YYYY-MM-DD
}

@Injectable({ providedIn: 'root' })
export class BoletoService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Lista todos os boletos
   */
  getAllBoletos(): Observable<Boleto[]> {
    return this.http.get<Boleto[]>(`${this.base}/api/form/boleto`);
  }

  /**
   * Lista boletos de um usuário específico
   */
  getBoletosByUserId(userId: number): Observable<Boleto[]> {
    return this.http.get<Boleto[]>(`${this.base}/api/form/boleto/user/${userId}`);
  }

  /**
   * Busca boletos por CPF
   */
  getBoletosByCpf(cpf: string): Observable<Boleto[]> {
    // Remove formatação do CPF se houver
    const cleanCpf = cpf.replace(/[.-]/g, '');
    return this.http.get<Boleto[]>(`${this.base}/api/form/boleto/cpf/${cleanCpf}`);
  }

  /**
   * Busca boletos por status
   */
  getBoletosByStatus(status: 'OPEN' | 'OVERDUE' | 'PAID'): Observable<Boleto[]> {
    return this.http.get<Boleto[]>(`${this.base}/api/form/boleto/status/${status}`);
  }

  /**
   * Cria um novo boleto
   */
  createBoleto(boleto: CreateBoletoRequest): Observable<Boleto> {
    return this.http.post<Boleto>(`${this.base}/api/form/boleto`, boleto);
  }
}

