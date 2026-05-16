# QA Automation Platform

A comprehensive AI-assisted QA platform for generating, managing, and automating test cases. Built with Spring Boot and powered by Azure OpenAI.

## Overview

The QA Automation Platform is an enterprise-grade test case management system that leverages AI to accelerate test planning and automation. It provides an intuitive web interface for generating test cases from requirements, managing test libraries, and generating automation code for Selenium and Playwright.

**Technology Stack**: Spring Boot 3.2.0 | PostgreSQL | Azure OpenAI | Java 17+ | Maven

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
| Java | Java 17+ |
| Build | Maven 3.9.6 |
| Database | PostgreSQL 12+ |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| AI | Azure OpenAI (with OpenAI fallback) |
| Document Processing | Apache PDFBox, Apache POI |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Vector Search | pgvector (optional)

## Prerequisites

- **Java 17+** (Java 21 recommended)
- **PostgreSQL** 12+ running locally or accessible from your environment
- **Azure OpenAI** API key or **OpenAI** API key for AI features
- **Maven 3.9.6+** (included in `tools/apache-maven-3.9.6` or use local installation)

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=qa_automation_db
DB_USER=postgres
DB_PASSWORD=your_secure_password

# AI Configuration - Azure OpenAI
AZURE_OPENAI_ENABLED=true
AZURE_OPENAI_API_KEY=your-api-key
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_OPENAI_DEPLOYMENT_GPT4=gpt4-turbo
AZURE_OPENAI_DEPLOYMENT_EMBEDDING=text-embedding-3-small
AZURE_OPENAI_API_VERSION=2024-02-15-preview

# OR OpenAI (Fallback)
OPENAI_API_KEY=sk-your-key

# Jira Integration (Optional)
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_API_TOKEN=your-token

# Server
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api/v1
```

### Application Configuration

Default settings in `src/main/resources/application.yml`:
- Server Port: `8080`
- Context Path: `/api/v1`
- Upload Limit: `50MB`
- Temp Directory: `./temp-files`
- Database Migrations: `src/main/resources/db/migration/`

## Build & Run

### Setup Database

```bash
# Create database (if not exists)
createdb qa_automation_db

# Or use psql
psql -U postgres -c "CREATE DATABASE qa_automation_db;"
```

### Build Application

```bash
# Using Maven
mvn clean package -DskipTests

# Or with included Maven
./tools/apache-maven-3.9.6/bin/mvn clean package -DskipTests
```

### Run Application

```bash
# From JAR
java -jar target/qa-automation-platform-1.0.0-SNAPSHOT.jar

# Or using Maven
mvn spring-boot:run

# Or with included Maven
./tools/apache-maven-3.9.6/bin/mvn spring-boot:run
```

### Access Application

Once running, open your browser:
```
http://localhost:8080/api/v1
```

Health check:
```bash
curl http://localhost:8080/api/v1/health/check
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

- Data is persisted in PostgreSQL (not in-memory)
- Flyway manages database schema versioning and migrations automatically
- pgvector extension is optional; functionality works without it
- Generated test cases should be reviewed before export or automation code generation
- All sensitive configuration should use environment variables
- No build scripts or test scripts are committed to the repository

## Troubleshooting

### Database Issues
```bash
# Verify PostgreSQL is running
psql -U postgres -c "\l"

# Check database exists
psql -U postgres -c "SELECT datname FROM pg_database WHERE datname='qa_automation_db';"

# View Flyway migrations
psql -U postgres -d qa_automation_db -c "SELECT * FROM flyway_schema_history;"
```

### Application Issues
```bash
# Check if app is running
curl -s http://localhost:8080/api/v1/health/check | jq .

# View application logs (if saved)
tail -f app.log

# Verify port is available
netstat -tulpn | grep 8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows
```

### AI/API Issues
- Verify Azure OpenAI or OpenAI API keys are correct
- Check API quota and permissions
- Verify network connectivity to API endpoints
- Review Spring Boot logs for detailed error messages

### Build Issues
```bash
# Clean Maven cache
mvn clean

# Verify Java version
java -version

# Rebuild from scratch
mvn clean package -DskipTests

# Skip tests if build fails
mvn package -DskipTests
```

## Security

⚠️ **Important**: This repository does NOT commit:
- API keys, credentials, or secrets
- Database passwords
- Private keys or certificates
- Environment-specific configurations
- Sensitive configuration files

Use environment variables (see Configuration section) for all sensitive data.

## Contributing

1. Create a feature branch from \main\
2. Implement your changes following existing code patterns
3. Ensure code compiles: \mvn clean compile\
4. Test your changes: \mvn test\
5. Commit with clear, descriptive messages
6. Push and create a pull request

## License & Repository

**Repository**: https://github.com/Nandigouda/TestManagement-Tool

For issues, questions, or contributions, please open an issue on GitHub.
