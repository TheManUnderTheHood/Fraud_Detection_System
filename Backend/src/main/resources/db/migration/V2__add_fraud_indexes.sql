ALTER TABLE acounts ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS initiated_by VARCHAR(320);
UPDATE transactions SET initiated_by = 'SYSTEM' WHERE initiated_by IS NULL;
ALTER TABLE transactions MODIFY initiated_by VARCHAR(320) NOT NULL;
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS severity VARCHAR(20) NOT NULL DEFAULT 'HIGH';
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'RULE';
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(320);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS reviewed_at DATETIME(6);

ALTER TABLE acounts ADD INDEX idx_accounts_user_id (user_id);
ALTER TABLE transactions ADD INDEX idx_transactions_sender_time (sender_account_id, timestamp);
ALTER TABLE transactions ADD INDEX idx_transactions_pair_time (sender_account_id, receiver_account_id, timestamp);
ALTER TABLE fraud_alerts ADD INDEX idx_alerts_reviewed (reviewed);