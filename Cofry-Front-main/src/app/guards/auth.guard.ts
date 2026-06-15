import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  // SSR: permite passar (sem localStorage no servidor)
  if (typeof window === 'undefined') return true;

  const token = localStorage.getItem('token');
  if (token) return true;

  // Sem token → redireciona para a home pública (que exibe o login)
  inject(Router).navigate(['/']);
  return false;
};
