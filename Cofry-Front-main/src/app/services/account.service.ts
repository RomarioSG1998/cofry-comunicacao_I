import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface Account {
  accountId: number;
  userId: number;
  bankCode: string; // NOVO - código do banco
  bankName: string; // NOVO - nome do banco
  agency: string; // Mudou de agencyNumber para agency
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS';
  balance: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt?: string;
}

export interface CreateAccountRequest {
  userId: number;
  bankCode: string; // NOVO
  bankName: string; // NOVO
  agency: string; // Mudou de agencyNumber
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS';
  balance?: number;
  status?: 'ACTIVE' | 'INACTIVE';
}

export interface CreateAccountFormRequest {
  userId: number;
  bankCode: string; // NOVO
  bankName: string; // NOVO
  agency: string; // Mudou de agencyNumber
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS';
  balance?: number;
}

export interface UpdateBalanceRequest {
  balance: number;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Lista todas as contas bancárias
   * Aplica correção de encoding UTF-8 se necessário
   */
  getAllAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/api/accounts`).pipe(
      map((accounts) => 
        accounts.map(account => ({
          ...account,
          bankName: this.fixUtf8Encoding(account.bankName || '')
        }))
      )
    );
  }

  /**
   * Busca uma conta por ID
   * Aplica correção de encoding UTF-8 se necessário
   */
  getAccountById(accountId: number): Observable<Account> {
    return this.http.get<Account>(`${this.base}/api/accounts/${accountId}`).pipe(
      map((account) => ({
        ...account,
        bankName: this.fixUtf8Encoding(account.bankName || '')
      }))
    );
  }

  /**
   * Busca contas de um usuário específico
   * Aplica correção de encoding UTF-8 se necessário
   */
  getAccountsByUserId(userId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/api/accounts?userId=${userId}`).pipe(
      map((accounts) => 
        accounts.map(account => ({
          ...account,
          bankName: this.fixUtf8Encoding(account.bankName || '')
        }))
      )
    );
  }

  /**
   * Busca a conta ativa do usuário (primeira conta com status ACTIVE, ou primeira conta se não houver ativa)
   * Este método garante que todas as páginas usem a mesma conta para exibir o saldo
   */
  getActiveAccountByUserId(userId: number): Observable<Account | null> {
    return this.getAccountsByUserId(userId).pipe(
      map((accounts) => {
        if (!accounts || accounts.length === 0) {
          return null;
        }
        // Retorna a primeira conta ativa, ou a primeira conta se não houver ativa
        const activeAccount = accounts.find(acc => acc.status === 'ACTIVE') || accounts[0];
        return activeAccount;
      })
    );
  }

  /**
   * Corrige problemas de encoding UTF-8
   * Converte caracteres mal codificados como "ItaÃ°" para "Itaú"
   * 
   * Este problema geralmente ocorre quando:
   * - Backend envia dados em ISO-8859-1 mas sem header correto
   * - Frontend interpreta como UTF-8 causando caracteres incorretos
   */
  fixUtf8Encoding(text: string): string {
    if (!text) return text;
    
    // Se não há caracteres problemáticos, retorna como está
    if (!text.includes('Ã')) {
      return text;
    }
    
    try {
      // Método 1: Tenta decodificar usando escape/unescape
      // Funciona para casos como "ItaÃ°" -> "Itaú"
      const fixed = decodeURIComponent(escape(text));
      
      // Verifica se a correção funcionou (não deve ter mais "Ã")
      if (!fixed.includes('Ã')) {
        return fixed;
      }
      
      // Método 2: Tenta converter de ISO-8859-1 para UTF-8
      // Cria um TextDecoder para ISO-8859-1
      const bytes = new Uint8Array(text.length);
      for (let i = 0; i < text.length; i++) {
        bytes[i] = text.charCodeAt(i);
      }
      const decoder = new TextDecoder('iso-8859-1');
      const decoded = decoder.decode(bytes);
      
      return decoded;
    } catch (e) {
      // Se todos os métodos falharem, retorna o texto original
      console.warn('Erro ao corrigir encoding UTF-8:', e);
      return text;
    }
  }

  /**
   * Cria uma nova conta bancária
   * Aplica correção de encoding UTF-8 se necessário
   */
  createAccount(account: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.base}/api/accounts`, account).pipe(
      map((createdAccount) => ({
        ...createdAccount,
        bankName: this.fixUtf8Encoding(createdAccount.bankName || '')
      }))
    );
  }

  /**
   * Cria uma conta a partir de formulário
   * Aplica correção de encoding UTF-8 se necessário
   */
  createAccountFromForm(account: CreateAccountFormRequest): Observable<Account> {
    return this.http.post<Account>(`${this.base}/api/form/account`, account).pipe(
      map((createdAccount) => ({
        ...createdAccount,
        bankName: this.fixUtf8Encoding(createdAccount.bankName || '')
      }))
    );
  }

  /**
   * Atualiza o saldo de uma conta
   */
  updateBalance(accountId: number, balance: UpdateBalanceRequest): Observable<{ accountId: number; balance: number }> {
    return this.http.put<{ accountId: number; balance: number }>(
      `${this.base}/api/accounts/${accountId}/balance`,
      balance
    );
  }

  /**
   * Atualiza os dados de uma conta
   * Aplica correção de encoding UTF-8 se necessário
   */
  updateAccount(accountId: number, accountData: Partial<Account>): Observable<Account> {
    return this.http.put<Account>(`${this.base}/api/accounts/${accountId}`, accountData).pipe(
      map((updatedAccount) => ({
        ...updatedAccount,
        bankName: this.fixUtf8Encoding(updatedAccount.bankName || '')
      }))
    );
  }

  /**
   * Deleta uma conta
   * Trata erro 400 quando a conta possui transações vinculadas
   */
  deleteAccount(accountId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/accounts/${accountId}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 400) {
          const errorMessage = error.error?.error || 
            'Não é possível remover a conta. Existem transações vinculadas a esta conta.';
          return throwError(() => ({ 
            status: 400, 
            message: errorMessage,
            hasTransactions: true 
          }));
        }
        return throwError(() => ({ 
          status: error.status, 
          message: error.error?.error || 'Erro ao remover conta' 
        }));
      })
    );
  }

  /**
   * Desativa uma conta (em vez de deletar)
   */
  deactivateAccount(accountId: number): Observable<Account> {
    return this.http.put<Account>(
      `${this.base}/api/accounts/${accountId}`,
      { status: 'INACTIVE' }
    ).pipe(
      map((account) => ({
        ...account,
        bankName: this.fixUtf8Encoding(account.bankName || '')
      }))
    );
  }

  /**
   * Lista planos de conta disponíveis
   */
  getAccountPlans(): Observable<Array<{ name: string; value: string; price: number }>> {
    return this.http.get<Array<{ name: string; value: string; price: number }>>(
      `${this.base}/api/form/account/plans`
    );
  }
}

