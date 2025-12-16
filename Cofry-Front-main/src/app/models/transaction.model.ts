// Interface da API (resposta do backend)
export interface TransactionResponse {
  transactionId: number;
  sourceAccountId: number;
  destinationAccountId: number | null;
  categoryId: number | null;
  amount: number;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT';
  description: string;
  transactionDate: string; // YYYY-MM-DD
  isRecurring: boolean;
  installmentCurrent: number | null;
  installmentTotal: number | null;
  createdAt: string; // ISO 8601
}

// Interface de resposta da API
export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

// Interface para uso no frontend (compatibilidade)
export interface Transaction {
  id: number;
  descricao: string;
  tipo: 'PIX' | 'DEBITO' | 'CREDITO';
  categoria: 'MERCADO' | 'STREAMING' | 'TRANSPORTE' | 'OUTROS';
  forma_pagamento: string;
  valor: number;
  data_hora: string;
  // Novos campos da API
  transactionId?: number;
  transactionType?: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT';
  transactionDate?: string; // YYYY-MM-DD
  categoryId?: number | null;
  isRecurring?: boolean;
  installmentCurrent?: number | null;
  installmentTotal?: number | null;
}
