import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface CreditCard {
  idCartao?: number;
  idUsuario: number;
  limite: number;
  diaVencimento: number;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class CardService {
  private readonly baseUrl = 'http://localhost:8082/api/credit-cards';

  constructor(private http: HttpClient) {}

  getCreditCardsByUser(userId: number): Observable<CreditCard[]> {
    return this.http.get<ApiResponse<CreditCard[]>>(`${this.baseUrl}/user/${userId}`).pipe(
      map(response => {
        if (response && response.data && Array.isArray(response.data)) {
          return response.data;
        }
        return [];
      })
    );
  }

  createCreditCard(card: CreditCard): Observable<any> {
    return this.http.post(`${this.baseUrl}`, card);
  }

  updateCreditCard(card: CreditCard): Observable<any> {
    return this.http.put(`${this.baseUrl}`, card);
  }

  deleteCreditCard(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
