# Frontend (React)

UI for the MindMetrics thesis app.

## Run

```bash
npm install
npm run dev
```

Copy `.env.example` to `.env`:

```
VITE_API_BASE_URL=http://localhost:8080
```

## Folders

| Folder | What's there |
|--------|----------------|
| `pages/` | Route screens |
| `features/stroop/` | Stroop task |
| `features/memorySpan/` | Memory span task |
| `features/dualNBack/` | N-back task |
| `features/results/` | Charts and trial log |
| `services/` | API calls |

New page: add to `pages/`, then `routes/AppRoutes.jsx` and maybe `config/navigation.js`.
