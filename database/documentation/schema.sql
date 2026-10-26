-- ==========================================================
-- OnePath Database Schema Documentation & Manual DDL
-- Database: PostgreSQL 14+
-- ==========================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    assigned_college_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- 2. Colleges Table
CREATE TABLE IF NOT EXISTS colleges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50),
    location VARCHAR(255),
    website VARCHAR(255),
    logo_url VARCHAR(500),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_college_name ON colleges(name);
CREATE INDEX IF NOT EXISTS idx_college_short_name ON colleges(short_name);

-- 3. College Processes Table
CREATE TABLE IF NOT EXISTS college_processes (
    id BIGSERIAL PRIMARY KEY,
    college_id BIGINT NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    eligibility TEXT,
    timeline VARCHAR(150),
    fees VARCHAR(150),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    important_notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_college_proc_category ON college_processes(category);
CREATE INDEX IF NOT EXISTS idx_college_proc_status ON college_processes(status);

-- 4. College Process Steps Table
CREATE TABLE IF NOT EXISTS college_process_steps (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL REFERENCES college_processes(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    estimated_time VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. College Required Documents Table
CREATE TABLE IF NOT EXISTS college_required_documents (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL REFERENCES college_processes(id) ON DELETE CASCADE,
    document_name VARCHAR(200) NOT NULL,
    description TEXT,
    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    accepted_formats VARCHAR(200),
    how_to_obtain TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. College Process Updates & Circulars Table
CREATE TABLE IF NOT EXISTS college_process_updates (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL REFERENCES college_processes(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    update_type VARCHAR(100),
    effective_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Civic & Government Services Table
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    eligibility TEXT,
    timeline VARCHAR(150),
    fees VARCHAR(150),
    important_notes TEXT,
    official_portal_url VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_service_category ON services(category);
CREATE INDEX IF NOT EXISTS idx_service_slug ON services(slug);

-- 8. Service Steps Table
CREATE TABLE IF NOT EXISTS service_steps (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    estimated_time VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Service Required Documents Table
CREATE TABLE IF NOT EXISTS service_required_documents (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    document_name VARCHAR(200) NOT NULL,
    description TEXT,
    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    accepted_formats VARCHAR(200),
    how_to_obtain TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Platform Updates & Alerts Table
CREATE TABLE IF NOT EXISTS platform_updates (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    related_service_or_process VARCHAR(200),
    effective_date DATE,
    urgent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_update_category ON platform_updates(category);
CREATE INDEX IF NOT EXISTS idx_update_effective_date ON platform_updates(effective_date);

-- 11. Physical Service Centers Table
CREATE TABLE IF NOT EXISTS service_centers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(20),
    service_type VARCHAR(100),
    contact_number VARCHAR(50),
    email VARCHAR(100),
    operating_hours VARCHAR(150),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_center_city ON service_centers(city);
CREATE INDEX IF NOT EXISTS idx_center_service_type ON service_centers(service_type);

-- 12. User Document Checklists Table
CREATE TABLE IF NOT EXISTS user_document_checklists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_missing BOOLEAN NOT NULL DEFAULT FALSE,
    user_notes TEXT,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_checklist_user ON user_document_checklists(user_id);
CREATE INDEX IF NOT EXISTS idx_user_checklist_target ON user_document_checklists(target_type, target_id);
