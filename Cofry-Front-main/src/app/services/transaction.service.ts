import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Transaction } from '../models/transaction.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TransactionService {

  private apiUrl = 'http://localhost:8082/api/transactions';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getTransactionsByUser(): Observable<Transaction[]> {
    const userId = this.authService.getUserId();

    if (!userId) {
      return this.http.get<Transaction[]>(`${this.apiUrl}`);
      // ou throwError(() => new Error('User not authenticated'));
    }

    return this.http.get<Transaction[]>(
      `${this.apiUrl}/user/${userId}`
    );
  }
}
