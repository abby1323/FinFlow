CREATE TABLE fraud_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    rule_triggered VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fraud_transaction ON fraud_alerts(transaction_id);
CREATE INDEX idx_fraud_wallet ON fraud_alerts(wallet_id);
CREATE INDEX idx_fraud_status ON fraud_alerts(status);

CREATE TABLE transaction_counts (
    wallet_id UUID NOT NULL,
    window_start TIMESTAMP NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (wallet_id, window_start)
);