# Correção - Erro ao Criar Transação: Formato de Data

## 🐛 Problema

Ao tentar criar uma transação via `POST /api/transactions`, o backend retornava erro 500:

```
Text '2025-12-16T06:50:00.000Z' could not be parsed, unparsed text found at index 10
```

## 🔍 Causa

O frontend estava enviando a data no formato ISO 8601 completo com hora e timezone:
- `"2025-12-16T06:50:00.000Z"`

Mas o backend esperava apenas a data no formato:
- `"2025-12-16"` (YYYY-MM-DD)

## ✅ Solução

Foi implementado um método `parseDate()` que aceita múltiplos formatos de data:

1. **Formato simples (YYYY-MM-DD):** `"2025-12-16"`
2. **Formato ISO completo:** `"2025-12-16T06:50:00.000Z"`
3. **Formato ISO sem timezone:** `"2025-12-16T06:50:00"`

O método extrai automaticamente apenas a parte da data quando recebe um formato com hora.

---

## 📝 Código Implementado

### Método parseDate()

```java
/**
 * Faz parse de uma data que pode vir em diferentes formatos:
 * - "2025-12-16" (apenas data)
 * - "2025-12-16T06:50:00.000Z" (ISO 8601 completo)
 * - "2025-12-16T06:50:00" (ISO sem timezone)
 */
private LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.trim().isEmpty()) {
        return null;
    }
    
    try {
        // Tenta primeiro o formato padrão (apenas data)
        if (dateStr.length() == 10) {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        }
        
        // Se tem 'T', é formato ISO completo - extrai apenas a parte da data
        if (dateStr.contains("T")) {
            String dateOnly = dateStr.substring(0, 10); // Pega os primeiros 10 caracteres (YYYY-MM-DD)
            return LocalDate.parse(dateOnly, DATE_FORMATTER);
        }
        
        // Tenta parse direto
        return LocalDate.parse(dateStr);
    } catch (Exception e) {
        throw new IllegalArgumentException("Formato de data inválido: " + dateStr + ". Use o formato YYYY-MM-DD");
    }
}
```

---

## 📤 Formatos Aceitos

O endpoint agora aceita os seguintes formatos para o campo `transactionDate`:

### ✅ Formatos Válidos:

1. **Formato simples (recomendado):**
   ```json
   {
     "transactionDate": "2025-12-16"
   }
   ```

2. **Formato ISO completo com timezone:**
   ```json
   {
     "transactionDate": "2025-12-16T06:50:00.000Z"
   }
   ```

3. **Formato ISO sem timezone:**
   ```json
   {
     "transactionDate": "2025-12-16T06:50:00"
   }
   ```

4. **Sem campo (usa data atual):**
   ```json
   {
     "amount": 100.00,
     "description": "Transação"
     // transactionDate omitido → usa data atual
   }
   ```

---

## 💻 Exemplos de Uso

### Frontend - Angular/TypeScript

**Formato ISO (como vem do DatePicker do Angular):**
```typescript
const transaction = {
  sourceAccountId: 1,
  amount: 100.00,
  description: "Transação",
  transactionDate: new Date().toISOString() // "2025-12-16T06:50:00.000Z"
};
```

**Formato simples:**
```typescript
const transaction = {
  sourceAccountId: 1,
  amount: 100.00,
  description: "Transação",
  transactionDate: "2025-12-16" // Formato simples
};
```

**Sem data (usa hoje):**
```typescript
const transaction = {
  sourceAccountId: 1,
  amount: 100.00,
  description: "Transação"
  // transactionDate omitido → backend usa data atual
};
```

### JavaScript/Fetch

```javascript
// Com data ISO (vem naturalmente do JavaScript)
const transaction = {
  sourceAccountId: 1,
  amount: 100.00,
  description: "Transação",
  transactionDate: new Date().toISOString() // Funciona agora!
};

fetch('http://localhost:8080/api/transactions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(transaction)
});
```

---

## ⚠️ Importante

### Recomendação

Embora o backend agora aceite vários formatos, **recomenda-se usar o formato simples** (`YYYY-MM-DD`) quando possível:

```typescript
// ✅ RECOMENDADO - Formato simples
transactionDate: "2025-12-16"

// ⚠️ FUNCIONA mas não necessário - Formato ISO completo
transactionDate: "2025-12-16T06:50:00.000Z"
```

**Nota:** O backend sempre armazena apenas a data (sem hora), então a parte da hora é ignorada mesmo quando enviada no formato ISO.

---

## 🔄 Comportamento

### Validação

O backend valida que:
- O formato seja reconhecível (YYYY-MM-DD ou ISO completo)
- A data seja válida

**Mensagens de erro:**
- `"Formato de data inválido: {data}. Use o formato YYYY-MM-DD"` - Formato não reconhecido

### Valores Padrão

- Se `transactionDate` não for fornecido → usa a data atual (`LocalDate.now()`)

---

## ✅ Teste

### Exemplo de Request Válido (formato ISO):

```bash
POST http://localhost:8080/api/transactions
Content-Type: application/json

{
  "sourceAccountId": 1,
  "amount": "100,50",
  "transactionType": "PAYMENT",
  "description": "Pagamento de conta",
  "transactionDate": "2025-12-16T06:50:00.000Z"
}
```

### Exemplo de Response (Sucesso):

```json
{
  "success": true,
  "data": {
    "transactionId": 1,
    "sourceAccountId": 1,
    "amount": 100.50,
    "transactionType": "PAYMENT",
    "description": "Pagamento de conta",
    "transactionDate": "2025-12-16",
    ...
  }
}
```

**Nota:** A resposta sempre retorna a data no formato simples (`YYYY-MM-DD`), mesmo que tenha sido enviada no formato ISO completo.

---

**Data da Correção:** Janeiro 2025
**Status:** ✅ Corrigido e Testado

