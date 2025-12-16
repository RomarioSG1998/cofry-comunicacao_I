# Documentação da API REST - Cofry Backend

API REST completa para gerenciamento financeiro usando Servlets, padrão DAO e comunicação JSON.

## Estrutura do Projeto

```
Cofry-Backend2/
├── src/main/java/org/example/
│   ├── model/          # Entidades JPA
│   ├── dao/            # Data Access Objects
│   ├── service/        # Camada de serviços (lógica de negócio)
│   ├── controller/     # Servlets (endpoints REST)
│   ├── persistence/    # Gerenciamento de conexão
│   ├── utils/          # Utilitários (validação, criptografia)
│   └── config/         # Configurações
└── src/main/webapp/
    └── WEB-INF/
        └── web.xml     # Configuração do servlet container
```

## Endpoints da API

### 1. Usuários (Users)

**Base URL:** `/api/users`

#### GET `/api/users`
Lista todos os usuários.

**Resposta:**
```json
[
  {
    "userId": 1,
    "planId": 2,
    "firstName": "Jao",
    "lastName": "Silva",
    "taxId": "123.456.789-00",
    "email": "jao.silva@cofry.com",
    "phoneNumber": null,
    "passwordHash": "hash_seguro_123",
    "dateOfBirth": "1998-05-20",
    "isActive": true,
    "createdAt": "2025-10-01T10:00:00",
    "updatedAt": "2025-10-01T10:00:00"
  }
]
```

#### GET `/api/users/{id}`
Busca um usuário por ID.

#### POST `/api/users`
Cria um novo usuário.

**Body:**
```json
{
  "firstName": "Maria",
  "lastName": "Santos",
  "taxId": "987.654.321-00",
  "email": "maria@example.com",
  "dateOfBirth": "1995-03-15",
  "planId": 1
}
```

#### PUT `/api/users/{id}`
Atualiza um usuário existente.

#### DELETE `/api/users/{id}`
Remove um usuário.

---

### 2. Contas Bancárias (Accounts)

**Base URL:** `/api/accounts`

#### GET `/api/accounts`
Lista todas as contas bancárias.

#### GET `/api/accounts/{id}`
Busca uma conta por ID.

#### POST `/api/accounts`
Cria uma nova conta.

**Body:**
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

#### PUT `/api/accounts/{id}`
Atualiza uma conta.

#### DELETE `/api/accounts/{id}`
Remove uma conta.

---

### 3. Transações (Transactions)

**Base URL:** `/api/transactions`

#### GET `/api/transactions`
Lista todas as transações ou filtra por parâmetros:
- `?accountId=1` - Filtra por conta de origem
- `?type=PAYMENT` - Filtra por tipo (DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT)
- `?categoryId=1` - Filtra por categoria
- `?startDate=2025-10-01&endDate=2025-10-31` - Filtra por período

#### GET `/api/transactions/{id}`
Busca uma transação por ID.

#### POST `/api/transactions`
Cria uma nova transação.

**Body:**
```json
{
  "sourceAccountId": 1,
  "destinationAccountId": null,
  "categoryId": 1,
  "amount": 150.00,
  "transactionType": "PAYMENT",
  "description": "Supermercado",
  "transactionDate": "2025-10-15",
  "isRecurring": false,
  "installmentCurrent": null,
  "installmentTotal": null
}
```

#### PUT `/api/transactions/{id}`
Atualiza uma transação.

#### DELETE `/api/transactions/{id}`
Remove uma transação.

---

### 4. Orçamentos (Budgets)

**Base URL:** `/api/budgets`

#### GET `/api/budgets`
Lista todos os orçamentos ou filtra:
- `?userId=1` - Por usuário
- `?categoryId=1` - Por categoria
- `?month=10&year=2025` - Por período

#### GET `/api/budgets/{id}`
Busca um orçamento por ID.

#### POST `/api/budgets`
Cria um novo orçamento.

**Body:**
```json
{
  "userId": 1,
  "categoryId": 1,
  "amountLimit": 600.00,
  "periodMonth": 10,
  "periodYear": 2025
}
```

#### PUT `/api/budgets/{id}`
Atualiza um orçamento.

#### DELETE `/api/budgets/{id}`
Remove um orçamento.

---

### 5. Metas de Poupança (Savings Goals)

**Base URL:** `/api/savings-goals`

#### GET `/api/savings-goals`
Lista todas as metas ou filtra:
- `?userId=1` - Por usuário
- `?status=IN_PROGRESS` - Por status (IN_PROGRESS, COMPLETED, PAUSED)
- `?userId=1&status=IN_PROGRESS` - Por usuário e status

#### GET `/api/savings-goals/{id}`
Busca uma meta por ID.

#### POST `/api/savings-goals`
Cria uma nova meta.

**Body:**
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

#### POST `/api/savings-goals/{id}/deposit`
Adiciona valor à meta (depósito).

**Body:**
```json
{
  "amount": 500.00
}
```

#### PUT `/api/savings-goals/{id}`
Atualiza uma meta.

#### DELETE `/api/savings-goals/{id}`
Remove uma meta.

---

## Respostas de Erro

Todas as respostas de erro seguem o formato:

```json
{
  "error": "Mensagem de erro",
  "status": 400
}
```

### Códigos HTTP

- `200 OK` - Sucesso
- `201 Created` - Recurso criado com sucesso
- `400 Bad Request` - Erro de validação ou requisição inválida
- `404 Not Found` - Recurso não encontrado
- `500 Internal Server Error` - Erro interno do servidor

---

## Exemplos de Uso

### Criar um usuário

```bash
curl -X POST http://localhost:8080/Cofry-Backend2/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "João",
    "lastName": "Silva",
    "taxId": "123.456.789-00",
    "email": "joao@example.com",
    "dateOfBirth": "1990-01-01"
  }'
```

### Listar todas as transações de uma conta

```bash
curl http://localhost:8080/Cofry-Backend2/api/transactions?accountId=1
```

### Atualizar uma transação

```bash
curl -X PUT http://localhost:8080/Cofry-Backend2/api/transactions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200.00,
    "description": "Descrição atualizada"
  }'
```

### Adicionar depósito a uma meta

```bash
curl -X POST http://localhost:8080/Cofry-Backend2/api/savings-goals/1/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00
  }'
```

---

## Configuração e Deploy

### 1. Compilar o projeto

```bash
mvn clean package
```

### 2. Deploy no Tomcat

Copie o arquivo `target/Cofry-Backend2-1.0-SNAPSHOT.war` para o diretório `webapps` do Tomcat.

### 3. Acessar a API

```
http://localhost:8080/Cofry-Backend2/api/users
```

---

## Tecnologias Utilizadas

- **Java 21**
- **Servlets API 4.0** (javax.servlet)
- **JPA/Hibernate 5.6** (javax.persistence)
- **PostgreSQL**
- **Gson 2.10.1** (JSON)
- **Maven**
- **Apache Tomcat 9+**

---

## Estrutura MVC Completa

```
┌─────────────┐
│  Controller │  ← Servlets (@WebServlet)
│  (REST API) │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  ← Lógica de Negócio
└──────┬──────┘
       │
       ▼
┌─────────────┐
│     DAO     │  ← Acesso a Dados
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Model    │  ← Entidades JPA
└─────────────┘
```

---

## Endpoints de Investimentos

### 1. Criar Transação de Investimento

**POST** `/api/investments/transaction`

Cria uma nova transação de investimento (Compra ou Venda) e atualiza automaticamente a posição do usuário.

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
- `userId` (Integer, obrigatório): ID do usuário
- `assetId` (Integer, obrigatório): ID do ativo
- `type` (String, obrigatório): Tipo da transação - "Compra" ou "Venda"
- `price` (String, obrigatório): Preço por unidade do ativo
- `quantity` (String, obrigatório): Quantidade de ativos
- `status` (String, opcional): Status da transação (padrão: "COMPLETED")

**Resposta (201 Created):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 1,
    "assetId": 1,
    "type": "Compra",
    "price": 45.50,
    "quantity": 10.0,
    "totalValue": 455.00,
    "transactionDate": "2025-12-15T10:30:00",
    "status": "COMPLETED"
  }
}
```

**Comportamento:**
- **Compra**: Adiciona à posição do usuário e recalcula o preço médio ponderado
- **Venda**: Subtrai da posição do usuário (valida quantidade disponível)

---

### 2. Histórico de Transações

**GET** `/api/investments/history/user/{userId}`

Retorna todas as transações de investimento de um usuário, ordenadas por data (mais recente primeiro).

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
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
      "transactionDate": "2025-12-15T11:00:00",
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
      "transactionDate": "2025-12-15T10:30:00",
      "status": "COMPLETED"
    }
  ]
}
```

**Campos da Resposta:**
- `id`: ID da transação
- `userId`: ID do usuário
- `assetId`: ID do ativo
- `assetTicker`: Código do ativo (ex: PETR4, BTC)
- `assetName`: Nome completo do ativo
- `type`: Tipo da transação ("Compra" ou "Venda")
- `price`: Preço por unidade
- `quantity`: Quantidade negociada
- `totalValue`: Valor total da transação (price × quantity)
- `transactionDate`: Data e hora da transação
- `status`: Status da transação (ex: "COMPLETED", "PENDING")

---

### 3. Distribuição de Ativos

**GET** `/api/investments/distribution/user/{userId}`

Retorna a distribuição detalhada dos ativos do usuário, com valor total e percentual de cada ativo na carteira.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
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
}
```

**Campos da Resposta:**
- `assetId`: ID do ativo
- `ticker`: Código do ativo (ex: PETR4, BTC)
- `assetName`: Nome do ativo
- `categoryId`: ID da categoria
- `categoryName`: Nome da categoria
- `quantity`: Quantidade na carteira
- `averagePrice`: Preço médio de compra
- `totalValue`: Valor total da posição (quantidade × preço médio)
- `percentage`: Percentual da carteira (0-100)

---

### 4. Distribuição por Categoria

**GET** `/api/investments/distribution/user/{userId}/category`

Retorna a distribuição dos ativos agrupada por categoria, útil para análise de diversificação.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
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
}
```

---

### 5. Resumo Completo do Portfólio

**GET** `/api/investments/portfolio/user/{userId}`

Retorna um resumo completo do portfólio, incluindo valor total, número de ativos, distribuição detalhada e por categoria.

**Parâmetros:**
- `userId` (path): ID do usuário

**Resposta (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "userId": 1,
    "totalPortfolioValue": 25000.00,
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
        "percentage": 45.50
      }
    ],
    "distributionByCategory": [
      {
        "categoryId": 2,
        "categoryName": "Cripto",
        "totalValue": 12500.00,
        "percentage": 54.50
      }
    ]
  }
}
```

**Campos da Resposta:**
- `userId`: ID do usuário
- `totalPortfolioValue`: Valor total da carteira
- `totalAssets`: Número de ativos únicos na carteira
- `distribution`: Lista detalhada de cada ativo (mesmo formato do endpoint de distribuição)
- `distributionByCategory`: Agrupamento por categoria (mesmo formato do endpoint de distribuição por categoria)

---

## Observações Importantes

### Cálculo de Preço Médio

Ao realizar uma **compra**, o sistema recalcula automaticamente o preço médio ponderado:

```
Preço Médio = (Quantidade Anterior × Preço Médio Anterior + Quantidade Nova × Preço Nova) / (Quantidade Total)
```

### Validações

- **Venda**: Não permite vender mais ativos do que o usuário possui
- **Quantidade**: Deve ser maior que zero
- **Preço**: Deve ser maior que zero
- **Tipo**: Apenas "Compra" ou "Venda" são aceitos

### Transações Atômicas

Todas as operações são executadas em transações de banco de dados, garantindo:
- Consistência dos dados
- Rollback automático em caso de erro
- Atualização simultânea de transação e posição

---

## Exemplos de Uso

### Exemplo 1: Comprar Ações

```bash
curl -X POST http://localhost:8080/api/investments/transaction \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "assetId": 1,
    "type": "Compra",
    "price": "45.50",
    "quantity": "10",
    "status": "COMPLETED"
  }'
```

### Exemplo 2: Consultar Histórico

```bash
curl -X GET http://localhost:8080/api/investments/history/user/1
```

### Exemplo 3: Ver Distribuição de Ativos

```bash
curl -X GET http://localhost:8080/api/investments/distribution/user/1
```

### Exemplo 4: Ver Resumo do Portfólio

```bash
curl -X GET http://localhost:8080/api/investments/portfolio/user/1
```

