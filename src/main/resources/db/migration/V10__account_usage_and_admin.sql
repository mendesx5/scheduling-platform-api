ALTER TABLE users
  ADD COLUMN active BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN username VARCHAR(80),
  ADD COLUMN language VARCHAR(10) NOT NULL DEFAULT 'pt-BR';

CREATE UNIQUE INDEX ux_users_tenant_username ON users(tenant_id, lower(username))
  WHERE username IS NOT NULL;

ALTER TABLE tenants
  ADD COLUMN timezone VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
  ADD COLUMN date_format VARCHAR(30) NOT NULL DEFAULT 'dd/MM/yyyy',
  ADD COLUMN time_format VARCHAR(10) NOT NULL DEFAULT '24h',
  ADD COLUMN week_starts_on VARCHAR(10) NOT NULL DEFAULT 'MONDAY',
  ADD COLUMN notify_new_booking BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN notify_cancellation BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN notify_booking_reminder BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN notify_email BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN notify_whatsapp BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE tenant_usage_daily (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  usage_date DATE NOT NULL,
  api_requests BIGINT NOT NULL DEFAULT 0,
  bookings_created BIGINT NOT NULL DEFAULT 0,
  bookings_cancelled BIGINT NOT NULL DEFAULT 0,
  active_users BIGINT NOT NULL DEFAULT 0,
  customers_created BIGINT NOT NULL DEFAULT 0,
  revenue NUMERIC(14,2) NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, usage_date)
);
CREATE INDEX idx_usage_daily_tenant_date ON tenant_usage_daily(tenant_id, usage_date);

CREATE TABLE admin_audit_logs (
  id BIGSERIAL PRIMARY KEY,
  admin_id BIGINT REFERENCES platform_admins(id),
  tenant_id BIGINT REFERENCES tenants(id),
  action VARCHAR(80) NOT NULL,
  details TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_admin_audit_tenant ON admin_audit_logs(tenant_id, created_at);

CREATE TABLE password_reset_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
  platform_admin_id BIGINT REFERENCES platform_admins(id) ON DELETE CASCADE,
  token_hash VARCHAR(128) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((user_id IS NOT NULL AND platform_admin_id IS NULL) OR (user_id IS NULL AND platform_admin_id IS NOT NULL))
);
CREATE INDEX idx_reset_token_hash ON password_reset_tokens(token_hash);

ALTER TABLE subscriptions ADD COLUMN pending_plan VARCHAR(30), ADD COLUMN pending_billing_cycle VARCHAR(10), ADD COLUMN previous_asaas_subscription_id VARCHAR(80);
