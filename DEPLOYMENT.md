# QA Automation Platform - Deployment Guide

## Release Information
- **Version**: 1.0.0
- **Release Date**: May 16, 2026
- **JAR File**: `qa-automation-platform-1.0.0.jar`
- **Size**: 294.8 MB
- **Java**: Java 17+

## Quick Start

### 1. Prepare the JAR

The release JAR is ready at:
```
target/qa-automation-platform-1.0.0.jar
```

Copy it to your server:
```bash
# Linux/Mac
scp target/qa-automation-platform-1.0.0.jar user@server:/opt/apps/

# Or manually copy to server folder
cp target/qa-automation-platform-1.0.0.jar /path/to/deployment/
```

### 2. Create Environment Configuration

Create a `.env` file in the same directory as the JAR:

```env
# Database
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=qa_automation_db
DB_USER=postgres
DB_PASSWORD=your_secure_password

# AI Configuration - Azure OpenAI
AZURE_OPENAI_ENABLED=true
AZURE_OPENAI_API_KEY=your-azure-api-key
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_OPENAI_DEPLOYMENT_GPT4=gpt4-turbo
AZURE_OPENAI_DEPLOYMENT_EMBEDDING=text-embedding-3-small
AZURE_OPENAI_API_VERSION=2024-02-15-preview

# OR OpenAI (Fallback)
OPENAI_API_KEY=sk-your-key

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api/v1

# Jira Integration (Optional)
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_API_TOKEN=your-token
```

### 3. Run the Application

**Option A: Direct Execution**
```bash
java -jar qa-automation-platform-1.0.0.jar
```

**Option B: With Custom Port**
```bash
java -Dserver.port=8080 -jar qa-automation-platform-1.0.0.jar
```

**Option C: With Custom Configuration**
```bash
java -jar qa-automation-platform-1.0.0.jar \
  --DB_HOST=your-db-host \
  --DB_PORT=5432 \
  --DB_NAME=qa_automation_db \
  --DB_USER=postgres \
  --DB_PASSWORD=password \
  --AZURE_OPENAI_API_KEY=your-key
```

### 4. Verify Application is Running

```bash
# Check if app is listening
curl http://localhost:8080/api/v1/health/check

# Expected response:
# {"status":"UP", ...}

# Or use browser
http://localhost:8080/api/v1
```

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` or `db.example.com` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `qa_automation_db` |
| `DB_USER` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | Secure password |
| `SERVER_PORT` | Application port | `8080` |
| `SERVER_SERVLET_CONTEXT_PATH` | API path prefix | `/api/v1` |
| `AZURE_OPENAI_API_KEY` | Azure OpenAI API key | Your key |
| `AZURE_OPENAI_ENDPOINT` | Azure OpenAI endpoint | `https://resource.openai.azure.com` |
| `OPENAI_API_KEY` | OpenAI fallback key | `sk-...` |

## Systemd Service (Linux/Unix)

Create `/etc/systemd/system/qa-automation.service`:

```ini
[Unit]
Description=QA Automation Platform
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/opt/apps
ExecStart=/usr/bin/java -jar /opt/apps/qa-automation-platform-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
EnvironmentFile=/opt/apps/.env

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable qa-automation
sudo systemctl start qa-automation
sudo systemctl status qa-automation
```

View logs:
```bash
sudo journalctl -u qa-automation -f
```

## Docker Deployment (Optional)

Create `Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY qa-automation-platform-1.0.0.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
# Build image
docker build -t qa-automation:1.0.0 .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=postgres-host \
  -e DB_NAME=qa_automation_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=password \
  -e AZURE_OPENAI_API_KEY=your-key \
  --name qa-automation \
  qa-automation:1.0.0
```

## Troubleshooting

### Database Connection Failed
```bash
# Check PostgreSQL is running and accessible
psql -h your-db-host -U postgres -c "\l"

# Test connectivity
curl -v telnet://your-db-host:5432
```

### API Key Invalid
```bash
# Verify Azure OpenAI API key
curl -H "api-key: YOUR_KEY" \
  https://your-resource.openai.azure.com/openai/deployments/gpt4-turbo/chat/completions?api-version=2024-02-15-preview
```

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Kill process and restart
kill -9 <PID>  # or use taskkill on Windows
```

### High Memory Usage
```bash
# Run with limited heap
java -Xmx2g -Xms1g -jar qa-automation-platform-1.0.0.jar
```

## Performance Tuning

### JVM Arguments for Production
```bash
java -Xmx4g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dfile.encoding=UTF-8 \
  -jar qa-automation-platform-1.0.0.jar
```

### Recommended Server Specs
- **CPU**: 4+ cores
- **RAM**: 8GB minimum (16GB recommended)
- **Disk**: 50GB SSD for database and temp files
- **Network**: Stable connection to PostgreSQL and AI services

## Monitoring

### Application Health
```bash
# Health check
curl http://localhost:8080/api/v1/health/check

# Full health details
curl http://localhost:8080/api/v1/health
```

### Database Health
```sql
-- Connect to database
psql -h your-db-host -U postgres -d qa_automation_db

-- Check migrations
SELECT * FROM flyway_schema_history;

-- Check active connections
SELECT usename, count(*) FROM pg_stat_activity GROUP BY usename;
```

## Support

For issues or questions:
1. Check application logs: `tail -f application.log`
2. Review database migrations: `SELECT * FROM flyway_schema_history;`
3. Open an issue: https://github.com/Nandigouda/TestManagement-Tool/issues

## Next Steps

After deployment:
1. Verify health endpoint returns `UP` status
2. Test database connectivity
3. Generate a test case to verify AI integration
4. Configure backups for PostgreSQL database
5. Set up monitoring and alerting

---

**Version**: 1.0.0 | **Last Updated**: May 16, 2026
