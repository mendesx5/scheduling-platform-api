ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS next_billing_date DATE;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS last_billing_date DATE;
UPDATE subscriptions SET next_billing_date = COALESCE(end_date, start_date + INTERVAL '1 month')::date WHERE next_billing_date IS NULL;
