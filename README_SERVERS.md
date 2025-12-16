# 🚀 Guia de Inicialização dos Servidores

Este documento descreve como iniciar e gerenciar os servidores do projeto Cofry.

## 📋 Portas Configuradas

O projeto usa **3 portas** para evitar conflitos:

| Servidor | Porta | Descrição |
|----------|-------|-----------|
| **Frontend** | 4200 | Angular Dev Server |
| **Backend** | 8081 | Java/Tomcat (evita conflito com Tomcat do sistema na 8080) |
| **API Server** | 4000 | Node.js/Express SSR (opcional) |

## 🚀 Iniciar Todos os Servidores

### Método Recomendado (Script Automatizado)

```bash
./start_servers.sh
```

Este script:
- ✅ Verifica se as portas estão livres
- ✅ Para processos conflitantes automaticamente
- ✅ Inicia o Backend na porta 8081
- ✅ Inicia o Frontend na porta 4200
- ✅ Verifica se tudo está funcionando
- ✅ Mostra status e URLs de acesso

### Método Manual

Se preferir iniciar manualmente:

```bash
# 1. Backend (em um terminal)
cd Cofry-Backend-patrick
mvn exec:java -Dexec.mainClass="org.example.Main"

# 2. Frontend (em outro terminal)
cd Cofry-Front-main
npm start

# 3. API Server (opcional, após build)
cd Cofry-Front-main
npm run serve:ssr:Cofry-FrontEnd
```

## 🛑 Parar Todos os Servidores

```bash
./stop_servers.sh
```

Este script para todos os servidores nas portas:
- 4200 (Frontend)
- 8081 (Backend)
- 4000 (API Server)

## 🔍 Verificar Status

### Verificar se os servidores estão rodando:

```bash
# Verificar portas
lsof -i :4200  # Frontend
lsof -i :8081  # Backend
lsof -i :4000  # API Server
```

### Testar endpoints:

```bash
# Backend Health Check
curl http://localhost:8081/health

# Backend Login (teste)
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test"}'

# Frontend
curl http://localhost:4200
```

## 📝 Logs

Os logs são salvos em:

- Frontend: `/tmp/cofry-frontend.log`
- Backend: `/tmp/cofry-backend.log`
- API Server: `/tmp/cofry-api-server.log`

Para ver logs em tempo real:

```bash
tail -f /tmp/cofry-frontend.log   # Frontend
tail -f /tmp/cofry-backend.log    # Backend
tail -f /tmp/cofry-api-server.log # API Server
```

## ⚠️ Problemas Comuns

### 1. Porta já em uso

Se uma porta estiver em uso, o script `start_servers.sh` tenta liberá-la automaticamente. Se não conseguir:

```bash
# Ver qual processo está usando a porta
lsof -i :8081

# Parar o processo manualmente
kill -9 <PID>
```

### 2. Backend não responde

Se o backend não estiver respondendo:

1. Verifique os logs: `tail -f /tmp/cofry-backend.log`
2. Verifique se está rodando: `lsof -i :8081`
3. Reinicie: `./stop_servers.sh && ./start_servers.sh`

### 3. Login não funciona

Se o login não funcionar:

1. Verifique se o backend está na porta **8081** (não 8080)
2. Verifique se o frontend está configurado para `http://localhost:8081`
3. Verifique o console do navegador para erros de CORS
4. Teste o endpoint diretamente: `curl -X POST http://localhost:8081/login ...`

### 4. Conflito com Tomcat do sistema

O backend está configurado para usar a porta **8081** para evitar conflito com o Tomcat do sistema que geralmente usa a porta 8080. Se ainda houver problemas:

```bash
# Parar Tomcat do sistema (requer sudo)
sudo systemctl stop tomcat10
```

## 🔗 URLs de Acesso

Após iniciar os servidores:

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8081
- **Health Check:** http://localhost:8081/health
- **API Server:** http://localhost:4000 (se iniciado)

## 📚 Estrutura de Comunicação

```
Frontend (4200) ──HTTP──> Backend (8081)
                              │
                              ├──> /login
                              ├──> /auth/Create
                              └──> /api/*
```

O CORS está configurado para permitir requisições do frontend (4200) para o backend (8081).

## ✅ Checklist de Inicialização

- [ ] Executar `./start_servers.sh`
- [ ] Verificar se Backend está rodando (porta 8081)
- [ ] Verificar se Frontend está rodando (porta 4200)
- [ ] Testar Health Check: `curl http://localhost:8081/health`
- [ ] Abrir Frontend no navegador: http://localhost:4200
- [ ] Testar login no frontend

## 🎯 Boas Práticas

1. **Sempre use o script `start_servers.sh`** para garantir que as portas corretas são usadas
2. **Verifique os logs** se algo não estiver funcionando
3. **Use `stop_servers.sh`** antes de reiniciar para evitar processos duplicados
4. **Mantenha as portas padrão** (4200, 8081, 4000) para evitar problemas de configuração

---

**Última atualização:** Backend configurado para porta 8081 para evitar conflito com Tomcat do sistema.

