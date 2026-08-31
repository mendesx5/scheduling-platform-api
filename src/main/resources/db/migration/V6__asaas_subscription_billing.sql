ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS asaas_customer_id VARCHAR(80);
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS asaas_subscription_id VARCHAR(80);
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS asaas_checkout_id VARCHAR(120);
