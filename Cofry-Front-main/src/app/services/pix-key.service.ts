import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface PixKey {
  idChave?: number;
  idUsuario: number;
  tipoChave: string;
  valorChave: string;
  idConta: number;
}

interface ApiResponse<T> {
  status: string;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PixKeyService {
  private readonly baseUrl = 'http://localhost:8082/api/pix-keys';

  constructor(private http: HttpClient) {}

  getPixKeysByUser(userId: number): Observable<PixKey[]> {
    return this.http.get<ApiResponse<PixKey[]>>(`${this.baseUrl}/user/${userId}`).pipe(
      map(response => {
        if (response && response.data && Array.isArray(response.data)) {
          return response.data;
        }
        return [];
      })
    );
  }

  createPixKey(pixKey: PixKey): Observable<any> {
    return this.http.post(`${this.baseUrl}`, pixKey);
  }

  deletePixKey(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
