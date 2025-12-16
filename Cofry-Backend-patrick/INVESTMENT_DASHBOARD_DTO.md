# DTOs do Dashboard de Investimentos

## 📋 Resumo

DTOs criados para exibir o dashboard de investimentos com cards de ativos e histórico de ordens.

---

## 🎯 DTOs Criados

### 1. **AssetCardDTO**
DTO para representar um card individual de ativo no dashboard.

**Campos:**
```java
private Integer assetId;         // ID do ativo
private String ticker;           // Código do ativo (ex: "PETR4", "BTC", "AAPL")
private String assetName;        // Nome do ativo (ex: "Petrobras PN", "Bitcoin", "Apple Inc.")
private BigDecimal currentPrice; // Preço atual do ativo
private BigDecimal priceChange;  // Variação percentual (pode ser positivo ou negativo)
private String iconUrl;          // URL do ícone/logo (opcional)
private String iconColor;        // Cor de fundo do ícone (opcional, ex: "#FFD700")
private String currency;         // Moeda (padrão: "BRL")
```

**Exemplo de JSON:**
```json
{
  "assetId": 1,
  "ticker": "PETR4",
  "assetName": "Petrobras PN",
  "currentPrice": 34.50,
  "priceChange": 2.4,
  "iconUrl": null,
  "iconColor": "#FFD700",
  "currency": "BRL"
}
```

**Métodos Auxiliares:**
- `isPositiveChange()` - Retorna `true` se a variação é positiva (preço subiu)

---

### 2. **OrderHistoryItemDTO**
DTO para representar uma linha do histórico de ordens.

**Campos:**
```java
private Integer transactionId; // ID da transação
private String assetTicker;    // Código do ativo (ex: "PETR4", "BTC")
private String assetName;      // Nome do ativo (opcional)
private String type;           // "Compra" ou "Venda"
private LocalDate date;        // Data da ordem
private BigDecimal value;      // Valor total da transação (R$)
private String status;         // "Executada", "Pendente", "Cancelada"
```

**Exemplo de JSON:**
```json
{
  "transactionId": 1,
  "assetTicker": "PETR4",
  "assetName": "Petrobras PN",
  "type": "Compra",
  "date": "2025-10-10",
  "value": 1200.00,
  "status": "Executada"
}
```

**Métodos Auxiliares:**
- `isBuy()` - Retorna `true` se a ordem é do tipo "Compra"
- `isSell()` - Retorna `true` se a ordem é do tipo "Venda"

---

### 3. **InvestmentDashboardDTO**
DTO principal que agrupa cards de ativos e histórico de ordens.

**Campos:**
```java
private Integer userId;                    // ID do usuário
private List<AssetCardDTO> assetCards;     // Lista de cards de ativos
private List<OrderHistoryItemDTO> orderHistory; // Lista de ordens do histórico
```

**Exemplo de JSON Completo:**
```json
{
  "userId": 1,
  "assetCards": [
    {
      "assetId": 1,
      "ticker": "PETR4",
      "assetName": "Petrobras PN",
      "currentPrice": 34.50,
      "priceChange": 2.4,
      "iconUrl": null,
      "iconColor": "#FFD700",
      "currency": "BRL"
    },
    {
      "assetId": 2,
      "ticker": "AAPL",
      "assetName": "Apple Inc.",
      "currentPrice": 890.20,
      "priceChange": 0.8,
      "iconUrl": "https://example.com/apple-logo.png",
      "iconColor": "#808080",
      "currency": "BRL"
    },
    {
      "assetId": 3,
      "ticker": "BTC",
      "assetName": "Bitcoin",
      "currentPrice": 345000.00,
      "priceChange": -1.2,
      "iconUrl": null,
      "iconColor": "#FF8C00",
      "currency": "BRL"
    }
  ],
  "orderHistory": [
    {
      "transactionId": 1,
      "assetTicker": "PETR4",
      "assetName": "Petrobras PN",
      "type": "Compra",
      "date": "2025-10-10",
      "value": 1200.00,
      "status": "Executada"
    },
    {
      "transactionId": 2,
      "assetTicker": "BTC",
      "assetName": "Bitcoin",
      "type": "Venda",
      "date": "2025-10-08",
      "value": 500.00,
      "status": "Executada"
    }
  ]
}
```

---

## 🔄 Relação com DTOs Existentes

### InvestmentTransactionResponseDTO
O `InvestmentTransactionResponseDTO` existente pode ser convertido para `OrderHistoryItemDTO` quando necessário:

**Mapeamento:**
- `transactionId` → `id`
- `assetTicker` → `assetTicker`
- `assetName` → `assetName`
- `type` → `type` (já está como "Compra" ou "Venda")
- `transactionDate` → `date` (converter LocalDateTime para LocalDate)
- `totalValue` → `value`
- `status` → `status`

---

## 📝 Exemplos de Uso no Backend

### Exemplo 1: Criar AssetCardDTO
```java
AssetCardDTO petr4Card = new AssetCardDTO();
petr4Card.setAssetId(1);
petr4Card.setTicker("PETR4");
petr4Card.setAssetName("Petrobras PN");
petr4Card.setCurrentPrice(new BigDecimal("34.50"));
petr4Card.setPriceChange(new BigDecimal("2.4"));
petr4Card.setIconColor("#FFD700"); // Amarelo
petr4Card.setCurrency("BRL");
```

### Exemplo 2: Criar OrderHistoryItemDTO
```java
OrderHistoryItemDTO order = new OrderHistoryItemDTO();
order.setTransactionId(1);
order.setAssetTicker("PETR4");
order.setAssetName("Petrobras PN");
order.setType("Compra");
order.setDate(LocalDate.of(2025, 10, 10));
order.setValue(new BigDecimal("1200.00"));
order.setStatus("Executada");
```

### Exemplo 3: Criar InvestmentDashboardDTO Completo
```java
List<AssetCardDTO> cards = Arrays.asList(petr4Card, appleCard, btcCard);
List<OrderHistoryItemDTO> history = Arrays.asList(order1, order2);

InvestmentDashboardDTO dashboard = new InvestmentDashboardDTO();
dashboard.setUserId(1);
dashboard.setAssetCards(cards);
dashboard.setOrderHistory(history);
```

---

## 🎨 Formatação no Frontend

### Cards de Ativos

**Exemplo de exibição:**
- **Ticker:** Exibir em destaque (ex: "PETR4")
- **Nome:** Exibir em fonte menor e mais clara (ex: "Petrobras PN")
- **Preço:** Formatar como moeda brasileira (ex: "R$ 34,50")
- **Variação:** 
  - Se positiva: verde com "+2.4%"
  - Se negativa: vermelho com "-1.2%"
- **Ícone:** Exibir quadrado com cor de fundo e texto do ticker ou logo se disponível

### Histórico de Ordens

**Tabela com colunas:**
- **ATIVO:** Ticker do ativo (ex: "PETR4", "BTC")
- **TIPO:** Badge colorido
  - "Compra" → verde
  - "Venda" → vermelho
- **DATA:** Formatar como "10 Out 2025" (DD MMM YYYY)
- **VALOR:** Formatar como moeda brasileira (ex: "R$ 1.200,00")
- **STATUS:** 
  - "Executada" → círculo verde + texto
  - "Pendente" → círculo amarelo + texto
  - "Cancelada" → círculo vermelho + texto

---

## 📍 Endpoints Sugeridos

### GET /api/investments/dashboard/user/{userId}
Retorna o dashboard completo com cards de ativos e histórico de ordens.

**Resposta:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "assetCards": [...],
    "orderHistory": [...]
  }
}
```

---

## ✅ Checklist de Implementação

- [x] Criar `AssetCardDTO`
- [x] Criar `OrderHistoryItemDTO`
- [x] Criar `InvestmentDashboardDTO`
- [ ] Criar Service para montar o dashboard
- [ ] Criar endpoint REST para retornar o dashboard
- [ ] Implementar lógica para buscar preços atuais dos ativos
- [ ] Implementar cálculo de variação percentual
- [ ] Implementar conversão de `InvestmentTransactionResponseDTO` para `OrderHistoryItemDTO`

---

**Data de Criação:** Janeiro 2025
**Status:** ✅ DTOs Criados

