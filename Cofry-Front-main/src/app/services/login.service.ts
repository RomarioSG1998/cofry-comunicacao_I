import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private base = environment.apiUrl;
  constructor(private http: HttpClient) {}

  login(usuario: any): Observable<any> {
    return this.http.post(`${this.base}/login`, usuario);
  }
}
