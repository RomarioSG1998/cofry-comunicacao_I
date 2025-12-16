# Alterações Backend - Guia para Frontend

Este documento detalha todas as alterações recentes no backend que o frontend precisa conhecer para funcionar corretamente.

**Data da última atualização:** Janeiro 2025

---

## 📋 Sumário

1. [Novo Endpoint: Informações Completas do Usuário](#1-novo-endpoint-informações-completas-do-usuário)
2. [Correção: Exclusão de Contas Bancárias](#2-correção-exclusão-de-contas-bancárias)
3. [Configuração UTF-8 para Nomes de Bancos](#3-configuração-utf-8-para-nomes-de-bancos)
4. [Endpoints Disponíveis - Resumo](#4-endpoints-disponíveis---resumo)

---

## 1. Novo Endpoint: Informações Completas do Usuário

### 🆕 Endpoint Adicionado

**GET** `/api/users/{id}/complete`

Retorna todas as informações do usuário em uma única requisição, incluindo dados pessoais, endereços e contas bancárias.

### Uso no Frontend

```javascript
// Exemplo com fetch
async function getUserCompleteInfo(userId) {
  try {
    const response = await fetch(`http://localhost:8080/api/users/${userId}/complete`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.success) {
      const userData = result.data;
      
      // Dados pessoais
      console.log('User:', userData.firstName, userData.lastName);
      console.log('Email:', userData.email);
      
      // Endereços
      console.log('Addresses:', userData.addresses);
      
      // Contas bancárias
      console.log('Accounts:', userData.accounts);
      
      return userData;
    }
  } catch (error) {
    console.error('Erro ao buscar informações do usuário:', error);
    throw error;
  }
}

// Exemplo com Angular HttpClient
getUserCompleteInfo(userId: number): Observable<UserCompleteDTO> {
  return this.http.get<UserCompleteDTO>(
    `http://localhost:8080/api/users/${userId}/complete`
  ).pipe(
    map((response: any) => response.data || response),
    catchError(error => {
      console.error('Erro ao buscar informações do usuário:', error);
      return throwError(error);
    })
  );
}
```

### Estrutura da Resposta

```typescript
interface UserCompleteDTO {
  // Dados pessoais
  userId: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  cpf: string;
  phoneNumber: string | null;
  dateOfBirth: string; // ISO date format: "YYYY-MM-DD"
  isActive: boolean;
  planId: number;
  createdAt: string; // ISO datetime format
  updatedAt: string; // ISO datetime format
  
  // Endereços
  addresses: AddressResponseDTO[];
  
  // Contas bancárias
  accounts: AccountResponseDTO[];
}

interface AddressResponseDTO {
  addressId: number;
  userId: number;
  phoneNumber: string | null;
  zipCode: string;
  houseNumber: string;
  street: string;
  district: string;
  city: string;
  state: string;
  complement: string | null;
  country: string;
  createdAt: string; // ISO datetime format
}

interface AccountResponseDTO {
  accountId: number;
  userId: number;
  bankCode: string | null;
  bankName: string | null;
  agency: string;
  accountNumber: string;
  accountType: "CHECKING" | "SAVINGS";
  balance: number;
  status: string;
  createdAt: string; // ISO datetime format
}
```

### Comparação com Endpoint Existente

| Endpoint | Retorna | Quando Usar |
|----------|---------|-------------|
| `GET /api/users/{id}` | Apenas dados básicos do usuário | Quando você precisa apenas de informações pessoais simples |
| `GET /api/users/{id}/complete` | Dados completos + endereços + contas | Quando você precisa de todas as informações do usuário em uma única chamada (ex: página de perfil) |

### Exemplo de Resposta Completa

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "firstName": "João",
    "lastName": "Silva",
    "fullName": "João Silva",
    "email": "joao@example.com",
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

---

## 2. Correção: Exclusão de Contas Bancárias

### ⚠️ Mudança Importante

O endpoint de exclusão de contas (`DELETE /api/accounts/{id}`) agora verifica se existem transações relacionadas antes de permitir a exclusão.

### Comportamento Anterior vs. Novo

**Antes:**
- Tentava deletar e retornava erro genérico do banco de dados

**Agora:**
- Verifica se há transações relacionadas
- Se houver transações, retorna erro 400 com mensagem clara
- Se não houver transações, deleta normalmente

### Tratamento de Erro no Frontend

```typescript
// Exemplo de tratamento de erro
async function deleteAccount(accountId: number): Promise<void> {
  try {
    const response = await fetch(`http://localhost:8080/api/accounts/${accountId}`, {
      method: 'DELETE'
    });
    
    if (!response.ok) {
      const errorData = await response.json();
      
      // Erro 400: Conta tem transações relacionadas
      if (response.status === 400) {
        throw new Error(errorData.error || 'Não é possível remover a conta. Existem transações vinculadas.');
      }
      
      // Outros erros
      throw new Error(errorData.error || 'Erro ao remover conta');
    }
    
    // Sucesso
    console.log('Conta removida com sucesso');
  } catch (error) {
    console.error('Erro ao deletar conta:', error);
    // Mostrar mensagem de erro ao usuário
    alert(error.message);
  }
}
```

### Mensagens de Erro Esperadas

```json
// Erro quando há transações relacionadas (400 Bad Request)
{
  "error": "Não é possível remover a conta. Existem transações vinculadas a esta conta. Remova as transações primeiro ou desative a conta em vez de removê-la.",
  "status": 400
}

// Erro quando conta não encontrada (404 Not Found)
{
  "error": "Conta não encontrada com ID: {id}",
  "status": 404
}

// Sucesso (200 OK)
{
  "success": true,
  "data": "Conta removida com sucesso"
}
```

### Recomendações para o Frontend

1. **Antes de deletar, avise o usuário:**
   ```typescript
   const confirmDelete = confirm(
     'Tem certeza que deseja remover esta conta? ' +
     'Se houver transações relacionadas, a exclusão não será permitida.'
   );
   ```

2. **Mostre mensagem específica quando houver transações:**
   ```typescript
   if (error.message.includes('transações vinculadas')) {
     // Mostrar opção de desativar conta em vez de deletar
     showDeactivateOption(accountId);
   }
   ```

3. **Considere oferecer desativação em vez de exclusão:**
   - Use `PUT /api/accounts/{id}` para alterar o `status` para `"INACTIVE"`
   - Isso permite manter o histórico de transações

---

## 3. Configuração UTF-8 para Nomes de Bancos

### ✅ Correção Implementada

O backend agora está configurado para salvar e retornar nomes de bancos corretamente em UTF-8, suportando caracteres especiais como acentos.

### Impacto no Frontend

**Nenhuma mudança necessária no código do frontend**, mas agora os nomes de bancos serão exibidos corretamente.

### Exemplos de Nomes de Bancos que Funcionam

```
✅ "Banco do Brasil"
✅ "Banco Itaú"
✅ "Banco Bradesco"
✅ "Caixa Econômica Federal"
```

### Garantias

- ✅ Encoding UTF-8 em todas as respostas HTTP
- ✅ Nomes de bancos salvos corretamente no banco de dados
- ✅ Caracteres especiais (acentos, ç, etc.) funcionam perfeitamente

---

## 4. Endpoints Disponíveis - Resumo

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/users` | Lista todos os usuários |
| GET | `/api/users/{id}` | Busca usuário por ID (dados básicos) |
| **GET** | **`/api/users/{id}/complete`** | **🆕 Busca usuário por ID (dados completos + endereços + contas)** |
| POST | `/api/users` | Cria novo usuário |
| PUT | `/api/users/{id}` | Atualiza usuário |

### Contas Bancárias

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/accounts` | Lista todas as contas |
| GET | `/api/accounts/{id}` | Busca conta por ID |
| POST | `/api/accounts` | Cria nova conta |
| PUT | `/api/accounts/{id}` | Atualiza conta |
| PUT | `/api/accounts/{id}/balance` | Define saldo da conta |
| DELETE | `/api/accounts/{id}` | **⚠️ Remove conta (verifica transações)** |

---

## 📝 Checklist para o Frontend

Use este checklist para garantir que seu código está atualizado:

- [ ] Atualizar chamadas para buscar dados do usuário para usar `/api/users/{id}/complete` quando necessário
- [ ] Implementar tratamento de erro 400 ao deletar contas (transações relacionadas)
- [ ] Considerar oferecer opção de desativar conta em vez de deletar
- [ ] Verificar que nomes de bancos são exibidos corretamente (UTF-8 já configurado no backend)
- [ ] Testar fluxo completo de exclusão de contas

---

## 🔗 Referências

- **Base URL:** `http://localhost:8080`
- **Documentação Completa de Rotas:** Ver arquivo `FRONTEND_API_ROUTES.md`
- **Formato de Respostas:** Todas as respostas seguem o padrão:
  ```json
  {
    "success": true,
    "data": { /* dados aqui */ }
  }
  ```
  ou para erros:
  ```json
  {
    "error": "Mensagem de erro",
    "status": 400
  }
  ```

---

## 💡 Dicas de Implementação

### 1. Service Pattern (Angular/React)

Crie um service para gerenciar chamadas de API:

```typescript
// user.service.ts
@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
  
  getUserBasic(userId: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/${userId}`);
  }
  
  getUserComplete(userId: number): Observable<UserCompleteDTO> {
    return this.http.get<UserCompleteDTO>(`${API_URL}/users/${userId}/complete`);
  }
}
```

### 2. Error Handling Global

Configure um interceptor para tratamento global de erros:

```typescript
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 400) {
          // Tratar erro de validação/regra de negócio
          console.error('Erro de validação:', error.error.error);
        }
        return throwError(error);
      })
    );
  }
}
```

### 3. TypeScript Interfaces

Defina interfaces TypeScript para tipagem forte:

```typescript
export interface UserCompleteDTO {
  userId: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  cpf: string;
  phoneNumber: string | null;
  dateOfBirth: string;
  isActive: boolean;
  planId: number;
  createdAt: string;
  updatedAt: string;
  addresses: AddressResponseDTO[];
  accounts: AccountResponseDTO[];
}
```

---

## ❓ Dúvidas ou Problemas?

Se encontrar algum problema ou tiver dúvidas sobre essas alterações:

1. Verifique se está usando a versão mais recente do backend
2. Confirme que a base URL está correta: `http://localhost:8080`
3. Verifique os logs do backend para erros detalhados
4. Consulte o arquivo `FRONTEND_API_ROUTES.md` para documentação completa de todos os endpoints

---

**Última atualização:** Janeiro 2025


