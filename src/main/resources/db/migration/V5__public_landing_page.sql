CREATE TABLE tenant_page_settings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    template VARCHAR(20) NOT NULL DEFAULT 'MODERN',
    background_color VARCHAR(20),
    surface_color VARCHAR(20),
    text_color VARCHAR(20),
    primary_color VARCHAR(20),
    secondary_color VARCHAR(20),
    accent_color VARCHAR(20),
    hero_title VARCHAR(255),
    hero_subtitle TEXT,
    hero_image_url TEXT,
    hero_cta_text VARCHAR(120),
    about_title VARCHAR(255),
    about_text TEXT,
    about_image_url TEXT,
    inclusions_title VARCHAR(255),
    inclusions_subtitle TEXT,
    gallery_title VARCHAR(255),
    gallery_subtitle TEXT,
    show_about BOOLEAN NOT NULL DEFAULT true,
    show_venues BOOLEAN NOT NULL DEFAULT true,
    show_inclusions BOOLEAN NOT NULL DEFAULT true,
    show_gallery BOOLEAN NOT NULL DEFAULT true,
    show_location BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE tenant_page_gallery_images (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_page_gallery_tenant_sort ON tenant_page_gallery_images(tenant_id, sort_order);

CREATE TABLE tenant_page_highlights (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    icon VARCHAR(80),
    sort_order INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_page_highlights_tenant_sort ON tenant_page_highlights(tenant_id, sort_order);
