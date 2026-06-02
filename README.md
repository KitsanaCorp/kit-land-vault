# 🏦 KitLandVault - Family Expense & Bucket System Tracker

**KitLandVault** is a personal and family finance management application designed around the **Zero-Based Budgeting (Bucket System)** concept. It features a modern 6-bucket financial allocation structure (Transit -> Destination Flow), a shared spending module (**Co-pay & Partner Settlement**), and inter-family loan/repayment trackers.

---

## 🛠️ Tech Stack

* **Frontend:** Angular v21 (Standalone Components, PWA, Tailwind CSS v4, Chart.js)
* **Backend:** Java Spring Boot v3.4 (Maven, Spring Security JWT, JPA Hibernate)
* **Database:** PostgreSQL v15
* **Infrastructure:** Docker & Docker Compose

---

## 💻 Local Installation and Setup Guide

Follow these steps to install and run the project locally on your machine:

### 📋 Prerequisites
1. **Docker Desktop** (To run the PostgreSQL database container)
2. **Java JDK 17 or newer** (JDK 21+ is highly recommended for Spring Boot 3.4)
3. **Node.js** (LTS 20+ recommended) and **npm**

---

### Step 1: Start the PostgreSQL Database (Docker)
We have prepared a pre-configured PostgreSQL container under the `infra/` folder:

1. Open your terminal and navigate to the `infra/` directory:
   ```bash
   cd infra
   ```
2. Start the database container in background mode:
   ```bash
   docker compose up -d
   ```
   * *Note:* The database will run on port `5432` with username `postgres`, password `password`, and create a default database named `kitlandvault`.

---

### Step 2: Build & Run the Backend (Spring Boot)
1. Open a new terminal window and navigate to the `backend/` directory:
   ```bash
   cd backend
   ```
2. Start the Spring Boot application:
   * **For Windows (PowerShell/CMD):**
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   * **For macOS / Linux:**
     ```bash
     chmod +x mvnw
     ./mvnw spring-boot:run
     ```
   * *Note:* On its initial run, Hibernate will automatically generate all required tables in the database. The **DataInitializer** will also trigger to seed mock data. The backend server will listen on port `8080` ([http://localhost:8080](http://localhost:8080)).

---

### Step 3: Build & Run the Frontend (Angular)
1. Open another terminal window and navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Install npm dependencies (required only on first setup):
   ```bash
   npm install
   ```
3. Run the Angular development server:
   * **For macOS / Linux / Standard CMD:**
     ```bash
     npm start
     ```
   * **For Windows PowerShell** (if blocked by execution policies):
     ```powershell
     cmd.exe /c npm run start
     ```
   * *Note:* The frontend server will run on port `4200` ([http://localhost:4200](http://localhost:4200)). The Angular server is pre-configured with a proxy (via `/api`) to forward backend requests automatically to `http://localhost:8080`.

---

## 🔑 Seeded User Accounts (Default Credentials)

Upon the first successful startup, the database is initialized with two default accounts:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `KitVault@2026` | `ADMIN` | Main administrator account with pre-seeded wallets and goals. |
| `kit` | `password123` | `USER` | Standard user demo account. |

---

## 📂 Project Directory Structure

```
/kit-land-vault
  ├── /frontend      # Angular v21 client application
  ├── /backend       # Spring Boot v3.4 API backend application
  ├── /infra         # Docker Compose configuration & DB setup
  ├── ARCHITECTURE.md.txt  # Core database schema and entity mappings documentation
  └── README.md      # This guide
```

---

## 💡 Development Tips
* **Database Inspection:** You can connect to your local PostgreSQL instance at `localhost:5432` using DB managers like DBeaver or pgAdmin. Connection parameters can be found in `backend/src/main/resources/application.yml`.
* **API Security & Testing:** The backend APIs are protected via Spring Security with JWT. When calling secured endpoints (e.g. via Postman), first request a JWT token by posting to `/api/auth/login`.
