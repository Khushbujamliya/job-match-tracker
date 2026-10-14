![CI](https://github.com/Khushbujamliya/job-match-tracker/actions/workflows/ci.yml/badge.svg)

# AI Job-Match & Referral Tracker

A Java / Spring Boot backend that **ranks job postings for a resume by meaning** (AI embeddings + MongoDB vector search) and creates **trackable referral links** whose clicks are counted asynchronously through **Kafka**, with **Redis** caching hot lookups. It ships with a small web UI and runs as a full Docker Compose stack.

## Demo

- ▶ **Video walkthrough (2.5 min):** https://drive.google.com/file/d/1959BJGyBitg2MZlVNWuKThLZLXxPkWQM/view?usp=drive_link
- Screenshots:
  <img width="1436" height="835" alt="Screenshot 2026-10-13 at 4 59 55 PM" src="https://github.com/user-attachments/assets/aec12155-8ced-408d-bd3a-452d59358457" />
  <img width="1436" height="835" alt="Screenshot 2026-10-13 at 5 00 03 PM" src="https://github.com/user-attachments/assets/b9a59e85-25c8-4cfc-b58e-4eb7a28cf324" />
  <img width="1031" height="183" alt="Screenshot 2026-10-13 at 5 00 19 PM" src="https://github.com/user-attachments/assets/ec39f7e9-b93e-4f29-958b-a839a0b340d6" />



- The API and UI were hosted on an Oracle Cloud VM during my job search. The hosted instance is temporary, so the video above shows it running. You can run the project yourself in a few minutes (see below).

## Features

- Create jobs and resumes; each is embedded by a local model (Ollama, `nomic-embed-text`, 768 dimensions) and stored in MongoDB
- **Semantic matching:** MongoDB Atlas Vector Search (`$vectorSearch`, cosine similarity) returns the best jobs for a resume, and matches are saved
- **Referral links:** short codes, a `302` redirect, and click counts
- **Asynchronous click tracking:** the redirect publishes an event to Kafka and returns; a consumer updates the counter off the request path
- **Redis cache-aside** for short-link lookups (30-minute TTL)
- Dashboard endpoints (top links, links per job) and a web UI served by the app (one-click demo data, ranked matches, live click counts)
- Validation, centralized error handling (`@RestControllerAdvice`), health checks via Actuator
- CI with GitHub Actions and Testcontainers

## Architecture

```
Browser (static page)
   │  fetch('/api/...')
   ▼
Spring Boot app (:8080) ───────────► MongoDB  (resumes, jobs, matches, short_links; vector index)
   │        │
   │        └──► Ollama (embeddings, localhost:11434)
   │
   ├──► Redis  (short-link cache, cache-aside)
   │
   └──► Kafka topic "link-clicks" ──► consumer ──► MongoDB (click count)
```

**Add a job or resume:** validate → build text → Ollama embedding → save with the vector.

**Match:** read the resume's vector → `$vectorSearch` on jobs → ranked results → save matches.

**Referral click (`GET /r/{code}`):** Redis lookup (on a miss read MongoDB and cache) → publish a click event to Kafka → `302` redirect. A consumer increments `clickCount` a moment later.

## Tech stack

| Area | Choice | Why |
|---|---|---|
| Language / framework | Java 21, Spring Boot 4 | Standard backend stack |
| Database | MongoDB (Atlas Local image) | Document model for resumes and jobs, with built-in vector search |
| AI | Ollama + `nomic-embed-text` | Free, local, no API key |
| Messaging | Apache Kafka (KRaft mode) | Decouples the redirect from the database write |
| Cache | Redis | Cache-aside for hot short links |
| Packaging | Docker, Docker Compose | One-command infrastructure |
| CI | GitHub Actions, Testcontainers | Tests run against a real, disposable MongoDB |
| UI | Static HTML + JavaScript | Served by Spring Boot, no extra deployment |

## Project structure

```
src/main/java/com/jobmatch/job_match_tracker/
  config/       Kafka producer/consumer, Ollama properties
  controller/   REST endpoints (resumes, jobs, matches, links, redirect, dashboard)
  dto/          Request/response objects with validation
  exception/    Custom exceptions + global handler
  model/        MongoDB documents (Resume, Job, Match, ShortLink)
  repository/   Spring Data repositories
  service/      Business logic, embeddings, Kafka producer/consumer
src/main/resources/static/index.html   Web UI
Dockerfile                              Multi-stage build
docker-compose.yml                      Local compose (app in a bridge network)
docker-compose.vm.yml                   Single-VM compose (app uses host networking)
.github/workflows/ci.yml                CI pipeline
```

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/ping` | Liveness check |
| POST / GET | `/api/resumes` | Create (embeds) / list resumes |
| GET / DELETE | `/api/resumes/{id}` | Fetch / delete a resume |
| POST / GET | `/api/jobs` | Create (embeds) / list jobs |
| GET / DELETE | `/api/jobs/{id}` | Fetch / delete a job |
| GET | `/api/resumes/{id}/matches?limit=5` | Vector-search the best jobs |
| GET | `/api/resumes/{id}/match-history` | Saved matches for a resume |
| POST | `/api/links` | Create a short link |
| GET | `/r/{code}` | Redirect and count the click |
| GET | `/api/dashboard/top-links?limit=10` | Most-clicked links |
| GET | `/api/dashboard/jobs/{jobId}/links` | Links created for a job |
| GET | `/actuator/health` | Health, including MongoDB |

## Running locally

**Prerequisites:** Docker, Java 21, and [Ollama](https://ollama.com) with the model pulled:

```bash
ollama pull nomic-embed-text
```

1. Start the infrastructure:

```bash
docker compose -f docker-compose.vm.yml up -d mongo kafka redis
```

2. Create the collection and the vector index (one time; a fresh database has no `jobs` collection yet, and a search index needs one):

```bash
docker exec jobmatch-mongo mongosh "mongodb://jobmatch:jobmatch123@localhost:27017/jobmatch?authSource=admin" --quiet --eval '
if (!db.getCollectionNames().includes("jobs")) { db.createCollection("jobs"); }
db.jobs.createSearchIndex("jobs_vector_index", "vectorSearch", {
  fields: [{ type: "vector", path: "embedding", numDimensions: 768, similarity: "cosine" }]
})'
```

3. Run the app:

```bash
./mvnw spring-boot:run
```

4. Open **http://localhost:8080/**, click **Load demo data**, and try the matching, links, and dashboard.

### Configuration

| Property | Environment variable | Default |
|---|---|---|
| `spring.mongodb.uri` | `SPRING_MONGODB_URI` | `mongodb://jobmatch:jobmatch123@localhost:27017/jobmatch?authSource=admin&directConnection=true` |
| `spring.kafka.bootstrap-servers` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `spring.data.redis.host` | `SPRING_DATA_REDIS_HOST` | `localhost` |
| `ollama.base-url` | `OLLAMA_BASEURL` | `http://localhost:11434` |
| `app.base-url` | `APP_BASE_URL` | `http://localhost:8080` |

## Deploying on a single VM

Tested on an Ubuntu 22.04 VM (Oracle Cloud). Install Docker and Ollama on the VM, open port `8080` in **both** the cloud security list and the OS firewall, then:

```bash
git clone https://github.com/Khushbujamliya/job-match-tracker.git
cd job-match-tracker
docker compose -f docker-compose.vm.yml up -d --build
```

Create the collection and vector index as above. The compose file runs the app with host networking, so Kafka advertises `localhost:9092`.

## Testing and CI

```bash
./mvnw test
```

Integration tests use **Testcontainers** (a real MongoDB in Docker, not mocks) and run on every push through GitHub Actions.

## Design decisions

- **Reference, not embed:** resumes, jobs, and matches are independent and many-to-many, so matches store only ids. MongoDB has no foreign keys, so the service checks existence before saving.
- **Kafka for click tracking:** the redirect used to write to MongoDB on every click. Publishing an event instead keeps the redirect fast, and a consumer applies the update. Delivery is at-least-once, so a click could rarely be double-counted, which is acceptable for a counter.
- **Cache-aside by hand:** written explicitly (instead of `@Cacheable`) to make the get-or-populate logic and the TTL trade-off visible.
- **`ErrorHandlingDeserializer` on the consumer:** one malformed message cannot crash the consumer thread.
- **Vector search in MongoDB:** one database for documents and vectors, instead of a second system to keep in sync.

## Challenges and what I learned

- **Spring Boot 4 changes:** the MongoDB property moved to `spring.mongodb.uri`, and the test starters were split, so I used `MockMvc` built by hand.
- **Kafka JSON and dates:** serializing `Instant` failed until I registered Jackson's `JavaTimeModule` on the serializer's `ObjectMapper`.
- **Kafka advertised listeners:** the address a broker advertises must be reachable from the client's side, which differs between Docker's bridge network and host networking.
- **MongoDB creates collections lazily:** the vector index failed on a fresh database until the collection existed.
- **Two firewalls and rule order:** a cloud security list plus the OS-level `iptables`, where an ACCEPT rule placed below a REJECT never applies.

## Known limitations

- No authentication, so anyone can create data (which also triggers embedding work)
- HTTP only, with no domain or TLS
- Single node for every service, with no redundancy
- Embeddings run on CPU, so the first request after idle can take seconds
- Match scores measure semantic text similarity, not verified qualifications
- Demo credentials are in the compose files (fine for local use, not for production)

## Future improvements

- JWT authentication and rate limiting
- HTTPS through a reverse proxy
- Generate embeddings asynchronously through Kafka
- Create the collection and vector index automatically at startup
- Pagination, metrics (Prometheus), and a React frontend

## Author

Khushbu Jamliya · [GitHub](https://github.com/Khushbujamliya) · [LinkedIn](https://www.linkedin.com/in/khushbu-jamliya)
