# Fraud Detection System - 20% Progress Copy

This folder is an independent copy of the core banking and rule-based fraud workflow.
It does not use the original project's database or contain OpenRouter/AI Agent code.

## Included

- Spring Boot 3 backend with MySQL and Flyway
- BCrypt password encryption and JWT authentication
- User registration and login
- Bank account creation and account retrieval
- Owned-account transfer validation, balance locking, and transaction history
- Rule-based fraud alerts:
  - transactions above $100,000
  - transfers above 80% of sender balance
  - more than 5 transfers from one account within 60 seconds
  - 3 or more transfers between the same accounts within 24 hours
- React registration/login, account, transfer, history, and fraud-alert dashboard

## Database setup

Create or start a separate MySQL database. The example URL uses a dedicated database named
`fraud_db_20_percent`; it does not connect to or modify `fraud_db`.

For Docker:

```bash
docker run --name fraud20-mysql \
  -e MYSQL_DATABASE=fraud_db_20_percent \
  -e MYSQL_USER=fraud20 \
  -e MYSQL_PASSWORD=replace_me_locally \
  -e MYSQL_ROOT_PASSWORD=replace_root_password_locally \
  -p 3307:3306 \
  -d mysql:8
```

Copy `Backend/.env.example` to a local untracked environment source, replace the placeholders,
and export its values before starting Spring Boot. A JWT secret must be a valid Base64-encoded
key. Generate one with:

```bash
openssl rand -base64 32
```

Example shell variables:

```bash
export DB_URL='jdbc:mysql://localhost:3307/fraud_db_20_percent?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='fraud20'
export DB_PASSWORD='replace_me_locally'
export JWT_SECRET='replace_with_your_generated_base64_secret'
export JWT_EXPIRATION_MS='3600000'
export CORS_ALLOWED_ORIGIN='http://localhost:5173'
```

Flyway creates and validates the schema. The migration uses the `accounts` table name.

## Run the backend

```bash
cd Backend
bash mvnw spring-boot:run
```

The API listens on `http://localhost:8080`.

## Run the frontend

```bash
cd Frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Copy `Frontend/.env.example` to `Frontend/.env` if the API URL
needs to differ from `http://localhost:8080`.

## API smoke test

Register, create an account, then transfer to a second account:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Test User","email":"test@example.com","password":"Test123!"}'

curl -X POST http://localhost:8080/account/create \
  -H 'Authorization: Bearer <JWT>'
```

## Verification performed

- Separate MySQL 8 database: passed
- Flyway migrations V1 and V2: passed
- Spring Boot 3.2.5 backend build: passed
- React/Vite production build: passed
- Frontend HTTP response: passed
- Registration and BCrypt password storage: passed
- Login and JWT `/auth/me`: passed
- Account creation and retrieval: passed
- Normal $100 transfer: passed and remained unflagged
- $9,000 transfer: passed and generated `LOW_BALANCE_BURST`
- Transaction history persistence: passed
- Fraud alert persistence: passed
- Alert review and admin alert retrieval: passed
- Insufficient balance: passed with HTTP 400
- Cross-user account ownership: passed with HTTP 400

No OpenRouter/API key or AI Agent dependency is included.
