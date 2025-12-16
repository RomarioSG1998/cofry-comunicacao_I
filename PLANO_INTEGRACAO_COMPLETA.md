# 📋 PLANO DE INTEGRAÇÃO COMPLETA - BACKEND E FRONTEND

## 🔍 ANÁLISE ATUAL

### ✅ O QUE JÁ ESTÁ IMPLEMENTADO

#### Backend:
- ✅ Login (POST `/login`) - Funcionando
- ✅ Registro (POST `/auth/Create`) - Funcionando
- ✅ Health Check (GET `/health`) - Funcionando
- ✅ DAOs completos para todas as entidades:
  - UserDAO
  - TransacaoDAO (com buscarPorUsuario)
  - ContaDAO
  - PlanoDAO
  - CategoriaDAO
  - CartaoCreditoDAO
  - InvestimentoDAO
  - BoletoDDADAO
  - MetaPoupancaDAO
  - OrcamentoDAO
  - AssinaturaDAO

#### Frontend:
- ✅ Login component e service
- ✅ Sign-up component e service
- ✅ AuthService para gerenciar estado do usuário
- ✅ TransactionService (estrutura pronta)
- ✅ PlanService (estrutura pronta)
- ✅ UserService (estrutura pronta)
- ✅ FeedbackService (para notificações)

### ❌ O QUE FALTA IMPLEMENTAR

#### Backend - Servlets Faltantes:

1. **Transações** (PRIORIDADE ALTA)
   - GET `/api/transactions/user/:userId` - Listar transações do usuário
   - POST `/api/transactions` - Criar transação
   - PUT `/api/transactions/:id` - Atualizar transação
   - DELETE `/api/transactions/:id` - Deletar transação

2. **Contas** (PRIORIDADE ALTA)
   - GET `/api/accounts/user/:userId` - Listar contas do usuário
   - POST `/api/accounts` - Criar conta
   - PUT `/api/accounts/:id` - Atualizar conta
   - DELETE `/api/accounts/:id` - Deletar conta

3. **Planos/Assinaturas** (PRIORIDADE MÉDIA)
   - GET `/api/subscription-plans` - Listar todos os planos
   - GET `/api/users/:userId/plan` - Obter plano do usuário
   - POST `/api/subscription-plans` - Criar plano

4. **Categorias** (PRIORIDADE MÉDIA)
   - GET `/api/transaction-categories` - Listar categorias

5. **Cartões** (PRIORIDADE MÉDIA)
   - GET `/api/cards/user/:userId` - Listar cartões do usuário
   - POST `/api/cards` - Criar cartão

6. **Investimentos** (PRIORIDADE BAIXA)
   - GET `/api/investments/user/:userId` - Listar investimentos
   - POST `/api/investments` - Criar investimento

7. **Boletos/DDA** (PRIORIDADE BAIXA)
   - GET `/api/boletos/user/:userId` - Listar boletos
   - POST `/api/boletos` - Criar boleto

8. **Dados do Usuário** (PRIORIDADE MÉDIA)
   - GET `/api/user-data/:userId` - Obter dados completos do usuário

#### Frontend - Ajustes Necessários:

1. **URLs dos Serviços**
   - ✅ LoginService - Já corrigido (porta 8080)
   - ✅ SignUpService - Já corrigido (porta 8080)
   - ✅ PlanService - Já corrigido (porta 8080)
   - ✅ TransactionService - Já está na porta 8080 (OK)
   - ✅ AccountService - Já está na porta 8080 (OK)
   - ✅ UserService - Já está na porta 8080 (OK)

2. **Mapeamento de Dados**
   - Ajustar modelo Transaction do frontend para corresponder ao backend
   - Backend usa: `idTrans`, `idUsuario`, `valor` (BigDecimal), `data` (LocalDate)
   - Frontend espera: `id`, `descricao`, `tipo`, `categoria`, `forma_pagamento`, `valor`, `data_hora`

3. **Componentes que Precisam de Integração**
   - AfterLogin - Já usa TransactionService, mas precisa ajustar
   - Plans - Precisa integrar com PlanService
   - Cards - Precisa criar service e integrar
   - Invest - Precisa criar service e integrar
   - Boletos - Precisa criar service e integrar

## 📝 PLANO DE IMPLEMENTAÇÃO

### FASE 1: CRÍTICO (Prioridade Máxima)

#### 1.1 Transações (já tem DAO completo)
- [x] Criar TransactionServlet.java
- [x] Endpoint GET `/api/transactions/user/:userId`
- [x] Endpoint POST `/api/transactions`
- [x] Endpoint PUT `/api/transactions/:id`
- [x] Endpoint DELETE `/api/transactions/:id`
- [x] Registrar no Main.java
- [x] Ajustar modelo Transaction no frontend
- [x] Ajustar TransactionService no frontend
- [ ] Testar integração completa

#### 1.2 Contas (já tem DAO completo)
- [x] Criar AccountServlet.java
- [x] Endpoint GET `/api/accounts/user/:userId`
- [x] Endpoint POST `/api/accounts`
- [x] Endpoint PUT `/api/accounts/:id`
- [x] Endpoint DELETE `/api/accounts/:id`
- [x] Registrar no Main.java
- [x] Criar AccountService no frontend
- [ ] Integrar no AfterLogin para mostrar saldo

### FASE 2: IMPORTANTE (Prioridade Alta)

#### 2.1 Planos/Assinaturas
- [x] Criar PlanServlet.java
- [x] Endpoint GET `/api/subscription-plans`
- [ ] Endpoint GET `/api/users/:userId/plan` (pode ser implementado depois)
- [x] Registrar no Main.java
- [x] Corrigir PlanService no frontend (URL)
- [ ] Integrar no Plans component

#### 2.2 Categorias
- [x] Criar CategoryServlet.java
- [x] Endpoint GET `/api/transaction-categories`
- [x] Registrar no Main.java
- [ ] Usar em formulários de transação

### FASE 3: COMPLEMENTAR (Prioridade Média)

#### 3.1 Cartões
- [ ] Criar CardServlet.java
- [ ] Endpoint GET `/api/cards/user/:userId`
- [ ] Endpoint POST `/api/cards`
- [ ] Registrar no Main.java
- [ ] Criar CardService no frontend
- [ ] Integrar no Cards component

#### 3.2 Investimentos
- [ ] Criar InvestmentServlet.java
- [ ] Endpoint GET `/api/investments/user/:userId`
- [ ] Endpoint POST `/api/investments`
- [ ] Registrar no Main.java
- [ ] Criar InvestmentService no frontend
- [ ] Integrar no Invest component

#### 3.3 Boletos/DDA
- [ ] Criar BoletoServlet.java
- [ ] Endpoint GET `/api/boletos/user/:userId`
- [ ] Endpoint POST `/api/boletos`
- [ ] Registrar no Main.java
- [ ] Criar BoletoService no frontend
- [ ] Integrar no Boletos component

#### 3.4 Dados do Usuário
- [ ] Criar UserDataServlet.java
- [ ] Endpoint GET `/api/user-data/:userId`
- [ ] Registrar no Main.java
- [ ] Corrigir UserService no frontend

## 🔧 PADRÕES A SEGUIR

### Estrutura de Servlets:

```java
public class EntityServlet extends HttpServlet {
    private EntityDAO entityDAO = new EntityDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Ler parâmetros
        // Buscar dados
        // Retornar JSON
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Ler JSON do body
        // Validar dados
        // Salvar no banco
        // Retornar resposta
    }
}
```

### Padrão de Resposta JSON:

```json
{
  "status": "sucesso" | "erro",
  "message": "Mensagem descritiva",
  "data": { ... } // opcional
}
```

### URLs dos Serviços Frontend:

Todos devem usar: `http://localhost:8081/api/...`

## 📊 PRIORIZAÇÃO POR USO

1. **Transações** - Usado no AfterLogin (home) ✅ CRÍTICO
2. **Contas** - Usado para mostrar saldo ✅ CRÍTICO
3. **Planos** - Usado no Plans component ⚠️ IMPORTANTE
4. **Categorias** - Usado em formulários ⚠️ IMPORTANTE
5. **Cartões** - Usado no Cards component 📋 COMPLEMENTAR
6. **Investimentos** - Usado no Invest component 📋 COMPLEMENTAR
7. **Boletos** - Usado no Boletos component 📋 COMPLEMENTAR

## ✅ CHECKLIST FINAL

- [x] Todos os servlets criados (TransactionServlet, AccountServlet, PlanServlet, CategoryServlet, ApiRouterServlet)
- [x] Todos os servlets registrados no Main.java
- [x] ApiRouterServlet criado como solução ernativa para mapeamento
- [x] Todas as URLs dos serviços frontend corrigidas
- [x] Modelos do frontend correspondem aos do backend (com mapeamento)
- [x] Tratamento de erros implementado
- [x] CORS configurado (já está ✅)
- [ ] Testes básicos de todos os endpoints (pendente - problema de mapeamento)
- [ ] Integração testada em cada componente do frontend

## 🔧 SOLUÇÃO IMPLEMENTADA

Foi criado um **ApiRouterServlet** único que roteia todas as requisições `/api/*`. Isso resolve o problema de mapeamento do Tomcat embedded que estava causando 404 nos endpoints individuais.

O ApiRouterServlet:
- Roteia todas as requisições GET, POST, PUT, DELETE para `/api/*`
- Processa internamente e delega para os DAOs apropriados
- Mantém a mesma estrutura de resposta JSON
- Suporta todos os endpoints implementados
