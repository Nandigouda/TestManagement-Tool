from app.models import TestCaseRequest
from app.testcase_agent import TestCaseAgent, rows_to_markdown


def test_generated_rows_follow_mandatory_rules() -> None:
    agent = TestCaseAgent()
    rows = agent.run(
        TestCaseRequest(
            user_requirement="Generate test cases from stories",
            stories=["As a QA, I upload story and expect structured test cases."],
        )
    )

    assert rows
    assert "Pre-condition" in rows[0].test_steps
    assert all("should" in row.expected_result.lower() for row in rows)
    assert all(row.automation_state == "Not Automated" for row in rows)
    assert all(row.test_case_title.endswith("Verify") for row in rows)


def test_markdown_has_required_columns() -> None:
    agent = TestCaseAgent()
    rows = agent.run(TestCaseRequest(user_requirement="Req"))
    markdown = rows_to_markdown(rows)
    assert "| Test Case Title | Test Steps | Expected Result | Labels | Automation State | Test Case Status | Created in Sprint |" in markdown


def test_validate_rows_handles_empty_expected_result_without_crashing() -> None:
    agent = TestCaseAgent()
    rows = agent._deterministic_rows(TestCaseRequest(user_requirement="Req"))
    rows[1].expected_result = ""

    validated = agent._validate_rows(rows)

    assert validated[1].expected_result == "System should display the expected outcome"


def test_validate_rows_enforces_allowed_uat_prefix() -> None:
    agent = TestCaseAgent()
    rows = agent._deterministic_rows(TestCaseRequest(user_requirement="Req"))
    rows[0].test_case_title = "Smoke Tests, Verify"

    validated = agent._validate_rows(rows)

    assert validated[0].test_case_title.startswith("UAT-Testing-RuleFamily,")
