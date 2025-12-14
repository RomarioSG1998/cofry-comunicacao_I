export interface Transaction {
  id: number;
  descricao: string;
  tipo: 'PIX' | 'DEBITO' | 'CREDITO';
  categoria: 'MERCADO' | 'STREAMING' | 'TRANSPORTE' | 'OUTROS';
  forma_pagamento: string;
  valor: number;
  data_hora: string;
}
