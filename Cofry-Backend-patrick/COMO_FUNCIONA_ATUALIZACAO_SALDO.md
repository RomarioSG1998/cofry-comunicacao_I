# 💰 Como Funciona a Atualização Automática de Saldo

## ✅ Resumo

O saldo das contas é **atualizado automaticamente** quando você cria, atualiza ou deleta transações. Tudo acontece em uma única transação do banco de dados, garantindo que não haja inconsistências.

---

## 🔄 Fluxo de Criação de Transação

```
1. Frontend envia POST /api/transactions
   ↓
2. TransactionServlet.doPost()
   - Parse do JSON (amount, date, etc.)
   - Determina transactionType automaticamente (se não fornecido)
   ↓
3. TransactionService.createTransaction()
   - Valida transação
   - Verifica se contas existem
   - Valida saldo suficiente (para saídas)
   ↓
4. saveTransactionAndUpdateBalances()  ← AQUI ACONTECE A MÁGICA
   ┌─────────────────────────────────────────┐
   │ INICIA TRANSAÇÃO DO BANCO               │
   │ ├─ Salva transação no banco            │
   │ └─ Atualiza saldo(s) das contas        │
   │ COMMIT (ou ROLLBACK se erro)            │
   └─────────────────────────────────────────┘
   ↓
5. Retorna transação criada
```

---

## 💵 Lógica de Atualização de Saldo

### Método: `updateAccountBalancesInConnection()`

Localização: `src/main/java/org/example/service/TransactionService.java` (linha ~200)

```java
private void updateAccountBalancesInConnection(Connection conn, Transaction transaction) {
    BigDecimal amount = transaction.getAmount();
    TransactionTypeEnum type = transaction.getTransactionType();
    
    String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ? AND status = 'ACTIVE'";
    
    switch (type) {
        case DEPOSIT:
            // ✅ Adiciona valor ao saldo
            // SQL: UPDATE accounts SET balance = balance + valor WHERE account_id = X
            break;
            
        case WITHDRAWAL:
        case PAYMENT:
            // ❌ Subtrai valor do saldo
            // SQL: UPDATE accounts SET balance = balance + (-valor) WHERE account_id = X
            break;
            
        case TRANSFER:
            // ❌ Subtrai da origem
            // ✅ Adiciona ao destino
            // SQL executado 2 vezes (uma para cada conta)
            break;
    }
}
```

---

## 📊 Exemplos Práticos

### Exemplo 1: Depósito (Entrada)

**Situação Inicial:**
- Saldo da conta: R$ 1.000,00

**Transação Criada:**
```json
{
  "sourceAccountId": 1,
  "amount": 500.00,
  "transactionType": "DEPOSIT",
  "description": "Depósito recebido"
}
```

**O que acontece:**
```sql
-- 1. Salva a transação
INSERT INTO transactions (...) VALUES (...);

-- 2. Atualiza o saldo
UPDATE accounts 
SET balance = balance + 500.00 
WHERE account_id = 1 AND status = 'ACTIVE';
```

**Resultado Final:**
- Saldo da conta: R$ 1.500,00 ✅

---

### Exemplo 2: Pagamento (Saída)

**Situação Inicial:**
- Saldo da conta: R$ 1.000,00

**Transação Criada:**
```json
{
  "sourceAccountId": 1,
  "amount": 300.00,
  "transactionType": "PAYMENT",
  "description": "Pagamento de conta"
}
```

**Validação Antes:**
- ✅ Verifica se saldo (R$ 1.000,00) >= valor (R$ 300,00)
- ✅ Saldo suficiente, continua

**O que acontece:**
```sql
-- 1. Salva a transação
INSERT INTO transactions (...) VALUES (...);

-- 2. Atualiza o saldo (subtrai)
UPDATE accounts 
SET balance = balance + (-300.00) 
WHERE account_id = 1 AND status = 'ACTIVE';
```

**Resultado Final:**
- Saldo da conta: R$ 700,00 ✅

---

### Exemplo 3: Transferência

**Situação Inicial:**
- Conta Origem: R$ 1.000,00
- Conta Destino: R$ 500,00

**Transação Criada:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 400.00,
  "transactionType": "TRANSFER",
  "description": "Transferência para conta 2"
}
```

**Validação Antes:**
- ✅ Verifica se conta origem tem saldo suficiente (R$ 1.000,00 >= R$ 400,00)
- ✅ Saldo suficiente, continua

**O que acontece:**
```sql
-- 1. Salva a transação
INSERT INTO transactions (...) VALUES (...);

-- 2. Subtrai da conta origem
UPDATE accounts 
SET balance = balance + (-400.00) 
WHERE account_id = 1 AND status = 'ACTIVE';

-- 3. Adiciona à conta destino
UPDATE accounts 
SET balance = balance + 400.00 
WHERE account_id = 2 AND status = 'ACTIVE';
```

**Resultado Final:**
- Conta Origem: R$ 600,00 ✅
- Conta Destino: R$ 900,00 ✅

---

## 🔒 Atomicidade (Tudo ou Nada)

Todas as operações (salvar transação + atualizar saldos) acontecem em **uma única transação do banco**:

```java
return JdbcUtil.executeInTransaction(conn -> {
    // 1. Salva transação
    Transaction saved = saveTransactionInConnection(conn, transaction);
    
    // 2. Atualiza saldos
    updateAccountBalancesInConnection(conn, saved);
    
    return saved;
    // Se qualquer erro ocorrer aqui, tudo é revertido (ROLLBACK)
});
```

**Garantias:**
- ✅ Se tudo der certo: transação salva + saldos atualizados
- ❌ Se der erro: nada é salvo (rollback automático)
- ✅ Não há risco de salvar transação sem atualizar saldo
- ✅ Não há risco de atualizar saldo sem salvar transação

---

## ✅ Validações Implementadas

### 1. **Validação de Saldo Suficiente**

Antes de criar transações de saída, o sistema verifica se há saldo:

```java
private void validateSufficientBalance(Transaction transaction) {
    if (type == WITHDRAWAL || type == PAYMENT || type == TRANSFER) {
        Account sourceAccount = accountDAO.findById(sourceAccountId);
        
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                "Saldo insuficiente. Saldo atual: R$ " + currentBalance + 
                ", Valor necessário: R$ " + amount
            );
        }
    }
}
```

**Tipos que validam saldo:**
- ❌ `WITHDRAWAL` (Saque)
- ❌ `PAYMENT` (Pagamento)
- ❌ `TRANSFER` (Transferência)

**Tipos que não validam saldo:**
- ✅ `DEPOSIT` (Depósito - sempre pode adicionar dinheiro)

---

## 📝 SQL Executado

### Para DEPOSIT:
```sql
UPDATE accounts 
SET balance = balance + 500.00 
WHERE account_id = 1 AND status = 'ACTIVE';
```

### Para PAYMENT/WITHDRAWAL:
```sql
UPDATE accounts 
SET balance = balance + (-300.00)  -- Usa valor negativo
WHERE account_id = 1 AND status = 'ACTIVE';
```

### Para TRANSFER:
```sql
-- Primeiro: subtrai da origem
UPDATE accounts 
SET balance = balance + (-400.00) 
WHERE account_id = 1 AND status = 'ACTIVE';

-- Segundo: adiciona ao destino
UPDATE accounts 
SET balance = balance + 400.00 
WHERE account_id = 2 AND status = 'ACTIVE';
```

---

## 🔍 Onde Está Implementado

### Classe Principal: `TransactionService`

**Arquivo:** `src/main/java/org/example/service/TransactionService.java`

**Métodos importantes:**

1. **`createTransaction()`** (linha ~37)
   - Método principal chamado pelo controller
   - Valida e cria a transação
   - Chama `saveTransactionAndUpdateBalances()`

2. **`saveTransactionAndUpdateBalances()`** (linha ~113)
   - Gerencia a transação do banco
   - Garante atomicidade
   - Chama `saveTransactionInConnection()` e `updateAccountBalancesInConnection()`

3. **`updateAccountBalancesInConnection()`** (linha ~200)
   - **AQUI É ONDE O SALDO É ATUALIZADO**
   - Lógica por tipo de transação
   - Executa UPDATE no banco

4. **`validateSufficientBalance()`** (linha ~82)
   - Valida saldo antes de criar transação de saída
   - Lança exceção se saldo insuficiente

---

## 🎯 Resumo Visual

```
┌─────────────────────────────────────────────────────────┐
│ POST /api/transactions                                  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────┐
│ TransactionService.createTransaction()                  │
│ ├─ Valida transação                                     │
│ ├─ Verifica contas existem                              │
│ └─ Valida saldo suficiente (se saída)                   │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────┐
│ saveTransactionAndUpdateBalances()                      │
│ ┌────────────────────────────────────────────┐          │
│ │ INICIA TRANSAÇÃO DO BANCO                  │          │
│ │                                            │          │
│ │ 1. Salva transação:                        │          │
│ │    INSERT INTO transactions (...)          │          │
│ │                                            │          │
│ │ 2. Atualiza saldo(s):                      │          │
│ │    UPDATE accounts SET balance = ...       │          │
│ │                                            │          │
│ │ COMMIT (ou ROLLBACK se erro)               │          │
│ └────────────────────────────────────────────┘          │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────┐
│ Retorna transação criada com sucesso                    │
└─────────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuração no Banco de Dados

A tabela `accounts` tem o campo `balance`:

```sql
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,  -- ← Saldo armazenado aqui
    status VARCHAR(20) DEFAULT 'ACTIVE',
    ...
);
```

**Importante:**
- Saldo é atualizado via SQL `UPDATE` direto no banco
- Não precisa buscar, modificar e salvar o objeto Account
- Mais eficiente e seguro

---

## ✅ Status Atual

- ✅ **Criação de transação** → Atualiza saldo automaticamente
- ⚠️ **Atualização de transação** → Ainda não reverte saldo antigo
- ⚠️ **Exclusão de transação** → Ainda não reverte saldo

**Nota:** Apenas a criação está implementada. Para atualizar/deletar, seria necessário reverter o saldo antigo primeiro e depois aplicar o novo.

---

**Última atualização:** 16 de Janeiro de 2025  
**Status:** ✅ Funcionando para criação de transações

