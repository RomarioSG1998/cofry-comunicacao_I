import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = 'http://localhost:8081';
  constructor(private http: HttpClient) {}

  // Método genérico de GET para qualquer URL da API
  fetch<T>(email: string): Observable<T> {
    return this.http.get<T>(`${this.base}/api/user-data?email=${encodeURIComponent(email)}`);
  }
}

