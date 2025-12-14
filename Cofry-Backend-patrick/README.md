# 🏦 Cofry Backend

API REST desenvolvida em Java para gerenciamento financeiro pessoal. Sistema completo de controle de finanças com funcionalidades de contas bancárias, transações, orçamentos, metas de poupança e planos de assinatura.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Executando a Aplicação](#executando-a-aplicação)
- [Endpoints da API](#endpoints-da-api)
- [Estrutura do Banco de Dados](#estrutura-do-banco-de-dados)
- [Padrões de Resposta](#padrões-de-resposta)
- [Segurança](#segurança)

## 🎯 Sobre o Projeto

O **Cofry Backend** é uma API REST desenvolvida em Java que fornece serviços para um sistema de gestão financeira pessoal. A aplicação permite:

- ✅ Cadastro e autenticação de usuários
- 💰 Gerenciamento de contas bancárias
- 📊 Controle de transações financeiras
- 📈 Criação e acompanhamento de orçamentos
- 🎯 Definição de metas de poupança
- 📦 Gestão de planos de assinatura
- 🏠 Cadastro de endereços
- 📁 Categorização de transações

## 🛠 Tecnologias

- **Java 21** - Linguagem de programação
- **Maven** - Gerenciamento de dependências
- **PostgreSQL** - Banco de dados relacional
- **Jakarta Servlet API 4.0.1** - Framework web
- **Hibernate 5.6.15** - ORM (Object-Relational Mapping)
- **Gson 2.10.1** - Serialização/Deserialização JSON
- **BCrypt** - Criptografia de senhas
- **Docker** - Containerização (opcional)

## 🏗 Arquitetura

A aplicação segue uma arquitetura em camadas (Layered Architecture):

```
┌─────────────────────────────────────┐
│         Controllers (API)           │  ← Endpoints REST
├─────────────────────────────────────┤
│         Services (Lógica)          │  ← Regras de negócio
├─────────────────────────────────────┤
│         DAO (Data Access)          │  ← Acesso ao banco
├─────────────────────────────────────┤
│         Models (Entidades)         │  ← Mapeamento JPA
├─────────────────────────────────────┤
│      PostgreSQL Database           │  ← Banco de dados
└─────────────────────────────────────┘
```

### Estrutura de Pastas

```
src/main/java/org/example/
├── Config/              # Configurações (CORS, etc.)
├── Controller/          # Controllers REST (10 endpoints)
├── DAO/                 # Data Access Objects (8 DAOs)
├── Model/               # Entidades JPA (8 models)
├── Service/             # Serviços de negócio (2 services)
├── Utils/               # Utilitários (validação, criptografia, etc.)
└── domain/
    ├── DOM/             # Domain Objects (respostas padronizadas)
    └── DTOS/            # Data Transfer Objects
```

## 📦 Pré-requisitos

Antes de começar, você precisa ter instalado:

- **Java JDK 21** ou superior
- **Maven 3.6+**
- **PostgreSQL 12+**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code) - opcional
- **Git** - para clonar o repositório

## ⚙️ Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/Patick-gu/Cofry-Backend.git
cd Cofry-Backend
```

### 2. Configure o Banco de Dados

1. Crie um banco de dados PostgreSQL chamado `Cofry-local`:

```sql
CREATE DATABASE "Cofry-local";
```

2. Execute o script SQL para criar as tabelas:

```bash
psql -U postgres -d Cofry-local -f CofryLocal.sql
```

### 3. Configure as Credenciais

Edite o arquivo `ConnectionFactory.java` com suas credenciais do PostgreSQL:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/Cofry-local";
private static final String USER = "postgres";  // Seu usuário
private static final String PASS = "root";      // Sua senha
```

**Localização:** `src/main/java/org/example/Persistence/ConnectionFactory.java`

### 4. Instale as Dependências

```bash
mvn clean install
```

## 🚀 Executando a Aplicação

### Opção 1: Via Maven (Tomcat Embedded)

```bash
mvn clean package
mvn tomcat7:run
```

### Opção 2: Via IDE

1. Abra o projeto na sua IDE
2. Configure o servidor (Tomcat, Jetty, etc.)
3. Execute a aplicação

### Opção 3: Via Docker

```bash
docker build -t cofry-backend .
docker run -p 8080:8080 cofry-backend
```

A aplicação estará disponível em: `http://localhost:8080`

## 📡 Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/Create` | Cria um novo usuário |
| POST | `/login` | Autentica um usuário |

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/user-data/*` | Obtém dados do usuário |

### Contas Bancárias

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/accounts/*` | Lista contas |
| POST | `/api/accounts/*` | Cria uma conta |
| PUT | `/api/accounts/*` | Atualiza uma conta |
| DELETE | `/api/accounts/*` | Remove uma conta |

### Transações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/transactions/*` | Lista transações |
| POST | `/api/transactions/*` | Cria uma transação |
| PUT | `/api/transactions/*` | Atualiza uma transação |
| DELETE | `/api/transactions/*` | Remove uma transação |

### Categorias de Transação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/transaction-categories/*` | Lista categorias |
| POST | `/api/transaction-categories/*` | Cria uma categoria |

### Orçamentos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/budgets/*` | Lista orçamentos |
| POST | `/api/budgets/*` | Cria um orçamento |
| PUT | `/api/budgets/*` | Atualiza um orçamento |

### Metas de Poupança

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/savings-goals/*` | Lista metas |
| POST | `/api/savings-goals/*` | Cria uma meta |
| PUT | `/api/savings-goals/*` | Atualiza uma meta |

### Planos de Assinatura

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/subscription-plans/*` | Lista planos |
| POST | `/api/subscription-plans/*` | Cria um plano |

### Endereços

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/addresses/*` | Lista endereços |
| POST | `/api/addresses/*` | Cria um endereço |
| PUT | `/api/addresses/*` | Atualiza um endereço |

## 💾 Estrutura do Banco de Dados

O banco de dados possui as seguintes tabelas principais:

- **users** - Usuários do sistema
- **accounts** - Contas bancárias
- **addresses** - Endereços dos usuários
- **transactions** - Transações financeiras
- **transaction_categories** - Categorias de transação
- **budgets** - Orçamentos
- **savings_goals** - Metas de poupança
- **subscription_plans** - Planos de assinatura

### Tipos ENUM

- `account_type_enum`: CHECKING, SAVINGS
- `transaction_type_enum`: DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT
- `goal_status_enum`: IN_PROGRESS, COMPLETED, PAUSED

## 📝 Padrões de Resposta

A API utiliza classes DOM (Domain Object Model) para padronizar todas as respostas:

### Resposta de Sucesso

```json
{
  "status": "sucesso",
  "message": "Operação realizada com sucesso!"
}
```

### Resposta com Dados

```json
{
  "status": "sucesso",
  "message": "Dados encontrados",
  "data": {
    "userId": 1,
    "firstName": "João",
    "lastName": "Silva",
    "email": "joao@example.com"
  }
}
```

### Resposta de Erro

```json
{
  "status": "erro",
  "message": "Mensagem de erro descritiva"
}
```

### Resposta de Login

```json
{
  "status": "sucesso",
  "message": "Login realizado com sucesso!",
  "data": {
    "userId": 1,
    "firstName": "João",
    "lastName": "Silva",
    "email": "joao@example.com"
  }
}
```

## 🔒 Segurança

### Criptografia de Senhas

As senhas são criptografadas usando **BCrypt** antes de serem armazenadas no banco de dados:

```java
String hashedPassword = PasswordUtils.hashPassword(password);
```

### Validações

- **CPF**: Validação de CPF brasileiro
- **Email**: Validação de formato de email
- **Senha**: Verificação de força da senha
- **Campos obrigatórios**: Validação de campos requeridos

### CORS

A aplicação possui um filtro CORS configurado para permitir requisições do frontend:

```java
@WebFilter("/*")
public class CorsFilter implements Filter {
    // Configuração CORS para desenvolvimento
}
```

**⚠️ Atenção:** Em produção, configure o CORS para permitir apenas origens específicas.

## 📚 Exemplos de Uso

### Criar Usuário

```bash
curl -X POST http://localhost:8080/auth/Create \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João Silva",
    "taxId": "12345678900",
    "email": "joao@example.com",
    "password": "senha123",
    "dateOfBirth": "1990-01-01"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123"
  }'
```

## 🧪 Testes

Arquivos de teste JSON estão disponíveis em `tests/api/` para validação dos endpoints.

## 📄 Licença

Este projeto é privado e de propriedade do desenvolvedor.

## 👥 Contribuidores

- **Patrick** - Desenvolvedor principal

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório.

---

**Desenvolvido com ❤️ para gestão financeira pessoal**
