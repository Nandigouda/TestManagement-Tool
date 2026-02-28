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
