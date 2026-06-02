import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Investment {
  idInvest?: number;
  idUsuario: number;
  tipoAtivo: string;
  valorAplicado: number;
  roiAtual?: number;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class InvestService {
  private readonly baseUrl = 'http://localhost:8082/api/investments';

  constructor(private http: HttpClient) {}

  getInvestmentsByUser(userId: number): Observable<Investment[]> {
    return this.http.get<ApiResponse<Investment[]>>(`${this.baseUrl}/user/${userId}`).pipe(
      map(response => {
        if (response && response.data && Array.isArray(response.data)) {
          return response.data;
        }
        return [];
      })
    );
  }

  createInvestment(investment: Investment): Observable<any> {
    return this.http.post(`${this.baseUrl}`, investment);
  }

  updateInvestment(investment: Investment): Observable<any> {
    return this.http.put(`${this.baseUrl}`, investment);
  }

  deleteInvestment(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
