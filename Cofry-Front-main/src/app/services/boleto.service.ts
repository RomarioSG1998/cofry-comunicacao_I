import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface BoletoDDA {
  idBoleto?: number;
  idUsuario: number;
  codBarras: string;
  vencimento: string; // ISO 8601 YYYY-MM-DD
  status: string; // "pendente", "pago"
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class BoletoService {
  private readonly baseUrl = 'http://localhost:8082/api/boletos';

  constructor(private http: HttpClient) {}

  getBoletosByUser(userId: number): Observable<BoletoDDA[]> {
    return this.http.get<ApiResponse<BoletoDDA[]>>(`${this.baseUrl}/user/${userId}`).pipe(
      map(response => {
        if (response && response.data && Array.isArray(response.data)) {
          return response.data;
        }
        return [];
      })
    );
  }

  createBoleto(boleto: BoletoDDA): Observable<any> {
    return this.http.post(`${this.baseUrl}`, boleto);
  }

  updateBoleto(boleto: BoletoDDA): Observable<any> {
    return this.http.put(`${this.baseUrl}`, boleto);
  }

  deleteBoleto(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
