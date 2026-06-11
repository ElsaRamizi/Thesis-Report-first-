# Backend (Spring Boot)

REST API for MindMetrics — BSc thesis, ELTE.

## Run

```bash
mvn spring-boot:run
```

Needs MySQL database `mindmetrics`. See root `.env.example` for `SPRING_DATASOURCE_*`, `JWT_SECRET`, `APP_ENCRYPTION_KEY`.

Default profile is `dev` (see `application-dev.properties`).

## Main API paths

| Path | Purpose |
|------|---------|
| `/api/auth` | register, login, logout, refresh |
| `/api/sessions` | save session, history, metrics |
| `/api/session` | dual n-back live trial saving |
| `/api/user/profile` | participant DOB, notes |
| `/api/user/doctor-connections` | share data with clinician |
| `/api/clinician` | participants, sessions, compare, export, reports |
| `/api/clinician/research` | research studies |
| `/api/research` | participant studies |

## Files I use most for debugging

- `SessionService` — saves trials and metrics  
- `ClinicianService` — clinician views, reports, CSV  
- `AuthService` — login and cookies  
- `SecurityConfig` — roles per URL  
