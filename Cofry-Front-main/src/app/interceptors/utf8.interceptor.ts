import { HttpInterceptorFn } from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';

/**
 * Interceptor para garantir que todas as requisições e respostas usem UTF-8
 * Resolve problemas de encoding como "ItaÃ°" em vez de "Itaú"
 */
export const utf8Interceptor: HttpInterceptorFn = (req, next) => {
  // Adiciona headers UTF-8 nas requisições
  const utf8Req = req.clone({
    setHeaders: {
      'Content-Type': 'application/json; charset=utf-8',
      'Accept': 'application/json; charset=utf-8',
      'Accept-Charset': 'utf-8'
    }
  });

  return next(utf8Req);
};


