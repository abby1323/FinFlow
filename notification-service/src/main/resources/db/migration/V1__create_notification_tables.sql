CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    message TEXT NOT NULL,
    delivered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'DELIVERED'
);

CREATE INDEX idx_notification_transaction ON notification_logs(transaction_id);
CREATE INDEX idx_notification_wallet ON notification_logs(wallet_id);