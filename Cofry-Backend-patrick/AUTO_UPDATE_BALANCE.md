# Atualização Automática do Saldo das Contas

## 📋 Resumo

O saldo das contas bancárias agora é atualizado **automaticamente** quando uma transação é criada, baseado no tipo de transação.

---

## 🎯 Como Funciona

Quando uma transação é criada via `POST /api/transactions`, o backend:

1. **Valida** se há saldo suficiente (para transações de saída)
2. **Salva** a transação no banco de dados
3. **Atualiza automaticamente** o saldo da(s) conta(s) envolvida(s)

Tudo isso acontece em **uma única transação do banco**, garantindo atomicidade (tudo ou nada).

---

## 💰 Lógica de Atualização de Saldo

### **DEPOSIT (Depósito) - Entrada**
- **Ação:** Adiciona o valor à conta de origem
- **Exemplo:** Saldo: R$ 1000,00 → Depósito de R$ 500,00 → Novo saldo: R$ 1.500,00

### **WITHDRAWAL/PAYMENT (Saque/Pagamento) - Saída**
- **Ação:** Subtrai o valor da conta de origem
- **Validação:** Verifica se há saldo suficiente antes de executar
- **Exemplo:** Saldo: R$ 1000,00 → Pagamento de R$ 300,00 → Novo saldo: R$ 700,00

### **TRANSFER (Transferência)**
- **Ação:** 
  - Subtrai o valor da conta de origem
  - Adiciona o valor à conta de destino
- **Validação:** Verifica se a conta de origem tem saldo suficiente
- **Exemplo:** 
  - Conta Origem: R$ 1000,00 → Transferência de R$ 500,00 → Novo saldo: R$ 500,00
  - Conta Destino: R$ 200,00 → Recebe R$ 500,00 → Novo saldo: R$ 700,00

---

## ✅ Validações Implementadas

### Validação de Saldo Suficiente

Antes de criar uma transação de saída (`WITHDRAWAL`, `PAYMENT`, `TRANSFER`), o sistema verifica se a conta de origem tem saldo suficiente.

**Erro retornado se saldo insuficiente:**
```
Saldo insuficiente. Saldo atual: R$ 100,00, Valor necessário: R$ 500,00
```

**Status HTTP:** 400 (Bad Request)

---

## 🔒 Atomicidade

Todas as operações (salvar transação + atualizar saldos) são executadas em **uma única transação do banco de dados**:

- ✅ **Sucesso:** Transação é salva E saldos são atualizados
- ❌ **Erro:** Nada é salvo (rollback automático)

Isso garante que não haja inconsistências no banco de dados.

---

## 📝 Exemplos

### Exemplo 1: Depósito (Entrada)

**Request:**
```json
{
  "sourceAccountId": 1,
  "isIncome": true,
  "amount": 2000.00,
  "description": "Salário recebido"
}
```

**Resultado:**
- Transação criada com tipo `DEPOSIT`
- Saldo da conta 1: `+R$ 2.000,00`

---

### Exemplo 2: Pagamento (Saída)

**Request:**
```json
{
  "sourceAccountId": 1,
  "isIncome": false,
  "amount": 150.00,
  "description": "Supermercado"
}
```

**Resultado:**
- Transação criada com tipo `PAYMENT`
- Saldo da conta 1: `-R$ 150,00`
- Se não houver saldo suficiente → Erro 400

---

### Exemplo 3: Transferência

**Request:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 500.00,
  "description": "Transferência para poupança"
}
```

**Resultado:**
- Transação criada com tipo `TRANSFER`
- Saldo da conta 1 (origem): `-R$ 500,00`
- Saldo da conta 2 (destino): `+R$ 500,00`
- Se conta 1 não tiver saldo suficiente → Erro 400

---

## 📊 Estrutura no Banco de Dados

### Tabela `accounts`

O campo `balance` armazena o saldo atual da conta:

```sql
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,  -- ← Saldo da conta
    ...
);
```

### Tabela `transactions`

As transações são salvas e o saldo é atualizado automaticamente:

```sql
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    source_account_id INT NOT NULL,
    destination_account_id INT,
    amount NUMERIC(15, 2) NOT NULL,
    transaction_type transaction_type_enum NOT NULL,
    ...
);
```

---

## 🔄 Fluxo de Execução

```
┌─────────────────────────────────────┐
│  Criar Transação                    │
│  (POST /api/transactions)           │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  Determinar Tipo Automaticamente    │
│  (DEPOSIT, PAYMENT, TRANSFER)       │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  Validar Saldo (se saída)           │
│  Verifica se há saldo suficiente    │
└──────────────┬──────────────────────┘
               │
               ↓ (Sucesso)
┌─────────────────────────────────────┐
│  INICIAR TRANSAÇÃO DO BANCO         │
│  ┌──────────────────────────────┐   │
│  │ 1. Salvar transação          │   │
│  │ 2. Atualizar saldo(s)        │   │
│  └──────────────────────────────┘   │
│  COMMIT (ou ROLLBACK se erro)       │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│  Retornar Transação Criada          │
└─────────────────────────────────────┘
```

---

## ⚠️ Comportamento Importante

### Saldo Negativo

O sistema **permite saldo negativo** após uma transação ser criada. A validação de saldo suficiente é feita **antes** de criar a transação, mas depois que a transação é criada, o saldo pode ficar negativo se outras transações forem processadas simultaneamente.

**Recomendação:** Para evitar saldos negativos, considere:
- Usar locks no banco de dados
- Implementar validação adicional com `CHECK` constraint no banco
- Usar transações com nível de isolamento adequado

### Status da Conta

O saldo só é atualizado se a conta estiver com status `'ACTIVE'`. Contas inativas não têm o saldo alterado.

---

## 🔍 Verificação do Saldo

### Como Consultar o Saldo Atualizado

Após criar uma transação, você pode consultar o saldo atualizado através do endpoint:

```bash
GET /api/accounts/{accountId}
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "accountId": 1,
    "balance": 1500.00,  // ← Saldo atualizado automaticamente
    ...
  }
}
```

---

## 📝 Notas Técnicas

### Métodos Implementados

1. **`validateSufficientBalance()`**
   - Valida se há saldo suficiente antes de criar transação de saída
   - Lança `IllegalStateException` se saldo insuficiente

2. **`saveTransactionAndUpdateBalances()`**
   - Executa tudo em uma única transação do banco
   - Garante atomicidade

3. **`saveTransactionInConnection()`**
   - Salva a transação usando uma conexão específica
   - Usado dentro de uma transação maior

4. **`updateAccountBalancesInConnection()`**
   - Atualiza os saldos usando uma conexão específica
   - Usado dentro de uma transação maior

---

## ✅ Testes Recomendados

1. ✅ Criar depósito e verificar se saldo aumentou
2. ✅ Criar pagamento e verificar se saldo diminuiu
3. ✅ Criar transferência e verificar ambos os saldos
4. ✅ Tentar criar pagamento sem saldo suficiente (deve dar erro)
5. ✅ Verificar atomicidade (tudo ou nada)

---

**Data de Implementação:** Janeiro 2025
**Status:** ✅ Implementado e Funcionando

