# 📊 Análise de Rotas: Frontend vs API Backend

Este documento relaciona as rotas que o frontend **precisa** com as rotas **disponíveis** na API do backend.

**Base URL Backend:** `http://localhost:8080`  
**Base URL Frontend Atual:** `http://localhost:8080/Cofry` ⚠️

---

## 🔴 PROBLEMAS IDENTIFICADOS

### 1. **Inconsistência na Base URL**
   - Frontend está usando: `http://localhost:8080/Cofry`
   - API documentada usa: `http://localhost:8080`
   - **Ação necessária:** Remover `/Cofry` ou ajustar backend

### 2. **Rotas Incorretas ou Inexistentes**
   - Algumas rotas do frontend não correspondem à documentação da API

---

## 📋 ROTAS ATUAIS DO FRONTEND

### ✅ **Login Service** (`login.service.ts`)
```typescript
POST http://localhost:8080/Cofry/login
```

**❌ PROBLEMA:** 
- Frontend usa: `/Cofry/login`
- API documentada: `/api/auth/login`
- **Payload diferente:** Frontend envia `{ email, password }`, API espera `{ emailOrCpf, password }`

**✅ CORREÇÃO NECESSÁRIA:**
```typescript
POST http://localhost:8080/api/auth/login
Body: {
  "emailOrCpf": "usuario@email.com",
  "password": "senha123"
}
```

---

### ✅ **Sign Up Service** (`sign-up.service.ts`)
```typescript
POST http://localhost:8080/Cofry/auth/Create
```

**❌ PROBLEMA:**
- Frontend usa: `/Cofry/auth/Create`
- API documentada: `/api/form/user`

**✅ CORREÇÃO NECESSÁRIA:**
```typescript
POST http://localhost:8080/api/form/user
Body: {
  "firstName": "Maria",
  "lastName": "Santos",
  "taxId": "987.654.321-00",
  "email": "maria@email.com",
  "password": "senha123",
  "phoneNumber": "(11) 98765-4321",
  "dateOfBirth": "1995-03-15",
  "planId": 1
}
```

---

### ✅ **User Service** (`user.service.ts`)
```typescript
GET http://localhost:8080/Cofry/api/user-data?email={email}
```

**❌ PROBLEMA:**
- Frontend usa: `/Cofry/api/user-data?email=...`
- API documentada: `/api/users/{id}` ou `/api/users` (lista todos)

**✅ CORREÇÃO NECESSÁRIA:**
```typescript
// Opção 1: Buscar por ID (se tiver userId)
GET http://localhost:8080/api/users/{userId}

// Opção 2: Listar todos e filtrar (não recomendado)
GET http://localhost:8080/api/users
```

**💡 RECOMENDAÇÃO:** 
- Após login, salvar `userId` no localStorage
- Usar `/api/users/{userId}` para buscar dados do usuário

---

### ✅ **Transaction Service** (`transaction.service.ts`)
```typescript
GET http://localhost:8080/Cofry/api/transactions
GET http://localhost:8080/Cofry/api/transactions/user/{userId}
```

**❌ PROBLEMA:**
- Frontend usa: `/Cofry/api/transactions/user/{userId}`
- API documentada: `/api/transactions?userId={userId}` (query parameter)

**✅ CORREÇÃO NECESSÁRIA:**
```typescript
GET http://localhost:8080/api/transactions?userId={userId}
// Ou com mais filtros:
GET http://localhost:8080/api/transactions?userId={userId}&type=PAYMENT&startDate=2025-01-01
```

---

### ⚠️ **Plan Service** (`plan.service.ts`)
```typescript
GET /api/plans
GET /api/users/me/plan
GET /api/users/{userId}/plan
```

**❌ PROBLEMA:**
- Base URL está como `/api` (relativa)
- Rotas `/api/users/me/plan` e `/api/users/{userId}/plan` **NÃO EXISTEM** na documentação

**✅ CORREÇÃO NECESSÁRIA:**
- Ajustar base URL para `http://localhost:8080/api`
- **NÃO HÁ ROTA DE PLANOS** na documentação atual
- **AÇÃO:** Verificar se backend tem essas rotas ou usar dados do usuário (`planId` vem em `/api/users/{id}`)

---

## 🎯 ROTAS QUE O FRONTEND PRECISA MAS NÃO ESTÁ USANDO

### 1. **Contas Bancárias** (Saldo, Extrato)
```typescript
// Buscar conta do usuário
GET /api/accounts?userId={userId}
// ou
GET /api/accounts/{accountId}

// Atualizar saldo (se necessário)
PUT /api/accounts/{id}/balance
```

**📍 Onde usar:**
- `after-login.ts` - Exibir saldo real
- `extrato.ts` - Listar transações da conta

---

### 2. **PIX - Transferência**
```typescript
POST /api/pix/transfer
Body: {
  "sourceUserId": 1,
  "destinationUserId": 2,
  "amount": 100.00,
  "description": "Transferência PIX"
}
```

**📍 Onde usar:**
- `pix.ts` - Realizar transferências PIX
- `transferir.ts` - Transferir para outros usuários

---

### 3. **Boletos (DDA)**
```typescript
// Listar boletos do usuário
GET /api/form/boleto/user/{userId}

// Buscar boletos por CPF
GET /api/form/boleto/cpf/{cpf}

// Criar boleto
POST /api/form/boleto
```

**📍 Onde usar:**
- `boletos.ts` - Listar e pagar boletos
- `pagar.ts` - Criar novos boletos

---

### 4. **Cartões**
```typescript
// Listar cartões do usuário
GET /api/form/card/user/{userId}

// Criar cartão
POST /api/form/card

// Listar tipos de cartão
GET /api/form/card/types
```

**📍 Onde usar:**
- `cards.ts` - Gerenciar cartões do usuário

---

### 5. **Investimentos**
```typescript
// Resumo do portfólio
GET /api/investments/portfolio/user/{userId}

// Histórico de transações
GET /api/investments/history/user/{userId}

// Criar transação de investimento
POST /api/investments/transaction
```

**📍 Onde usar:**
- `invest.ts` - Exibir carteira de investimentos

---

### 6. **Endereços** (Para cadastro)
```typescript
// Buscar endereço por CEP
GET /api/form/address/lookup?zipCode={cep}

// Listar estados
GET /api/form/address/states

// Listar cidades
GET /api/form/address/cities?state={uf}
```

**📍 Onde usar:**
- `sign-up.ts` - Preencher endereço automaticamente

---

## 📝 MAPEAMENTO COMPLETO: PÁGINAS → ROTAS NECESSÁRIAS

### 🏠 **Home** (`home.ts`)
- ✅ Nenhuma rota necessária (página estática)

### 🔐 **Login** (`login.ts`)
- ✅ `POST /api/auth/login` (corrigir)

### 📝 **Sign Up** (`sign-up.ts`)
- ✅ `POST /api/form/user` (corrigir)
- ✅ `GET /api/form/address/lookup?zipCode={cep}` (adicionar)
- ✅ `GET /api/form/address/states` (adicionar)
- ✅ `GET /api/form/address/cities?state={uf}` (adicionar)

### 🏡 **After Login** (`after-login.ts`)
- ✅ `GET /api/users/{userId}` (corrigir - buscar dados do usuário)
- ✅ `GET /api/accounts?userId={userId}` (adicionar - buscar saldo)
- ✅ `GET /api/transactions?userId={userId}` (corrigir)
- ✅ `GET /api/form/boleto/user/{userId}` (adicionar - próximos vencimentos)

### 💳 **Cards** (`cards.ts`)
- ✅ `GET /api/form/card/user/{userId}` (adicionar)
- ✅ `POST /api/form/card` (adicionar - criar cartão)
- ✅ `GET /api/form/card/types` (adicionar)
- ✅ `PUT /api/form/card/{id}` (adicionar - atualizar)
- ✅ `DELETE /api/form/card/{id}` (adicionar - deletar)

### 💰 **Invest** (`invest.ts`)
- ✅ `GET /api/investments/portfolio/user/{userId}` (adicionar)
- ✅ `GET /api/investments/history/user/{userId}` (adicionar)
- ✅ `GET /api/investments/distribution/user/{userId}` (adicionar)
- ✅ `POST /api/investments/transaction` (adicionar)

### 📄 **Plans** (`plans.ts`)
- ⚠️ Verificar se existe rota de planos no backend
- ✅ Usar `planId` de `/api/users/{id}` (já vem no objeto usuário)

### 📋 **Boletos** (`boletos.ts`)
- ✅ `GET /api/form/boleto/user/{userId}` (adicionar)
- ✅ `GET /api/form/boleto/cpf/{cpf}` (adicionar)
- ✅ `POST /api/form/boleto` (adicionar - criar boleto)

### 💸 **PIX** (`pix.ts`)
- ✅ `POST /api/pix/transfer` (adicionar)
- ✅ `GET /api/accounts?userId={userId}` (adicionar - verificar saldo)

### 💵 **Pagar** (`pagar.ts`)
- ✅ `POST /api/form/boleto` (adicionar - criar boleto)
- ✅ `GET /api/form/boleto/cpf/{cpf}` (adicionar - buscar boletos)

### 🔄 **Transferir** (`transferir.ts`)
- ✅ `POST /api/pix/transfer` (adicionar)
- ✅ `GET /api/users` (adicionar - buscar destinatário)

### 📊 **Extrato** (`extrato.ts`)
- ✅ `GET /api/transactions?userId={userId}` (corrigir)
- ✅ `GET /api/transactions?userId={userId}&startDate={date}&endDate={date}` (adicionar - filtros)

---

## 🔧 PLANO DE CORREÇÃO

### **Fase 1: Corrigir Rotas Existentes**
1. ✅ Atualizar `login.service.ts` para usar `/api/auth/login`
2. ✅ Atualizar `sign-up.service.ts` para usar `/api/form/user`
3. ✅ Atualizar `user.service.ts` para usar `/api/users/{id}`
4. ✅ Atualizar `transaction.service.ts` para usar query parameters
5. ✅ Remover `/Cofry` de todas as base URLs

### **Fase 2: Adicionar Novos Serviços**
1. ✅ Criar `account.service.ts` (contas bancárias)
2. ✅ Criar `pix.service.ts` (transferências PIX)
3. ✅ Criar `boleto.service.ts` (boletos/DDA)
4. ✅ Criar `card.service.ts` (cartões)
5. ✅ Criar `investment.service.ts` (investimentos)
6. ✅ Criar `address.service.ts` (endereços/CEP)

### **Fase 3: Integrar nas Páginas**
1. ✅ Atualizar cada página para usar os novos serviços
2. ✅ Adicionar tratamento de erros
3. ✅ Adicionar loading states
4. ✅ Testar todas as integrações

---

## 📌 NOTAS IMPORTANTES

1. **Autenticação:**
   - A API não menciona JWT tokens na documentação
   - Verificar se é necessário enviar token no header `Authorization`
   - Frontend atual usa `localStorage.getItem('token')`

2. **IDs de Usuário:**
   - Após login, salvar `userId` no localStorage
   - Usar `userId` para todas as requisições que precisam identificar o usuário

3. **Formato de Datas:**
   - API espera: `YYYY-MM-DD` (ex: `2025-01-15`)
   - API espera com hora: `YYYY-MM-DDTHH:mm:ss` (ex: `2025-01-15T14:30:00`)

4. **Formato de Valores:**
   - Enviar como números decimais (ex: `150.00`)
   - Investimentos podem ser strings (ex: `"45.50"`)

5. **CORS:**
   - API está configurada para aceitar requisições de qualquer origem
   - Não deve haver problemas de CORS

---

## ✅ CHECKLIST DE IMPLEMENTAÇÃO

- [ ] Corrigir base URL (remover `/Cofry`)
- [ ] Corrigir rota de login
- [ ] Corrigir rota de sign-up
- [ ] Corrigir rota de usuário
- [ ] Corrigir rota de transações
- [ ] Criar serviço de contas bancárias
- [ ] Criar serviço de PIX
- [ ] Criar serviço de boletos
- [ ] Criar serviço de cartões
- [ ] Criar serviço de investimentos
- [ ] Criar serviço de endereços
- [ ] Integrar saldo real no dashboard
- [ ] Integrar PIX na página de PIX
- [ ] Integrar boletos na página de boletos
- [ ] Integrar cartões na página de cartões
- [ ] Integrar investimentos na página de investimentos
- [ ] Testar todas as integrações
- [ ] Adicionar tratamento de erros
- [ ] Adicionar loading states

---

**Última atualização:** 2025-01-15

