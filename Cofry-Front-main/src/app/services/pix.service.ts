import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';

export interface UserInfo {
  userId: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  cpf: string; // Formatado: "123.456.789-00"
  phoneNumber: string | null;
  isActive: boolean;
}

export interface PixTransferRequest {
  sourceAccountId: number;              // ✅ OBRIGATÓRIO
  destinationAccountId?: number;        // ⚠️ OPCIONAL
  destinationUserId?: number;           // ⚠️ OPCIONAL
  destinationCpf?: string;              // ⚠️ FUTURO
  amount: string;                       // ✅ OBRIGATÓRIO - Formato: "100.00"
  description?: string;                 // ⚠️ OPCIONAL
}

export interface PixTransferResponse {
  transactionId: number;
  sourceAccountId: number;
  destinationAccountId: number;
  sourceUserId: number;
  destinationUserId: number;
  amount: number;
  description: string;
  transactionDate: string;
  createdAt: string;
  status: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class PixService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Busca usuário por CPF
   * Nota: Tenta buscar através de endpoint específico ou lista todos e filtra
   */
  getUserByCpf(cpf: string): Observable<UserInfo> {
    const cleanCpf = cpf.replace(/[^\d]/g, '');
    
    if (cleanCpf.length !== 11) {
      return throwError(() => new Error('CPF inválido. Deve ter 11 dígitos.'));
    }

    // Tenta endpoint específico primeiro (se existir)
    return this.http.get<ApiResponse<UserInfo>>(`${this.base}/api/users/cpf/${cleanCpf}`).pipe(
      map(response => {
        if (response && response.success && response.data) {
          return response.data;
        }
        throw new Error('Usuário não encontrado');
      }),
      catchError(() => {
        // Fallback: busca todos os usuários e filtra por CPF
        return this.http.get<UserInfo[]>(`${this.base}/api/users`).pipe(
          map(users => {
            const user = users.find(u => {
              const userCpf = String(u.cpf || (u as any).taxId || '').replace(/[^\d]/g, '');
              return userCpf === cleanCpf;
            });
            
            if (!user) {
              throw new Error(`Usuário não encontrado com CPF: ${this.formatCPF(cleanCpf)}`);
            }
            
            // Garante que o objeto tenha a estrutura esperada
            return {
              userId: (user as any).userId || (user as any).id,
              firstName: (user as any).firstName || '',
              lastName: (user as any).lastName || '',
              email: (user as any).email || '',
              cpf: this.formatCPF(cleanCpf),
              phoneNumber: (user as any).phoneNumber || null,
              isActive: (user as any).isActive !== false
            };
          }),
          catchError(error => {
            const errorMessage = error.error?.error || error.message || 'Erro ao buscar usuário por CPF';
            return throwError(() => new Error(errorMessage));
          })
        );
      })
    );
  }

  /**
   * Realiza transferência PIX
   */
  transferPix(request: PixTransferRequest): Observable<PixTransferResponse> {
    // Validações
    this.validatePixRequest(request);
    
    return this.http.post<ApiResponse<PixTransferResponse>>(
      `${this.base}/api/pix/transfer`,
      request
    ).pipe(
      map(response => {
        if (response && response.success && response.data) {
          return response.data;
        }
        // Se a resposta não tem a estrutura esperada, tenta usar diretamente
        if ((response as any).transactionId) {
          return response as unknown as PixTransferResponse;
        }
        throw new Error('Erro ao realizar transferência PIX');
      }),
      catchError(error => {
        const errorMessage = error.error?.error || error.error?.message || 'Erro ao realizar transferência PIX';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Fluxo completo: Busca usuário por CPF e realiza transferência
   */
  transferPixByCpf(
    sourceAccountId: number,
    destinationCpf: string,
    amount: string,
    description?: string
  ): Observable<PixTransferResponse> {
    // 1. Busca usuário por CPF
    return this.getUserByCpf(destinationCpf).pipe(
      map(user => {
        if (!user.isActive) {
          throw new Error('Usuário destinatário está inativo');
        }
        return user;
      }),
      // 2. Prepara request de transferência
      switchMap(user => {
        const transferRequest: PixTransferRequest = {
          sourceAccountId: sourceAccountId,
          destinationUserId: user.userId,
          amount: amount,
          description: description || `Transferência PIX para ${user.firstName} ${user.lastName}`
        };
        // 3. Executa transferência
        return this.transferPix(transferRequest);
      })
    );
  }

  /**
   * Valida request de transferência PIX
   */
  private validatePixRequest(request: PixTransferRequest): void {
    if (!request.sourceAccountId) {
      throw new Error('Conta de origem é obrigatória');
    }
    
    if (!request.destinationAccountId && !request.destinationUserId) {
      throw new Error('Conta de destino ou ID do usuário é obrigatório');
    }
    
    const amount = parseFloat(request.amount);
    if (!request.amount || isNaN(amount) || amount <= 0) {
      throw new Error('Valor deve ser maior que zero');
    }
  }

  /**
   * Formata CPF para exibição (123.456.789-00)
   */
  formatCPF(cpf: string): string {
    const clean = cpf.replace(/[^\d]/g, '');
    if (clean.length !== 11) {
      return cpf; // Retorna original se não tiver 11 dígitos
    }
    return clean.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  /**
   * Valida CPF (algoritmo de validação)
   */
  validateCPF(cpf: string): boolean {
    const cleanCpf = cpf.replace(/[^\d]/g, '');
    
    if (cleanCpf.length !== 11) {
      return false;
    }
    
    // Verifica se todos os dígitos são iguais (CPFs inválidos)
    if (/^(\d)\1{10}$/.test(cleanCpf)) {
      return false;
    }
    
    // Validação dos dígitos verificadores
    let sum = 0;
    let remainder;
    
    // Valida primeiro dígito
    for (let i = 1; i <= 9; i++) {
      sum += parseInt(cleanCpf.substring(i - 1, i)) * (11 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cleanCpf.substring(9, 10))) {
      return false;
    }
    
    // Valida segundo dígito
    sum = 0;
    for (let i = 1; i <= 10; i++) {
      sum += parseInt(cleanCpf.substring(i - 1, i)) * (12 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cleanCpf.substring(10, 11))) {
      return false;
    }
    
    return true;
  }
}

