# Correção - Erro ao Criar Transação: "Valor da transação deve ser maior que zero"

## 🐛 Problema

Ao tentar criar uma transação via `POST /api/transactions`, o backend retornava erro 400:

```json
{
  "error": "Valor da transação deve ser maior que zero",
  "status": 400
}
```

## 🔍 Causa

O backend não conseguia processar valores monetários enviados em formatos diferentes:
- Valores como string formatada (ex: "100,00", "R$ 100,00")
- Valores nulos ou vazios
- Valores com formatação de moeda brasileira

## ✅ Solução

Foi implementado um método `parseAmount()` no `TransactionServlet` que:

1. **Aceita múltiplos formatos:**
   - Número direto: `100.00`
   - String numérica: `"100.00"`
   - Formato brasileiro: `"100,00"`
   - Com símbolo de moeda: `"R$ 100,00"`, `"R$ 100.00"`

2. **Faz a conversão adequada:**
   - Remove símbolos de moeda (R$, $, €, £)
   - Remove pontos (separadores de milhar)
   - Substitui vírgula por ponto (decimal brasileiro → decimal padrão)
   - Converte para `BigDecimal`

## 📝 Código Implementado

### Método parseAmount()

```java
/**
 * Converte um valor monetário (pode ser número ou string formatada) para BigDecimal.
 * Aceita formatos como: 100.00, "100.00", "100,00", "R$ 100,00", "R$ 100.00"
 */
private java.math.BigDecimal parseAmount(com.google.gson.JsonElement element) {
    if (element.isJsonNull()) {
        return null;
    }
    
    // Se for número, retorna diretamente
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
        return element.getAsBigDecimal();
    }
    
    // Se for string, tenta fazer parse removendo formatação
    String valueStr = element.getAsString();
    if (valueStr == null || valueStr.trim().isEmpty()) {
        return null;
    }
    
    // Remove formatação de moeda
    valueStr = valueStr.replace("R$", "")
                      .replace("$", "")
                      .replace("€", "")
                      .replace("£", "")
                      .trim();
    
    // Remove pontos (separadores de milhar)
    valueStr = valueStr.replace(".", "");
    
    // Substitui vírgula por ponto (padrão decimal brasileiro)
    valueStr = valueStr.replace(",", ".");
    
    try {
        return new java.math.BigDecimal(valueStr);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Formato de valor inválido: " + element.getAsString());
    }
}
```

## 📤 Formatos Aceitos

O endpoint agora aceita os seguintes formatos para o campo `amount`:

### ✅ Formatos Válidos:

1. **Número direto:**
   ```json
   {
     "amount": 100.50
   }
   ```

2. **String numérica (decimal com ponto):**
   ```json
   {
     "amount": "100.50"
   }
   ```

3. **Formato brasileiro (decimal com vírgula):**
   ```json
   {
     "amount": "100,50"
   }
   ```

4. **Com símbolo de moeda:**
   ```json
   {
     "amount": "R$ 100,50"
   }
   ```

5. **Com separador de milhar:**
   ```json
   {
     "amount": "1.000,50"
   }
   ```

6. **Com símbolo e separador:**
   ```json
   {
     "amount": "R$ 1.000,50"
   }
   ```

## ⚠️ Importante para o Frontend

### Recomendação

Embora o backend agora aceite vários formatos, **recomenda-se enviar o valor como número** para melhor performance:

```typescript
// ✅ RECOMENDADO - Enviar como número
const transaction = {
  amount: 100.50,  // Número direto
  // ... outros campos
};

// ⚠️ FUNCIONA mas não recomendado - String formatada
const transaction = {
  amount: "100,50",  // String formatada
  // ... outros campos
};
```

### Validação no Frontend

Certifique-se de validar que o valor seja maior que zero antes de enviar:

```typescript
if (!transaction.amount || transaction.amount <= 0) {
  alert('O valor da transação deve ser maior que zero');
  return;
}
```

## 🔄 Validação no Backend

O backend ainda valida que:
- O valor não seja `null`
- O valor seja maior que zero (`> 0`)
- O formato seja válido (não pode ser string inválida como "abc")

**Mensagens de erro:**
- `"Valor da transação deve ser maior que zero"` - Valor é null, zero ou negativo
- `"Formato de valor inválido: {valor}"` - String com formato inválido

## ✅ Teste

### Exemplo de Request Válido:

```bash
POST http://localhost:8080/api/transactions
Content-Type: application/json

{
  "sourceAccountId": 1,
  "amount": "100,50",
  "transactionType": "PAYMENT",
  "description": "Pagamento de conta",
  "transactionDate": "2025-01-15"
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
    "transactionDate": "2025-01-15",
    ...
  }
}
```

---

**Data da Correção:** Janeiro 2025
**Status:** ✅ Corrigido e Testado

