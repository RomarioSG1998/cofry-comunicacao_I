# Endpoints de Cartões - Funcionalidade Completa

## ✅ Status: Implementado e Funcionando

O sistema possui **funcionalidade completa** para gerenciar cartões de crédito, débito e pré-pago.

---

## 🔗 Endpoints Disponíveis

### **Base URL:** `http://localhost:8080`

---

## 📝 Endpoints

### 1. **Criar Cartão**

**POST** `/api/form/card`

Cria um novo cartão para um usuário.

**Request Body:**
```json
{
  "userId": 1,
  "accountId": 1,                    // Opcional: vinculado a uma conta
  "cardNumber": "4532015112830366",  // Número completo ou últimos 4 dígitos
  "cardHolderName": "JOÃO SILVA SANTOS",
  "expiryDate": "12/25",             // Formato: MM/YY
  "cvv": "123",                      // Opcional
  "cardType": "CREDIT",              // CREDIT, DEBIT, PREPAID
  "brand": "Visa",                   // Opcional: Visa, Mastercard, Elo, etc.
  "limitAmount": "5000.00"           // Opcional: Limite para cartão de crédito
}
```

**Resposta (201 Created):**
```json
{
  "success": true,
  "data": {
    "cardId": 1,
    "userId": 1,
    "accountId": 1,
    "cardNumber": "**** **** **** 0366",  // Mascarado
    "cardHolderName": "JOÃO SILVA SANTOS",
    "expiryDate": "2025-12-31",
    "cardType": "CREDIT",
    "brand": "Visa",
    "status": "ACTIVE",
    "limitAmount": 5000.00,
    "currentBalance": 0.00,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
}
```

**Exemplo JavaScript:**
```javascript
fetch('http://localhost:8080/api/form/card', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userId: 1,
    cardNumber: '4532015112830366',
    cardHolderName: 'JOÃO SILVA SANTOS',
    expiryDate: '12/25',
    cvv: '123',
    cardType: 'CREDIT',
    brand: 'Visa',
    limitAmount: '5000.00'
  })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Cartão criado:', data.data);
  }
});
```

---

### 2. **Listar Tipos de Cartão**

**GET** `/api/form/card/types`

Retorna os tipos de cartão disponíveis.

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "name": "CREDIT",
      "value": "CREDIT"
    },
    {
      "name": "DEBIT",
      "value": "DEBIT"
    },
    {
      "name": "PREPAID",
      "value": "PREPAID"
    }
  ]
}
```

---

### 3. **Listar Cartões por Usuário**

**GET** `/api/form/card/user/{userId}`

Retorna todos os cartões de um usuário.

**Exemplo:**
```bash
GET http://localhost:8080/api/form/card/user/1
```

**Resposta:**
```json
{
  "success": true,
  "data": [
    {
      "cardId": 1,
      "userId": 1,
      "accountId": 1,
      "cardNumber": "**** **** **** 0366",
      "cardHolderName": "JOÃO SILVA SANTOS",
      "expiryDate": "2025-12-31",
      "cardType": "CREDIT",
      "brand": "Visa",
      "status": "ACTIVE",
      "limitAmount": 5000.00,
      "currentBalance": 0.00
    }
  ]
}
```

**JavaScript:**
```javascript
fetch('http://localhost:8080/api/form/card/user/1')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const cards = data.data;
      console.log('Cartões do usuário:', cards);
    }
  });
```

---

### 4. **Buscar Cartão por ID**

**GET** `/api/form/card/{id}`

Retorna os dados de um cartão específico.

**Exemplo:**
```bash
GET http://localhost:8080/api/form/card/1
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "cardId": 1,
    "userId": 1,
    "accountId": 1,
    "cardNumber": "**** **** **** 0366",
    "cardHolderName": "JOÃO SILVA SANTOS",
    "expiryDate": "2025-12-31",
    "cardType": "CREDIT",
    "brand": "Visa",
    "status": "ACTIVE",
    "limitAmount": 5000.00,
    "currentBalance": 0.00,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
}
```

---

### 5. **Atualizar Cartão**

**PUT** `/api/form/card/{id}`

Atualiza os dados de um cartão.

**Request Body:**
```json
{
  "cardHolderName": "MARIA SILVA",
  "expiryDate": "06/26",
  "status": "ACTIVE",
  "limitAmount": "8000.00"
}
```

**Resposta (200 OK):**
```json
{
  "success": true,
  "data": {
    "cardId": 1,
    "userId": 1,
    "cardNumber": "**** **** **** 0366",
    "cardHolderName": "MARIA SILVA",
    "expiryDate": "2026-06-30",
    "cardType": "CREDIT",
    "brand": "Visa",
    "status": "ACTIVE",
    "limitAmount": 8000.00,
    "currentBalance": 0.00
  }
}
```

---

### 6. **Deletar Cartão**

**DELETE** `/api/form/card/{id}`

Remove um cartão do sistema.

**Exemplo:**
```bash
DELETE http://localhost:8080/api/form/card/1
```

**Resposta (200 OK):**
```json
{
  "success": true,
  "data": "Cartão removido com sucesso"
}
```

---

## 📊 Estrutura dos Dados

### Tipos de Cartão (`CardTypeEnum`)

- **`CREDIT`**: Cartão de crédito
- **`DEBIT`**: Cartão de débito
- **`PREPAID`**: Cartão pré-pago

### Status do Cartão

- **`ACTIVE`**: Ativo (padrão)
- **`BLOCKED`**: Bloqueado
- **`EXPIRED`**: Expirado

### Campos Principais

- **`cardId`**: ID único do cartão
- **`userId`**: ID do usuário dono do cartão (obrigatório)
- **`accountId`**: ID da conta vinculada (opcional)
- **`cardNumber`**: Número do cartão (mascarado nas respostas: `**** **** **** 1234`)
- **`cardHolderName`**: Nome do portador do cartão
- **`expiryDate`**: Data de expiração (formato: `YYYY-MM-DD`)
- **`cvv`**: Código de segurança (opcional, não retornado nas respostas por segurança)
- **`cardType`**: Tipo do cartão (`CREDIT`, `DEBIT`, `PREPAID`)
- **`brand`**: Bandeira (Visa, Mastercard, Elo, etc.)
- **`status`**: Status do cartão (`ACTIVE`, `BLOCKED`, `EXPIRED`)
- **`limitAmount`**: Limite do cartão (para cartões de crédito)
- **`currentBalance`**: Saldo usado (para cartões de crédito)

---

## 🔒 Segurança

### Mascaramento de Número

O número do cartão é **automaticamente mascarado** nas respostas:
- Número original: `4532015112830366`
- Número mascarado: `**** **** **** 0366` (apenas últimos 4 dígitos visíveis)

### CVV

O CVV **não é retornado** nas respostas por segurança. É armazenado apenas durante a criação/atualização.

---

## 💻 Exemplo Completo (Angular/TypeScript)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CardService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Criar cartão
  createCard(cardData: any): Observable<any> {
    return this.http.post<{success: boolean, data: any}>(
      `${this.apiUrl}/form/card`,
      cardData
    ).pipe(
      map(response => response.data)
    );
  }

  // Listar cartões do usuário
  getUserCards(userId: number): Observable<any[]> {
    return this.http.get<{success: boolean, data: any[]}>(
      `${this.apiUrl}/form/card/user/${userId}`
    ).pipe(
      map(response => response.data)
    );
  }

  // Buscar cartão por ID
  getCardById(cardId: number): Observable<any> {
    return this.http.get<{success: boolean, data: any}>(
      `${this.apiUrl}/form/card/${cardId}`
    ).pipe(
      map(response => response.data)
    );
  }

  // Atualizar cartão
  updateCard(cardId: number, cardData: any): Observable<any> {
    return this.http.put<{success: boolean, data: any}>(
      `${this.apiUrl}/form/card/${cardId}`,
      cardData
    ).pipe(
      map(response => response.data)
    );
  }

  // Deletar cartão
  deleteCard(cardId: number): Observable<any> {
    return this.http.delete<{success: boolean, data: string}>(
      `${this.apiUrl}/form/card/${cardId}`
    ).pipe(
      map(response => response.data)
    );
  }

  // Listar tipos de cartão
  getCardTypes(): Observable<any[]> {
    return this.http.get<{success: boolean, data: any[]}>(
      `${this.apiUrl}/form/card/types`
    ).pipe(
      map(response => response.data)
    );
  }
}
```

---

## 📋 Resumo dos Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| **POST** | `/api/form/card` | Criar novo cartão |
| **GET** | `/api/form/card/types` | Listar tipos de cartão |
| **GET** | `/api/form/card/user/{userId}` | Listar cartões do usuário |
| **GET** | `/api/form/card/{id}` | Buscar cartão por ID |
| **PUT** | `/api/form/card/{id}` | Atualizar cartão |
| **DELETE** | `/api/form/card/{id}` | Deletar cartão |

---

## ✅ Checklist

- [x] Endpoint para criar cartão
- [x] Endpoint para listar cartões por usuário
- [x] Endpoint para buscar cartão por ID
- [x] Endpoint para atualizar cartão
- [x] Endpoint para deletar cartão
- [x] Endpoint para listar tipos de cartão
- [x] Mascaramento de número do cartão nas respostas
- [x] Suporte a cartões de crédito, débito e pré-pago
- [x] Vinculação opcional com conta bancária
- [x] Limite de crédito e saldo usado
- [x] Status do cartão (ativo, bloqueado, expirado)

---

**Data:** Janeiro 2025  
**Status:** ✅ Implementado e Funcionando

