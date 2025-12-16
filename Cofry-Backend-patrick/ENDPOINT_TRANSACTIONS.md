# Endpoints de Transações - GET /api/transactions

## 📋 Resumo

Endpoints disponíveis para listar e buscar transações, com diversos filtros para facilitar a exibição na tela.

---

## 🔗 Endpoints Disponíveis

### **GET** `/api/transactions`

Lista transações com vários filtros disponíveis.

**Base URL:** `http://localhost:8080`

---

## 📝 Parâmetros de Filtro

### Query Parameters (Todos Opcionais)

- **`userId`** (integer): Filtra por usuário (retorna todas as transações onde o usuário está envolvido)
- **`accountId`** (integer): Filtra por conta de origem
- **`type`** (string): Filtra por tipo (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `PAYMENT`)
- **`categoryId`** (integer): Filtra por categoria
- **`startDate`** (date): Data inicial do período (formato: `YYYY-MM-DD`)
- **`endDate`** (date): Data final do período (formato: `YYYY-MM-DD`)

**Nota:** Se nenhum parâmetro for fornecido, retorna todas as transações.

**Prioridade dos filtros (aplicado na ordem):**
1. `userId` (se fornecido, usa este)
2. `accountId` (se fornecido)
3. `type` (se fornecido)
4. `categoryId` (se fornecido)
5. `startDate` + `endDate` (se ambos fornecidos)
6. Sem filtros → retorna todas as transações

---

## 📤 Resposta

### Sucesso (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "transactionId": 1,
      "sourceAccountId": 1,
      "destinationAccountId": null,
      "categoryId": 1,
      "amount": 150.00,
      "transactionType": "PAYMENT",
      "description": "Supermercado",
      "transactionDate": "2025-01-15",
      "isRecurring": false,
      "installmentCurrent": null,
      "installmentTotal": null,
      "createdAt": "2025-01-15T10:30:00"
    },
    {
      "transactionId": 2,
      "sourceAccountId": 1,
      "destinationAccountId": 2,
      "categoryId": null,
      "amount": 500.00,
      "transactionType": "TRANSFER",
      "description": "Transferência para poupança",
      "transactionDate": "2025-01-14",
      "isRecurring": false,
      "installmentCurrent": null,
      "installmentTotal": null,
      "createdAt": "2025-01-14T14:20:00"
    }
  ]
}
```

### Estrutura da Transação

```typescript
interface Transaction {
  transactionId: number;          // ID da transação
  sourceAccountId: number;        // ID da conta de origem
  destinationAccountId: number | null;  // ID da conta de destino (null se não for transferência)
  categoryId: number | null;      // ID da categoria (opcional)
  amount: number;                 // Valor da transação
  transactionType: string;        // "DEPOSIT", "WITHDRAWAL", "TRANSFER", "PAYMENT"
  description: string;            // Descrição da transação
  transactionDate: string;        // Data da transação (YYYY-MM-DD)
  isRecurring: boolean;           // Se é recorrente
  installmentCurrent: number | null;  // Parcela atual (se parcelado)
  installmentTotal: number | null;    // Total de parcelas (se parcelado)
  createdAt: string;              // Data/hora de criação (ISO 8601)
}
```

---

## 📍 Outros Endpoints

### **GET** `/api/transactions/{id}`
Busca uma transação específica por ID.

**Exemplo:**
```bash
GET http://localhost:8080/api/transactions/1
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "transactionId": 1,
    "sourceAccountId": 1,
    "destinationAccountId": null,
    "categoryId": 1,
    "amount": 150.00,
    "transactionType": "PAYMENT",
    "description": "Supermercado",
    "transactionDate": "2025-01-15",
    "isRecurring": false,
    "installmentCurrent": null,
    "installmentTotal": null,
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

---

## 💻 Exemplos de Uso

### 1. Listar Transações de um Usuário

**Use quando:** Quer mostrar todas as transações do usuário logado (incluindo transferências de entrada e saída).

```bash
GET http://localhost:8080/api/transactions?userId=1
```

**JavaScript/Fetch:**
```javascript
fetch('http://localhost:8080/api/transactions?userId=1')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const transactions = data.data;
      // Exibir transações na tela
      console.log('Transações do usuário:', transactions);
    }
  });
```

**Angular/TypeScript:**
```typescript
getUserTransactions(userId: number): Observable<Transaction[]> {
  return this.http.get<{success: boolean, data: Transaction[]}>(
    `${this.apiUrl}/transactions?userId=${userId}`
  ).pipe(
    map(response => response.data)
  );
}
```

---

### 2. Listar Transações de uma Conta Específica

**Use quando:** Quer mostrar transações de uma conta específica.

```bash
GET http://localhost:8080/api/transactions?accountId=1
```

**JavaScript:**
```javascript
fetch('http://localhost:8080/api/transactions?accountId=1')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const accountTransactions = data.data;
      console.log('Transações da conta:', accountTransactions);
    }
  });
```

---

### 3. Filtrar por Tipo de Transação

**Use quando:** Quer mostrar apenas depósitos, pagamentos, etc.

```bash
# Apenas pagamentos
GET http://localhost:8080/api/transactions?type=PAYMENT

# Apenas depósitos
GET http://localhost:8080/api/transactions?type=DEPOSIT

# Apenas transferências
GET http://localhost:8080/api/transactions?type=TRANSFER
```

**Tipos disponíveis:**
- `DEPOSIT` - Depósitos (entrada)
- `WITHDRAWAL` - Saques
- `PAYMENT` - Pagamentos (saída)
- `TRANSFER` - Transferências

---

### 4. Filtrar por Categoria

**Use quando:** Quer mostrar transações de uma categoria específica.

```bash
GET http://localhost:8080/api/transactions?categoryId=1
```

---

### 5. Filtrar por Período (Data)

**Use quando:** Quer mostrar transações de um período específico.

```bash
GET http://localhost:8080/api/transactions?startDate=2025-01-01&endDate=2025-01-31
```

**JavaScript:**
```javascript
const startDate = '2025-01-01';
const endDate = '2025-01-31';
const url = `http://localhost:8080/api/transactions?startDate=${startDate}&endDate=${endDate}`;

fetch(url)
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const monthlyTransactions = data.data;
      console.log('Transações do mês:', monthlyTransactions);
    }
  });
```

---

### 6. Combinar Filtros

**Use quando:** Quer combinar múltiplos filtros (mas atenção: apenas o primeiro filtro encontrado é aplicado conforme prioridade).

**Exemplo (filtro por usuário + tipo):**
```bash
# ❌ NÃO FUNCIONA - apenas userId será aplicado
GET http://localhost:8080/api/transactions?userId=1&type=PAYMENT

# ✅ FUNCIONA - Para filtrar por usuário e tipo, faça no frontend
GET http://localhost:8080/api/transactions?userId=1
# Depois filtre por tipo no JavaScript:
transactions.filter(t => t.transactionType === 'PAYMENT')
```

---

### 7. Listar Todas as Transações

**Use quando:** Precisa de todas as transações (cuidado: pode ser muito grande).

```bash
GET http://localhost:8080/api/transactions
```

---

## 🎨 Exemplo Completo para Frontend

### Angular/TypeScript Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Listar transações de um usuário
  getUserTransactions(userId: number): Observable<Transaction[]> {
    return this.http.get<{success: boolean, data: Transaction[]}>(
      `${this.apiUrl}/transactions`,
      { params: { userId: userId.toString() } }
    ).pipe(
      map(response => response.data)
    );
  }

  // Listar transações de uma conta
  getAccountTransactions(accountId: number): Observable<Transaction[]> {
    return this.http.get<{success: boolean, data: Transaction[]}>(
      `${this.apiUrl}/transactions`,
      { params: { accountId: accountId.toString() } }
    ).pipe(
      map(response => response.data)
    );
  }

  // Filtrar por tipo
  getTransactionsByType(type: string): Observable<Transaction[]> {
    return this.http.get<{success: boolean, data: Transaction[]}>(
      `${this.apiUrl}/transactions`,
      { params: { type: type } }
    ).pipe(
      map(response => response.data)
    );
  }

  // Filtrar por período
  getTransactionsByDateRange(startDate: string, endDate: string): Observable<Transaction[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<{success: boolean, data: Transaction[]}>(
      `${this.apiUrl}/transactions`,
      { params }
    ).pipe(
      map(response => response.data)
    );
  }

  // Buscar transação por ID
  getTransactionById(id: number): Observable<Transaction> {
    return this.http.get<{success: boolean, data: Transaction}>(
      `${this.apiUrl}/transactions/${id}`
    ).pipe(
      map(response => response.data)
    );
  }
}
```

### React/TypeScript Hook

```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

interface Transaction {
  transactionId: number;
  sourceAccountId: number;
  destinationAccountId: number | null;
  categoryId: number | null;
  amount: number;
  transactionType: string;
  description: string;
  transactionDate: string;
  isRecurring: boolean;
  installmentCurrent: number | null;
  installmentTotal: number | null;
  createdAt: string;
}

export const useTransactions = (userId?: number) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        setLoading(true);
        const url = userId 
          ? `http://localhost:8080/api/transactions?userId=${userId}`
          : 'http://localhost:8080/api/transactions';
        
        const response = await axios.get<{success: boolean, data: Transaction[]}>(url);
        
        if (response.data.success) {
          setTransactions(response.data.data);
          setError(null);
        }
      } catch (err: any) {
        setError(err.response?.data?.error || 'Erro ao carregar transações');
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [userId]);

  return { transactions, loading, error };
};
```

---

## 📊 Ordenação

As transações são retornadas ordenadas por:
- **Por usuário:** `transaction_date DESC, created_at DESC`
- **Por conta:** `transaction_date DESC`
- **Por categoria:** `transaction_date DESC`
- **Por tipo:** `transaction_date DESC`
- **Por período:** `transaction_date DESC`
- **Todas:** `transaction_id` (crescente)

---

## ⚠️ Limitações e Recomendações

### 1. **Filtros Não Combinados**

Os filtros têm prioridade e apenas o primeiro encontrado é aplicado. Se precisar combinar filtros, faça a filtragem no frontend ou crie um endpoint específico.

**Exemplo de filtragem no frontend:**
```typescript
// Buscar todas as transações do usuário
const allTransactions = await getUserTransactions(userId);

// Filtrar por tipo no frontend
const payments = allTransactions.filter(t => t.transactionType === 'PAYMENT');

// Filtrar por período no frontend
const thisMonth = payments.filter(t => {
  const date = new Date(t.transactionDate);
  return date.getMonth() === new Date().getMonth();
});
```

### 2. **Performance**

Se houver muitas transações, considere:
- Implementar paginação
- Limitar por período
- Filtrar sempre por `userId` ou `accountId`

### 3. **Recomendação de Uso**

**Para a tela principal de transações:**
```
GET /api/transactions?userId={userId}
```

**Para uma conta específica:**
```
GET /api/transactions?accountId={accountId}
```

**Para filtros específicos:**
```
GET /api/transactions?userId={userId}&type=PAYMENT  // Depois filtra no frontend
```

---

## ✅ Checklist de Uso

- [x] Endpoint `GET /api/transactions` implementado
- [x] Suporte a filtro por `userId`
- [x] Suporte a filtro por `accountId`
- [x] Suporte a filtro por `type`
- [x] Suporte a filtro por `categoryId`
- [x] Suporte a filtro por período (`startDate` + `endDate`)
- [x] Endpoint `GET /api/transactions/{id}` implementado
- [ ] Considerar implementar paginação para grandes volumes
- [ ] Considerar permitir combinar múltiplos filtros

---

**Data:** Janeiro 2025
**Status:** ✅ Implementado e Funcionando

