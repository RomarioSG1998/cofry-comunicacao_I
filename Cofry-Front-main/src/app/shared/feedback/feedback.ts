import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedbackService } from '../../services/feedback.service';
import { Subject, takeUntil } from 'rxjs';

interface FeedbackMessage {
  id: number;
  type: 'success' | 'error' | 'info';
  message: string;
  title?: string;
  timestamp: Date;
}

@Component({
  selector: 'app-feedback',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feedback.html',
  styleUrls: ['./feedback.css']
})
export class FeedbackComponent implements OnInit, OnDestroy {
  private feedbackService = inject(FeedbackService);
  private destroy$ = new Subject<void>();
  
  messages: FeedbackMessage[] = [];
  private messageIdCounter = 0;

  ngOnInit() {
    // Escutar eventos do FeedbackService
    this.feedbackService.feedback$
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        this.addMessage(event.type, event.message, event.title);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addMessage(type: 'success' | 'error' | 'info', message: string, title?: string) {
    const feedbackMessage: FeedbackMessage = {
      id: this.messageIdCounter++,
      type,
      message,
      title,
      timestamp: new Date()
    };

    this.messages.push(feedbackMessage);

    // Remover mensagem automaticamente após 5 segundos
    setTimeout(() => {
      this.removeMessage(feedbackMessage.id);
    }, 5000);
  }

  removeMessage(id: number) {
    this.messages = this.messages.filter(msg => msg.id !== id);
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success':
        return '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>';
      case 'error':
        return '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';
      case 'info':
        return '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
      default:
        return '';
    }
  }
}

