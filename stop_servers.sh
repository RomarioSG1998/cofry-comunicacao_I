#!/bin/bash

# Script para desativar os servidores do projeto Cofry
# Frontend: porta 4200 (Angular dev server)
# Backend: porta 8081 (Java/Tomcat)
# API Server: porta 4000 (Node.js/Express SSR)

echo "🛑 Desativando servidores do projeto Cofry..."
echo ""

# Função para matar processos em uma porta específica
kill_port() {
    local port=$1
    local name=$2
    
    echo "🔍 Verificando porta $port ($name)..."
    
    # Encontrar processos usando a porta
    local pids=$(lsof -ti:$port 2>/dev/null || ss -tlnp 2>/dev/null | grep ":$port " | grep -oP 'pid=\K[0-9]+' | head -1)
    
    if [ -z "$pids" ]; then
        # Tentar com netstat se lsof não funcionar
        pids=$(netstat -tlnp 2>/dev/null | grep ":$port " | grep -oP '[0-9]+/java|[0-9]+/node' | grep -oP '^[0-9]+' | head -1)
    fi
    
    if [ -n "$pids" ]; then
        echo "   ✅ Encontrado processo(es) na porta $port: $pids"
        for pid in $pids; do
            echo "   🗑️  Encerrando processo $pid..."
            kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
        done
        sleep 1
        echo "   ✅ Porta $port liberada"
    else
        echo "   ℹ️  Nenhum processo encontrado na porta $port"
    fi
    echo ""
}

# Função para matar processos Java relacionados ao projeto
kill_java_backend() {
    echo "🔍 Verificando processos Java do backend..."
    
    # Procurar processos Java que possam ser do backend
    local java_pids=$(ps aux | grep -E "Main\.java|org\.example\.Main|Cofry-Back" | grep -v grep | awk '{print $2}')
    
    if [ -n "$java_pids" ]; then
        echo "   ✅ Encontrado processo(es) Java do backend: $java_pids"
        for pid in $java_pids; do
            echo "   🗑️  Encerrando processo Java $pid..."
            kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
        done
        sleep 1
        echo "   ✅ Processos Java do backend encerrados"
    else
        echo "   ℹ️  Nenhum processo Java do backend encontrado"
    fi
    echo ""
}

# Função para matar processos Node relacionados ao frontend
kill_node_frontend() {
    echo "🔍 Verificando processos Node do frontend..."
    
    # Procurar processos Node que possam ser do frontend (ng serve, node server, etc)
    local node_pids=$(ps aux | grep -E "ng serve|node.*server|Cofry-Front" | grep -v grep | awk '{print $2}')
    
    if [ -n "$node_pids" ]; then
        echo "   ✅ Encontrado processo(es) Node do frontend: $node_pids"
        for pid in $node_pids; do
            echo "   🗑️  Encerrando processo Node $pid..."
            kill -TERM $pid 2>/dev/null || kill -9 $pid 2>/dev/null
        done
        sleep 1
        echo "   ✅ Processos Node do frontend encerrados"
    else
        echo "   ℹ️  Nenhum processo Node do frontend encontrado"
    fi
    echo ""
}

# Função para desativar Tomcat do sistema (porta 8080 - não interfere mais)
stop_system_tomcat() {
    echo "🔍 Verificando Tomcat do sistema (porta 8080 - não interfere mais)..."
    
    # Verificar se o serviço tomcat está rodando
    if systemctl is-active --quiet tomcat10 2>/dev/null || systemctl is-active --quiet tomcat9 2>/dev/null || systemctl is-active --quiet tomcat8 2>/dev/null; then
        local tomcat_service=""
        if systemctl is-active --quiet tomcat10 2>/dev/null; then
            tomcat_service="tomcat10"
        elif systemctl is-active --quiet tomcat9 2>/dev/null; then
            tomcat_service="tomcat9"
        elif systemctl is-active --quiet tomcat8 2>/dev/null; then
            tomcat_service="tomcat8"
        fi
        
        if [ -n "$tomcat_service" ]; then
            echo "   ✅ Encontrado serviço Tomcat do sistema: $tomcat_service"
            echo "   🗑️  Parando serviço $tomcat_service..."
            sudo systemctl stop $tomcat_service 2>/dev/null
            if [ $? -eq 0 ]; then
                echo "   ✅ Serviço $tomcat_service parado com sucesso"
            else
                echo "   ⚠️  Não foi possível parar o serviço (pode precisar de sudo)"
                # Tentar matar o processo diretamente
                local tomcat_pid=$(ps aux | grep -E "tomcat.*Bootstrap" | grep -v grep | awk '{print $2}' | head -1)
                if [ -n "$tomcat_pid" ]; then
                    echo "   🗑️  Tentando encerrar processo Tomcat (PID: $tomcat_pid)..."
                    sudo kill -TERM $tomcat_pid 2>/dev/null || sudo kill -9 $tomcat_pid 2>/dev/null
                fi
            fi
        fi
    else
        # Verificar se há processo Tomcat rodando mesmo sem serviço ativo
        local tomcat_pid=$(ps aux | grep -E "tomcat.*Bootstrap" | grep -v grep | awk '{print $2}' | head -1)
        if [ -n "$tomcat_pid" ]; then
            echo "   ✅ Encontrado processo Tomcat (PID: $tomcat_pid)"
            echo "   🗑️  Encerrando processo Tomcat..."
            sudo kill -TERM $tomcat_pid 2>/dev/null || sudo kill -9 $tomcat_pid 2>/dev/null
            if [ $? -eq 0 ]; then
                echo "   ✅ Processo Tomcat encerrado"
            else
                echo "   ⚠️  Não foi possível encerrar o processo (pode precisar de sudo)"
            fi
        else
            echo "   ℹ️  Nenhum Tomcat do sistema encontrado"
        fi
    fi
    echo ""
}

# Desativar servidores
kill_port 4200 "Frontend (Angular dev server)"
kill_port 8081 "Backend (Java/Tomcat)"
kill_port 4000 "API Server (Node.js/Express SSR)"

# Verificar processos específicos
kill_java_backend
kill_node_frontend
stop_system_tomcat

# Verificação final
echo "🔍 Verificação final..."
sleep 2

frontend_running=$(lsof -ti:4200 2>/dev/null || echo "")
backend_running=$(lsof -ti:8081 2>/dev/null || echo "")
api_running=$(lsof -ti:4000 2>/dev/null || echo "")

if [ -z "$frontend_running" ] && [ -z "$backend_running" ] && [ -z "$api_running" ]; then
    echo "✅ Todos os servidores foram desativados com sucesso!"
else
    echo "⚠️  Alguns servidores ainda podem estar rodando:"
    [ -n "$frontend_running" ] && echo "   - Frontend (porta 4200): PID $frontend_running"
    [ -n "$backend_running" ] && echo "   - Backend (porta 8081): PID $backend_running"
    [ -n "$api_running" ] && echo "   - API Server (porta 4000): PID $api_running"
    echo ""
    echo "💡 Tente executar novamente ou use 'kill -9 <PID>' para forçar o encerramento"
fi

echo ""
echo "✨ Concluído!"

