# Alterações Necessárias no Front-End - Sistema de Contas Bancárias

## 📋 Resumo das Mudanças

As seguintes alterações foram feitas no backend e precisam ser refletidas no front-end:

1. **Campos de banco adicionados** (`bankCode` e `bankName`)
2. **Campo `selectedPlan` removido** (plano agora é do usuário, não da conta)
3. **Estrutura de dados atualizada**

---

## 🔄 Mudanças nos DTOs

### AccountRequestDTO (Criação/Atualização de Conta)

**Campos REMOVIDOS:**
- ❌ `selectedPlan` - Removido completamente

**Campos ADICIONADOS:**
- ✅ `bankCode` (String) - Código FEBRABAN do banco (ex: "001")
- ✅ `bankName` (String) - Nome do banco (ex: "Banco do Brasil")

**Campos MANTIDOS:**
- ✅ `userId` (Integer)
- ✅ `agency` (String) - Número da agência
- ✅ `accountNumber` (String) - Número da conta
- ✅ `accountType` (String) - Tipo da conta: "CHECKING" ou "SAVINGS"

**Estrutura JSON Atualizada:**
```json
{
  "userId": 1,
  "bankCode": "001",
  "bankName": "Banco do Brasil",
  "agency": "1596",
  "accountNumber": "75614-9",
  "accountType": "CHECKING"
}
```

### AccountResponseDTO (Resposta da API)

**Campos REMOVIDOS:**
- ❌ `selectedPlan` - Removido completamente
- ❌ `bank` - Substituído por `bankCode` e `bankName`

**Campos ADICIONADOS:**
- ✅ `bankCode` (String) - Código FEBRABAN do banco
- ✅ `bankName` (String) - Nome do banco

**Campos MANTIDOS:**
- ✅ `accountId` (Integer)
- ✅ `userId` (Integer)
- ✅ `agency` (String)
- ✅ `accountNumber` (String)
- ✅ `accountType` (String)
- ✅ `balance` (BigDecimal/Number)
- ✅ `status` (String)
- ✅ `createdAt` (DateTime)

**Estrutura JSON de Resposta:**
```json
{
  "success": true,
  "data": {
    "accountId": 1,
    "userId": 1,
    "bankCode": "001",
    "bankName": "Banco do Brasil",
    "agency": "1596",
    "accountNumber": "75614-9",
    "accountType": "CHECKING",
    "balance": 0.00,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 🎯 Ações Necessárias no Front-End

### 1. Atualizar Interfaces/Tipos TypeScript

```typescript
// Antes
interface AccountRequest {
  userId: number;
  bank?: string;
  agency: string;
  accountNumber: string;
  accountType: string;
  selectedPlan: string; // ❌ REMOVER
}

interface AccountResponse {
  accountId: number;
  userId: number;
  bank?: string; // ❌ REMOVER
  agency: string;
  accountNumber: string;
  accountType: string;
  balance: number;
  status: string;
  selectedPlan: string; // ❌ REMOVER
  createdAt: string;
}

// Depois
interface AccountRequest {
  userId: number;
  bankCode: string; // ✅ ADICIONAR
  bankName: string; // ✅ ADICIONAR
  agency: string;
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS';
  // selectedPlan removido
}

interface AccountResponse {
  accountId: number;
  userId: number;
  bankCode: string; // ✅ ADICIONAR
  bankName: string; // ✅ ADICIONAR
  agency: string;
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS';
  balance: number;
  status: string;
  createdAt: string;
  // selectedPlan removido
}
```

### 2. Atualizar Formulários de Criação/Edição de Conta

**Remover:**
- ❌ Campo de seleção de plano (`selectedPlan`)
- ❌ Qualquer lógica relacionada a plano na criação de conta
- ❌ Validação de `selectedPlan`

**Adicionar:**
- ✅ Campo para `bankCode` (código do banco - 3 dígitos)
- ✅ Campo para `bankName` (nome do banco)
- ✅ Validações para os novos campos

**Exemplo de Formulário:**
```typescript
// Exemplo com React/Angular
const accountForm = {
  userId: currentUser.id,
  bankCode: "001", // Campo obrigatório
  bankName: "Banco do Brasil", // Campo obrigatório
  agency: "1596",
  accountNumber: "75614-9",
  accountType: "CHECKING" // "CHECKING" ou "SAVINGS"
  // selectedPlan removido
};
```

### 3. Atualizar Componentes de Exibição de Conta

**Na exibição do card de conta (conforme exemplo da UI):**

```typescript
// Antes
<div>
  <span>{account.bank}</span>
  {account.selectedPlan && <Tag>{account.selectedPlan}</Tag>}
</div>

// Depois
<div>
  <span>{account.bankCode} - {account.bankName}</span>
  {/* selectedPlan não existe mais aqui */}
</div>
```

**Estrutura sugerida para o card:**
- Mostrar: `{bankCode} - {bankName}` (ex: "001 - Banco do Brasil")
- Mostrar: `Agência: {agency}`
- Mostrar: `Tipo: {accountType === 'CHECKING' ? 'Corrente' : 'Poupança'}`
- Mostrar: `Conta: {accountNumber}`
- Mostrar: `Saldo: R$ {balance}`
- **NÃO mostrar** plano (plano é do usuário, não da conta)

### 4. Atualizar Chamadas de API

**Endpoint:** `POST /api/form/account`

```typescript
// Antes
const createAccount = async (data: AccountRequest) => {
  const response = await fetch('http://localhost:8080/api/form/account', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: data.userId,
      bank: data.bank,
      agency: data.agency,
      accountNumber: data.accountNumber,
      accountType: data.accountType,
      selectedPlan: data.selectedPlan // ❌ REMOVER
    })
  });
  return response.json();
};

// Depois
const createAccount = async (data: AccountRequest) => {
  const response = await fetch('http://localhost:8080/api/form/account', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: data.userId,
      bankCode: data.bankCode, // ✅ ADICIONAR
      bankName: data.bankName, // ✅ ADICIONAR
      agency: data.agency,
      accountNumber: data.accountNumber,
      accountType: data.accountType
      // selectedPlan removido
    })
  });
  return response.json();
};
```

### 5. Mapeamento de Tipos de Conta

Se necessário, adicionar tradução para exibição:

```typescript
const accountTypeMap = {
  'CHECKING': 'Corrente',
  'SAVINGS': 'Poupança'
};

// Uso
const displayType = accountTypeMap[account.accountType] || account.accountType;
```

### 6. Lista de Bancos (Opcional - Sugestão)

Se quiser facilitar a seleção do banco, pode criar uma lista:

```typescript
const banks = [
  { code: '001', name: 'Banco do Brasil' },
  { code: '033', name: 'Banco Santander' },
  { code: '104', name: 'Caixa Econômica Federal' },
  { code: '237', name: 'Banco Bradesco' },
  { code: '341', name: 'Banco Itaú' },
  { code: '422', name: 'Banco Safra' },
  // ... mais bancos
];
```

---

## ⚠️ Pontos Importantes

1. **Plano não é mais da conta**: O plano agora pertence apenas ao usuário. Se precisar exibir o plano, deve buscar do objeto User, não do Account.

2. **bankCode e bankName são obrigatórios**: Certifique-se de sempre enviar ambos os campos ao criar/atualizar uma conta.

3. **accountType**: Continua aceitando apenas "CHECKING" ou "SAVINGS" (em maiúsculas).

4. **Backward Compatibility**: Se houver contas antigas sem `bankCode`/`bankName`, elas podem ter valores `null`. Trate isso na exibição.

---

## 📝 Checklist de Implementação

- [ ] Atualizar interfaces TypeScript (AccountRequest e AccountResponse)
- [ ] Remover campo `selectedPlan` de todos os formulários
- [ ] Adicionar campos `bankCode` e `bankName` nos formulários
- [ ] Atualizar componentes de exibição de conta
- [ ] Atualizar chamadas de API (POST /api/form/account)
- [ ] Remover lógica de plano relacionada a contas
- [ ] Testar criação de conta com novos campos
- [ ] Testar exibição de conta com novos campos
- [ ] Validar que plano não aparece mais nas contas
- [ ] Adicionar validações para `bankCode` e `bankName`

---

## 🧪 Exemplo Completo de Uso

```typescript
// Criar conta
const newAccount: AccountRequest = {
  userId: 1,
  bankCode: "001",
  bankName: "Banco do Brasil",
  agency: "1596",
  accountNumber: "75614-9",
  accountType: "CHECKING"
};

const response = await fetch('http://localhost:8080/api/form/account', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(newAccount)
});

const result = await response.json();
// result.data contém AccountResponse sem selectedPlan
```

---

## 📞 Observações Finais

- Todas as rotas de conta continuam funcionando, apenas a estrutura dos dados mudou
- A rota `GET /api/accounts` também retorna as contas com a nova estrutura
- O plano do usuário deve ser gerenciado separadamente via endpoints de usuário


