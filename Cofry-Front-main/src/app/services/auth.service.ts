import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  getUserId(): string {
    if (!isPlatformBrowser(this.platformId)) {
      return '';
    }

    const token = localStorage.getItem('token');
    if (!token) return '';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId ?? '';
    } catch {
      return '';
    }
  }
}
