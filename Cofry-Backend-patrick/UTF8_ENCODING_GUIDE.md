# Guia de Garantia UTF-8 para Nome do Banco

Este documento descreve as configurações implementadas para garantir que o nome do banco seja salvo e retornado corretamente em UTF-8, suportando caracteres especiais como acentos.

## ✅ Configurações Implementadas

### 1. **Web.xml - Encoding de Request/Response**
O arquivo `web.xml` já está configurado para UTF-8:
```xml
<request-character-encoding>UTF-8</request-character-encoding>
<response-character-encoding>UTF-8</response-character-encoding>
```

### 2. **JsonResponse - Encoding UTF-8**
O `JsonResponse` já está configurado para enviar respostas com encoding UTF-8:
```java
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
```

### 3. **RequestParser - Leitura UTF-8**
O `RequestParser` usa `request.getReader()` que respeita o encoding configurado no `web.xml` (UTF-8).

### 4. **PostgreSQL Database**
Para PostgreSQL, o encoding é configurado no nível do database. Certifique-se de que o database foi criado com UTF-8:

```sql
-- Verificar encoding atual do database
SELECT datname, pg_encoding_to_char(encoding) as encoding 
FROM pg_database 
WHERE datname = 'Cofry-local';

-- Se não for UTF8, recriar o database:
-- DROP DATABASE "Cofry-local";
-- CREATE DATABASE "Cofry-local" 
--   WITH ENCODING 'UTF8' 
--   LC_COLLATE='pt_BR.UTF-8' 
--   LC_CTYPE='pt_BR.UTF-8' 
--   TEMPLATE=template0;
```

### 5. **ConnectionFactory**
O `ConnectionFactory` usa a URL padrão do PostgreSQL. O driver JDBC do PostgreSQL automaticamente usa o encoding do database se ele estiver configurado corretamente.

## 🔍 Verificação

### Verificar encoding do database:
```sql
SELECT datname, pg_encoding_to_char(encoding) as encoding 
FROM pg_database 
WHERE datname = 'Cofry-local';
```

### Verificar encoding das colunas:
```sql
SELECT 
    column_name, 
    data_type, 
    character_set_name,
    collation_name
FROM information_schema.columns 
WHERE table_name = 'accounts' 
  AND table_schema = 'public'
  AND column_name IN ('bank_name', 'bank_code')
ORDER BY ordinal_position;
```

## 📝 Exemplo de Uso

Ao criar uma conta com nome de banco que contém caracteres especiais:

**Request:**
```json
{
  "userId": 1,
  "bankCode": "001",
  "bankName": "Banco do Brasil",
  "agency": "1596",
  "accountNumber": "75614-9",
  "accountType": "CHECKING"
}
```

**Response (UTF-8 garantido):**
```json
{
  "success": true,
  "data": {
    "accountId": 1,
    "userId": 1,
    "bankCode": "001",
    "bankName": "Banco do Brasil",
    "agency": "1596",
    "accountNumber": "75614-9",
    "accountType": "CHECKING",
    "balance": 0.00,
    "status": "ACTIVE"
  }
}
```

## ⚠️ Importante

1. **O database PostgreSQL deve estar criado com encoding UTF-8** para garantir que os dados sejam salvos corretamente.

2. **O driver JDBC do PostgreSQL** automaticamente usa o encoding do database, então não é necessário adicionar parâmetros na URL (como seria necessário no MySQL).

3. **As configurações de HTTP** (web.xml) garantem que a comunicação HTTP use UTF-8.

4. **O Gson** por padrão usa UTF-8 para serialização/deserialização JSON.

## 🛠️ Script de Verificação

Execute o script `database-scripts/ensure-utf8-encoding.sql` para verificar o encoding atual do database e das colunas.


