# 📋 Guia para Criar Cartão - Frontend

## 🎯 Objetivo

Este documento fornece todas as informações necessárias para implementar a funcionalidade de **criar cartão** no frontend.

---

## 🔗 Endpoint Principal

### **POST** `/api/form/card`

**URL Completa:** `http://localhost:8080/api/form/card`

---

## 📝 Estrutura do Request

### Headers
```
Content-Type: application/json
```

### Body (JSON)

```typescript
interface CardRequest {
  userId: number;                    // ✅ OBRIGATÓRIO - ID do usuário
  accountId?: number;                // ⚠️ OPCIONAL - ID da conta vinculada
  cardNumber: string;                // ✅ OBRIGATÓRIO - Número do cartão (aceita espaços)
  cardHolderName: string;            // ✅ OBRIGATÓRIO - Nome do portador
  expiryDate: string;                // ✅ OBRIGATÓRIO - Data de expiração (formato: "MM/YY")
  cvv?: string;                      // ⚠️ OPCIONAL - Código de segurança
  cardType: string;                  // ✅ OBRIGATÓRIO - Tipo: "CREDIT", "DEBIT", "PREPAID"
  brand?: string;                    // ⚠️ OPCIONAL - Bandeira: "Visa", "Mastercard", "Elo", etc.
  limitAmount?: string;              // ⚠️ OPCIONAL - Limite (obrigatório se cardType = "CREDIT")
  status?: string;                   // ⚠️ OPCIONAL - Status: "ACTIVE" (padrão)
}
```

### Exemplo Completo

```json
{
  "userId": 1,
  "accountId": 1,
  "cardNumber": "2222 2222 2222 2222 222",
  "cardHolderName": "PATRICK GUTEMBERG GOMES DUARTE",
  "expiryDate": "12/90",
  "cvv": "1234",
  "cardType": "CREDIT",
  "brand": "master",
  "limitAmount": "500000"
}
```

---

## ✅ Validações do Frontend (Recomendadas)

### 1. **Card Number (Número do Cartão)**
- ✅ Aceita espaços, o backend remove automaticamente
- ✅ Deve ter entre **13 e 19 dígitos** (após remover espaços)
- ✅ Formatado ou não, o backend processa

**Exemplo de validação:**
```javascript
const cleanCardNumber = cardNumber.replace(/\s/g, '');
if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
  // Erro: "Número do cartão deve ter entre 13 e 19 dígitos"
}
```

### 2. **Card Holder Name (Nome do Portador)**
- ✅ Obrigatório
- ✅ Aceita até 100 caracteres
- ✅ Será convertido para MAIÚSCULAS pelo backend

### 3. **Expiry Date (Data de Expiração)**
- ✅ Formato: **"MM/YY"** (ex: "12/25", "06/30")
- ✅ Aceita anos de 2 dígitos (00-99)
- ✅ O backend converte para data completa automaticamente

**Exemplo de validação:**
```javascript
const expiryRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
if (!expiryRegex.test(expiryDate)) {
  // Erro: "Data de expiração inválida. Use o formato MM/AA"
}
```

### 4. **Card Type (Tipo de Cartão)**
- ✅ Obrigatório
- ✅ Valores aceitos: `"CREDIT"`, `"DEBIT"`, `"PREPAID"`
- ⚠️ Se for `"CREDIT"`, o campo `limitAmount` é **obrigatório**

### 5. **Limit Amount (Limite de Crédito)**
- ⚠️ **Obrigatório** se `cardType = "CREDIT"`
- ✅ Aceita string com formato monetário (ex: "500000", "5000.00")
- ✅ Deve ser maior que zero

### 6. **CVV**
- ⚠️ Opcional no backend, mas recomendado validar no frontend
- ✅ Geralmente 3 ou 4 dígitos
- ✅ Apenas números

### 7. **Brand (Bandeira)**
- ⚠️ Opcional (o backend detecta automaticamente se não informado)
- ✅ Valores comuns: `"Visa"`, `"Mastercard"`, `"Elo"`, `"American Express"`
- ✅ Pode ser minúsculo, o backend processa

---

## 📤 Estrutura do Response

### Sucesso (201 Created)

```json
{
  "success": true,
  "data": {
    "cardId": 1,
    "userId": 1,
    "accountId": 1,
    "cardNumber": "**** **** **** 2222",  // Mascarado automaticamente
    "cardHolderName": "PATRICK GUTEMBERG GOMES DUARTE",
    "expiryDate": "2090-12-31",
    "cardType": "CREDIT",
    "brand": "Mastercard",
    "status": "ACTIVE",
    "limitAmount": 500000.00,
    "currentBalance": 0.00,
    "createdAt": "2025-01-16T03:40:00",
    "updatedAt": "2025-01-16T03:40:00"
  }
}
```

### Erro (400 Bad Request)

```json
{
  "success": false,
  "error": "Card number is required"
}
```

### Erro (400 Bad Request) - Validação

```json
{
  "success": false,
  "error": "Invalid card number (must have between 13 and 19 digits)"
}
```

### Erro (400 Bad Request) - Limite obrigatório

```json
{
  "success": false,
  "error": "Limit amount is required for credit cards"
}
```

---

## 💻 Implementação Completa

### Angular/TypeScript

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface CardRequest {
  userId: number;
  accountId?: number;
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv?: string;
  cardType: 'CREDIT' | 'DEBIT' | 'PREPAID';
  brand?: string;
  limitAmount?: string;
  status?: string;
}

export interface CardResponse {
  cardId: number;
  userId: number;
  accountId?: number;
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cardType: string;
  brand: string;
  status: string;
  limitAmount: number;
  currentBalance: number;
  createdAt: string;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class CardService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  createCard(cardData: CardRequest): Observable<CardResponse> {
    // Validações no frontend antes de enviar
    this.validateCardData(cardData);

    return this.http.post<{success: boolean, data: CardResponse}>(
      `${this.apiUrl}/form/card`,
      cardData
    ).pipe(
      map(response => {
        if (response.success) {
          return response.data;
        }
        throw new Error('Erro ao criar cartão');
      }),
      catchError(error => {
        const errorMessage = error.error?.error || 'Erro ao criar cartão';
        throw new Error(errorMessage);
      })
    );
  }

  private validateCardData(card: CardRequest): void {
    // Validar número do cartão
    const cleanCardNumber = card.cardNumber.replace(/\s/g, '');
    if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
      throw new Error('Número do cartão deve ter entre 13 e 19 dígitos');
    }

    // Validar nome do portador
    if (!card.cardHolderName || card.cardHolderName.trim().length === 0) {
      throw new Error('Nome do portador é obrigatório');
    }

    // Validar data de expiração
    const expiryRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!expiryRegex.test(card.expiryDate)) {
      throw new Error('Data de expiração inválida. Use o formato MM/AA');
    }

    // Validar tipo de cartão
    if (!['CREDIT', 'DEBIT', 'PREPAID'].includes(card.cardType)) {
      throw new Error('Tipo de cartão inválido');
    }

    // Validar limite para cartão de crédito
    if (card.cardType === 'CREDIT' && (!card.limitAmount || parseFloat(card.limitAmount) <= 0)) {
      throw new Error('Limite de crédito é obrigatório e deve ser maior que zero');
    }
  }

  // Listar tipos de cartão disponíveis
  getCardTypes(): Observable<Array<{name: string, value: string}>> {
    return this.http.get<{success: boolean, data: Array<{name: string, value: string}>}>(
      `${this.apiUrl}/form/card/types`
    ).pipe(
      map(response => response.data)
    );
  }
}
```

### Componente Angular

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CardService, CardRequest } from './card.service';

@Component({
  selector: 'app-create-card',
  templateUrl: './create-card.component.html'
})
export class CreateCardComponent {
  cardForm: FormGroup;
  cardTypes: Array<{name: string, value: string}> = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private cardService: CardService
  ) {
    this.cardForm = this.fb.group({
      userId: [null, Validators.required],
      accountId: [null],
      cardNumber: ['', [Validators.required, this.validateCardNumber]],
      cardHolderName: ['', [Validators.required, Validators.maxLength(100)]],
      expiryDate: ['', [Validators.required, this.validateExpiryDate]],
      cvv: ['', [Validators.minLength(3), Validators.maxLength(4)]],
      cardType: ['', Validators.required],
      brand: [''],
      limitAmount: ['']
    });

    // Carregar tipos de cartão
    this.loadCardTypes();

    // Validar limite quando o tipo mudar
    this.cardForm.get('cardType')?.valueChanges.subscribe(type => {
      const limitControl = this.cardForm.get('limitAmount');
      if (type === 'CREDIT') {
        limitControl?.setValidators([Validators.required, Validators.min(0.01)]);
      } else {
        limitControl?.clearValidators();
      }
      limitControl?.updateValueAndValidity();
    });
  }

  loadCardTypes(): void {
    this.cardService.getCardTypes().subscribe(
      types => this.cardTypes = types,
      error => console.error('Erro ao carregar tipos:', error)
    );
  }

  validateCardNumber(control: any): {[key: string]: any} | null {
    if (!control.value) return null;
    const clean = control.value.replace(/\s/g, '');
    if (clean.length < 13 || clean.length > 19) {
      return { invalidLength: true };
    }
    return null;
  }

  validateExpiryDate(control: any): {[key: string]: any} | null {
    if (!control.value) return null;
    const regex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!regex.test(control.value)) {
      return { invalidFormat: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.cardForm.valid) {
      this.loading = true;
      const cardData: CardRequest = this.cardForm.value;

      this.cardService.createCard(cardData).subscribe(
        response => {
          console.log('Cartão criado com sucesso:', response);
          alert('Cartão criado com sucesso!');
          this.cardForm.reset();
          this.loading = false;
        },
        error => {
          console.error('Erro ao criar cartão:', error);
          alert('Erro: ' + error.message);
          this.loading = false;
        }
      );
    }
  }
}
```

---

## 🎨 Exemplo de Template (HTML)

```html
<form [formGroup]="cardForm" (ngSubmit)="onSubmit()">
  <div class="form-group">
    <label>Número do Cartão *</label>
    <input 
      type="text" 
      formControlName="cardNumber" 
      placeholder="2222 2222 2222 2222"
      maxlength="25"
    />
    <small *ngIf="cardForm.get('cardNumber')?.hasError('invalidLength')">
      Número do cartão deve ter entre 13 e 19 dígitos
    </small>
  </div>

  <div class="form-group">
    <label>Nome do Portador *</label>
    <input 
      type="text" 
      formControlName="cardHolderName" 
      placeholder="JOÃO SILVA SANTOS"
      maxlength="100"
    />
  </div>

  <div class="form-group">
    <label>Data de Expiração (MM/AA) *</label>
    <input 
      type="text" 
      formControlName="expiryDate" 
      placeholder="12/25"
      maxlength="5"
    />
    <small *ngIf="cardForm.get('expiryDate')?.hasError('invalidFormat')">
      Use o formato MM/AA (ex: 12/25)
    </small>
  </div>

  <div class="form-group">
    <label>CVV</label>
    <input 
      type="text" 
      formControlName="cvv" 
      placeholder="123"
      maxlength="4"
    />
  </div>

  <div class="form-group">
    <label>Tipo de Cartão *</label>
    <select formControlName="cardType">
      <option value="">Selecione...</option>
      <option *ngFor="let type of cardTypes" [value]="type.value">
        {{ type.name }}
      </option>
    </select>
  </div>

  <div class="form-group">
    <label>Bandeira</label>
    <input 
      type="text" 
      formControlName="brand" 
      placeholder="Visa, Mastercard, Elo..."
    />
  </div>

  <div class="form-group" *ngIf="cardForm.get('cardType')?.value === 'CREDIT'">
    <label>Limite de Crédito (R$) *</label>
    <input 
      type="text" 
      formControlName="limitAmount" 
      placeholder="5000.00"
    />
    <small *ngIf="cardForm.get('limitAmount')?.hasError('required')">
      Limite é obrigatório para cartões de crédito
    </small>
  </div>

  <div class="form-group">
    <label>Conta Vinculada (Opcional)</label>
    <input 
      type="number" 
      formControlName="accountId" 
      placeholder="ID da conta"
    />
  </div>

  <button type="submit" [disabled]="cardForm.invalid || loading">
    {{ loading ? 'Criando...' : 'Criar Cartão' }}
  </button>
</form>
```

---

## 🔍 Endpoints Auxiliares

### Listar Tipos de Cartão

**GET** `/api/form/card/types`

```typescript
getCardTypes(): Observable<Array<{name: string, value: string}>> {
  return this.http.get(`${this.apiUrl}/form/card/types`)
    .pipe(map((response: any) => response.data));
}
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    { "name": "CREDIT", "value": "CREDIT" },
    { "name": "DEBIT", "value": "DEBIT" },
    { "name": "PREPAID", "value": "PREPAID" }
  ]
}
```

---

## 📋 Checklist de Implementação

- [ ] Criar service para comunicação com API
- [ ] Criar interface/type para CardRequest e CardResponse
- [ ] Implementar validações no frontend
- [ ] Criar formulário reativo
- [ ] Implementar máscara para número do cartão (opcional)
- [ ] Implementar máscara para data de expiração (MM/AA)
- [ ] Carregar tipos de cartão do backend
- [ ] Validar limite obrigatório para cartão de crédito
- [ ] Exibir mensagens de erro amigáveis
- [ ] Implementar loading state
- [ ] Redirecionar ou atualizar lista após criar

---

## ⚠️ Pontos Importantes

### 1. **Número do Cartão**
- O backend **remove espaços automaticamente** antes de validar
- Aceita formatado: "2222 2222 2222 2222 222"
- Aceita sem formatação: "2222222222222222222"
- O número é **mascarado automaticamente** na resposta

### 2. **Data de Expiração**
- **Formato obrigatório:** `"MM/YY"` (ex: "12/25", "06/30")
- O backend converte para data completa automaticamente
- Aceita anos de 2 dígitos

### 3. **Limite de Crédito**
- **Obrigatório apenas** se `cardType = "CREDIT"`
- Aceita string: "500000" ou "5000.00"
- Deve ser maior que zero

### 4. **Detecção Automática de Bandeira**
- Se não informar `brand`, o backend tenta detectar pelo número
- Baseado nos primeiros dígitos do cartão

### 5. **Mascaramento**
- O número é **sempre mascarado** nas respostas
- Formato: `"**** **** **** XXXX"` (últimos 4 dígitos visíveis)

---

## 🚨 Tratamento de Erros

```typescript
createCard(cardData: CardRequest): Observable<CardResponse> {
  return this.http.post(`${this.apiUrl}/form/card`, cardData).pipe(
    map(response => response.data),
    catchError(error => {
      const errorMessage = this.getErrorMessage(error);
      return throwError(() => new Error(errorMessage));
    })
  );
}

private getErrorMessage(error: any): string {
  if (error.error?.error) {
    return error.error.error;
  }
  if (error.message) {
    return error.message;
  }
  return 'Erro desconhecido ao criar cartão';
}
```

---

## ✅ Exemplo de Uso Completo

```typescript
// No componente
const cardData: CardRequest = {
  userId: 1,
  accountId: 1,  // Opcional
  cardNumber: '2222 2222 2222 2222 222',
  cardHolderName: 'PATRICK GUTEMBERG GOMES DUARTE',
  expiryDate: '12/90',
  cvv: '1234',
  cardType: 'CREDIT',
  brand: 'master',
  limitAmount: '500000'
};

this.cardService.createCard(cardData).subscribe(
  card => {
    console.log('Cartão criado:', card);
    // card.cardNumber será "**** **** **** 2222"
  },
  error => {
    console.error('Erro:', error.message);
  }
);
```

---

**Última atualização:** 16 de Janeiro de 2025  
**Status:** ✅ Pronto para implementação

