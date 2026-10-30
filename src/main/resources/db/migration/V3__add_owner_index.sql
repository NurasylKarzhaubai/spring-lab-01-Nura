-- Apply AFTER verifying V1 and V2 and restarting once.
CREATE INDEX idx_accounts_owner ON accounts(owner_name);
