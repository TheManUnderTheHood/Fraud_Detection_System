ALTER TABLE acounts ADD INDEX idx_accounts_user_id (user_id);
ALTER TABLE transactions ADD INDEX idx_transactions_sender_time (sender_account_id, timestamp);
ALTER TABLE transactions ADD INDEX idx_transactions_pair_time (sender_account_id, receiver_account_id, timestamp);
ALTER TABLE fraud_alerts ADD INDEX idx_alerts_reviewed (reviewed);