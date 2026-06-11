# MindMetrics

BSc thesis — ELTE Faculty of Informatics  
Elsë Ramizi (LS2FK0), supervisor: Szabó Dávid

Three browser games (Stroop, Memory Span, Dual N-Back). Results go to MySQL. Clinicians can see assigned participants and run simple reports. Spring Boot backend + React frontend.

## Repo

```
Thesis-Report-first-/
├── demo/       backend (Java 21, Spring Boot)
└── frontend/   React + Vite
```

Backend is the usual Spring layout: `controller`, `service`, `repository`, `model`, `dto`.

## Setup

Need Java 21, Maven, Node 20+, MySQL 8.

```sql
CREATE DATABASE IF NOT EXISTS mindmetrics;
```

Copy `.env.example` and fill in passwords locally — don't commit real secrets.

## Run

Backend:

```powershell
cd demo
mvn spring-boot:run
```

→ http://localhost:8080

Frontend:

```powershell
cd frontend
copy .env.example .env
npm install
npm run dev
```

→ http://localhost:5173

## Roles

Register on the login page:

| Role | Does what |
|------|-----------|
| USER | Play tasks, own history and charts |
| CLINICIAN | See participants, reports, export CSV |

## Tasks

1. Stroop — ink colour  
2. Memory Span — digit sequences, adaptive span  
3. Dual N-Back — adaptive N-back, trials saved live  

Each session stores RT, accuracy, full trial log.

## Also in the app

- Dashboard with Chart.js  
- Session history + trial log  
- Clinician profiles, compare sessions, group trends  
- Automated report (rule-based text on accuracy/RT)  
- CSV export (hashed participant IDs)  
- Research studies module (extra, not core thesis)

## Stack

Spring Boot 3, JWT cookies, BCrypt, React 19, Chart.js, MySQL

## Tests

```powershell
cd demo && mvn test
cd frontend && npm run build
```

## Demo at defense

1. Register → play a task → History → trial log  
2. Clinician login → participant → dashboard / group trends  
3. One automated report + CSV export  

If something fails: check MySQL is up and env vars match `.env.example`.
