import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface InvestmentTransaction {
  id: number;
  userId: number;
  assetId: number;
  assetTicker?: string;
  assetName?: string;
  type: 'Compra' | 'Venda';
  price: number;
  quantity: number;
  totalValue: number;
  transactionDate: string;
  status: 'COMPLETED' | 'PENDING';
}

export interface CreateInvestmentTransactionRequest {
  userId: number;
  assetId: number;
  type: 'Compra' | 'Venda';
  price: string; // String format
  quantity: string; // String format
  status?: 'COMPLETED' | 'PENDING';
}

export interface AssetDistribution {
  assetId: number;
  ticker: string;
  assetName: string;
  categoryId: number;
  categoryName: string;
  quantity: number;
  averagePrice: number;
  totalValue: number;
  percentage: number;
}

export interface CategoryDistribution {
  categoryId: number;
  categoryName: string;
  totalValue: number;
  percentage: number;
}

export interface PortfolioSummary {
  userId: number;
  totalPortfolioValue: number;
  totalAssets: number;
  distribution: AssetDistribution[];
  distributionByCategory: CategoryDistribution[];
}

@Injectable({ providedIn: 'root' })
export class InvestmentService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Cria uma transação de investimento
   */
  createTransaction(transaction: CreateInvestmentTransactionRequest): Observable<InvestmentTransaction> {
    return this.http.post<InvestmentTransaction>(
      `${this.base}/api/investments/transaction`,
      transaction
    );
  }

  /**
   * Busca histórico de transações de investimento de um usuário
   */
  getHistoryByUserId(userId: number): Observable<InvestmentTransaction[]> {
    return this.http.get<InvestmentTransaction[]>(
      `${this.base}/api/investments/history/user/${userId}`
    );
  }

  /**
   * Busca distribuição detalhada de ativos do usuário
   */
  getDistributionByUserId(userId: number): Observable<AssetDistribution[]> {
    return this.http.get<AssetDistribution[]>(
      `${this.base}/api/investments/distribution/user/${userId}`
    );
  }

  /**
   * Busca distribuição por categoria
   */
  getDistributionByCategory(userId: number): Observable<CategoryDistribution[]> {
    return this.http.get<CategoryDistribution[]>(
      `${this.base}/api/investments/distribution/user/${userId}/category`
    );
  }

  /**
   * Busca resumo completo do portfólio
   */
  getPortfolioSummary(userId: number): Observable<PortfolioSummary> {
    return this.http.get<PortfolioSummary>(
      `${this.base}/api/investments/portfolio/user/${userId}`
    );
  }
}

