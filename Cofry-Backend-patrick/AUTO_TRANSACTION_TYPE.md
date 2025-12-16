# Determinação Automática do Tipo de Transação

## 📋 Resumo

O tipo de transação (`transactionType`) agora é determinado automaticamente pelo backend, eliminando a necessidade do frontend enviar esse campo.

---

## 🎯 Como Funciona

O backend determina o tipo de transação automaticamente baseado nos seguintes critérios:

### 1. **Transferência (TRANSFER)**
Se a transação tem uma `destinationAccountId` (conta de destino), é automaticamente classificada como `TRANSFER`.

```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,  // ← Se tiver isso, é TRANSFER
  "amount": 100.00,
  "description": "Transferência entre contas"
}
```

### 2. **Depósito (DEPOSIT) - Entrada**
Se a transação **NÃO** tem `destinationAccountId` e o campo `isIncome` é `true`, é classificada como `DEPOSIT`.

```json
{
  "sourceAccountId": 1,
  "isIncome": true,  // ← Indica entrada de dinheiro
  "amount": 100.00,
  "description": "Salário recebido"
}
```

### 3. **Pagamento (PAYMENT) - Saída**
Se a transação **NÃO** tem `destinationAccountId` e:
- O campo `isIncome` é `false`, ou
- O campo `isIncome` não foi fornecido

É classificada como `PAYMENT` (saída de dinheiro).

```json
{
  "sourceAccountId": 1,
  "isIncome": false,  // ← Indica saída de dinheiro (ou omitir)
  "amount": 100.00,
  "description": "Pagamento de conta"
}
```

---

## 📝 Exemplos de Uso

### Exemplo 1: Transferência entre Contas

**Request:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 500.00,
  "description": "Transferência para conta poupança"
}
```

**Resultado:** `transactionType = "TRANSFER"` (determinado automaticamente)

---

### Exemplo 2: Entrada de Dinheiro (Depósito)

**Request:**
```json
{
  "sourceAccountId": 1,
  "isIncome": true,
  "amount": 2000.00,
  "description": "Salário",
  "categoryId": 1
}
```

**Resultado:** `transactionType = "DEPOSIT"` (determinado automaticamente)

---

### Exemplo 3: Saída de Dinheiro (Pagamento)

**Request (com isIncome explícito):**
```json
{
  "sourceAccountId": 1,
  "isIncome": false,
  "amount": 150.00,
  "description": "Supermercado",
  "categoryId": 2
}
```

**Request (sem isIncome - padrão é saída):**
```json
{
  "sourceAccountId": 1,
  "amount": 150.00,
  "description": "Supermercado",
  "categoryId": 2
}
```

**Resultado:** `transactionType = "PAYMENT"` (determinado automaticamente)

---

### Exemplo 4: Tipo Manual (Opcional)

Se você **quiser especificar o tipo manualmente**, ainda pode enviar o campo `transactionType`:

```json
{
  "sourceAccountId": 1,
  "transactionType": "WITHDRAWAL",  // ← Tipo manual
  "amount": 100.00,
  "description": "Saque"
}
```

**Resultado:** `transactionType = "WITHDRAWAL"` (usa o valor fornecido)

---

## 🔄 Prioridade de Determinação

A ordem de prioridade é:

1. **Se `transactionType` foi fornecido explicitamente** → Usa o valor fornecido
2. **Se tem `destinationAccountId`** → `TRANSFER`
3. **Se tem `isIncome = true`** → `DEPOSIT`
4. **Se tem `isIncome = false` ou não fornecido** → `PAYMENT` (padrão)

---

## 💻 Implementação no Frontend

### Angular/TypeScript

```typescript
interface TransactionRequest {
  sourceAccountId: number;
  destinationAccountId?: number;  // Opcional - se tiver, é TRANSFER
  isIncome?: boolean;              // Opcional - true = entrada, false/omitido = saída
  amount: number;
  description: string;
  categoryId?: number;
  transactionDate?: string;
  // transactionType não precisa mais ser enviado!
}

// Exemplo: Entrada de dinheiro
const incomeTransaction: TransactionRequest = {
  sourceAccountId: 1,
  isIncome: true,
  amount: 2000.00,
  description: "Salário"
};

// Exemplo: Saída de dinheiro
const expenseTransaction: TransactionRequest = {
  sourceAccountId: 1,
  isIncome: false,  // ou omitir
  amount: 150.00,
  description: "Supermercado"
};

// Exemplo: Transferência
const transferTransaction: TransactionRequest = {
  sourceAccountId: 1,
  destinationAccountId: 2,  // Automático: TRANSFER
  amount: 500.00,
  description: "Transferência"
};
```

### JavaScript/Fetch

```javascript
// Entrada de dinheiro
fetch('http://localhost:8080/api/transactions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    sourceAccountId: 1,
    isIncome: true,  // ← Entrada
    amount: 2000.00,
    description: "Salário"
  })
});

// Saída de dinheiro
fetch('http://localhost:8080/api/transactions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    sourceAccountId: 1,
    isIncome: false,  // ← Saída (ou omitir)
    amount: 150.00,
    description: "Supermercado"
  })
});
```

---

## 📊 Resumo dos Campos

| Campo | Obrigatório | Descrição |
|-------|-------------|-----------|
| `sourceAccountId` | ✅ Sim | ID da conta de origem |
| `amount` | ✅ Sim | Valor da transação |
| `description` | ✅ Sim | Descrição da transação |
| `destinationAccountId` | ❌ Não | Se fornecido, tipo = TRANSFER |
| `isIncome` | ❌ Não | true = DEPOSIT, false/omitido = PAYMENT |
| `transactionType` | ❌ Não | Se fornecido, usa esse valor (ignora lógica automática) |
| `categoryId` | ❌ Não | ID da categoria |
| `transactionDate` | ❌ Não | Data (padrão: hoje) |

---

## ⚠️ Importante

### Compatibilidade com Código Antigo

Se o frontend ainda enviar `transactionType`, o backend **respeitará** o valor fornecido e não aplicará a lógica automática. Isso garante compatibilidade com código existente.

### Recomendação

- ✅ **Use a determinação automática** para novos códigos (mais simples)
- ✅ **Envie `isIncome`** para distinguir entrada/saída
- ⚠️ **Evite enviar `transactionType` manualmente** a menos que seja necessário

---

## 🔍 Lógica Completa

```
┌─────────────────────────────────────┐
│  transactionType fornecido?         │
│         ↓                           │
│    SIM → Usa o valor fornecido      │
│    NÃO ↓                            │
│  destinationAccountId existe?       │
│         ↓                           │
│    SIM → TRANSFER                   │
│    NÃO ↓                            │
│  isIncome = true?                   │
│         ↓                           │
│    SIM → DEPOSIT                    │
│    NÃO → PAYMENT (padrão)           │
└─────────────────────────────────────┘
```

---

**Data de Implementação:** Janeiro 2025
**Status:** ✅ Implementado e Funcionando

