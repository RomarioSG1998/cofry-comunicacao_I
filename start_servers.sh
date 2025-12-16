#!/bin/bash

# Script para iniciar todos os servidores do projeto Cofry
# Segue o padrão correto de portas para evitar conflitos
#
# Portas configuradas:
# - Frontend: 4200 (Angular dev server)
# - Backend: 8081 (Java/Tomcat) - Evita conflito com Tomcat do sistema na 8080
# - API Server: 4000 (Node.js/Express SSR)

set -e  # Parar em caso de erro

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Diretórios do projeto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/Cofry-Front-main"
BACKEND_DIR="$PROJECT_ROOT/Cofry-Backend-patrick"

# Portas
FRONTEND_PORT=4200
BACKEND_PORT=8081
API_PORT=4000

# Arquivos de log
FRONTEND_LOG="/tmp/cofry-frontend.log"
BACKEND_LOG="/tmp/cofry-backend.log"
API_LOG="/tmp/cofry-api-server.log"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}🚀 Iniciando Servidores do Projeto Cofry${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Função para verificar se uma porta está em uso
check_port() {
    local port=$1
    local name=$2
    
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Porta $port ($name) já está em uso${NC}"
        lsof -i :$port | head -2
        return 1
    else
        echo -e "${GREEN}✅ Porta $port ($name) está livre${NC}"
        return 0
    fi
}

# Função para matar processo em uma porta
kill_port() {
    local port=$1
    local name=$2
    
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pids" ]; then
        echo -e "${YELLOW}🗑️  Liberando porta $port ($name)...${NC}"
        for pid in $pids; do
            kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
        done
        sleep 2
    fi
}

# Verificar e limpar portas
echo -e "${BLUE}📋 Verificando portas...${NC}"
echo ""

check_port $FRONTEND_PORT "Frontend" || kill_port $FRONTEND_PORT "Frontend"
check_port $BACKEND_PORT "Backend" || kill_port $BACKEND_PORT "Backend"
check_port $API_PORT "API Server" || kill_port $API_PORT "API Server"

echo ""
echo -e "${BLUE}🔧 Iniciando servidores...${NC}"
echo ""

# 1. Iniciar Backend (Java/Tomcat)
echo -e "${BLUE}1. Iniciando Backend (porta $BACKEND_PORT)...${NC}"
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}❌ Diretório do backend não encontrado: $BACKEND_DIR${NC}"
    exit 1
fi

cd "$BACKEND_DIR"
echo "   Compilando backend..."
mvn clean compile -q >/dev/null 2>&1 || {
    echo -e "${RED}❌ Erro ao compilar backend${NC}"
    exit 1
}

echo "   Iniciando servidor Tomcat..."
mvn exec:java -Dexec.mainClass="org.example.Main" > "$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!
echo "   Backend iniciado (PID: $BACKEND_PID)"
echo "   Log: $BACKEND_LOG"
sleep 5

# 2. Iniciar Frontend (Angular)
echo ""
echo -e "${BLUE}2. Iniciando Frontend (porta $FRONTEND_PORT)...${NC}"
if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}❌ Diretório do frontend não encontrado: $FRONTEND_DIR${NC}"
    exit 1
fi

cd "$FRONTEND_DIR"
echo "   Instalando dependências (se necessário)..."
if [ ! -d "node_modules" ]; then
    npm install --silent >/dev/null 2>&1
fi

echo "   Iniciando servidor de desenvolvimento..."
npm start > "$FRONTEND_LOG" 2>&1 &
FRONTEND_PID=$!
echo "   Frontend iniciado (PID: $FRONTEND_PID)"
echo "   Log: $FRONTEND_LOG"
sleep 8

# 3. Iniciar API Server (SSR) - opcional
echo ""
echo -e "${BLUE}3. API Server (porta $API_PORT) - Opcional${NC}"
echo "   Para iniciar o API Server SSR, execute após build:"
echo "   cd $FRONTEND_DIR && npm run serve:ssr:Cofry-FrontEnd"

echo ""
echo -e "${BLUE}⏳ Aguardando servidores iniciarem...${NC}"
sleep 5

# Verificar status
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}✅ Status dos Servidores${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Verificar Backend
if lsof -i :$BACKEND_PORT >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend (porta $BACKEND_PORT): RODANDO${NC}"
    echo "   URL: http://localhost:$BACKEND_PORT"
    echo "   Health: http://localhost:$BACKEND_PORT/health"
    
    # Testar endpoint
    if curl -s http://localhost:$BACKEND_PORT/health >/dev/null 2>&1; then
        echo "   Status: Respondendo"
    else
        echo -e "   ${YELLOW}Status: Iniciando... (pode levar alguns segundos)${NC}"
    fi
else
    echo -e "${RED}❌ Backend (porta $BACKEND_PORT): NÃO ESTÁ RODANDO${NC}"
    echo "   Verifique os logs: tail -f $BACKEND_LOG"
fi

echo ""

# Verificar Frontend
if lsof -i :$FRONTEND_PORT >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend (porta $FRONTEND_PORT): RODANDO${NC}"
    echo "   URL: http://localhost:$FRONTEND_PORT"
    
    # Testar endpoint
    if curl -s http://localhost:$FRONTEND_PORT >/dev/null 2>&1; then
        echo "   Status: Respondendo"
    else
        echo -e "   ${YELLOW}Status: Iniciando... (pode levar alguns segundos)${NC}"
    fi
else
    echo -e "${RED}❌ Frontend (porta $FRONTEND_PORT): NÃO ESTÁ RODANDO${NC}"
    echo "   Verifique os logs: tail -f $FRONTEND_LOG"
fi

echo ""

# Verificar API Server
if lsof -i :$API_PORT >/dev/null 2>&1; then
    echo -e "${GREEN}✅ API Server (porta $API_PORT): RODANDO${NC}"
    echo "   URL: http://localhost:$API_PORT"
else
    echo -e "${YELLOW}ℹ️  API Server (porta $API_PORT): Não iniciado (opcional)${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}📋 Informações${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "🔗 URLs de acesso:"
echo "   Frontend:  http://localhost:$FRONTEND_PORT"
echo "   Backend:   http://localhost:$BACKEND_PORT"
echo "   API Server: http://localhost:$API_PORT"
echo ""
echo "📝 Logs:"
echo "   Frontend:  tail -f $FRONTEND_LOG"
echo "   Backend:   tail -f $BACKEND_LOG"
echo "   API Server: tail -f $API_LOG"
echo ""
echo "🛑 Para parar todos os servidores:"
echo "   ./stop_servers.sh"
echo ""
echo -e "${GREEN}✅ Servidores iniciados!${NC}"
echo ""
echo "💡 Dica: Se o login não funcionar, verifique:"
echo "   1. Se o backend está respondendo: curl http://localhost:$BACKEND_PORT/health"
echo "   2. Se há erros nos logs do backend"
echo "   3. Se o CORS está configurado corretamente"
echo ""

