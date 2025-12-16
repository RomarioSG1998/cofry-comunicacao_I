# Alterações - Relacionamento Usuário x Endereços e Contas

## 📋 Resumo

Foram implementadas alterações para garantir que o sistema sempre filtre endereços e contas bancárias pelo usuário relacionado, estabelecendo uma separação clara dos dados por usuário.

---

## 🔧 Alterações Implementadas

### 1. **AccountServlet - Filtro por Usuário**

**Arquivo:** `src/main/java/org/example/controller/AccountServlet.java`

**Mudanças:**
- `GET /api/accounts` agora **requer** o parâmetro `userId` (query parameter)
  - **Antes:** Retornava todas as contas do sistema
  - **Agora:** Retorna apenas contas do usuário especificado
  - **Uso:** `GET /api/accounts?userId=1`

- Novo endpoint: `GET /api/accounts/user/{userId}`
  - Alternativa para listar contas do usuário via path parameter
  - **Uso:** `GET /api/accounts/user/1`

**Endpoints Disponíveis:**
```
GET    /api/accounts?userId={userId}  - Lista contas do usuário (userId obrigatório)
GET    /api/accounts/user/{userId}    - Lista contas do usuário (alternativa)
GET    /api/accounts/{id}             - Busca conta por ID
POST   /api/accounts                  - Cria nova conta
PUT    /api/accounts/{id}             - Atualiza conta
PUT    /api/accounts/{id}/balance     - Define saldo da conta
DELETE /api/accounts/{id}             - Remove conta
```

**Exemplo de Erro:**
```json
// GET /api/accounts (sem userId)
{
  "error": "Parâmetro userId é obrigatório",
  "status": 400
}
```

---

### 2. **AddressServlet - Novo Servlet Criado**

**Arquivo:** `src/main/java/org/example/controller/AddressServlet.java` (NOVO)

**Funcionalidades:**
- `GET /api/addresses?userId={userId}` - Lista endereços do usuário (userId obrigatório)
- `GET /api/addresses/user/{userId}` - Lista endereços do usuário (alternativa)
- `GET /api/addresses/{id}` - Busca endereço por ID
- `POST /api/addresses` - Cria novo endereço
- `PUT /api/addresses/{id}` - Atualiza endereço
- `DELETE /api/addresses/{id}` - Remove endereço

**Nota:** O `AddressFormServlet` continua funcionando normalmente para formulários (`/api/form/address`).

---

### 3. **AccountService - Método de Validação**

**Arquivo:** `src/main/java/org/example/service/AccountService.java`

**Novo Método:**
```java
public Account getAccountByIdAndUserId(Integer id, Integer userId)
```

**Funcionalidade:**
- Busca uma conta por ID e valida se ela pertence ao usuário especificado
- Lança exceção se a conta não existir ou não pertencer ao usuário
- Útil para garantir segurança em operações sensíveis

---

### 4. **AddressService - Método de Validação**

**Arquivo:** `src/main/java/org/example/service/AddressService.java`

**Novo Método:**
```java
public Address getAddressByIdAndUserId(Integer id, Integer userId)
```

**Funcionalidade:**
- Busca um endereço por ID e valida se ele pertence ao usuário especificado
- Lança exceção se o endereço não existir ou não pertencer ao usuário
- Útil para garantir segurança em operações sensíveis

---

## 🔒 Segurança e Relacionamentos

### Relacionamentos no Banco de Dados

Os relacionamentos já estavam corretamente configurados:

**Tabela `addresses`:**
- `user_id INT NOT NULL`
- Foreign Key: `fk_user_address` → `users(user_id) ON DELETE CASCADE`
- Quando um usuário é deletado, seus endereços são automaticamente removidos

**Tabela `accounts`:**
- `user_id INT NOT NULL`
- Foreign Key: `fk_user_account` → `users(user_id) ON DELETE RESTRICT`
- Quando um usuário é deletado, a operação é bloqueada se houver contas relacionadas

---

## 📝 Exemplos de Uso

### Buscar Contas do Usuário

**Opção 1 - Query Parameter:**
```bash
GET http://localhost:8080/api/accounts?userId=1
```

**Opção 2 - Path Parameter:**
```bash
GET http://localhost:8080/api/accounts/user/1
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "accountId": 1,
      "userId": 1,
      "bankCode": "001",
      "bankName": "Banco do Brasil",
      "accountNumber": "12345-6",
      "agency": "1234",
      "accountType": "CHECKING",
      "balance": 1000.00,
      "status": "ACTIVE",
      "createdAt": "2025-01-15T10:00:00"
    }
  ]
}
```

### Buscar Endereços do Usuário

**Opção 1 - Query Parameter:**
```bash
GET http://localhost:8080/api/addresses?userId=1
```

**Opção 2 - Path Parameter:**
```bash
GET http://localhost:8080/api/addresses/user/1
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "addressId": 1,
      "userId": 1,
      "zipCode": "01310-100",
      "houseNumber": "123",
      "street": "Avenida Paulista",
      "district": "Bela Vista",
      "city": "São Paulo",
      "state": "SP",
      "complement": "Apto 101",
      "country": "Brazil",
      "createdAt": "2025-01-15T10:00:00"
    }
  ]
}
```

### Erro ao Buscar sem userId

```bash
GET http://localhost:8080/api/accounts
```

**Resposta:**
```json
{
  "error": "Parâmetro userId é obrigatório",
  "status": 400
}
```

---

## ⚠️ Importante para o Frontend

### Mudanças de Breaking Change

1. **`GET /api/accounts`** agora **requer** o parâmetro `userId`
   - **Antes:** Retornava todas as contas
   - **Agora:** Retorna apenas contas do usuário especificado
   - **Ação necessária:** Atualizar todas as chamadas para incluir `?userId={userId}`

2. **Novo endpoint:** `GET /api/addresses` para listar endereços
   - **Antes:** Não existia endpoint REST padrão para endereços
   - **Agora:** Disponível com filtro obrigatório por usuário
   - **Nota:** O `AddressFormServlet` (`/api/form/address`) continua funcionando normalmente

### Recomendações

1. **Sempre passar userId:**
   - Use `GET /api/accounts?userId={userId}` ou `GET /api/accounts/user/{userId}`
   - Use `GET /api/addresses?userId={userId}` ou `GET /api/addresses/user/{userId}`

2. **Alternativa - Endpoint Completo do Usuário:**
   - Considere usar `GET /api/users/{id}/complete` que já retorna usuário + endereços + contas em uma única chamada

3. **Validação de Propriedade:**
   - Ao buscar por ID (`GET /api/accounts/{id}` ou `GET /api/addresses/{id}`), não há validação automática de propriedade
   - Para operações sensíveis, considere usar os métodos `getAccountByIdAndUserId()` ou `getAddressByIdAndUserId()` no backend

---

## ✅ Checklist de Implementação no Frontend

- [ ] Atualizar chamadas para `GET /api/accounts` para incluir `?userId={userId}`
- [ ] Atualizar ou criar chamadas para `GET /api/addresses` com `?userId={userId}`
- [ ] Testar que apenas dados do usuário logado são retornados
- [ ] Tratar erro 400 quando `userId` não for fornecido
- [ ] Considerar usar `GET /api/users/{id}/complete` para dados completos do usuário

---

## 🔄 Compatibilidade

### Endpoints que NÃO mudaram:

- ✅ `POST /api/accounts` - Continua funcionando normalmente
- ✅ `PUT /api/accounts/{id}` - Continua funcionando normalmente
- ✅ `DELETE /api/accounts/{id}` - Continua funcionando normalmente
- ✅ `GET /api/accounts/{id}` - Continua funcionando normalmente (busca por ID)
- ✅ `POST /api/form/address` - Continua funcionando normalmente
- ✅ `GET /api/form/address/*` - Continua funcionando normalmente

### Endpoints que mudaram:

- ⚠️ `GET /api/accounts` - Agora requer `userId` (BREAKING CHANGE)
- ➕ `GET /api/addresses` - Novo endpoint (requer `userId`)

---

**Data da Alteração:** Janeiro 2025
**Status:** ✅ Implementado e Testado

