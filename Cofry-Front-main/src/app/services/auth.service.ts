import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userNameSubject = new BehaviorSubject<string>('');
  public userName$: Observable<string> = this.userNameSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    // Carrega o nome do usuário do localStorage quando o serviço é inicializado
    if (isPlatformBrowser(this.platformId)) {
      const userName = localStorage.getItem('userName') || '';
      if (userName) {
        this.userNameSubject.next(userName);
        console.log('AuthService - Nome do usuário carregado:', userName);
      }
    }
  }

  getUserId(): string {
    if (!isPlatformBrowser(this.platformId)) {
      return '';
    }

    // Primeiro tenta pegar do localStorage (salvo durante login)
    const userId = localStorage.getItem('userId');
    if (userId) {
      return userId;
    }

    // Fallback: tenta pegar de token JWT se existir
    const token = localStorage.getItem('token');
    if (!token) return '';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId ?? '';
    } catch {
      return '';
    }
  }

  getUserName(): string {
    if (!isPlatformBrowser(this.platformId)) {
      return '';
    }
    return localStorage.getItem('userName') || '';
  }

  setUserData(userData: { firstName?: string; email?: string; userId?: number | string; token?: string }): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    
    console.log('AuthService - setUserData chamado com:', userData);
    
    if (userData.firstName) {
      localStorage.setItem('userName', userData.firstName);
      this.userNameSubject.next(userData.firstName);
      console.log('AuthService - Nome do usuário salvo e notificado:', userData.firstName);
    }
    
    if (userData.email) {
      localStorage.setItem('userEmail', userData.email);
    }
    
    if (userData.userId) {
      localStorage.setItem('userId', userData.userId.toString());
    }

    if (userData.token) {
      localStorage.setItem('token', userData.token);
    }
  }

  clearUserData(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    localStorage.removeItem('userName');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userId');
    localStorage.removeItem('token');
    this.userNameSubject.next('');
  }
}
