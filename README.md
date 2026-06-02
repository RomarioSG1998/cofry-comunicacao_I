# 🚀 Cofry - Dockerized Multi-Service Environment

This repository is fully containerized using **Docker** and **Docker Compose**, orchestrating the frontend, backend, and database services to allow seamless local development and production-like runs.

---

## 🏗️ Architecture Overview

The application consists of three main containerized services:

| Service | Port (Host) | Port (Internal) | Technology / Stack | Description |
| :--- | :---: | :---: | :--- | :--- |
| **`cofry-frontend`** | `4000` | `4000` | Node.js 20 & Angular 21 (SSR) | Server-Side Rendered Angular application |
| **`cofry-backend`** | `8082` | `8082` | Eclipse Temurin JDK 21 (Tomcat Embedded) | Java application running Servlet handlers |
| **`cofry-db`** | `5433` | `5432` | PostgreSQL 15 | Local database (initialized automatically) |

---

## ⚡ Quick Start

### 1. Prerequisites
Ensure you have **Docker** and **Docker Compose** installed and running on your system.

### 2. Run the application
To build and start all containers in detached mode, run:
```bash
docker compose up -d --build
```

This command will:
1. Initialize the PostgreSQL database container.
2. Compile and package the Java backend code with Maven, package dependencies, and start Tomcat.
3. Install dependencies and compile the Angular SSR frontend, then start the Node SSR server.

---

## 🔍 Database Auto-Initialization (Flyway Migrations)
Database migrations are fully managed by **Flyway** within the `cofry-backend` container. On startup, Flyway automatically detects the database connection and runs:
- **`V1__schema.sql`**: Sets up the tables (`usuario`, `transacao`, `plano`, `conta`, etc.) with proper PostgreSQL auto-incrementing serial primary keys.
- **`V2__seed.sql`**: Inserts initial seed data, including default subscription plans, category types (receitas/despesas), test user credentials (`romario@cofry.com`), and test bank accounts.

---

## ⚙️ Configuration & Environment Variables

The backend application supports dynamic database configuration. You can switch between the **local containerized PostgreSQL** and the **AWS RDS instance** by adjusting the environment variables inside `docker-compose.yml`:

### Local Database Configuration (Default)
```yaml
environment:
  DB_URL: jdbc:postgresql://cofry-db:5432/postgres
  DB_USER: postgres
  DB_PASS: jala.0725
```

### AWS RDS Database Configuration
To connect directly to the AWS RDS database instead:
```yaml
environment:
  DB_URL: jdbc:postgresql://cofry-db.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/postgres
  DB_USER: postgres
  DB_PASS: jala.0725
```

---

## 🛠️ Handy Operations

### Inspect running services
```bash
docker compose ps
```

### Check live application logs
* **All services:**
  ```bash
  docker compose logs -f
  ```
* **Backend only:**
  ```bash
  docker compose logs -f cofry-backend
  ```
* **Frontend only:**
  ```bash
  docker compose logs -f cofry-frontend
  ```

### Stop the services
To shut down the application without losing local database data:
```bash
docker compose down
```

To shut down and wipe the local database volume (clean reset):
```bash
docker compose down -v
```
