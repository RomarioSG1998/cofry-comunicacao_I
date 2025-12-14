import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SignUpService {
  private base = 'http://localhost:8082';
  constructor(private http: HttpClient) {}

  register(usuario: any): Observable<any> {
    return this.http.post(`${this.base}/auth/Create`, usuario);
  }
}
