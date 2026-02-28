from __future__ import annotations

from fastapi import FastAPI
from fastapi.responses import PlainTextResponse

from .models import TestCaseRequest, TestCaseResponse
from .testcase_agent import TestCaseAgent, rows_to_csv, rows_to_markdown

app = FastAPI(title="Test Case Generation Agent", version="0.1.0")
agent = TestCaseAgent()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/generate-test-cases", response_model=TestCaseResponse)
def generate_test_cases(request: TestCaseRequest) -> TestCaseResponse:
    rows = agent.run(request)
    return TestCaseResponse(rows=rows, markdown_table=rows_to_markdown(rows))


@app.post("/generate-test-cases/csv", response_class=PlainTextResponse)
def generate_test_cases_csv(request: TestCaseRequest) -> str:
    rows = agent.run(request)
    return rows_to_csv(rows)
