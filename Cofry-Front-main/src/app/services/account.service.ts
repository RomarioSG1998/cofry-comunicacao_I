import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Account {
  idConta?: number;
  idUsuario: number;
  saldo: number;
  instituicao: string;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private apiUrl = `${environment.apiUrl}/api/accounts`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getAccountsByUser(): Observable<Account[]> {
    const userId = this.authService.getUserId();
    return this.http.get<ApiResponse<Account[]>>(`${this.apiUrl}/user/${userId}`).pipe(
      map(res => res.data || [])
    );
  }

  createAccount(account: Account): Observable<any> {
    return this.http.post<any>(this.apiUrl, account);
  }

  updateAccount(id: number, account: Account): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, account);
  }

  deleteAccount(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
