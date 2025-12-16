import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
  };
}

@Injectable({ providedIn: 'root' })
export class LoginService {
  private base = 'http://localhost:8080';
  
  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/api/auth/login`, credentials);
  }
}




