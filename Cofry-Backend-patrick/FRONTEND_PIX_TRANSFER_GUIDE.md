# 📋 Guia para Transferência PIX - Frontend

## 🎯 Objetivo

Este documento fornece todas as informações necessárias para implementar a funcionalidade de **transferência PIX** entre usuários do sistema usando **CPF** como identificador do destinatário.

---

## 🔗 Endpoints Disponíveis

### **Base URL:** `http://localhost:8080`

---

## 📝 Fluxo de Transferência PIX

### Processo em 2 Etapas:

1. **Buscar usuário por CPF** → Obter `userId` do destinatário
2. **Realizar transferência PIX** → Usar `destinationUserId`

---

## 🔍 Etapa 1: Buscar Usuário por CPF

### **GET** `/api/users/cpf/{cpf}`

**Nota:** Este endpoint ainda não existe no backend. Você precisará buscar o usuário através do endpoint existente ou criar um endpoint auxiliar.

**Alternativa:** Use o endpoint de busca de usuário por CPF que pode ser implementado ou use a lógica de login que já valida CPF.

### Opção 1: Criar endpoint auxiliar (Recomendado)

**GET** `/api/users/search?cpf={cpf}`

**Implementação sugerida no backend (se não existir):**

```java
// Endpoint GET /api/users/search?cpf={cpf}
@WebServlet(name = "UserSearchServlet", urlPatterns = {"/api/users/search"})
```

### Opção 2: Listar usuários e filtrar no frontend (Não recomendado)

Como alternativa temporária, você pode listar todos os usuários e filtrar por CPF no frontend, mas isso não é eficiente.

### Estrutura de Resposta (Usuário encontrado):

```typescript
interface UserInfo {
  userId: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  cpf: string; // Formatado: "123.456.789-00"
  phoneNumber: string | null;
  isActive: boolean;
}
```

**Exemplo de Resposta:**
```json
{
  "success": true,
  "data": {
    "userId": 2,
    "firstName": "Maria",
    "lastName": "Santos",
    "email": "maria@example.com",
    "cpf": "987.654.321-00",
    "phoneNumber": "+55 11 98765-4321",
    "isActive": true
  }
}
```

---

## 💸 Etapa 2: Realizar Transferência PIX

### **POST** `/api/pix/transfer`

Realiza a transferência PIX entre contas.

**Headers:**
```
Content-Type: application/json
```

### Request Body (JSON)

```typescript
interface PixTransferRequest {
  sourceAccountId: number;              // ✅ OBRIGATÓRIO - ID da conta de origem (quem envia)
  destinationAccountId?: number;        // ⚠️ OPCIONAL - ID da conta de destino (alternativa a destinationUserId)
  destinationUserId?: number;           // ⚠️ OPCIONAL - ID do usuário de destino (usa primeira conta ativa)
  destinationCpf?: string;              // ⚠️ FUTURO - CPF do destinatário (backend pode implementar)
  amount: string;                       // ✅ OBRIGATÓRIO - Valor da transferência (formato: "100.00")
  description?: string;                 // ⚠️ OPCIONAL - Descrição/observação da transferência
}
```

### Exemplo Completo

```json
{
  "sourceAccountId": 1,
  "destinationUserId": 2,
  "amount": "250.00",
  "description": "Transferência PIX para Maria Santos"
}
```

**Nota Importante:**
- Você pode usar **`destinationUserId`** (recomendado quando tem CPF)
- Ou usar **`destinationAccountId`** (se souber a conta específica)
- Se usar `destinationUserId`, o sistema escolhe automaticamente a primeira conta ativa do usuário

---

## 📤 Estrutura do Response

### Sucesso (200 OK)

```json
{
  "success": true,
  "data": {
    "transactionId": 10,
    "sourceAccountId": 1,
    "destinationAccountId": 3,
    "sourceUserId": 1,
    "destinationUserId": 2,
    "amount": 250.00,
    "description": "Transferência PIX para Maria Santos",
    "transactionDate": "2025-01-16",
    "createdAt": "2025-01-16T10:30:00",
    "status": "SUCCESS",
    "message": "Transferência PIX realizada com sucesso"
  }
}
```

### Erro (400 Bad Request)

```json
{
  "success": false,
  "error": "Saldo insuficiente. Saldo disponível: 150.00"
}
```

### Erro - Usuário não encontrado

```json
{
  "success": false,
  "error": "Usuário não encontrado com CPF: 123.456.789-00"
}
```

### Erro - CPF inválido

```json
{
  "success": false,
  "error": "CPF inválido"
}
```

---

## ✅ Validações do Frontend

### 1. **Validação de CPF**

```typescript
function validateCPF(cpf: string): boolean {
  // Remove formatação
  const cleanCpf = cpf.replace(/[^\d]/g, '');
  
  // Verifica se tem 11 dígitos
  if (cleanCpf.length !== 11) {
    return false;
  }
  
  // Verifica se todos os dígitos são iguais (CPFs inválidos)
  if (/^(\d)\1{10}$/.test(cleanCpf)) {
    return false;
  }
  
  // Validação dos dígitos verificadores
  let sum = 0;
  let remainder;
  
  // Valida primeiro dígito
  for (let i = 1; i <= 9; i++) {
    sum += parseInt(cleanCpf.substring(i - 1, i)) * (11 - i);
  }
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cleanCpf.substring(9, 10))) {
    return false;
  }
  
  // Valida segundo dígito
  sum = 0;
  for (let i = 1; i <= 10; i++) {
    sum += parseInt(cleanCpf.substring(i - 1, i)) * (12 - i);
  }
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cleanCpf.substring(10, 11))) {
    return false;
  }
  
  return true;
}

function formatCPF(cpf: string): string {
  const clean = cpf.replace(/[^\d]/g, '');
  return clean.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}
```

### 2. **Validação de Valor**

```typescript
function validateAmount(amount: string): boolean {
  const numAmount = parseFloat(amount);
  
  if (isNaN(numAmount) || numAmount <= 0) {
    return false;
  }
  
  // Verifica se o valor não é muito grande
  if (numAmount > 1000000) {
    return false;
  }
  
  return true;
}

function formatAmount(amount: string): string {
  // Remove formatação
  const clean = amount.replace(/[^\d,]/g, '').replace(',', '.');
  const num = parseFloat(clean);
  
  if (isNaN(num)) {
    return '0.00';
  }
  
  return num.toFixed(2);
}
```

### 3. **Validação de Saldo**

```typescript
function validateBalance(accountBalance: number, transferAmount: number): boolean {
  return accountBalance >= transferAmount;
}
```

---

## 💻 Implementação Completa

### Angular/TypeScript Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface UserInfo {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  cpf: string;
  phoneNumber: string | null;
  isActive: boolean;
}

export interface PixTransferRequest {
  sourceAccountId: number;
  destinationAccountId?: number;
  destinationUserId?: number;
  amount: string;
  description?: string;
}

export interface PixTransferResponse {
  transactionId: number;
  sourceAccountId: number;
  destinationAccountId: number;
  sourceUserId: number;
  destinationUserId: number;
  amount: number;
  description: string;
  transactionDate: string;
  createdAt: string;
  status: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class PixService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Busca usuário por CPF
   * Nota: Este endpoint pode precisar ser implementado no backend
   * Alternativa: usar endpoint de busca existente
   */
  getUserByCpf(cpf: string): Observable<UserInfo> {
    // Limpa o CPF
    const cleanCpf = cpf.replace(/[^\d]/g, '');
    const formattedCpf = this.formatCPF(cleanCpf);
    
    // Se o backend tiver endpoint específico:
    // return this.http.get<{success: boolean, data: UserInfo}>(
    //   `${this.apiUrl}/users/search?cpf=${formattedCpf}`
    // ).pipe(map(response => response.data));
    
    // Alternativa: Buscar todos e filtrar (não recomendado para produção)
    return this.http.get<{success: boolean, data: UserInfo[]}>(
      `${this.apiUrl}/users`
    ).pipe(
      map(response => {
        const user = response.data.find(u => 
          u.cpf.replace(/[^\d]/g, '') === cleanCpf
        );
        if (!user) {
          throw new Error('Usuário não encontrado com CPF: ' + formattedCpf);
        }
        return user;
      }),
      catchError(error => {
        return throwError(() => new Error(
          error.error?.error || 'Erro ao buscar usuário por CPF'
        ));
      })
    );
  }

  /**
   * Realiza transferência PIX
   */
  transferPix(request: PixTransferRequest): Observable<PixTransferResponse> {
    // Validações
    this.validatePixRequest(request);
    
    return this.http.post<{success: boolean, data: PixTransferResponse}>(
      `${this.apiUrl}/pix/transfer`,
      request
    ).pipe(
      map(response => {
        if (response.success) {
          return response.data;
        }
        throw new Error('Erro ao realizar transferência PIX');
      }),
      catchError(error => {
        const errorMessage = error.error?.error || 'Erro ao realizar transferência PIX';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Fluxo completo: Busca usuário por CPF e realiza transferência
   */
  transferPixByCpf(
    sourceAccountId: number,
    destinationCpf: string,
    amount: string,
    description?: string
  ): Observable<PixTransferResponse> {
    // 1. Busca usuário por CPF
    return this.getUserByCpf(destinationCpf).pipe(
      map(user => {
        if (!user.isActive) {
          throw new Error('Usuário destinatário está inativo');
        }
        return user;
      }),
      // 2. Realiza transferência
      map(user => {
        const transferRequest: PixTransferRequest = {
          sourceAccountId: sourceAccountId,
          destinationUserId: user.userId,
          amount: amount,
          description: description || `Transferência PIX para ${user.firstName} ${user.lastName}`
        };
        return transferRequest;
      }),
      // 3. Executa transferência
      switchMap(request => this.transferPix(request))
    );
  }

  private validatePixRequest(request: PixTransferRequest): void {
    if (!request.sourceAccountId) {
      throw new Error('Conta de origem é obrigatória');
    }
    
    if (!request.destinationAccountId && !request.destinationUserId) {
      throw new Error('Conta de destino ou ID do usuário é obrigatório');
    }
    
    if (!request.amount || parseFloat(request.amount) <= 0) {
      throw new Error('Valor deve ser maior que zero');
    }
  }

  private formatCPF(cpf: string): string {
    const clean = cpf.replace(/[^\d]/g, '');
    return clean.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
}
```

---

## 🎨 Componente Angular

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PixService } from './pix.service';

@Component({
  selector: 'app-pix-transfer',
  templateUrl: './pix-transfer.component.html'
})
export class PixTransferComponent {
  pixForm: FormGroup;
  loading = false;
  userFound: any = null;
  searchingUser = false;

  constructor(
    private fb: FormBuilder,
    private pixService: PixService
  ) {
    this.pixForm = this.fb.group({
      sourceAccountId: [null, Validators.required],
      destinationCpf: ['', [Validators.required, this.validateCPF]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['']
    });
  }

  validateCPF(control: any): {[key: string]: any} | null {
    if (!control.value) return null;
    
    const cleanCpf = control.value.replace(/[^\d]/g, '');
    if (cleanCpf.length !== 11) {
      return { invalidCpf: true };
    }
    
    // Validação básica (pode usar biblioteca como ngx-mask)
    return null;
  }

  /**
   * Busca usuário por CPF ao digitar
   */
  searchUserByCpf(): void {
    const cpf = this.pixForm.get('destinationCpf')?.value;
    if (!cpf || cpf.replace(/[^\d]/g, '').length !== 11) {
      this.userFound = null;
      return;
    }

    this.searchingUser = true;
    this.pixService.getUserByCpf(cpf).subscribe(
      user => {
        this.userFound = user;
        this.searchingUser = false;
        
        // Preenche descrição sugerida
        if (!this.pixForm.get('description')?.value) {
          this.pixForm.patchValue({
            description: `Transferência PIX para ${user.firstName} ${user.lastName}`
          });
        }
      },
      error => {
        this.userFound = null;
        this.searchingUser = false;
        console.error('Usuário não encontrado:', error.message);
      }
    );
  }

  /**
   * Submete o formulário de transferência
   */
  onSubmit(): void {
    if (this.pixForm.valid && this.userFound) {
      this.loading = true;
      
      const formValue = this.pixForm.value;
      
      this.pixService.transferPixByCpf(
        formValue.sourceAccountId,
        formValue.destinationCpf,
        formValue.amount,
        formValue.description
      ).subscribe(
        response => {
          alert('Transferência realizada com sucesso!');
          console.log('Transferência:', response);
          this.pixForm.reset();
          this.userFound = null;
          this.loading = false;
        },
        error => {
          alert('Erro: ' + error.message);
          console.error('Erro na transferência:', error);
          this.loading = false;
        }
      );
    }
  }

  /**
   * Formata CPF enquanto digita
   */
  formatCpfInput(event: any): void {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length <= 11) {
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
      this.pixForm.patchValue({ destinationCpf: value }, { emitEvent: false });
      
      // Busca usuário automaticamente quando CPF estiver completo
      if (value.length === 14) { // CPF formatado tem 14 caracteres
        setTimeout(() => this.searchUserByCpf(), 500);
      }
    }
  }

  /**
   * Formata valor monetário
   */
  formatAmountInput(event: any): void {
    let value = event.target.value.replace(/\D/g, '');
    if (value) {
      value = (parseInt(value) / 100).toFixed(2);
      value = value.replace('.', ',');
      value = value.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
      this.pixForm.patchValue({ amount: 'R$ ' + value }, { emitEvent: false });
    }
  }
}
```

---

## 🎨 Template HTML

```html
<form [formGroup]="pixForm" (ngSubmit)="onSubmit()">
  <!-- Conta de Origem -->
  <div class="form-group">
    <label>Conta de Origem *</label>
    <select formControlName="sourceAccountId">
      <option value="">Selecione a conta...</option>
      <option *ngFor="let account of userAccounts" [value]="account.accountId">
        {{ account.bankName }} - {{ account.accountNumber }} 
        (Saldo: R$ {{ account.balance | number:'1.2-2' }})
      </option>
    </select>
  </div>

  <!-- CPF do Destinatário -->
  <div class="form-group">
    <label>CPF do Destinatário *</label>
    <input 
      type="text" 
      formControlName="destinationCpf" 
      placeholder="000.000.000-00"
      maxlength="14"
      (input)="formatCpfInput($event)"
      (blur)="searchUserByCpf()"
    />
    <small *ngIf="pixForm.get('destinationCpf')?.hasError('invalidCpf')">
      CPF inválido
    </small>
    
    <!-- Loading -->
    <div *ngIf="searchingUser" class="loading">
      <span>Buscando usuário...</span>
    </div>
    
    <!-- Usuário encontrado -->
    <div *ngIf="userFound && !searchingUser" class="user-found">
      <strong>✓ Usuário encontrado:</strong>
      <p>{{ userFound.firstName }} {{ userFound.lastName }}</p>
      <p>{{ userFound.email }}</p>
    </div>
    
    <!-- Erro -->
    <div *ngIf="!userFound && pixForm.get('destinationCpf')?.touched && pixForm.get('destinationCpf')?.value?.length === 14 && !searchingUser" class="error">
      Usuário não encontrado com este CPF
    </div>
  </div>

  <!-- Valor -->
  <div class="form-group">
    <label>Valor (R$) *</label>
    <input 
      type="text" 
      formControlName="amount" 
      placeholder="0,00"
      (input)="formatAmountInput($event)"
    />
    <small *ngIf="pixForm.get('amount')?.hasError('min')">
      Valor deve ser maior que zero
    </small>
  </div>

  <!-- Descrição -->
  <div class="form-group">
    <label>Descrição</label>
    <textarea 
      formControlName="description" 
      placeholder="Observação sobre a transferência"
      rows="3"
    ></textarea>
  </div>

  <!-- Botão -->
  <button 
    type="submit" 
    [disabled]="pixForm.invalid || !userFound || loading"
    class="btn-primary"
  >
    {{ loading ? 'Processando...' : 'Transferir via PIX' }}
  </button>
</form>

<!-- Resumo da Transferência (se necessário) -->
<div *ngIf="userFound" class="transfer-summary">
  <h3>Resumo da Transferência</h3>
  <p><strong>Para:</strong> {{ userFound.firstName }} {{ userFound.lastName }}</p>
  <p><strong>CPF:</strong> {{ userFound.cpf }}</p>
  <p><strong>Valor:</strong> R$ {{ pixForm.get('amount')?.value | number:'1.2-2' }}</p>
</div>
```

---

## 📋 Checklist de Implementação

- [ ] Criar service para comunicação com API PIX
- [ ] Implementar busca de usuário por CPF
- [ ] Implementar validação de CPF no frontend
- [ ] Criar formulário reativo para transferência
- [ ] Implementar máscara de CPF (input formatting)
- [ ] Implementar máscara de valor monetário
- [ ] Implementar busca automática de usuário ao digitar CPF
- [ ] Validar saldo suficiente antes de transferir
- [ ] Exibir informações do destinatário após busca
- [ ] Implementar loading states
- [ ] Exibir mensagens de erro amigáveis
- [ ] Confirmar transferência antes de enviar (modal de confirmação)
- [ ] Atualizar saldo da conta após transferência bem-sucedida
- [ ] Redirecionar para histórico de transações

---

## ⚠️ Pontos Importantes

### 1. **Busca de Usuário por CPF**

O backend atual **não possui endpoint específico** para buscar usuário por CPF. Você pode:

- **Opção A (Recomendado):** Solicitar ao backend criar endpoint `GET /api/users/cpf/{cpf}`
- **Opção B (Temporário):** Listar todos os usuários e filtrar no frontend (ineficiente)

### 2. **Identificação do Destinatário**

Para transferir PIX, você pode usar:
- **`destinationUserId`** - Recomendado quando você busca por CPF primeiro
- **`destinationAccountId`** - Se souber a conta específica

### 3. **Validação de CPF**

O CPF pode ser enviado com ou sem formatação. O backend aceita ambos os formatos:
- Formatado: `"123.456.789-00"`
- Sem formatação: `"12345678900"`

### 4. **Atualização de Saldos**

Os saldos são atualizados **automaticamente** pelo backend:
- Conta de origem: saldo **diminui**
- Conta de destino: saldo **aumenta**

### 5. **Transações Criadas**

O backend cria **duas transações**:
- **TRANSFER** na conta de origem (saída)
- **DEPOSIT** na conta de destino (entrada)

---

## 🔐 Segurança

### Recomendações:

1. **Validação Dupla:** Sempre valide CPF no frontend E backend
2. **Confirmação:** Sempre peça confirmação antes de transferir valores altos
3. **Limites:** Considere implementar limite de transferência diário
4. **Logs:** Registre todas as transferências realizadas

---

## 📊 Exemplo de Fluxo Completo

```typescript
// 1. Usuário digita CPF
userCpf = "123.456.789-00";

// 2. Busca usuário
const user = await pixService.getUserByCpf(userCpf);
// Retorna: { userId: 2, firstName: "Maria", lastName: "Santos", ... }

// 3. Usuário preenche valor
amount = "250.00";

// 4. Realiza transferência
const result = await pixService.transferPix({
  sourceAccountId: 1,
  destinationUserId: user.userId, // Usa o userId obtido
  amount: "250.00",
  description: "Transferência PIX para Maria Santos"
});

// 5. Exibe confirmação
console.log("Transferência realizada:", result);
```

---

**Última atualização:** 16 de Janeiro de 2025  
**Status:** ✅ Pronto para implementação

