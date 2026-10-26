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
