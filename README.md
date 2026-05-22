# 🛡️ SecureBank AML: Enterprise Fraud Detection System

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Cloud-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

A full-stack, enterprise-grade Financial Technology (FinTech) application designed to simulate real-world banking transactions and **Anti-Money Laundering (AML)** compliance. 

This system features a custom-built, rule-based **Fraud Detection Engine** that evaluates transactions in real-time, stateless JWT security, ACID-compliant database operations, and a modern React/Tailwind dashboard for both customers and compliance officers.

---

## ✨ Key Features

### 🏦 Core Banking Simulator
* **Account Management:** Users can register, open bank accounts, and view exact balances using precision `BigDecimal` math.
* **ACID Transactions:** Money transfers are wrapped in Spring `@Transactional` to guarantee data integrity (Atomicity, Consistency, Isolation, Durability) even during system failures.
* **Audit Logging:** Every critical action (account creation, transfers, fraud reviews) is permanently logged to an `AuditLog` table for regulatory compliance.

### 🕵️‍♂️ Rule-Based Fraud Engine (AML)
Transactions are intercepted and evaluated against historical data before execution. If flagged, the transfer proceeds, but a permanent `FraudAlert` is generated.
1. **The Threshold Rule:** Flags any transaction exceeding $100,000.
2. **Velocity Check (Smurfing):** Flags if a user attempts > 5 transactions within 60 seconds.
3. **Account Drain (Bust-Out):** Flags if a user attempts to suddenly transfer > 80% of their total available balance.
4. **Layering (Circular Transfers):** Flags repeated transfers to the exact same recipient within a 24-hour window.

### 🔒 Enterprise Security
* **Stateless Authentication:** Implemented JWT (JSON Web Tokens) with a custom Spring Security Filter Chain. No session data is stored on the server.
* **Role-Based Access Control (RBAC):** Distinct `USER` and `ADMIN` roles. The Compliance Center API is strictly protected via `.hasRole("ADMIN")`.
* **Password Cryptography:** All passwords are mathematically hashed using `BCryptPasswordEncoder`.

### 📊 Modern Frontend Dashboard
* **Customer Portal:** View real-time balances, initiate transfers, and visualize cash flow using **Recharts** bar graphs.
* **Compliance Center:** A dedicated Admin panel displaying system-wide KPIs (Key Performance Indicators) and a queue of pending fraud alerts requiring manual review.

---

## 🏗️ Architecture & Tech Stack

**Backend (REST API):**
* Java 17+
* Spring Boot 3.x (Web, Security, Data JPA, Validation)
* JJWT (Java JSON Web Token)
* Hibernate ORM
* MySQL (Hosted on Aiven Cloud)

**Frontend (SPA):**
* React 18 (Vite)
* Tailwind CSS v4
* Lucide React (Icons)
* Recharts (Data Visualization)
* Axios (API Client) & React Toastify

---

## 🚀 Installation & Setup

### Prerequisites
* JDK 17 or higher
* Node.js (v18+)
* MySQL Server (Local or Cloud)

### 1. Database Setup
1. Create a MySQL database named `fraud_db`.
2. Open `src/main/resources/application.properties` and update your database credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fraud_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
