import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface FeedbackEvent {
  type: 'success' | 'error' | 'info';
  message: string;
  title?: string;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private feedbackSubject = new Subject<FeedbackEvent>();
  public feedback$ = this.feedbackSubject.asObservable();
  
  showSuccess(message: string, title?: string): void {
    this.feedbackSubject.next({ type: 'success', message, title });
    // Mantém alert para compatibilidade
    console.log(title ? `${title}: ${message}` : message);
  }

  showError(message: string): void {
    this.feedbackSubject.next({ type: 'error', message });
    // Mantém alert para compatibilidade
    console.error(`Erro: ${message}`);
  }

  showInfo(message: string): void {
    this.feedbackSubject.next({ type: 'info', message });
    // Mantém alert para compatibilidade
    console.log(message);
  }
}

