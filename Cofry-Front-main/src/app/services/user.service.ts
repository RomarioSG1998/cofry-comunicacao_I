import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Address } from './address.service';
import { Account } from './account.service';

export interface User {
  userId: number;
  planId: number;
  firstName: string;
  lastName: string;
  taxId: string;
  email: string;
  phoneNumber?: string;
  dateOfBirth: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Interface para dados completos do usuário retornados pelo endpoint /complete
 * Contém apenas os campos necessários: dados pessoais, planId, endereços e contas
 */
export interface UserCompleteDTO {
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  cpf: string;
  phoneNumber: string | null;
  dateOfBirth: string; // ISO date: "YYYY-MM-DD"
  planId: number;
  addresses: Address[];
  accounts: Account[];
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = 'http://localhost:8080';
  
  constructor(private http: HttpClient) {}

  /**
   * Busca um usuário por ID (dados básicos)
   */
  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.base}/api/users/${userId}`);
  }

  /**
   * Busca dados completos do usuário (pessoais + endereços + contas)
   * Endpoint: GET /api/users/{id}/complete
   */
  getUserComplete(userId: number): Observable<UserCompleteDTO> {
    return this.http.get<any>(`${this.base}/api/users/${userId}/complete`).pipe(
      map((response) => {
        // Trata resposta que pode vir como { success: true, data: {...} } ou diretamente os dados
        const data = response.data || response;
        
        // Mapeia houseNumber para number nos endereços
        const addresses = (data.addresses || []).map((addr: any) => ({
          ...addr,
          number: addr.houseNumber || addr.number
        }));
        
        // Nota: A correção UTF-8 dos nomes de bancos será aplicada no componente
        // para evitar dependência circular entre serviços
        const accounts = data.accounts || [];
        
        return {
          firstName: data.firstName,
          lastName: data.lastName,
          fullName: data.fullName,
          email: data.email,
          cpf: data.cpf,
          phoneNumber: data.phoneNumber,
          dateOfBirth: data.dateOfBirth,
          planId: data.planId,
          addresses: addresses,
          accounts: accounts
        };
      })
    );
  }

  /**
   * Lista todos os usuários
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/api/users`);
  }

  /**
   * Atualiza os dados de um usuário
   */
  updateUser(userId: number, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.base}/api/users/${userId}`, userData);
  }

  /**
   * Atualiza dados do usuário via formulário (CPF, email, data de nascimento)
   * Rota: PUT /api/form/user/{id}
   */
  updateUserFromForm(userId: number, userData: { cpf?: string; email?: string; dateOfBirth?: string }): Observable<User> {
    return this.http.put<User>(`${this.base}/api/form/user/${userId}`, userData);
  }

  /**
   * Deleta um usuário
   */
  deleteUser(userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.base}/api/users/${userId}`);
  }

  /**
   * Método legado mantido para compatibilidade
   * @deprecated Use getUserById() após obter userId do login
   */
  fetch<T>(email: string): Observable<T> {
    // Busca todos os usuários e filtra por email (não recomendado para produção)
    return this.http.get<T>(`${this.base}/api/users`);
  }
}




