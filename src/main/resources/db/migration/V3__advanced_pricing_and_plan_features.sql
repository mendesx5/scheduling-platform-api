-- Flexible pricing model while keeping legacy price/duration columns compatible.
ALTER TABLE venues ADD COLUMN pricing_type VARCHAR(20) NOT NULL DEFAULT 'FIXED_SLOT';
ALTER TABLE venues ADD COLUMN base_price NUMERIC(12,2);
ALTER TABLE venues ADD COLUMN slot_duration_minutes INTEGER;
ALTER TABLE venues ADD COLUMN minimum_duration_minutes INTEGER;
ALTER TABLE venues ADD COLUMN maximum_duration_minutes INTEGER;
ALTER TABLE venues ADD COLUMN duration_step_minutes INTEGER;
ALTER TABLE venues ADD COLUMN daily_price NUMERIC(12,2);
ALTER TABLE venues ADD COLUMN minimum_days INTEGER;
ALTER TABLE venues ADD COLUMN maximum_days INTEGER;
ALTER TABLE venues ADD COLUMN max_guests INTEGER;
ALTER TABLE venues ADD COLUMN requires_approval BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE venues ADD COLUMN requires_payment BOOLEAN NOT NULL DEFAULT false;
UPDATE venues SET base_price=price, slot_duration_minutes=duration_minutes WHERE base_price IS NULL;

CREATE TABLE venue_packages (
 id BIGSERIAL PRIMARY KEY,
 tenant_id BIGINT NOT NULL REFERENCES tenants(id),
 venue_id BIGINT NOT NULL,
 name VARCHAR(120) NOT NULL,
 description TEXT,
 duration_minutes INTEGER NOT NULL CHECK(duration_minutes>0),
 price NUMERIC(12,2) NOT NULL CHECK(price>=0),
 active BOOLEAN NOT NULL DEFAULT true,
 FOREIGN KEY (venue_id,tenant_id) REFERENCES venues(id,tenant_id) ON DELETE CASCADE
);
CREATE INDEX idx_venue_packages_tenant_venue ON venue_packages(tenant_id,venue_id);

ALTER TABLE bookings ADD CONSTRAINT uq_bookings_id_tenant UNIQUE (id, tenant_id);

CREATE TABLE addons (
 id BIGSERIAL PRIMARY KEY,
 tenant_id BIGINT NOT NULL REFERENCES tenants(id),
 venue_id BIGINT NOT NULL,
 name VARCHAR(120) NOT NULL,
 description TEXT,
 pricing_type VARCHAR(20) NOT NULL,
 price NUMERIC(12,2) NOT NULL CHECK(price>=0),
 active BOOLEAN NOT NULL DEFAULT true,
 FOREIGN KEY (venue_id,tenant_id) REFERENCES venues(id,tenant_id) ON DELETE CASCADE
);
ALTER TABLE addons ADD CONSTRAINT uq_addons_id_tenant UNIQUE (id, tenant_id);
CREATE INDEX idx_addons_tenant_venue ON addons(tenant_id,venue_id);

CREATE TABLE venue_booking_policies (
 id BIGSERIAL PRIMARY KEY,
 tenant_id BIGINT NOT NULL REFERENCES tenants(id),
 venue_id BIGINT NOT NULL UNIQUE,
 requires_approval BOOLEAN NOT NULL DEFAULT true,
 minimum_advance_minutes INTEGER NOT NULL DEFAULT 0 CHECK(minimum_advance_minutes>=0),
 maximum_advance_days INTEGER NOT NULL DEFAULT 365 CHECK(maximum_advance_days>0),
 cancellation_allowed BOOLEAN NOT NULL DEFAULT true,
 cancellation_deadline_hours INTEGER NOT NULL DEFAULT 24 CHECK(cancellation_deadline_hours>=0),
 FOREIGN KEY (venue_id,tenant_id) REFERENCES venues(id,tenant_id) ON DELETE CASCADE
);

ALTER TABLE bookings ADD COLUMN base_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN addons_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN notes TEXT;
UPDATE bookings SET base_amount=total_amount WHERE base_amount=0;

CREATE TABLE booking_addons (
 id BIGSERIAL PRIMARY KEY,
 tenant_id BIGINT NOT NULL REFERENCES tenants(id),
 booking_id BIGINT NOT NULL,
 addon_id BIGINT NOT NULL,
 addon_name VARCHAR(120) NOT NULL,
 quantity INTEGER NOT NULL CHECK(quantity>0),
 unit_price NUMERIC(12,2) NOT NULL CHECK(unit_price>=0),
 total_price NUMERIC(12,2) NOT NULL CHECK(total_price>=0),
 FOREIGN KEY (booking_id,tenant_id) REFERENCES bookings(id,tenant_id) ON DELETE CASCADE,
 FOREIGN KEY (addon_id,tenant_id) REFERENCES addons(id,tenant_id)
);
CREATE INDEX idx_booking_addons_tenant_booking ON booking_addons(tenant_id,booking_id);

-- Existing early-development FREE tenants become the new entry plan.
UPDATE tenants SET plan='STARTER' WHERE plan IS NULL OR plan='FREE';
UPDATE subscriptions SET plan='STARTER' WHERE plan IS NULL OR plan='FREE';
