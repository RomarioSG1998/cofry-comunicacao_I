import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = environment.apiUrl;
  constructor(private http: HttpClient) {}

  fetch<T>(email: string): Observable<T> {
    return this.http.get<T>(`${this.base}/api/user-data?email=${encodeURIComponent(email)}`);
  }
}
