# Endpoint de Ativos - GET /api/assets

## 📋 Resumo

Endpoint criado para retornar todos os ativos salvos no banco de dados, ideal para criar um scroll horizontal automático na página de investimentos.

---

## 🔗 Endpoint

### **GET** `/api/assets`

Retorna lista de ativos ativos (padrão) ou todos os ativos.

**Base URL:** `http://localhost:8080`

---

## 📝 Parâmetros

### Query Parameters (Opcional)

- **`all`** (boolean): 
  - `?all=true` - Retorna todos os ativos (incluindo inativos)
  - Sem parâmetro ou `?all=false` - Retorna apenas ativos ativos (padrão)

---

## 📤 Resposta

### Sucesso (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "ticker": "PETR4",
      "name": "Petrobras PN",
      "categoryId": 1,
      "apiIdentifier": "PETR4.SAO",
      "isActive": true
    },
    {
      "id": 2,
      "ticker": "AAPL",
      "name": "Apple Inc.",
      "categoryId": 2,
      "apiIdentifier": "AAPL",
      "isActive": true
    },
    {
      "id": 3,
      "ticker": "BTC",
      "name": "Bitcoin",
      "categoryId": 3,
      "apiIdentifier": "BTCUSD",
      "isActive": true
    }
  ]
}
```

### Estrutura do Asset

```typescript
interface Asset {
  id: number;              // ID do ativo
  ticker: string;          // Código do ativo (ex: "PETR4", "BTC", "AAPL")
  name: string;            // Nome completo do ativo
  categoryId: number;      // ID da categoria do ativo
  apiIdentifier: string;   // Identificador para buscar preços via API externa
  isActive: boolean;       // Se o ativo está ativo ou não
}
```

---

## 📍 Outros Endpoints Relacionados

### **GET** `/api/assets/{id}`
Busca um ativo específico por ID.

**Exemplo:**
```bash
GET http://localhost:8080/api/assets/1
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "ticker": "PETR4",
    "name": "Petrobras PN",
    "categoryId": 1,
    "apiIdentifier": "PETR4.SAO",
    "isActive": true
  }
}
```

### **GET** `/api/assets/ticker/{ticker}`
Busca um ativo específico por ticker.

**Exemplo:**
```bash
GET http://localhost:8080/api/assets/ticker/PETR4
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "ticker": "PETR4",
    "name": "Petrobras PN",
    "categoryId": 1,
    "apiIdentifier": "PETR4.SAO",
    "isActive": true
  }
}
```

---

## 💻 Exemplos de Uso

### JavaScript/Fetch

```javascript
// Buscar todos os ativos ativos (padrão)
fetch('http://localhost:8080/api/assets')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const assets = data.data;
      // Usar assets para criar scroll horizontal
      console.log('Ativos:', assets);
    }
  })
  .catch(error => console.error('Erro:', error));

// Buscar todos os ativos (incluindo inativos)
fetch('http://localhost:8080/api/assets?all=true')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const allAssets = data.data;
      console.log('Todos os ativos:', allAssets);
    }
  });
```

### Angular/TypeScript

```typescript
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AssetService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Buscar ativos ativos (padrão)
  getActiveAssets(): Observable<Asset[]> {
    return this.http.get<{success: boolean, data: Asset[]}>(`${this.apiUrl}/assets`)
      .pipe(
        map(response => response.data)
      );
  }

  // Buscar todos os ativos
  getAllAssets(): Observable<Asset[]> {
    return this.http.get<{success: boolean, data: Asset[]}>(`${this.apiUrl}/assets?all=true`)
      .pipe(
        map(response => response.data)
      );
  }
}
```

### React/TypeScript

```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

interface Asset {
  id: number;
  ticker: string;
  name: string;
  categoryId: number;
  apiIdentifier: string;
  isActive: boolean;
}

const AssetList = () => {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAssets = async () => {
      try {
        const response = await axios.get<{success: boolean, data: Asset[]}>(
          'http://localhost:8080/api/assets'
        );
        if (response.data.success) {
          setAssets(response.data.data);
        }
      } catch (error) {
        console.error('Erro ao buscar ativos:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAssets();
  }, []);

  if (loading) return <div>Carregando...</div>;

  return (
    <div className="horizontal-scroll">
      {assets.map(asset => (
        <div key={asset.id} className="asset-card">
          <h3>{asset.ticker}</h3>
          <p>{asset.name}</p>
        </div>
      ))}
    </div>
  );
};
```

---

## 🎨 Uso para Scroll Horizontal

### Exemplo de Implementação

```html
<!-- HTML -->
<div class="assets-scroll-container">
  <div class="assets-scroll" id="assetsScroll">
    <!-- Cards de ativos serão inseridos aqui via JavaScript -->
  </div>
</div>

<style>
.assets-scroll-container {
  overflow-x: auto;
  white-space: nowrap;
  padding: 20px;
}

.assets-scroll {
  display: inline-flex;
  gap: 15px;
}

.asset-card {
  min-width: 150px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  display: inline-block;
}
</style>

<script>
fetch('http://localhost:8080/api/assets')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      const container = document.getElementById('assetsScroll');
      data.data.forEach(asset => {
        const card = document.createElement('div');
        card.className = 'asset-card';
        card.innerHTML = `
          <h3>${asset.ticker}</h3>
          <p>${asset.name}</p>
        `;
        container.appendChild(card);
      });
    }
  });
</script>
```

---

## ⚙️ Comportamento

### Por Padrão (Sem Parâmetros)
- Retorna apenas ativos com `isActive = true`
- Ordenados por `ticker` (alfabético)
- Ideal para exibição na interface do usuário

### Com `?all=true`
- Retorna todos os ativos (ativos e inativos)
- Útil para administração ou relatórios completos

---

## ✅ Checklist de Implementação

- [x] Criado `AssetServlet`
- [x] Endpoint `GET /api/assets` implementado
- [x] Suporte a parâmetro `?all=true` para retornar todos
- [x] Endpoint `GET /api/assets/{id}` implementado
- [x] Endpoint `GET /api/assets/ticker/{ticker}` implementado
- [ ] Documentação atualizada na `FRONTEND_API_ROUTES.md`

---

**Data de Criação:** Janeiro 2025
**Status:** ✅ Implementado e Pronto para Uso

