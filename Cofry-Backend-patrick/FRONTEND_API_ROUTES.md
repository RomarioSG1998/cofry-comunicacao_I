# 📋 Relatório de Rotas da API - Front-End

Este documento contém **todas as rotas disponíveis** na API do Cofry Backend para uso no front-end.

---

## 🔗 Base URL

```
http://localhost:8080
```

**Importante:** Todas as rotas abaixo são relativas a esta base URL.

---

## 📝 Índice

1. [Autenticação](#1-autenticação)
2. [Usuários](#2-usuários)
3. [Endereços](#3-endereços)
4. [Contas Bancárias](#4-contas-bancárias)
5. [Cartões](#5-cartões)
6. [Transações](#6-transações)
7. [PIX](#7-pix)
8. [Boletos](#8-boletos)
9. [Investimentos](#9-investimentos)
10. [Orçamentos](#10-orçamentos)
11. [Metas de Poupança](#11-metas-de-poupança)

---

## 1. Autenticação

### 🔐 Login

**POST** `/api/auth/login`

Realiza o login de um usuário usando email/CPF e senha.

**Request Body:**
```json
{
  "emailOrCpf": "usuario@email.com",
  "password": "senha123"
}
```

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "userId": 1,
    "email": "usuario@email.com",
    "firstName": "João",
    "lastName": "Silva"
  }
}
```

**Resposta (400 Bad Request):**
```json
{
  "error": "CPF, senha ou email incorreto",
  "status": 400
}
```

---

## 2. Usuários

### 👤 Listar Todos os Usuários

**GET** `/api/users`

Retorna uma lista com todos os usuários cadastrados.

**Resposta:**
```json
[
  {
    "userId": 1,
    "planId": 2,
    "firstName": "João",
    "lastName": "Silva",
    "taxId": "123.456.789-00",
    "email": "joao@email.com",
    "phoneNumber": null,
    "dateOfBirth": "1990-01-15",
    "isActive": true,
    "createdAt": "2025-10-01T10:00:00",
    "updatedAt": "2025-10-01T10:00:00"
  }
]
```

---

### 👤 Buscar Usuário por ID

**GET** `/api/users/{id}`

Retorna os dados básicos de um usuário específico.

**Parâmetros:**
- `id` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "userId": 1,
  "planId": 2,
  "firstName": "João",
  "lastName": "Silva",
  "taxId": "123.456.789-00",
  "email": "joao@email.com",
  "phoneNumber": null,
  "dateOfBirth": "1990-01-15",
  "isActive": true,
  "createdAt": "2025-10-01T10:00:00",
  "updatedAt": "2025-10-01T10:00:00"
}
```

---

### 👤 Buscar Usuário Completo por ID (NOVO)

**GET** `/api/users/{id}/complete`

Retorna todas as informações do usuário em uma única requisição, incluindo dados pessoais, endereços e contas bancárias.

**Parâmetros:**
- `id` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "firstName": "João",
    "lastName": "Silva",
    "fullName": "João Silva",
    "email": "joao@email.com",
    "cpf": "123.456.789-00",
    "phoneNumber": "(11) 98765-4321",
    "dateOfBirth": "1990-01-15",
    "isActive": true,
    "planId": 1,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00",
    "addresses": [
      {
        "addressId": 1,
        "userId": 1,
        "phoneNumber": "(11) 98765-4321",
        "zipCode": "01310-100",
        "houseNumber": "123",
        "street": "Avenida Paulista",
        "district": "Bela Vista",
        "city": "São Paulo",
        "state": "SP",
        "complement": "Apto 45",
        "country": "Brazil",
        "createdAt": "2025-01-01T10:00:00"
      }
    ],
    "accounts": [
      {
        "accountId": 1,
        "userId": 1,
        "bankCode": "001",
        "bankName": "Banco do Brasil",
        "agency": "1596",
        "accountNumber": "75614-9",
        "accountType": "CHECKING",
        "balance": 1000.00,
        "status": "ACTIVE",
        "createdAt": "2025-01-01T10:00:00"
      }
    ]
  }
}
```

**Quando usar:**
- Use `/api/users/{id}/complete` quando precisar de todas as informações do usuário em uma única chamada (ex: página de perfil completo)
- Use `/api/users/{id}` quando precisar apenas dos dados básicos do usuário

---

### ➕ Criar Usuário (Form)

**POST** `/api/form/user`

Cria um novo usuário a partir de dados de formulário.

**Request Body:**
```json
{
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

**Resposta (201 Created):**
```json
{
  "userId": 2,
  "firstName": "Maria",
  "lastName": "Santos",
  "taxId": "987.654.321-00",
  "email": "maria@email.com",
  "phoneNumber": "(11) 98765-4321",
  "dateOfBirth": "1995-03-15",
  "planId": 1
}
```

---

### ✏️ Atualizar Usuário

**PUT** `/api/users/{id}`

Atualiza os dados de um usuário existente.

**Parâmetros:**
- `id` (path): ID do usuário

**Request Body:**
```json
{
  "firstName": "Maria",
  "lastName": "Santos Silva",
  "phoneNumber": "(11) 99999-9999"
}
```

**Resposta (200 OK):**
```json
{
  "userId": 2,
  "firstName": "Maria",
  "lastName": "Santos Silva",
  "taxId": "987.654.321-00",
  "email": "maria@email.com",
  "phoneNumber": "(11) 99999-9999",
  "dateOfBirth": "1995-03-15",
  "planId": 1
}
```

---

### 🗑️ Deletar Usuário

**DELETE** `/api/users/{id}`

Remove um usuário do sistema.

**Parâmetros:**
- `id` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "message": "Usuário removido com sucesso"
}
```

---

## 3. Endereços

### 🔍 Buscar Endereço por CEP

**GET** `/api/form/address/lookup?zipCode=01310100`

Busca informações de endereço usando o CEP (integrado com ViaCEP).

**Parâmetros:**
- `zipCode` (query): CEP (com ou sem hífen, ex: `01310100` ou `01310-100`)

**Resposta (200 OK):**
```json
{
  "zipCode": "01310-100",
  "street": "Avenida Paulista",
  "district": "Bela Vista",
  "city": "São Paulo",
  "state": "SP"
}
```

**Resposta (404 Not Found):**
```json
{
  "error": "Zip code not found",
  "status": 404
}
```

---

### 📍 Listar Estados

**GET** `/api/form/address/states`

Retorna a lista de todos os estados brasileiros.

**Resposta:**
```json
[
  {
    "code": "SP",
    "name": "São Paulo"
  },
  {
    "code": "RJ",
    "name": "Rio de Janeiro"
  }
]
```

---

### 🏙️ Listar Cidades por Estado

**GET** `/api/form/address/cities?state=SP`

Retorna a lista de cidades de um estado específico.

**Parâmetros:**
- `state` (query): Código do estado (ex: `SP`, `RJ`)

**Resposta:**
```json
[
  {
    "name": "São Paulo"
  },
  {
    "name": "Campinas"
  }
]
```

---

### ➕ Criar Endereço

**POST** `/api/form/address`

Cria um novo endereço para um usuário.

**Request Body:**
```json
{
  "userId": 1,
  "street": "Avenida Paulista",
  "number": "1000",
  "complement": "Apto 101",
  "district": "Bela Vista",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01310-100"
}
```

**Resposta (201 Created):**
```json
{
  "addressId": 1,
  "userId": 1,
  "street": "Avenida Paulista",
  "number": "1000",
  "complement": "Apto 101",
  "district": "Bela Vista",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01310-100"
}
```

---

## 4. Contas Bancárias

### 📊 Listar Todas as Contas

**GET** `/api/accounts`

Retorna todas as contas bancárias cadastradas.

**Resposta:**
```json
[
  {
    "accountId": 1,
    "userId": 1,
    "accountNumber": "99902-X",
    "agencyNumber": "001",
    "accountType": "CHECKING",
    "balance": 1500.00,
    "status": "ACTIVE"
  }
]
```

---

### 📊 Buscar Conta por ID

**GET** `/api/accounts/{id}`

Retorna os dados de uma conta específica.

**Parâmetros:**
- `id` (path): ID da conta

**Resposta:**
```json
{
  "accountId": 1,
  "userId": 1,
  "accountNumber": "99902-X",
  "agencyNumber": "001",
  "accountType": "CHECKING",
  "balance": 1500.00,
  "status": "ACTIVE"
}
```

---

### ➕ Criar Conta

**POST** `/api/accounts`

Cria uma nova conta bancária.

**Request Body:**
```json
{
  "userId": 1,
  "accountNumber": "99902-X",
  "agencyNumber": "001",
  "accountType": "CHECKING",
  "balance": 1000.00,
  "status": "ACTIVE"
}
```

**Campos:**
- `accountType`: `CHECKING` (Conta Corrente) ou `SAVINGS` (Conta Poupança)
- `status`: `ACTIVE` ou `INACTIVE`

**Resposta (201 Created):**
```json
{
  "accountId": 1,
  "userId": 1,
  "accountNumber": "99902-X",
  "agencyNumber": "001",
  "accountType": "CHECKING",
  "balance": 1000.00,
  "status": "ACTIVE"
}
```

---

### ➕ Criar Conta (Form)

**POST** `/api/form/account`

Cria uma nova conta a partir de dados de formulário.

**Request Body:**
```json
{
  "userId": 1,
  "accountNumber": "99902-X",
  "agencyNumber": "001",
  "accountType": "CHECKING",
  "balance": 1000.00
}
```

---

### 📋 Listar Planos de Conta

**GET** `/api/form/account/plans`

Retorna a lista de planos de conta disponíveis.

**Resposta:**
```json
[
  {
    "name": "Básico",
    "value": "BASIC",
    "price": 0.00
  },
  {
    "name": "Premium",
    "value": "PREMIUM",
    "price": 29.90
  }
]
```

---

### 💰 Atualizar Saldo da Conta

**PUT** `/api/accounts/{id}/balance`

Define o saldo de uma conta (útil para simulação).

**Parâmetros:**
- `id` (path): ID da conta

**Request Body:**
```json
{
  "balance": 5000.00
}
```

**Resposta (200 OK):**
```json
{
  "accountId": 1,
  "balance": 5000.00
}
```

---

### ✏️ Atualizar Conta

**PUT** `/api/accounts/{id}`

Atualiza os dados de uma conta.

**Parâmetros:**
- `id` (path): ID da conta

**Request Body:**
```json
{
  "accountNumber": "99903-X",
  "status": "INACTIVE"
}
```

---

### 🗑️ Deletar Conta

**DELETE** `/api/accounts/{id}`

Remove uma conta do sistema.

**⚠️ IMPORTANTE:** Este endpoint verifica se existem transações relacionadas à conta antes de permitir a exclusão. Se houver transações vinculadas, a exclusão não será permitida e retornará erro 400.

**Parâmetros:**
- `id` (path): ID da conta

**Resposta (200 OK) - Sucesso:**
```json
{
  "success": true,
  "data": "Conta removida com sucesso"
}
```

**Resposta (400 Bad Request) - Conta tem transações:**
```json
{
  "error": "Não é possível remover a conta. Existem transações vinculadas a esta conta. Remova as transações primeiro ou desative a conta em vez de removê-la.",
  "status": 400
}
```

**Resposta (404 Not Found) - Conta não encontrada:**
```json
{
  "error": "Conta não encontrada com ID: {id}",
  "status": 404
}
```

**Recomendações:**
- Se a conta tiver transações relacionadas, considere desativar a conta (`PUT /api/accounts/{id}` com `status: "INACTIVE"`) em vez de deletá-la
- Sempre trate o erro 400 no frontend e mostre uma mensagem apropriada ao usuário

---

## 5. Cartões

### ➕ Criar Cartão

**POST** `/api/form/card`

Cria um novo cartão para um usuário.

**Request Body:**
```json
{
  "userId": 1,
  "cardNumber": "1234567812345678",
  "cardholderName": "JOÃO SILVA",
  "expiryDate": "12/25",
  "cvv": "123",
  "cardType": "CREDIT"
}
```

**Campos:**
- `cardType`: `CREDIT` (Crédito), `DEBIT` (Débito) ou `BOTH` (Débito e Crédito)
- `expiryDate`: Formato `MM/AA` (ex: `12/25`)

**Resposta (201 Created):**
```json
{
  "cardId": 1,
  "userId": 1,
  "maskedCardNumber": "1234 **** **** 5678",
  "cardholderName": "JOÃO SILVA",
  "expiryDate": "12/25",
  "cardBrand": "VISA",
  "cardType": "CREDIT"
}
```

**Nota:** O número do cartão retornado é mascarado automaticamente.

---

### 📋 Listar Tipos de Cartão

**GET** `/api/form/card/types`

Retorna os tipos de cartão disponíveis.

**Resposta:**
```json
[
  {
    "name": "CREDIT",
    "value": "CREDIT"
  },
  {
    "name": "DEBIT",
    "value": "DEBIT"
  },
  {
    "name": "BOTH",
    "value": "BOTH"
  }
]
```

---

### 📋 Listar Cartões por Usuário

**GET** `/api/form/card/user/{userId}`

Retorna todos os cartões de um usuário.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
[
  {
    "cardId": 1,
    "userId": 1,
    "maskedCardNumber": "1234 **** **** 5678",
    "cardholderName": "JOÃO SILVA",
    "expiryDate": "12/25",
    "cardBrand": "VISA",
    "cardType": "CREDIT"
  }
]
```

---

### 🔍 Buscar Cartão por ID

**GET** `/api/form/card/{id}`

Retorna os dados de um cartão específico (número mascarado).

**Parâmetros:**
- `id` (path): ID do cartão

**Resposta:**
```json
{
  "cardId": 1,
  "userId": 1,
  "maskedCardNumber": "1234 **** **** 5678",
  "cardholderName": "JOÃO SILVA",
  "expiryDate": "12/25",
  "cardBrand": "VISA",
  "cardType": "CREDIT"
}
```

---

### ✏️ Atualizar Cartão

**PUT** `/api/form/card/{id}`

Atualiza os dados de um cartão.

**Parâmetros:**
- `id` (path): ID do cartão

**Request Body:**
```json
{
  "cardholderName": "MARIA SILVA",
  "expiryDate": "06/26"
}
```

**Resposta (200 OK):**
```json
{
  "cardId": 1,
  "userId": 1,
  "maskedCardNumber": "1234 **** **** 5678",
  "cardholderName": "MARIA SILVA",
  "expiryDate": "06/26",
  "cardBrand": "VISA",
  "cardType": "CREDIT"
}
```

---

### 🗑️ Deletar Cartão

**DELETE** `/api/form/card/{id}`

Remove um cartão do sistema.

**Parâmetros:**
- `id` (path): ID do cartão

**Resposta (200 OK):**
```json
{
  "message": "Cartão removido com sucesso"
}
```

---

## 6. Transações

### 📊 Listar Transações

**GET** `/api/transactions`

Lista todas as transações ou filtra por parâmetros.

**Query Parameters:**
- `accountId` (opcional): Filtra por conta de origem
- `userId` (opcional): Filtra por usuário (origem ou destino)
- `type` (opcional): Filtra por tipo (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `PAYMENT`)
- `categoryId` (opcional): Filtra por categoria
- `startDate` (opcional): Data inicial (formato: `YYYY-MM-DD`)
- `endDate` (opcional): Data final (formato: `YYYY-MM-DD`)

**Exemplos:**
- `/api/transactions?accountId=1`
- `/api/transactions?userId=1`
- `/api/transactions?type=PAYMENT&startDate=2025-01-01&endDate=2025-01-31`

**Resposta:**
```json
[
  {
    "transactionId": 1,
    "sourceAccountId": 1,
    "destinationAccountId": null,
    "categoryId": 1,
    "amount": 150.00,
    "transactionType": "PAYMENT",
    "description": "Supermercado",
    "transactionDate": "2025-01-15",
    "isRecurring": false
  }
]
```

---

### 🔍 Buscar Transação por ID

**GET** `/api/transactions/{id}`

Retorna os dados de uma transação específica.

**Parâmetros:**
- `id` (path): ID da transação

---

### ➕ Criar Transação

**POST** `/api/transactions`

Cria uma nova transação financeira.

**Request Body:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": null,
  "categoryId": 1,
  "amount": 150.00,
  "transactionType": "PAYMENT",
  "description": "Supermercado",
  "transactionDate": "2025-01-15",
  "isRecurring": false,
  "installmentCurrent": null,
  "installmentTotal": null
}
```

**Campos:**
- `transactionType`: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `PAYMENT`
- `destinationAccountId`: Obrigatório apenas para `TRANSFER`

---

### ✏️ Atualizar Transação

**PUT** `/api/transactions/{id}`

Atualiza os dados de uma transação.

**Parâmetros:**
- `id` (path): ID da transação

**Request Body:**
```json
{
  "amount": 200.00,
  "description": "Supermercado - Atualizado"
}
```

---

### 🗑️ Deletar Transação

**DELETE** `/api/transactions/{id}`

Remove uma transação do sistema.

**Parâmetros:**
- `id` (path): ID da transação

---

## 7. PIX

### 💸 Realizar Transferência PIX

**POST** `/api/pix/transfer`

Realiza uma transferência PIX entre dois usuários (simulação).

**Request Body:**
```json
{
  "sourceUserId": 1,
  "destinationUserId": 2,
  "amount": 100.00,
  "description": "Transferência PIX"
}
```

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Transferência PIX realizada com sucesso",
  "data": {
    "sourceTransactionId": 10,
    "destinationTransactionId": 11,
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 100.00,
    "description": "Transferência PIX",
    "transactionDate": "2025-01-15T14:30:00"
  }
}
```

**Nota:** A transferência cria duas transações:
- Uma de saída (débito) na conta de origem
- Uma de entrada (crédito) na conta de destino

---

## 8. Boletos

### ➕ Criar Boleto

**POST** `/api/form/boleto`

Gera um novo boleto bancário.

**Request Body:**
```json
{
  "userId": 1,
  "title": "Conta de Luz",
  "amount": 250.00,
  "dueDate": "2025-02-15"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "title": "Conta de Luz",
  "amount": 250.00,
  "formattedAmount": "R$ 250,00",
  "dueDate": "2025-02-15",
  "status": "OPEN",
  "statusLabel": "Em aberto",
  "bankCode": "001",
  "walletCode": "01",
  "ourNumber": "0000001",
  "boletoCode": "00190500954014481606906809350314337370000000100",
  "userId": 1,
  "paidAt": null,
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:00:00"
}
```

**Campos da Resposta:**
- `boletoCode`: Código de barras de 48 dígitos
- `status`: `OPEN` (Em aberto), `OVERDUE` (Vencido), `PAID` (Pago)
- `formattedAmount`: Valor formatado em R$ (para exibição)

---

### 📋 Listar Todos os Boletos

**GET** `/api/form/boleto`

Retorna todos os boletos cadastrados.

**Resposta:**
```json
[
  {
    "id": 1,
    "title": "Conta de Luz",
    "amount": 250.00,
    "formattedAmount": "R$ 250,00",
    "dueDate": "2025-02-15",
    "status": "OPEN",
    "statusLabel": "Em aberto",
    "boletoCode": "00190500954014481606906809350314337370000000100",
    "userId": 1
  }
]
```

---

### 📋 Listar Boletos por Usuário

**GET** `/api/form/boleto/user/{userId}`

Retorna todos os boletos de um usuário específico.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
[
  {
    "id": 1,
    "title": "Conta de Luz",
    "amount": 250.00,
    "formattedAmount": "R$ 250,00",
    "dueDate": "2025-02-15",
    "status": "OPEN",
    "statusLabel": "Em aberto",
    "boletoCode": "00190500954014481606906809350314337370000000100",
    "userId": 1
  }
]
```

---

### 🔍 Buscar Boletos por CPF

**GET** `/api/form/boleto/cpf/{cpf}`

Retorna todos os boletos de um usuário pelo CPF (útil para pagamento).

**Parâmetros:**
- `cpf` (path): CPF do usuário (com ou sem formatação)

**Exemplo:**
- `/api/form/boleto/cpf/123.456.789-00`
- `/api/form/boleto/cpf/12345678900`

**Resposta:**
```json
[
  {
    "id": 1,
    "title": "Conta de Luz",
    "amount": 250.00,
    "formattedAmount": "R$ 250,00",
    "dueDate": "2025-02-15",
    "status": "OPEN",
    "statusLabel": "Em aberto",
    "boletoCode": "00190500954014481606906809350314337370000000100",
    "userId": 1
  }
]
```

---

### 🔍 Buscar Boletos por Status

**GET** `/api/form/boleto/status/{status}`

Retorna todos os boletos com um status específico.

**Parâmetros:**
- `status` (path): `OPEN`, `OVERDUE`, ou `PAID`

**Exemplo:**
- `/api/form/boleto/status/OPEN`
- `/api/form/boleto/status/PAID`

**Resposta:**
```json
[
  {
    "id": 1,
    "title": "Conta de Luz",
    "amount": 250.00,
    "formattedAmount": "R$ 250,00",
    "dueDate": "2025-02-15",
    "status": "OPEN",
    "statusLabel": "Em aberto",
    "boletoCode": "00190500954014481606906809350314337370000000100",
    "userId": 1
  }
]
```

---

## 9. Investimentos

### 💰 Criar Transação de Investimento

**POST** `/api/investments/transaction`

Cria uma transação de compra ou venda de ativos e atualiza automaticamente a posição do usuário.

**Request Body:**
```json
{
  "userId": 1,
  "assetId": 1,
  "type": "Compra",
  "price": "45.50",
  "quantity": "10",
  "status": "COMPLETED"
}
```

**Campos:**
- `type`: `"Compra"` ou `"Venda"`
- `price`: Preço por unidade (String)
- `quantity`: Quantidade de ativos (String)
- `status`: `"COMPLETED"` ou `"PENDING"` (opcional, padrão: `"COMPLETED"`)

**Resposta (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "assetId": 1,
  "type": "Compra",
  "price": 45.50,
  "quantity": 10.0,
  "totalValue": 455.00,
  "transactionDate": "2025-01-15T10:30:00",
  "status": "COMPLETED"
}
```

**Nota:** O sistema calcula automaticamente o preço médio ponderado em compras.

---

### 📜 Histórico de Transações

**GET** `/api/investments/history/user/{userId}`

Retorna todas as transações de investimento de um usuário, ordenadas por data (mais recente primeiro).

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
[
  {
    "id": 2,
    "userId": 1,
    "assetId": 1,
    "assetTicker": "PETR4",
    "assetName": "Petrobras PN",
    "type": "Venda",
    "price": 50.00,
    "quantity": 5.0,
    "totalValue": 250.00,
    "transactionDate": "2025-01-15T11:00:00",
    "status": "COMPLETED"
  },
  {
    "id": 1,
    "userId": 1,
    "assetId": 1,
    "assetTicker": "PETR4",
    "assetName": "Petrobras PN",
    "type": "Compra",
    "price": 45.50,
    "quantity": 10.0,
    "totalValue": 455.00,
    "transactionDate": "2025-01-15T10:30:00",
    "status": "COMPLETED"
  }
]
```

---

### 📊 Distribuição de Ativos

**GET** `/api/investments/distribution/user/{userId}`

Retorna a distribuição detalhada dos ativos do usuário, com valor total e percentual de cada ativo na carteira.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
[
  {
    "assetId": 1,
    "ticker": "PETR4",
    "assetName": "Petrobras PN",
    "categoryId": 1,
    "categoryName": "Ações BR",
    "quantity": 5.0,
    "averagePrice": 45.50,
    "totalValue": 227.50,
    "percentage": 45.50
  },
  {
    "assetId": 2,
    "ticker": "BTC",
    "assetName": "Bitcoin",
    "categoryId": 2,
    "categoryName": "Cripto",
    "quantity": 0.05,
    "averagePrice": 250000.00,
    "totalValue": 12500.00,
    "percentage": 54.50
  }
]
```

---

### 📊 Distribuição por Categoria

**GET** `/api/investments/distribution/user/{userId}/category`

Retorna a distribuição dos ativos agrupada por categoria.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
[
  {
    "categoryId": 2,
    "categoryName": "Cripto",
    "totalValue": 12500.00,
    "percentage": 54.50
  },
  {
    "categoryId": 1,
    "categoryName": "Ações BR",
    "totalValue": 227.50,
    "percentage": 45.50
  }
]
```

---

### 📈 Resumo do Portfólio

**GET** `/api/investments/portfolio/user/{userId}`

Retorna um resumo completo do portfólio, incluindo valor total, número de ativos, distribuição detalhada e por categoria.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta:**
```json
{
  "userId": 1,
  "totalPortfolioValue": 12727.50,
  "totalAssets": 2,
  "distribution": [
    {
      "assetId": 1,
      "ticker": "PETR4",
      "assetName": "Petrobras PN",
      "categoryId": 1,
      "categoryName": "Ações BR",
      "quantity": 5.0,
      "averagePrice": 45.50,
      "totalValue": 227.50,
      "percentage": 1.79
    },
    {
      "assetId": 2,
      "ticker": "BTC",
      "assetName": "Bitcoin",
      "categoryId": 2,
      "categoryName": "Cripto",
      "quantity": 0.05,
      "averagePrice": 250000.00,
      "totalValue": 12500.00,
      "percentage": 98.21
    }
  ],
  "distributionByCategory": [
    {
      "categoryId": 2,
      "categoryName": "Cripto",
      "totalValue": 12500.00,
      "percentage": 98.21
    },
    {
      "categoryId": 1,
      "categoryName": "Ações BR",
      "totalValue": 227.50,
      "percentage": 1.79
    }
  ]
}
```

---

## 10. Orçamentos

### 📊 Listar Orçamentos

**GET** `/api/budgets`

Lista todos os orçamentos ou filtra por parâmetros.

**Query Parameters:**
- `userId` (opcional): Filtra por usuário
- `categoryId` (opcional): Filtra por categoria
- `month` (opcional): Mês (1-12)
- `year` (opcional): Ano

**Exemplos:**
- `/api/budgets?userId=1`
- `/api/budgets?month=1&year=2025`

---

### 🔍 Buscar Orçamento por ID

**GET** `/api/budgets/{id}`

Retorna os dados de um orçamento específico.

---

### ➕ Criar Orçamento

**POST** `/api/budgets`

Cria um novo orçamento.

**Request Body:**
```json
{
  "userId": 1,
  "categoryId": 1,
  "amountLimit": 600.00,
  "periodMonth": 1,
  "periodYear": 2025
}
```

---

### ✏️ Atualizar Orçamento

**PUT** `/api/budgets/{id}`

Atualiza um orçamento existente.

---

### 🗑️ Deletar Orçamento

**DELETE** `/api/budgets/{id}`

Remove um orçamento.

---

## 11. Metas de Poupança

### 📊 Listar Metas de Poupança

**GET** `/api/savings-goals`

Lista todas as metas ou filtra por parâmetros.

**Query Parameters:**
- `userId` (opcional): Filtra por usuário
- `status` (opcional): Filtra por status (`IN_PROGRESS`, `COMPLETED`, `PAUSED`)

**Exemplos:**
- `/api/savings-goals?userId=1`
- `/api/savings-goals?userId=1&status=IN_PROGRESS`

---

### 🔍 Buscar Meta por ID

**GET** `/api/savings-goals/{id}`

Retorna os dados de uma meta específica.

---

### ➕ Criar Meta de Poupança

**POST** `/api/savings-goals`

Cria uma nova meta de poupança.

**Request Body:**
```json
{
  "userId": 1,
  "name": "Viagem para Europa",
  "targetAmount": 10000.00,
  "currentAmount": 0.00,
  "targetDate": "2026-12-31",
  "status": "IN_PROGRESS"
}
```

**Campos:**
- `status`: `IN_PROGRESS`, `COMPLETED`, ou `PAUSED`

---

### 💰 Adicionar Depósito à Meta

**POST** `/api/savings-goals/{id}/deposit`

Adiciona um valor à meta de poupança.

**Parâmetros:**
- `id` (path): ID da meta

**Request Body:**
```json
{
  "amount": 500.00
}
```

**Resposta (200 OK):**
```json
{
  "goalId": 1,
  "currentAmount": 500.00,
  "targetAmount": 10000.00,
  "progress": 5.0
}
```

---

### ✏️ Atualizar Meta

**PUT** `/api/savings-goals/{id}`

Atualiza uma meta de poupança.

---

### 🗑️ Deletar Meta

**DELETE** `/api/savings-goals/{id}`

Remove uma meta de poupança.

---

## 🔒 Códigos de Status HTTP

- **200 OK**: Requisição bem-sucedida
- **201 Created**: Recurso criado com sucesso
- **400 Bad Request**: Erro de validação ou requisição inválida
- **404 Not Found**: Recurso não encontrado
- **500 Internal Server Error**: Erro interno do servidor

---

## 📦 Formato de Respostas de Erro

Todas as respostas de erro seguem o formato:

```json
{
  "error": "Mensagem de erro descritiva",
  "status": 400
}
```

---

## 🔄 CORS

A API está configurada para aceitar requisições de qualquer origem (CORS habilitado).

**Headers configurados:**
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH`
- `Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, Accept, Origin`

---

## 📝 Observações Importantes

1. **Formato de Datas:**
   - Datas simples: `YYYY-MM-DD` (ex: `2025-01-15`)
   - Datas com hora: `YYYY-MM-DDTHH:mm:ss` (ex: `2025-01-15T14:30:00`)

2. **Formato de Valores Monetários:**
   - Enviar como números decimais (ex: `150.00`)
   - Valores de investimento podem ser enviados como strings (ex: `"45.50"`)

3. **IDs:**
   - Todos os IDs são inteiros numéricos

4. **CPF:**
   - Aceita com ou sem formatação: `123.456.789-00` ou `12345678900`

5. **CEP:**
   - Aceita com ou sem hífen: `01310-100` ou `01310100`

---

## 🚀 Exemplos de Uso com Fetch (JavaScript)

### Exemplo 1: Login

```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    emailOrCpf: 'usuario@email.com',
    password: 'senha123'
  })
});

const data = await response.json();
console.log(data);
```

### Exemplo 2: Buscar Endereço por CEP

```javascript
const zipCode = '01310100';
const response = await fetch(
  `http://localhost:8080/api/form/address/lookup?zipCode=${zipCode}`
);
const address = await response.json();
console.log(address);
```

### Exemplo 3: Criar Transação PIX

```javascript
const response = await fetch('http://localhost:8080/api/pix/transfer', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    sourceUserId: 1,
    destinationUserId: 2,
    amount: 100.00,
    description: 'Transferência PIX'
  })
});

const pixResult = await response.json();
console.log(pixResult);
```

### Exemplo 4: Listar Transações do Usuário

```javascript
const userId = 1;
const response = await fetch(
  `http://localhost:8080/api/transactions?userId=${userId}`
);
const transactions = await response.json();
console.log(transactions);
```

---

## 📞 Suporte

Para dúvidas ou problemas, consulte a documentação completa em `API_DOCUMENTATION.md`.

---

**Última atualização:** 2025-01-15

