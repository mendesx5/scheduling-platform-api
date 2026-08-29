-- Enables equality operators for BIGINT inside GiST exclusion constraints.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Composite candidate keys make tenant ownership enforceable by foreign keys.
ALTER TABLE venues ADD CONSTRAINT uq_venues_id_tenant UNIQUE (id, tenant_id);
ALTER TABLE customers ADD CONSTRAINT uq_customers_id_tenant UNIQUE (id, tenant_id);

ALTER TABLE availabilities
    ADD CONSTRAINT fk_availability_venue_tenant
        FOREIGN KEY (venue_id, tenant_id) REFERENCES venues (id, tenant_id) ON DELETE CASCADE;

ALTER TABLE blocked_periods
    ADD CONSTRAINT fk_blocked_period_venue_tenant
        FOREIGN KEY (venue_id, tenant_id) REFERENCES venues (id, tenant_id) ON DELETE CASCADE;

ALTER TABLE bookings
    ADD CONSTRAINT fk_booking_venue_tenant
        FOREIGN KEY (venue_id, tenant_id) REFERENCES venues (id, tenant_id),
    ADD CONSTRAINT fk_booking_customer_tenant
        FOREIGN KEY (customer_id, tenant_id) REFERENCES customers (id, tenant_id);

-- Final protection against concurrent requests. Adjacent periods are allowed.
ALTER TABLE bookings
    ADD CONSTRAINT ex_booking_no_overlap
    EXCLUDE USING gist (
        tenant_id WITH =,
        venue_id WITH =,
        tstzrange(start_date_time, end_date_time, '[)') WITH &&
    ) WHERE (status <> 'CANCELLED');
