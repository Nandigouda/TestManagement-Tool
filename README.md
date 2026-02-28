# TestManagement-Tool

A starter implementation of a **Test Case Generation Agent** using Python + FastAPI, with orchestration points that are ready for LangGraph and enterprise integrations.

## What this provides

- FastAPI API endpoints for JSON and CSV test-case generation.
- Rule-enforced output format matching your test-case policy.
- Deterministic fallback generation when LLM credentials are not configured.
- Azure OpenAI call path (if environment variables are set).
- Structure ready for adding Azure AI Search, Jira Cloud, Zephyr Squad, and CI/CD hooks.

## API endpoints

- `GET /health`
- `POST /generate-test-cases`
- `POST /generate-test-cases/csv`

### Sample request

```json
{
  "user_requirement": "Generate test cases for decision flow update.",
  "stories": [
    "As a business analyst, I update event definitions and expect validation coverage."
  ],
  "preferred_prefix_label": "UAT-Modeling-Decision"
}
```

## Local run

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## Optional Azure OpenAI configuration

Set these variables to enable LLM generation:

- `AZURE_OPENAI_ENDPOINT`
- `AZURE_OPENAI_API_KEY`
- `AZURE_OPENAI_DEPLOYMENT` (optional, default `gpt-4o`)
- `AZURE_OPENAI_API_VERSION` (optional, default `2024-06-01`)

## Next integration tasks

1. Replace `_retrieve_context` in `app/testcase_agent.py` with Azure AI Search hybrid retrieval.
2. Add Jira Cloud webhook endpoint to ingest stories.
3. Add Zephyr Squad push endpoint to publish generated test cases.
4. Add LangGraph StateGraph nodes for retrieval, generation, validation, export, and sync.
5. Add CI pipeline to run lint/tests and optionally publish generated artifacts.
