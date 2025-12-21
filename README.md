# QA Automation Platform

A comprehensive AI-powered testing platform that generates test cases and automation scripts from requirements using intelligent agents and LLMs.

## 🚀 Features

- **AI-Powered Test Case Generation**: Convert requirements into structured test cases using Azure OpenAI
- **Code Generation**: Generate automation scripts in Python (Pytest/Selenium), Java (TestNG/Selenium), and JavaScript (Playwright)
- **File Processing**: Extract and analyze requirements from PDF, DOCX, TXT, and PPTX files
- **Test Case Library**: Store, organize, and manage generated test cases
- **Jira Integration**: Sync test cases directly to Jira
- **Professional UI**: Modern, responsive web interface with tab-based navigation

## 📋 Quick Start

### Prerequisites

- **Java 21** (required)
- **Maven** or **Mvnd** (for building)
- **Azure OpenAI API Key** (for AI features)
- Windows PowerShell 5.1+

### Installation & Running

1. **Clone/Download the project**
   ```powershell
   cd d:\TesTManagement Tool
   ```

2. **Start the application**
   ```powershell
   # Option 1: Quick start with pre-built JAR
   .\start.ps1

   # Option 2: Build and start with environment configuration
   .\start-with-env.ps1

   # Option 3: Stop the application
   .\stop.ps1
   ```

3. **Access the application**
   - Open browser: `http://localhost:8081/api/v1`
   - User: Nikhil (shown in top-right avatar as "NK")

## 🏗️ Architecture

### Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2.0 |
| **Database** | H2 In-Memory (Development) |
| **ORM** | Spring Data JPA with Hibernate 6.3.1 |
| **Build Tool** | Maven 3+ / Mvnd |
| **LLM** | Azure OpenAI GPT-4 Turbo |
| **Frontend** | Vanilla HTML5, Tailwind CSS, JavaScript |
| **Document Processing** | Apache PDFBox, Apache POI |

### Core Agents

1. **TextExtractionAgent**
   - Extracts text from uploaded documents
   - Cleans and normalizes content

2. **TestCaseGenerationAgent**
   - Converts requirements to structured test cases
   - Uses Azure OpenAI for intelligent generation

3. **AutomationCodeGenerationAgent**
   - Generates automation scripts
   - Supports: Python (Pytest/Selenium), Java (TestNG/Selenium), JavaScript (Playwright)

4. **JiraIntegrationAgent**
   - Pushes test cases to Jira
   - Manages issue creation and updates

### Project Structure

```
src/main/
├── java/com/qaautomation/
│   ├── agents/                          # Agent implementations
│   │   ├── Agent.java                   # Base interface
│   │   ├── TextExtractionAgent.java
│   │   ├── TestCaseGenerationAgent.java
│   │   ├── AutomationCodeGenerationAgent.java
│   │   └── JiraIntegrationAgent.java
│   ├── config/
│   │   └── AppConfiguration.java        # Spring configuration
│   ├── controllers/                     # REST API endpoints
│   │   ├── FileExtractionController.java
│   │   ├── TestCaseGenerationController.java
│   │   ├── CodeGenerationController.java
│   │   └── JiraIntegrationController.java
│   ├── models/                          # Data models
│   ├── repositories/                    # Database access layer
│   ├── services/                        # Business logic
│   │   ├── LLMService.java
│   │   ├── FileExtractionService.java
│   │   ├── TestCaseGenerationService.java
│   │   └── CodeGenerationService.java
│   └── QAAutomationApplication.java     # Main application class
├── resources/
│   ├── application.yml                  # Application configuration
│   └── static/
│       ├── index.html                   # Main UI
│       ├── css/
│       │   ├── styles.css
│       │   └── material-icons.css
│       └── js/
│           ├── jira-app.js
│           ├── export-utils.js
│           ├── components/
│           │   ├── unified-test-cases.js
│           │   ├── code-viewer.js
│           │   └── code-generation.js
│           └── services/
│               ├── api.service.js
│               └── router.service.js
```

## 🎯 Main Features

### 1. Test Case Generation (Generate Tab)

- **File Upload**: Drag-and-drop or browse to upload requirements documents
- **Text Input**: Paste or type requirements directly
- **Metadata**: Specify application name and module
- **AI Processing**: Automatically generates structured test cases

### 2. Test Case Library (Library Tab)

- **Metrics Dashboard**: View test case statistics
- **Advanced Filtering**: Filter by application, module, and priority
- **Test Case Table**: Browse all test cases with details
- **Priority Badges**: HIGH (red), MEDIUM (yellow), LOW (blue)
- **Code Generation Sidebar**: Select test cases and generate automation code

### 3. Jira Integration

- **Connection Management**: Configure Jira server URL and credentials
- **Status Monitoring**: Real-time connection status indicator
- **Configuration Form**: Store Jira project settings
- **Test Case Sync**: Push generated test cases to Jira

### 4. Execution Summary

- Coming soon: View test execution history and metrics

## 🔧 Configuration

### Environment Variables (.env file)

Create a `.env` file in the project root:

```env
# Azure OpenAI Configuration
AZURE_OPENAI_ENABLED=true
AZURE_OPENAI_API_KEY=your_api_key_here
AZURE_OPENAI_ENDPOINT=https://your-region.openai.azure.com/
AZURE_OPENAI_DEPLOYMENT=gpt4-turbo

# OpenAI Configuration (fallback)
OPENAI_API_KEY=sk_your_key_here

# Jira Configuration
JIRA_BASE_URL=https://your-domain.atlassian.net
```

### Application Properties (application.yml)

Key configurations:
- **Port**: 8081
- **Context Path**: /api/v1
- **Database**: H2 in-memory (testdb)
- **H2 Console**: http://localhost:8081/api/v1/h2-console

## 📡 API Endpoints

### Test Case Management
- `GET /api/v1/testcases` - List all test cases
- `GET /api/v1/testcases/:id` - Get test case details
- `POST /api/v1/testcases/generate` - Generate new test cases
- `GET /api/v1/testcases/metrics` - Get test case metrics

### File Extraction
- `POST /api/v1/files/extract` - Extract text from uploaded files

### Code Generation
- `POST /api/v1/code/generate` - Generate automation code

### Jira Integration
- `POST /api/v1/jira/connect` - Test Jira connection
- `POST /api/v1/jira/push-testcase` - Push test case to Jira

## 🛠️ Development Scripts

### PowerShell Scripts

1. **start.ps1** - Start pre-built application
   ```powershell
   .\start.ps1
   ```

2. **start-with-env.ps1** - Build and start with environment configuration
   ```powershell
   .\start-with-env.ps1
   ```

3. **stop.ps1** - Stop running application
   ```powershell
   .\stop.ps1
   ```

4. **configure-azure.ps1** - Configure Azure OpenAI settings
   ```powershell
   .\configure-azure.ps1
   ```

### Build Commands

```powershell
# Clean build
mvnd.cmd clean package -DskipTests

# Build with tests
mvnd.cmd clean package

# Run specific test
mvnd.cmd test -Dtest=TestClassName
```

## 📊 User Interface

### Main Layout

- **Header**: Application title with user avatar (NK for Nikhil)
- **Sidebar**: Navigation with icons
  - Test Cases (default)
  - Jira Integration
  - Execution Summary (Reports)
- **Main Content**: Dynamic content area with tabs

### Test Cases Page

**Generate Tab**:
- Left: File upload area
- Right: Requirements text input with application/module fields

**Library Tab**:
- Metrics cards (Total test cases, Applications, Modules, Tags)
- Filter bar (Application, Module, Priority, Apply button)
- Test case table with checkboxes
- Right sidebar: Code generation panel

### Color Scheme

- **Primary**: Jira Blue (#0052cc)
- **Success**: Green (#36b37e)
- **Danger**: Red (#ff5630)
- **Warning**: Orange (#ffab00)
- **Background**: Light Gray (#f4f5f7)

## 🔐 Security Considerations

- Environment variables with sensitive data (API keys) are masked in logs
- Jira API tokens stored securely (never logged in plain text)
- CORS configured for local development
- Input validation on all forms

## 📝 File Operations

### Supported File Formats
- PDF (.pdf)
- Word Documents (.docx)
- PowerPoint Presentations (.pptx)
- Text Files (.txt)

### Maximum File Size
- Configurable (default: 50MB)

## 🤝 Integration Examples

### Generate Test Cases from Requirements
1. Navigate to "Test Cases" > "Generate" tab
2. Upload requirements document OR type requirements
3. Specify application and module
4. Click "Generate Test Cases"
5. Review generated test cases in Library tab

### Push Test Cases to Jira
1. Navigate to "Jira Integration"
2. Enter Jira server URL and credentials
3. Click "Connect to Jira"
4. Go to "Test Cases" > "Library"
5. Select test cases via checkboxes
6. Generate code (optional) or sync to Jira

### Generate Automation Code
1. Open "Test Cases" > "Library"
2. Select test cases using checkboxes
3. Choose language from dropdown (Python/Java/JavaScript)
4. Click "Generate Code"
5. Code appears in modal with syntax highlighting

## 🐛 Troubleshooting

### Application Won't Start
```powershell
# Check Java version
java -version  # Should be 21+

# Check port 8081 is free
netstat -ano | findstr :8081

# Run with verbose output
.\start-with-env.ps1
```

### Build Failures
```powershell
# Clean Maven cache
mvnd.cmd clean -DskipTests

# Rebuild
mvnd.cmd package -DskipTests
```

### Missing Environment Variables
```powershell
# Verify .env file exists in project root
Test-Path .env

# Check format
Get-Content .env
```

## 📈 Performance

- **Test Case Generation**: ~5-10 seconds per requirement set
- **Code Generation**: ~3-5 seconds per test case set
- **Database**: In-memory H2 (instant response, no persistence)

## 🔄 Updates & Maintenance

- Keep Azure OpenAI API key updated in .env file
- Monitor disk space (application logs in app_output.txt)
- Review generated test cases for accuracy before Jira sync

## 📞 Support

For issues or questions:
1. Check application logs: `app_output.txt`, `app_logs.txt`
2. Review browser console for JavaScript errors
3. Verify environment variables are correctly set
4. Ensure Java 21 is installed and in PATH

## 📄 License

Private project - All rights reserved

---

**Last Updated**: December 11, 2025  
**Version**: 1.0.0  
**Status**: Production Ready
