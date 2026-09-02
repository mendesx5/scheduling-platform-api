-- Suporte a assinatura anual (com desconto) além da mensal.
ALTER TABLE subscriptions ADD COLUMN billing_cycle VARCHAR(10) NOT NULL DEFAULT 'MONTHLY';
