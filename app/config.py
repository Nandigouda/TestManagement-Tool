"""Static configuration for test-case generation constraints."""

ALLOWED_PREFIX_LABELS = [
    "UAT-Additional-Information",
    "UAT-Administration-Security",
    "UAT-Administration-Settings",
    "UAT-Modeling-Decision",
    "UAT-Modeling-DecisionFlow",
    "UAT-Modeling-FactType",
    "UAT-Modeling-KnowledgeModel",
    "UAT-Modeling-KnowledgeSource",
    "UAT-Modeling-ModelAI",
    "UAT-Modeling-RuleFamily",
    "UAT-Repo-Assets",
    "UAT-Repo-DeployAssets",
    "UAT-Repo-ModelingProjects",
    "UAT-Repo-ReleaseProjects",
    "UAT-Repo-Tasks&Governance",
    "UAT-Reports",
    "UAT-Search",
    "UAT-Task-FactType",
    "UAT-Task-Governance",
    "UAT-Task-KnowledgeSource",
    "UAT-Testing-Decision",
    "UAT-Testing-DecisionFlow",
    "UAT-Testing-RuleFamily",
    "UAT-Validation-Decision",
    "UAT-Validation-RuleFamily",
]

OUTPUT_COLUMNS = [
    "Test Case Title",
    "Test Steps",
    "Expected Result",
    "Labels",
    "Automation State",
    "Test Case Status",
    "Created in Sprint",
]

SYSTEM_PROMPT = """
You are a Test Case Generation Agent.

Strictly follow these rules:
1) Fully analyze stories/scenarios and include positive, negative, and edge coverage.
2) Output must be table-ready for Excel and preserve this order:
   Test Case Title | Test Steps | Expected Result | Labels | Automation State | Test Case Status | Created in Sprint
3) Every test case must include a first step with "Pre-condition".
4) Login steps must come only after preconditions.
5) Expected Result must always contain the word "should".
6) Test Case Title must be comma-separated and end with "Verify".
7) Prefix each title with exactly one allowed UAT label.
8) Automation State must always be "Not Automated".
9) Leave Labels/Test Case Status/Created in Sprint blank unless user asks otherwise.
10) Keep each step as an independent row for export readiness.
""".strip()
