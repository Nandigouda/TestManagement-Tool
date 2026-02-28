from __future__ import annotations

import csv
import io
import json
import os
from dataclasses import dataclass
from typing import Any, Dict, List

from .config import ALLOWED_PREFIX_LABELS, OUTPUT_COLUMNS, SYSTEM_PROMPT
from .models import TestCaseRequest, TestCaseRow


@dataclass
class AgentState:
    request: TestCaseRequest
    retrieved_context: str = ""
    generated_rows: List[TestCaseRow] | None = None


class TestCaseAgent:
    """LangGraph-ready orchestration with deterministic fallback."""

    def run(self, request: TestCaseRequest) -> List[TestCaseRow]:
        state = AgentState(request=request)
        state.retrieved_context = self._retrieve_context(request)
        rows = self._generate_rows(state)
        return self._validate_rows(rows)

    def _retrieve_context(self, request: TestCaseRequest) -> str:
        # Placeholder for Azure AI Search integration.
        story_blob = "\n".join(request.stories).strip()
        return story_blob or request.user_requirement

    def _generate_rows(self, state: AgentState) -> List[TestCaseRow]:
        raw_output = self._call_llm(state)
        parsed_rows = self._try_parse_json_rows(raw_output)
        if parsed_rows:
            return parsed_rows
        return self._deterministic_rows(state.request)

    def _call_llm(self, state: AgentState) -> str:
        """Call Azure OpenAI if configured, otherwise return empty string."""
        endpoint = os.getenv("AZURE_OPENAI_ENDPOINT")
        api_key = os.getenv("AZURE_OPENAI_API_KEY")
        deployment = os.getenv("AZURE_OPENAI_DEPLOYMENT", "gpt-4o")
        if not (endpoint and api_key):
            return ""

        try:
            from openai import AzureOpenAI

            client = AzureOpenAI(
                api_key=api_key,
                api_version=os.getenv("AZURE_OPENAI_API_VERSION", "2024-06-01"),
                azure_endpoint=endpoint,
            )
            prompt = (
                f"{SYSTEM_PROMPT}\n\n"
                f"User Requirement:\n{state.request.user_requirement}\n\n"
                f"Stories/Context:\n{state.retrieved_context}\n\n"
                f"Return JSON array with exact keys: {', '.join(OUTPUT_COLUMNS)}"
            )
            response = client.chat.completions.create(
                model=deployment,
                temperature=0,
                messages=[{"role": "user", "content": prompt}],
            )
            return response.choices[0].message.content or ""
        except Exception:
            return ""

    def _try_parse_json_rows(self, raw_output: str) -> List[TestCaseRow]:
        if not raw_output:
            return []
        try:
            payload: Any = json.loads(raw_output)
            if not isinstance(payload, list):
                return []
            rows = []
            for item in payload:
                if isinstance(item, dict):
                    rows.append(TestCaseRow(**item))
            return rows
        except Exception:
            return []

    def _deterministic_rows(self, request: TestCaseRequest) -> List[TestCaseRow]:
        prefix = request.preferred_prefix_label or "UAT-Testing-RuleFamily"
        if prefix not in ALLOWED_PREFIX_LABELS:
            prefix = "UAT-Testing-RuleFamily"

        title_base = "Story Analysis, Generate Test Cases, Functional Coverage, Verify"
        tc_title = f"{prefix}, {title_base}"
        pre_condition = "Pre-condition: User should be logged in and have access to test case generation module"

        steps = [
            (pre_condition, "User should have required permissions and module access"),
            ("Login with valid credentials", "User should be authenticated successfully"),
            ("Submit user requirement and story text", "System should accept requirement and story input"),
            ("Click Generate Test Cases", "System should generate positive, negative, and edge test cases"),
            ("Export output to Excel format", "System should produce rows aligned with required column order"),
        ]
        return [
            TestCaseRow(
                **{
                    "Test Case Title": tc_title,
                    "Test Steps": step,
                    "Expected Result": expected,
                    "Labels": "",
                    "Automation State": "Not Automated",
                    "Test Case Status": "",
                    "Created in Sprint": "",
                }
            )
            for step, expected in steps
        ]

    def _validate_rows(self, rows: List[TestCaseRow]) -> List[TestCaseRow]:
        for row in rows:
            if "should" not in row.expected_result.lower():
                row.expected_result = f"System should {row.expected_result[0].lower() + row.expected_result[1:]}"
            row.automation_state = "Not Automated"
            if not row.test_case_title.endswith("Verify"):
                row.test_case_title = f"{row.test_case_title.rstrip(', ')}, Verify"
        if rows and "Pre-condition" not in rows[0].test_steps:
            rows.insert(
                0,
                TestCaseRow(
                    **{
                        "Test Case Title": rows[0].test_case_title,
                        "Test Steps": "Pre-condition: User should be logged in and have access to module",
                        "Expected Result": "User should be logged in and have required access",
                        "Labels": "",
                        "Automation State": "Not Automated",
                        "Test Case Status": "",
                        "Created in Sprint": "",
                    }
                ),
            )
        return rows


def rows_to_markdown(rows: List[TestCaseRow]) -> str:
    lines = ["| " + " | ".join(OUTPUT_COLUMNS) + " |", "|" + "|".join(["---"] * len(OUTPUT_COLUMNS)) + "|"]
    for row in rows:
        record: Dict[str, Any] = row.model_dump(by_alias=True)
        lines.append("| " + " | ".join(str(record.get(c, "")) for c in OUTPUT_COLUMNS) + " |")
    return "\n".join(lines)


def rows_to_csv(rows: List[TestCaseRow]) -> str:
    output = io.StringIO()
    writer = csv.DictWriter(output, fieldnames=OUTPUT_COLUMNS)
    writer.writeheader()
    for row in rows:
        writer.writerow(row.model_dump(by_alias=True))
    return output.getvalue()
