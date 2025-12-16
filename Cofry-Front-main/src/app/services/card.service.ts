import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export interface Card {
  cardId: number;
  userId: number;
  accountId?: number | null;
  cardNumber: string; // Mascarado: "**** **** **** 0366"
  cardHolderName: string;
  expiryDate: string; // Formato: "2025-12-31" ou "12/25"
  cardType: 'CREDIT' | 'DEBIT' | 'PREPAID';
  brand?: string; // Visa, Mastercard, Elo, etc.
  status: 'ACTIVE' | 'BLOCKED' | 'EXPIRED';
  limitAmount?: number;
  currentBalance?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCardRequest {
  userId: number;
  accountId?: number | null;
  cardNumber: string; // Número completo ou últimos 4 dígitos
  cardHolderName: string;
  expiryDate: string; // Formato: "12/25" (MM/YY)
  cvv?: string;
  cardType: 'CREDIT' | 'DEBIT' | 'PREPAID';
  brand?: string;
  limitAmount?: string; // Opcional: Limite para cartão de crédito
}

export interface UpdateCardRequest {
  cardHolderName?: string;
  expiryDate?: string; // Formato: "06/26" (MM/YY)
  status?: 'ACTIVE' | 'BLOCKED' | 'EXPIRED';
  limitAmount?: string;
}

export interface CardType {
  name: string;
  value: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class CardService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Lista tipos de cartão disponíveis
   */
  getCardTypes(): Observable<CardType[]> {
    return this.http.get<ApiResponse<CardType[]>>(`${this.base}/api/form/card/types`).pipe(
      map(response => response.data || []),
      catchError(() => of([]))
    );
  }

  /**
   * Lista cartões de um usuário
   */
  getCardsByUserId(userId: number): Observable<Card[]> {
    console.log('CardService: Buscando cartões para userId:', userId);
    return this.http.get<ApiResponse<Card[]>>(`${this.base}/api/form/card/user/${userId}`).pipe(
      map(response => {
        console.log('CardService: Resposta da API:', response);
        if (response && response.success && response.data) {
          return response.data;
        }
        // Se a resposta não tem a estrutura esperada, tenta usar diretamente
        if (Array.isArray(response)) {
          return response;
        }
        return [];
      }),
      catchError((error) => {
        console.error('CardService: Erro ao buscar cartões:', error);
        console.error('CardService: Detalhes do erro:', error.error);
        return of([]);
      })
    );
  }

  /**
   * Busca um cartão por ID
   */
  getCardById(cardId: number): Observable<Card | null> {
    return this.http.get<ApiResponse<Card>>(`${this.base}/api/form/card/${cardId}`).pipe(
      map(response => response.data || null),
      catchError(() => of(null))
    );
  }

  /**
   * Cria um novo cartão
   */
  createCard(card: CreateCardRequest): Observable<Card | null> {
    console.log('CardService: Criando cartão com dados:', card);
    return this.http.post<ApiResponse<Card>>(`${this.base}/api/form/card`, card).pipe(
      map(response => {
        console.log('CardService: Resposta da criação:', response);
        if (response && response.success && response.data) {
          return response.data;
        }
        // Se a resposta não tem a estrutura esperada, tenta usar diretamente
        if (response && (response as any).cardId) {
          return response as unknown as Card;
        }
        return null;
      }),
      catchError((error) => {
        console.error('CardService: Erro ao criar cartão:', error);
        console.error('CardService: Status:', error.status);
        console.error('CardService: Erro detalhado:', error.error);
        // Re-lança o erro para que o componente possa tratá-lo
        throw error;
      })
    );
  }

  /**
   * Atualiza um cartão
   */
  updateCard(cardId: number, cardData: UpdateCardRequest): Observable<Card | null> {
    return this.http.put<ApiResponse<Card>>(`${this.base}/api/form/card/${cardId}`, cardData).pipe(
      map(response => response.data || null),
      catchError(() => of(null))
    );
  }

  /**
   * Deleta um cartão
   */
  deleteCard(cardId: number): Observable<boolean> {
    return this.http.delete<ApiResponse<string>>(`${this.base}/api/form/card/${cardId}`).pipe(
      map(response => response.success || false),
      catchError(() => of(false))
    );
  }
}

