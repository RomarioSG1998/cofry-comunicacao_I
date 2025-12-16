import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SignUpRequest {
  fullName: string;
  email: string;
  cpf: string;
  password: string;
  planId?: number; // Opcional, pode ser enviado se necessário
}

export interface SignUpResponse {
  userId?: number;
  fullName?: string;
  email?: string;
  cpf?: string;
  [key: string]: any; // Permite campos adicionais da resposta
}

@Injectable({ providedIn: 'root' })
export class SignUpService {
  private base = 'http://localhost:8080';
  
  constructor(private http: HttpClient) {}

  register(usuario: SignUpRequest): Observable<SignUpResponse> {
    return this.http.post<SignUpResponse>(`${this.base}/api/form/user`, usuario);
  }
}
