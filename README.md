![CI](https://github.com/Khushbujamliya/job-match-tracker/actions/workflows/ci.yml/badge.svg)

# AI Job-Match & Referral Tracker

An AI-powered backend that matches resumes to job postings using vector embeddings, and tracks referral links with click analytics — built to demonstrate backend system design, not just CRUD.

## What it does

- Stores resumes and job postings, and generates AI embeddings for each using a local LLM (Ollama + `nomic-embed-text`)
- Finds the best-matching jobs for a resume using MongoDB Atlas **Vector Search** (semantic similarity, not keyword matching)
- Generates short, trackable referral links for job applications
- Tracks link clicks **asynchronously** via Kafka, so the redirect never waits on a database write
- Caches hot link lookups in **Redis** to avoid repeated database reads
- Exposes a small analytics dashboard (top links, per-job link stats)

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Language / Framework | Java 21, Spring Boot 4 | Industry-standard backend stack |
| Database | MongoDB (+ Atlas Vector Search) | Document model fits variable-shaped resume/job data; built-in vector search avoids a separate vector DB |
| AI / Embeddings | Ollama (`nomic-embed-text`), local | Free, no API key, runs offline |
| Messaging | Apache Kafka (KRaft mode) | Decouples the redirect from the click-count write — the redirect never blocks on Mongo |
| Caching | Redis | Cache-aside pattern for hot short-link lookups |
| Containerization | Docker, Docker Compose | Entire stack (`mongo`, `kafka`, `redis`, the app itself) starts with one command |
| CI | GitHub Actions + Testcontainers | Every push is built and tested against a real, disposable MongoDB |

## Architecture

Client
│
▼
Spring Boot App ──────► MongoDB (resumes, jobs, matches, short links)
│ │
│ └──► Redis (short-link cache, cache-aside)
│
└──► Kafka (link-clicks topic) ──► Consumer ──► MongoDB (click count update)


- **Resume/Job matching**: text → Ollama embedding → stored as a vector field → MongoDB `$vectorSearch` aggregation finds the closest jobs by cosine similarity.
- **Link redirects**: `GET /r/{code}` checks Redis first (cache-aside); on a miss, reads MongoDB and populates the cache. Either way, it publishes a click event to Kafka and returns immediately — the click count is updated by a separate consumer, off the request path.
- **Referential integrity**: MongoDB has no foreign keys. Resume/Job/Match relationships are stored by id only (never embedded), and the application layer checks existence before saving a match.

## Running it locally

**Prerequisite:** install [Ollama](https://ollama.com) natively and pull the embedding model — it isn't containerized:

```bash
ollama pull nomic-embed-text
```

Then start everything else with one command:

```bash
docker compose up -d --build
```

This brings up the Spring Boot app, MongoDB, Kafka, and Redis together. The API is available at `http://localhost:8080`.

**One-time setup:** the MongoDB vector search index must be created manually (MongoDB doesn't support this via a Java annotation):

```bash
docker exec -it jobmatch-mongo mongosh "mongodb://jobmatch:jobmatch123@localhost:27017/jobmatch?authSource=admin"
```

```javascript
db.jobs.createSearchIndex("jobs_vector_index", "vectorSearch", {
  fields: [{ type: "vector", path: "embedding", numDimensions: 768, similarity: "cosine" }]
})
```

## API overview

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/resumes` | POST, GET | Create / list resumes (generates an embedding on create) |
| `/api/resumes/{id}` | GET, DELETE | Fetch / delete a resume |
| `/api/jobs` | POST, GET | Create / list jobs (generates an embedding on create) |
| `/api/jobs/{id}` | GET, DELETE | Fetch / delete a job |
| `/api/resumes/{id}/matches` | GET | Vector-search the best-matching jobs for a resume |
| `/api/resumes/{id}/match-history` | GET | Previously computed matches for a resume |
| `/api/links` | POST | Create a short, trackable link |
| `/r/{code}` | GET | Redirect + async click tracking |
| `/api/dashboard/top-links` | GET | Most-clicked links |
| `/api/dashboard/jobs/{jobId}/links` | GET | All links created for a job |

## Key design decisions

- **Reference, not embed** (Resume/Job/Match): they're independently created, updated, and queried — embedding would duplicate and go stale. See `Match`, which stores only `resumeId`/`jobId`.
- **Async click tracking via Kafka**: the redirect endpoint originally wrote to MongoDB synchronously on every click; moving this behind a queue removed the database write from the response path entirely.
- **Cache-aside for links, not `@Cacheable`**: implemented by hand to make the get-or-populate logic explicit and explainable, rather than hidden behind an annotation.
- **`ErrorHandlingDeserializer` on the Kafka consumer**: prevents one malformed message ("poison pill") from crashing the whole consumer thread.

## Testing

```bash
./mvnw test
```

Integration tests use **Testcontainers** to spin up a real, disposable MongoDB — not mocks — so the tests verify actual query behavior, not just that methods were called.

## Known limitations

- Match scores reflect semantic *text* similarity, not verified qualifications — in production this would be one signal among several, not a sole decision-maker.
- Ollama runs natively, outside the Docker stack, so this repo isn't fully one-command-portable to a machine without it pre-installed.