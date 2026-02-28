from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, Field


class TestCaseRow(BaseModel):
    test_case_title: str = Field(alias="Test Case Title")
    test_steps: str = Field(alias="Test Steps")
    expected_result: str = Field(alias="Expected Result")
    labels: str = Field(default="", alias="Labels")
    automation_state: str = Field(default="Not Automated", alias="Automation State")
    test_case_status: str = Field(default="", alias="Test Case Status")
    created_in_sprint: str = Field(default="", alias="Created in Sprint")

    class Config:
        populate_by_name = True


class TestCaseRequest(BaseModel):
    user_requirement: str
    stories: List[str] = Field(default_factory=list)
    preferred_prefix_label: Optional[str] = None


class TestCaseResponse(BaseModel):
    rows: List[TestCaseRow]
    markdown_table: str
