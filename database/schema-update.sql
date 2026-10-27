-- Safe SQL migration script for OnePath PostgreSQL / Neon production database
-- Fixes "ERROR: value too long for type character varying(150)"
--
-- Why this is needed:
-- Hibernate with spring.jpa.hibernate.ddl-auto=update does not alter existing column types.
-- If the tables were previously created with VARCHAR(150), this script alters the columns to TEXT.
-- Changing VARCHAR to TEXT in PostgreSQL is safe, instant, and preserves all existing data.

ALTER TABLE services ALTER COLUMN fees TYPE TEXT;
ALTER TABLE services ALTER COLUMN timeline TYPE TEXT;

ALTER TABLE college_processes ALTER COLUMN fees TYPE TEXT;
ALTER TABLE college_processes ALTER COLUMN timeline TYPE TEXT;

-- Safe creation of mentor_requests table for OnePath Mentor Assistance feature
CREATE TABLE IF NOT EXISTS mentor_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    department_or_process VARCHAR(150) NOT NULL,
    problem_description TEXT NOT NULL,
    desired_outcome TEXT NOT NULL,
    preferred_contact_method VARCHAR(30) NOT NULL,
    phone_number VARCHAR(30),
    additional_info TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    admin_notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mentor_request_id ON mentor_requests(request_id);
CREATE INDEX IF NOT EXISTS idx_mentor_status ON mentor_requests(status);
CREATE INDEX IF NOT EXISTS idx_mentor_user_id ON mentor_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_mentor_created_at ON mentor_requests(created_at);

