import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private base = 'http://localhost:8082';
  constructor(private http: HttpClient) {}

  login(usuario: any): Observable<any> {
    return this.http.post(`${this.base}/login`, usuario);
  }
}

