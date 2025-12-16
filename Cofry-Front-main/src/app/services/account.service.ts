import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable, map } from 'rxjs';

export interface Account {
  idConta: number;
  idUsuario: number;
  saldo: number;
  instituicao: string;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private apiUrl = 'http://localhost:8081/api/accounts';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getAccountsByUser(): Observable<Account[]> {
    const userId = this.authService.getUserId();

    if (!userId) {
      return this.http.get<any>(`${this.apiUrl}`).pipe(
        map(response => response.data || [])
      );
    }

    return this.http.get<any>(`${this.apiUrl}/user/${userId}`).pipe(
      map(response => response.data || [])
    );
  }

  getAccountById(id: number): Observable<Account> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.data)
    );
  }

  createAccount(account: Partial<Account>): Observable<Account> {
    const userId = this.authService.getUserId();
    if (!userId) {
      throw new Error('User not authenticated');
    }

    const accountData = {
      idUsuario: userId ? parseInt(userId) : account.idUsuario,
      saldo: account.saldo || 0,
      instituicao: account.instituicao || 'Banco'
    };

    return this.http.post<any>(`${this.apiUrl}`, accountData).pipe(
      map(response => response.data)
    );
  }

  updateAccount(id: number, account: Partial<Account>): Observable<Account> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, account).pipe(
      map(response => response.data)
    );
  }

  deleteAccount(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getTotalBalance(): Observable<number> {
    return this.getAccountsByUser().pipe(
      map(accounts => {
        return accounts.reduce((total, account) => {
          return total + (account.saldo || 0);
        }, 0);
      })
    );
  }
}
