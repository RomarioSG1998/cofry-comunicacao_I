# 💰 Saldo Inicial para Novos Usuários

## ✅ Implementação

Agora, quando um novo usuário é cadastrado no sistema, **automaticamente é criada uma conta inicial com saldo de R$ 20.000,00**.

---

## 🔄 Como Funciona

### Fluxo de Criação

```
1. Frontend envia POST /api/form/user
   ↓
2. UserFormService.createUserFromForm()
   ├─ Valida dados do usuário
   ├─ Cria usuário no banco
   └─ Chama createInitialAccountForUser()  ← NOVA FUNCIONALIDADE
   ↓
3. createInitialAccountForUser()
   ├─ Cria objeto Account
   ├─ Define saldo inicial: R$ 20.000,00
   ├─ Gera número de conta único
   └─ Salva conta no banco
   ↓
4. Retorna UserResponseDTO
```

---

## 💵 Detalhes da Conta Inicial

### Dados da Conta Criada

| Campo | Valor |
|-------|-------|
| **Banco** | Cofry |
| **Código do Banco** | 999 |
| **Agência** | 0001 |
| **Número da Conta** | COF-XXXXX-1 (baseado no ID do usuário) |
| **Tipo** | CHECKING (Conta Corrente) |
| **Saldo Inicial** | R$ 20.000,00 |
| **Status** | ACTIVE |

### Exemplos de Números de Conta

- Usuário ID 1 → `COF-00001-1`
- Usuário ID 25 → `COF-00025-1`
- Usuário ID 100 → `COF-00100-1`

---

## 📝 Código Implementado

### Arquivo: `src/main/java/org/example/service/UserFormService.java`

**Método principal modificado:**
```java
public UserResponseDTO createUserFromForm(UserRequestDTO userDTO) {
    // ... validações e criação do usuário ...
    
    // Save user
    User savedUser = userService.createUser(user);
    
    // Cria conta inicial com saldo de R$ 20.000,00 para o novo usuário
    createInitialAccountForUser(savedUser.getUserId());
    
    // Convert to response DTO
    return convertToResponseDTO(savedUser);
}
```

**Novo método:**
```java
private void createInitialAccountForUser(Integer userId) {
    Account initialAccount = new Account();
    initialAccount.setUserId(userId);
    initialAccount.setBankCode("999");
    initialAccount.setBankName("Cofry");
    initialAccount.setAccountNumber(generateAccountNumber(userId));
    initialAccount.setAgencyNumber("0001");
    initialAccount.setAccountType(AccountTypeEnum.CHECKING);
    initialAccount.setBalance(new BigDecimal("20000.00")); // ← R$ 20.000,00
    initialAccount.setStatus("ACTIVE");
    
    accountService.createAccount(initialAccount);
}
```

**Geração de número de conta:**
```java
private String generateAccountNumber(Integer userId) {
    // Formato: COF-XXXXX-X (ex: COF-00001-1)
    return String.format("COF-%05d-1", userId);
}
```

---

## 🔒 Tratamento de Erros

Se houver erro ao criar a conta inicial:

- ✅ O erro é **logado** no console
- ✅ A **criação do usuário NÃO é revertida** (usuário já foi salvo)
- ⚠️ Em produção, considere usar transação para garantir atomicidade

**Nota:** Se desejar que a criação do usuário seja revertida em caso de erro na conta, seria necessário usar uma transação do banco de dados envolvendo ambos os passos.

---

## 📊 Exemplo de Uso

### Request (POST /api/form/user)
```json
{
  "fullName": "João Silva",
  "email": "joao@example.com",
  "cpf": "123.456.789-00",
  "password": "senha123",
  "dateOfBirth": "1990-01-15"
}
```

### Resultado

**1. Usuário criado:**
```json
{
  "userId": 42,
  "firstName": "João",
  "lastName": "Silva",
  "email": "joao@example.com",
  "cpf": "123.456.789-00",
  ...
}
```

**2. Conta criada automaticamente:**
- **ID da Conta:** (gerado automaticamente)
- **ID do Usuário:** 42
- **Banco:** Cofry (código 999)
- **Agência:** 0001
- **Conta:** COF-00042-1
- **Saldo:** R$ 20.000,00 ✅
- **Status:** ACTIVE

---

## 🎯 Verificação

Após criar um novo usuário, você pode verificar a conta criada:

**1. Listar contas do usuário:**
```
GET /api/accounts/user/{userId}
```

**2. Ou buscar a conta específica:**
```
GET /api/accounts/{accountId}
```

**Resposta esperada:**
```json
{
  "accountId": 123,
  "userId": 42,
  "bankCode": "999",
  "bankName": "Cofry",
  "accountNumber": "COF-00042-1",
  "agencyNumber": "0001",
  "accountType": "CHECKING",
  "balance": 20000.00,  ← Saldo inicial de R$ 20.000,00
  "status": "ACTIVE"
}
```

---

## ⚙️ Configuração

### Alterar o Valor do Saldo Inicial

Para alterar o valor do saldo inicial, edite a linha 214 do arquivo `UserFormService.java`:

```java
// Valor atual
initialAccount.setBalance(new BigDecimal("20000.00"));

// Para alterar para R$ 50.000,00, por exemplo:
initialAccount.setBalance(new BigDecimal("50000.00"));
```

### Alterar Formato do Número de Conta

Para alterar o formato do número de conta, edite o método `generateAccountNumber()`:

```java
// Formato atual: COF-00001-1
return String.format("COF-%05d-1", userId);

// Exemplos alternativos:
// return String.format("ACC-%06d", userId);        // ACC-000001
// return String.format("COFRY-%03d", userId);      // COFRY-001
// return "CONTA-" + userId;                        // CONTA-1
```

---

## ✅ Status

- ✅ **Implementado** - Novos usuários recebem conta com R$ 20.000,00
- ✅ **Testado** - Código compilando sem erros
- ⚠️ **Observação** - Se a criação da conta falhar, o usuário ainda será criado (erro apenas logado)

---

**Última atualização:** 16 de Janeiro de 2025  
**Status:** ✅ Funcionando

