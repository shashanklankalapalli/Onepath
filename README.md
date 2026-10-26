# OnePath — Understand. Apply. Stay Updated.

OnePath is a modern full-stack platform that simplifies complicated government, civic, educational, and institutional processes for students and young adults.

---

## 🌟 Key Features

1. **Government & Civic Service Directory**: Clear eligibility, required documents, fee structures, and procedural steps for critical citizen services.
2. **Dedicated College Documentation Hub**: Modular institutional portal allowing students to view college-specific processes (admissions, hostels, certificates, and migration procedures).
3. **Interactive Document Checklist**: Live user checklists to check off ready documents and track progress.
4. **Missing Document Guidance**: Proactive instructions for how and where to replace or obtain missing certificates.
5. **Real-Time Important Updates**: Fast alerts regarding fee changes, deadline extensions, and rule amendments.
6. **Physical Service Centers Locator**: Directory of civic facilitation desks and biometric centers.
7. **Secure Authentication & RBAC**: JWT-based authentication with `USER`, `COLLEGE_ADMIN`, and `SUPER_ADMIN` roles.
8. **Resilient UI Architecture**: Fully dynamic rendering with beautiful loading, empty, and error states across all viewports.

---

## 🏗️ Technology Stack

- **Frontend**: React 18, Vite, TypeScript, Tailwind CSS, React Router v6, Axios, Lucide Icons
- **Backend**: Java 21 / Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security, Spring Validation)
- **Database**: PostgreSQL 14+ (Hibernate DDL Auto / JPA)
- **Security**: BCrypt Password Hashing + JJWT (JSON Web Token)

---

## 📁 Project Structure

```
OnePath/
├── frontend/                     # React + Vite + TypeScript Frontend
│   ├── src/
│   │   ├── api/                  # Axios HTTP client with JWT interceptor
│   │   ├── components/           # Navbar, Footer, EmptyState, ErrorAlert, LoadingSpinner
│   │   ├── context/              # AuthContext & Session management
│   │   ├── pages/                # Home, Services, College Dashboard, Checklist, Updates, Auth
│   │   ├── services/             # API services layer
│   │   ├── types/                # TypeScript interfaces for entities and DTOs
│   │   ├── App.tsx               # Route configurations and protected routes
│   │   ├── main.tsx              # React DOM mounting
│   │   └── index.css             # Tailwind CSS & design tokens
│   ├── package.json
│   ├── vite.config.ts
│   └── .env.example
│
├── backend/                      # Spring Boot 3 REST API Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/onepath/
│   │   │   │   ├── config/       # SecurityConfig, DataInitializer
│   │   │   │   ├── controller/   # REST Controllers (Auth, College, Service, Update, Checklist)
│   │   │   │   ├── dto/          # Clean Request/Response DTOs
│   │   │   │   ├── entity/       # JPA Entities (User, College, CollegeProcess, Service, etc.)
│   │   │   │   ├── exception/    # Centralized GlobalExceptionHandler
│   │   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   │   ├── security/     # JWT Utils, UserPrincipal, Auth Filters
│   │   │   │   └── service/      # Business logic services
│   │   │   └── resources/
│   │   │       └── application.properties
│   ├── pom.xml
│   └── .env.example
│
├── database/
│   └── documentation/
│       ├── schema.sql            # PostgreSQL DDL Reference
│       └── README.md             # Ingestion instructions
│
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started Locally

### 1. Prerequisites
- **Java JDK 21+**
- **Maven 3.8+**
- **Node.js 18+** & **npm**
- **PostgreSQL 14+**

---

### 2. Database Setup (PostgreSQL)

1. Start your PostgreSQL server.
2. Create the database:
   ```sql
   CREATE DATABASE onepath_db;
   ```
3. (Optional) Set your environment variables in PowerShell or `.env`:
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/onepath_db"
   $env:DB_USERNAME="postgres"
   $env:DB_PASSWORD="your_password"
   ```

---

### 3. Running the Backend

Open a terminal in the `backend` directory:
```powershell
cd backend
mvn spring-boot:run
```
The REST API will start on `http://localhost:8080`.

**Default Initial Accounts (Created Automatically on First Run):**
- **Super Administrator**: `admin@onepath.org` / `admin123`
- **Student User**: `student@onepath.org` / `student123`

---

### 4. Running the Frontend

Open another terminal in the `frontend` directory:
```powershell
cd frontend
npm install
npm run dev
```
The frontend UI will be available at `http://localhost:5173`.

---

## 🏛️ Adding Real College Documentation (Future Integration)

The `colleges` and `college_processes` tables are **strictly unpopulated by default**. The application is architected to dynamically render college processes without needing code modifications.

When you are ready to insert real university data:
1. Log in to the frontend with an administrator account (`admin@onepath.org`).
2. Navigate to `/colleges/manage` to register an institution, or use the REST API (`POST /api/colleges` and `POST /api/colleges/{id}/processes`).
3. Alternatively, execute SQL inserts directly using `database/documentation/schema.sql`.

---

## 🔒 Security & Environment Variables

Never commit secrets to source control. Configure your secrets via environment variables:

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC Connection String | `jdbc:postgresql://localhost:5432/onepath_db` |
| `DB_USERNAME` | PostgreSQL User | `postgres` |
| `DB_PASSWORD` | PostgreSQL Password | `postgres` |
| `JWT_SECRET` | 256-bit Secret Key | Configurable in production |
| `JWT_EXPIRATION_MS` | Token Validity (ms) | `86400000` (24 Hours) |
| `VITE_API_BASE_URL` | Frontend API Target | `http://localhost:8080/api` |
