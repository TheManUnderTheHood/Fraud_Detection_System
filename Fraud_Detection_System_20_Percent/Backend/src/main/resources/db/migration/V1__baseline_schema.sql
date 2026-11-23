CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance DECIMAL(15, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_account_id BIGINT NOT NULL,
    receiver_account_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    timestamp DATETIME(6),
    status VARCHAR(20) NOT NULL,
    flagged BOOLEAN NOT NULL,
    idempotency_key VARCHAR(100),
    initiated_by VARCHAR(320) NOT NULL,
    CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transactions_receiver FOREIGN KEY (receiver_account_id) REFERENCES accounts(id),
    CONSTRAINT uk_transaction_request UNIQUE (initiated_by, idempotency_key)
);

CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    reviewed BOOLEAN NOT NULL,
    severity VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    reviewed_by VARCHAR(320),
    reviewed_at DATETIME(6),
    created_at DATETIME(6),
    CONSTRAINT fk_alert_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_alert_source UNIQUE (transaction_id, source)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    performed_by VARCHAR(320) NOT NULL,
    timestamp DATETIME(6)
);