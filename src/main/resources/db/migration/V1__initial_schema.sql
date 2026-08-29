CREATE TABLE tenants (
 id BIGSERIAL PRIMARY KEY, name VARCHAR(120) NOT NULL, slug VARCHAR(80) NOT NULL UNIQUE,
 logo_url VARCHAR(500), cover_url VARCHAR(500), primary_color VARCHAR(20), phone VARCHAR(30),
 instagram VARCHAR(100), address VARCHAR(255), status VARCHAR(20) NOT NULL, plan VARCHAR(30) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), name VARCHAR(120) NOT NULL,
 email VARCHAR(180) NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL,
 UNIQUE (tenant_id, email)
);
CREATE TABLE platform_admins (
 id BIGSERIAL PRIMARY KEY, name VARCHAR(120) NOT NULL, email VARCHAR(180) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL
);
CREATE TABLE venues (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), name VARCHAR(120) NOT NULL,
 description TEXT, type VARCHAR(50) NOT NULL, price NUMERIC(12,2) NOT NULL, duration_minutes INTEGER NOT NULL, active BOOLEAN NOT NULL DEFAULT true
);
CREATE INDEX idx_venues_tenant ON venues(tenant_id);
CREATE TABLE availabilities (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), venue_id BIGINT NOT NULL REFERENCES venues(id) ON DELETE CASCADE,
 day_of_week VARCHAR(12) NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL,
 CHECK (start_time < end_time)
);
CREATE TABLE blocked_periods (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), venue_id BIGINT NOT NULL REFERENCES venues(id) ON DELETE CASCADE,
 start_date_time TIMESTAMPTZ NOT NULL, end_date_time TIMESTAMPTZ NOT NULL, reason VARCHAR(255),
 CHECK (start_date_time < end_date_time)
);
CREATE TABLE customers (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), name VARCHAR(120) NOT NULL,
 phone VARCHAR(30) NOT NULL, email VARCHAR(180), UNIQUE (tenant_id, phone)
);
CREATE TABLE bookings (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), venue_id BIGINT NOT NULL REFERENCES venues(id),
 customer_id BIGINT NOT NULL REFERENCES customers(id), start_date_time TIMESTAMPTZ NOT NULL, end_date_time TIMESTAMPTZ NOT NULL,
 status VARCHAR(20) NOT NULL, total_amount NUMERIC(12,2) NOT NULL, payment_status VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), CHECK (start_date_time < end_date_time)
);
CREATE INDEX idx_booking_conflict ON bookings(tenant_id, venue_id, start_date_time, end_date_time);
CREATE TABLE subscriptions (
 id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL UNIQUE REFERENCES tenants(id), plan VARCHAR(30) NOT NULL,
 status VARCHAR(20) NOT NULL, start_date DATE NOT NULL, end_date DATE
);
