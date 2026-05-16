# QA Automation Platform

AI-assisted QA platform for generating, storing, reviewing, exporting, and converting test cases into automation code.

The application is a Spring Boot backend with a static browser UI. It uses Azure OpenAI or OpenAI for generation, PostgreSQL for persistence, Flyway for schema migrations, and local fallback vector search when pgvector is not installed.

## Features

- Generate structured test cases from requirements text.
- Generate test cases with selected user guide folders or files as context.
- Chat with a QA assistant that can generate, regenerate, modify, add, merge, and export conversation test cases.
- Store generated test cases in PostgreSQL and review them from the Library tab.
- View metrics for total test cases, applications, modules, tags, and priority distribution.
- Export test cases to CSV or Excel-compatible files.
- Generate Selenium or Playwright automation code from selected test cases.
- Push test cases to Jira through the Jira integration endpoint.
- Extract text from PDF, DOCX, TXT, DOC, JPG, PNG, and JPEG files.

## Tech Stack

| Area | Technology |
| --- | --- |
| Backend | Spring Boot 3.2.0 |
| Java target | Java 17 |
| Runtime used by scripts | JDK 21 at `C:\Program Files\Java\jdk-21` |
| Build | Maven wrapper under `tools/apache-maven-3.9.6` or local Maven/Mvnd |
| Database | PostgreSQL |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| AI | Azure OpenAI, with OpenAI fallback configuration |
| Documents | Apache PDFBox, Apache POI |
| Frontend | Static HTML, CSS, vanilla JavaScript, Tailwind CDN in development |

## Prerequisites

- Java 17 or newer. The included PowerShell scripts currently expect JDK 21 at `C:\Program Files\Java\jdk-21`.
- PostgreSQL running locally or reachable from this machine.
- A database matching `DB_NAME` in `.env`.
- Azure OpenAI credentials for AI generation, or OpenAI fallback credentials.
- Windows PowerShell.

## Configuration

Create a `.env` file in the project root. Use `.env.example` as the starting point:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=qa_automation_db
DB_USER=postgres
DB_PASSWORD=your_postgres_password

AZURE_OPENAI_ENABLED=true
AZURE_OPENAI_API_KEY=your-azure-api-key-here
AZURE_OPENAI_ENDPOINT=https://your-resource-name.openai.azure.com
AZURE_OPENAI_DEPLOYMENT_GPT4=gpt4-turbo
AZURE_OPENAI_DEPLOYMENT_EMBEDDING=text-embedding-3-small
AZURE_OPENAI_API_VERSION=2024-02-15-preview

OPENAI_API_KEY=sk-test-key

JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_API_TOKEN=
```

Runtime defaults are defined in:

- `src/main/resources/application.properties`
- `src/main/resources/application.yml`

Important defaults:

- Server URL: `http://localhost:8081/api/v1`
- Server port: `8081`
- Context path: `/api/v1`
- Flyway migrations: `src/main/resources/db/migration`
- Upload temp directory: `./temp-files`
- Max upload size: `50MB`

## Run Locally

Build and start:

```powershell
.\build-and-start.ps1
```

Start an already-built jar:

```powershell
.\start.ps1
```

Stop the app:

```powershell
.\stop.ps1
```

Manual Maven build:

```powershell
.\tools\apache-maven-3.9.6\bin\mvn.cmd -q -DskipTests clean package
```

After startup, open:

```text
http://localhost:8081/api/v1
```

## Main Workflows

### Generate Test Cases

1. Open `Test Cases`.
2. Use the `Generate` tab.
3. Enter requirements text or upload a supported file.
4. Optionally select user guide folders.
5. Click `Generate`.
6. Review results in the generated list or the `Library` tab.

### Use Chat

1. Open `Chat`.
2. Send a scenario or attach files.
3. Select active guides if needed.
4. Use response actions such as regenerate, modify, add more, or merge.

### Generate Automation Code

1. Open `Test Cases` > `Library`.
2. Select test cases.
3. Open code generation.
4. Choose Selenium or Playwright and the target language.
5. Review the generated artifact.

## API Endpoints

All endpoints are under `/api/v1`.

### Health

- `GET /health/check`
- `GET /health`

### Test Cases

- `GET /testcases`
- `GET /testcases/{id}`
- `GET /testcases/filter`
- `GET /testcases/by-application?name=...`
- `GET /testcases/by-module?name=...`
- `GET /testcases/metrics`

### Test Case Generation

- `POST /agents/testcases`
- `POST /agents/testcases/generate`
- `POST /agents/testcases/generate/with-guide`
- `GET /agents/testcases`
- `GET /agents/testcases/{testCaseId}`
- `GET /agents/testcases/guides/folders`
- `GET /agents/testcases/guides/available`

### Chat

- `POST /chat/start`
- `POST /chat/message`
- `GET /chat/{conversationId}`
- `POST /chat/{conversationId}/regenerate`
- `POST /chat/{conversationId}/modify`
- `POST /chat/{conversationId}/add-more`
- `POST /chat/{conversationId}/merge`
- `GET /chat/{conversationId}/similar`
- `GET /chat/{conversationId}/duplicates`
- `GET /chat/{conversationId}/export`

### Files

- `POST /files/extract`
- `GET /files/{fileId}`
- `POST /chat/files/upload`
- `DELETE /chat/files/{attachmentId}`

### Code Generation

- `POST /agents/code`
- `POST /agents/code/from-testcases`
- `GET /agents/code/{artifactId}`

### Jira

- `POST /integrations/jira/push`

## Project Structure

```text
src/main/java/com/qaautomation
  agents/         AI, extraction, Jira, and code generation agents
  config/         Spring and Azure OpenAI configuration
  controller/     Health controller
  controllers/    REST controllers
  dto/            API DTOs
  models/         JPA entities and request/response models
  repositories/   Spring Data repositories
  services/       Business services
  utils/          Utility classes

src/main/resources
  application.properties
  application.yml
  db/migration/   Flyway SQL migrations
  static/         Browser UI assets
```

## Notes

- The app persists data in PostgreSQL. It is not an H2 in-memory application.
- Flyway validates and applies database migrations on startup.
- pgvector is optional in the current local setup. If the extension is unavailable, the health check may report `DEGRADED`, while PostgreSQL and Azure OpenAI can still be healthy.
- Generated test cases should be reviewed before export, Jira push, or automation-code generation.
- The Tailwind CDN warning in DevTools is expected for local development.

## Troubleshooting

Check whether the app is listening:

```powershell
netstat -aon | Select-String ':8081.*LISTENING'
```

Check the latest logs:

```powershell
Get-Content .\app_run_latest.log -Tail 120
Get-Content .\app_run_latest.err.log -Tail 120
```

If `clean package` cannot delete the jar, stop the process using port `8081` and rebuild:

```powershell
.\stop.ps1
.\tools\apache-maven-3.9.6\bin\mvn.cmd -q -DskipTests clean package
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8081/api/v1/health/check
```

## License

Private project. All rights reserved.
