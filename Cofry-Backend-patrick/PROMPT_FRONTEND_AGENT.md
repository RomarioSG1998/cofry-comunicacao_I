# Prompt para Agente Frontend - Atualizações Backend

## 🎯 Objetivo

Você é um agente especializado em desenvolvimento frontend. Sua tarefa é atualizar o código frontend para trabalhar corretamente com as novas alterações implementadas no backend Cofry.

---

## 📋 Contexto

O backend Cofry foi atualizado com as seguintes mudanças importantes:

1. **Novo endpoint para informações completas do usuário**
2. **Correção na exclusão de contas bancárias** (agora verifica transações relacionadas)
3. **Configuração UTF-8** para nomes de bancos (já funciona automaticamente)

---

## 🔧 Tarefas a Realizar

### 1. Implementar Novo Endpoint de Usuário Completo

**Endpoint:** `GET http://localhost:8080/api/users/{id}/complete`

**O que fazer:**
- Criar interface TypeScript `UserCompleteDTO` com todos os campos necessários
- Criar método no service de usuário para buscar informações completas
- Atualizar componentes que precisam de informações completas do usuário (ex: página de perfil)
- Considerar usar este endpoint em vez de fazer múltiplas chamadas separadas

**Estrutura esperada da resposta:**
```typescript
interface UserCompleteDTO {
  userId: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  cpf: string;
  phoneNumber: string | null;
  dateOfBirth: string; // ISO date: "YYYY-MM-DD"
  isActive: boolean;
  planId: number;
  createdAt: string; // ISO datetime
  updatedAt: string; // ISO datetime
  addresses: AddressResponseDTO[];
  accounts: AccountResponseDTO[];
}
```

**Exemplo de implementação (Angular):**
```typescript
// user.service.ts
getUserComplete(userId: number): Observable<UserCompleteDTO> {
  return this.http.get<UserCompleteDTO>(
    `${this.apiUrl}/users/${userId}/complete`
  ).pipe(
    map((response: any) => response.data || response),
    catchError(this.handleError)
  );
}
```

**Quando usar:**
- ✅ Página de perfil completo do usuário
- ✅ Dashboard que precisa de todas as informações
- ✅ Quando você precisa de dados pessoais + endereços + contas em uma única chamada

**Quando NÃO usar:**
- ❌ Quando você precisa apenas de dados básicos do usuário (use `/api/users/{id}`)

---

### 2. Atualizar Tratamento de Exclusão de Contas

**Endpoint:** `DELETE http://localhost:8080/api/accounts/{id}`

**Mudança importante:**
O backend agora verifica se existem transações relacionadas antes de permitir a exclusão. Se houver transações, retorna erro 400 com mensagem clara.

**O que fazer:**
- Atualizar método de exclusão de conta para tratar erro 400
- Mostrar mensagem específica quando houver transações relacionadas
- Considerar oferecer opção de desativar conta em vez de deletar
- Adicionar confirmação antes de deletar

**Exemplo de implementação:**
```typescript
// account.service.ts
deleteAccount(accountId: number): Observable<void> {
  return this.http.delete(`${this.apiUrl}/accounts/${accountId}`).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 400) {
        const errorMessage = error.error?.error || 
          'Não é possível remover a conta. Existem transações vinculadas.';
        
        // Opção 1: Mostrar erro e sugerir desativação
        this.showErrorWithDeactivateOption(errorMessage, accountId);
        
        // Opção 2: Apenas mostrar erro
        throw new Error(errorMessage);
      }
      return throwError(error);
    })
  );
}

// Método auxiliar para desativar conta
deactivateAccount(accountId: number): Observable<Account> {
  return this.http.put<Account>(
    `${this.apiUrl}/accounts/${accountId}`,
    { status: 'INACTIVE' }
  );
}
```

**Tratamento de erros esperados:**
```typescript
// Erro 400 - Conta tem transações
{
  "error": "Não é possível remover a conta. Existem transações vinculadas a esta conta. Remova as transações primeiro ou desative a conta em vez de removê-la.",
  "status": 400
}

// Erro 404 - Conta não encontrada
{
  "error": "Conta não encontrada com ID: {id}",
  "status": 404
}

// Sucesso 200
{
  "success": true,
  "data": "Conta removida com sucesso"
}
```

**UI/UX Recomendações:**
1. Antes de deletar, mostrar confirmação:
   ```typescript
   const confirmed = confirm(
     'Tem certeza que deseja remover esta conta?\n\n' +
     '⚠️ Se houver transações relacionadas, a exclusão não será permitida.'
   );
   ```

2. Quando houver erro 400, mostrar opção de desativar:
   ```typescript
   // Exemplo de diálogo
   if (error.status === 400) {
     const shouldDeactivate = confirm(
       'Esta conta possui transações vinculadas e não pode ser removida.\n\n' +
       'Deseja desativar a conta em vez de removê-la?'
     );
     
     if (shouldDeactivate) {
       this.deactivateAccount(accountId).subscribe(...);
     }
   }
   ```

---

### 3. Verificar Exibição de Nomes de Bancos

**O que fazer:**
- Verificar que nomes de bancos são exibidos corretamente
- Testar com nomes que contêm acentos (ex: "Banco Itaú", "Banco do Brasil")
- Nenhuma mudança de código necessária - apenas verificar que está funcionando

**Exemplos de nomes que devem funcionar:**
- ✅ "Banco do Brasil"
- ✅ "Banco Itaú"
- ✅ "Banco Bradesco"
- ✅ "Caixa Econômica Federal"

---

## 📝 Checklist de Implementação

Use este checklist para garantir que tudo foi implementado:

### Endpoint de Usuário Completo
- [ ] Interface `UserCompleteDTO` criada
- [ ] Método `getUserComplete()` adicionado ao service de usuário
- [ ] Componentes atualizados para usar o novo endpoint quando necessário
- [ ] Testes realizados para verificar que os dados são retornados corretamente

### Exclusão de Contas
- [ ] Método de exclusão atualizado para tratar erro 400
- [ ] Mensagem de erro específica implementada
- [ ] Opção de desativar conta implementada (opcional, mas recomendado)
- [ ] Confirmação antes de deletar implementada
- [ ] Testes realizados com conta que tem transações
- [ ] Testes realizados com conta sem transações

### Verificação Geral
- [ ] Nomes de bancos são exibidos corretamente (UTF-8)
- [ ] Todas as chamadas de API estão usando a base URL correta: `http://localhost:8080`
- [ ] Tratamento de erros está funcionando corretamente
- [ ] Mensagens de erro são amigáveis ao usuário

---

## 📚 Documentação de Referência

### Arquivos Importantes
1. **`FRONTEND_BACKEND_CHANGES.md`** - Documentação detalhada de todas as alterações
2. **`FRONTEND_API_ROUTES.md`** - Documentação completa de todas as rotas da API

### Base URL
```
http://localhost:8080
```

### Formato Padrão de Respostas

**Sucesso:**
```json
{
  "success": true,
  "data": { /* dados aqui */ }
}
```

**Erro:**
```json
{
  "error": "Mensagem de erro",
  "status": 400
}
```

---

## 🎨 Exemplos de Código Completos

### Exemplo 1: Service Angular Completo

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Método existente - dados básicos
  getUser(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${userId}`);
  }

  // NOVO - dados completos
  getUserComplete(userId: number): Observable<UserCompleteDTO> {
    return this.http.get<any>(`${this.apiUrl}/users/${userId}/complete`).pipe(
      map(response => response.data || response),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Erro na API:', error);
    return throwError(() => new Error(error.error?.error || 'Erro desconhecido'));
  }
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  deleteAccount(accountId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/accounts/${accountId}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 400) {
          const errorMessage = error.error?.error || 
            'Não é possível remover a conta. Existem transações vinculadas.';
          return throwError(() => new Error(errorMessage));
        }
        return throwError(() => new Error(error.error?.error || 'Erro ao remover conta'));
      })
    );
  }

  deactivateAccount(accountId: number): Observable<Account> {
    return this.http.put<Account>(
      `${this.apiUrl}/accounts/${accountId}`,
      { status: 'INACTIVE' }
    );
  }
}
```

### Exemplo 2: Componente Angular

```typescript
import { Component, OnInit } from '@angular/core';
import { UserService } from './services/user.service';
import { AccountService } from './services/account.service';
import { UserCompleteDTO } from './models/user-complete.dto';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html'
})
export class UserProfileComponent implements OnInit {
  userData: UserCompleteDTO | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private userService: UserService,
    private accountService: AccountService
  ) {}

  ngOnInit() {
    this.loadUserData(1); // Substituir pelo ID real do usuário logado
  }

  loadUserData(userId: number) {
    this.loading = true;
    this.error = null;

    this.userService.getUserComplete(userId).subscribe({
      next: (data) => {
        this.userData = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  deleteAccount(accountId: number) {
    const confirmed = confirm(
      'Tem certeza que deseja remover esta conta?\n\n' +
      '⚠️ Se houver transações relacionadas, a exclusão não será permitida.'
    );

    if (!confirmed) return;

    this.accountService.deleteAccount(accountId).subscribe({
      next: () => {
        alert('Conta removida com sucesso!');
        this.loadUserData(this.userData!.userId); // Recarregar dados
      },
      error: (error) => {
        if (error.message.includes('transações vinculadas')) {
          const shouldDeactivate = confirm(
            'Esta conta possui transações vinculadas e não pode ser removida.\n\n' +
            'Deseja desativar a conta em vez de removê-la?'
          );

          if (shouldDeactivate) {
            this.accountService.deactivateAccount(accountId).subscribe({
              next: () => {
                alert('Conta desativada com sucesso!');
                this.loadUserData(this.userData!.userId);
              },
              error: (err) => alert('Erro ao desativar conta: ' + err.message)
            });
          }
        } else {
          alert('Erro ao remover conta: ' + error.message);
        }
      }
    });
  }
}
```

### Exemplo 3: React Hook

```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const useUserComplete = (userId: number) => {
  const [userData, setUserData] = useState<UserCompleteDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const response = await axios.get(`${API_URL}/users/${userId}/complete`);
        setUserData(response.data.data || response.data);
        setError(null);
      } catch (err: any) {
        setError(err.response?.data?.error || 'Erro ao carregar dados do usuário');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchUserData();
    }
  }, [userId]);

  return { userData, loading, error };
};

export const useDeleteAccount = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const deleteAccount = async (accountId: number) => {
    try {
      setLoading(true);
      setError(null);
      await axios.delete(`${API_URL}/accounts/${accountId}`);
      return { success: true };
    } catch (err: any) {
      const errorMessage = err.response?.data?.error || 'Erro ao remover conta';
      setError(errorMessage);
      
      if (err.response?.status === 400) {
        return { 
          success: false, 
          hasTransactions: true, 
          error: errorMessage 
        };
      }
      
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  return { deleteAccount, loading, error };
};
```

---

## ✅ Critérios de Sucesso

Sua implementação será considerada completa quando:

1. ✅ O endpoint `/api/users/{id}/complete` está funcionando e retornando todos os dados
2. ✅ A exclusão de contas trata corretamente o erro 400 quando há transações
3. ✅ Mensagens de erro são claras e amigáveis ao usuário
4. ✅ Opção de desativar conta está disponível (recomendado)
5. ✅ Nomes de bancos são exibidos corretamente com acentos
6. ✅ Não há erros no console do navegador
7. ✅ Todas as funcionalidades existentes continuam funcionando

---

## 🚨 Pontos de Atenção

1. **Base URL:** Sempre use `http://localhost:8080` como base URL
2. **Formato de Resposta:** O backend pode retornar `{ success: true, data: {...} }` ou diretamente os dados. Trate ambos os casos
3. **Tratamento de Erros:** Sempre trate erros HTTP adequadamente e mostre mensagens amigáveis
4. **Loading States:** Implemente estados de carregamento para melhor UX
5. **Confirmações:** Sempre confirme ações destrutivas (deletar, etc.) antes de executar

---

## 📞 Suporte

Se encontrar problemas ou dúvidas:

1. Consulte `FRONTEND_BACKEND_CHANGES.md` para detalhes técnicos
2. Consulte `FRONTEND_API_ROUTES.md` para documentação completa de rotas
3. Verifique os logs do backend para erros detalhados
4. Teste os endpoints diretamente usando Postman/Insomnia para validar

---

**Boa sorte com a implementação! 🚀**


