# OnePath Database Architecture & Integration Guide

## 1. Overview
The OnePath database is built on **PostgreSQL 14+** using **Spring Data JPA** with Hibernate ORM (`ddl-auto: update` or manual SQL migrations).

### Architecture Highlights
- **Strict Separation of Concerns**: Civic/Government services and College-specific processes are isolated into dedicated entity trees.
- **Zero Hardcoded Data**: All UI renders dynamically based on records returned by the REST APIs.
- **Empty State Support**: When no colleges or processes are in the database, the frontend renders informative, aesthetic empty states without crashing.

---

## 2. Ingesting Real College Documentation (When Ready)

When you are ready to add real college processes to the platform, you have two flexible methods:

### Option A: Via REST API / Admin Interface
1. Start the backend (`mvn spring-boot:run`) and frontend (`npm run dev`).
2. Log in using an account with `SUPER_ADMIN` or `COLLEGE_ADMIN` privileges.
3. Use the `/colleges/manage` endpoint or send POST requests to:
   - `POST /api/colleges`
   - `POST /api/colleges/{collegeId}/processes`
   - `POST /api/colleges/{collegeId}/processes/{processId}/steps`
   - `POST /api/colleges/{collegeId}/processes/{processId}/documents`

### Option B: Direct SQL Ingestion
Insert the records into the tables in order of foreign key relationships:
```sql
-- 1. Insert College
INSERT INTO colleges (name, short_name, location, website, description, active)
VALUES ('Indian Institute of Science', 'IISc', 'Bengaluru, Karnataka', 'https://iisc.ac.in', 'Premier institute for scientific research and higher education.', true)
RETURNING id; -- e.g. Returns 1

-- 2. Insert Process
INSERT INTO college_processes (college_id, title, category, eligibility, timeline, fees, status, description)
VALUES (1, 'M.Tech / M.Des Admissions Onboarding', 'Admissions', 'Valid GATE score and Bachelor degree in Engineering/Technology.', 'April - June', '₹500', 'ACTIVE', 'Complete documentation submission and document verification process.');

-- 3. Insert Steps and Documents
INSERT INTO college_process_steps (process_id, step_number, title, description, estimated_time)
VALUES (1, 1, 'Online Portal Registration', 'Register on the institutional admissions portal with your Application Number.', '10 minutes');

INSERT INTO college_required_documents (process_id, document_name, mandatory, accepted_formats, how_to_obtain)
VALUES (1, 'Degree Certificate / Provisional Degree', true, 'PDF (Max 2MB)', 'Obtain from your undergraduate registrar or university examination portal.');
```
