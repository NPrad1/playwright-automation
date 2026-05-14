# Playwright Automation Framework

Scalable UI automation framework built using Playwright + Java + TestNG following Page Object Model design principles.

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Java 17 | Core language |
| Playwright | Browser automation |
| TestNG | Test execution and suite management |
| Maven | Build and dependency management |
| Log4j2 | Centralized logging |
| Extent Reports | Execution summary reporting |
| Allure Reports | Deep debug reporting with artifacts |
| Apache POI | Excel-based data-driven testing |
| GitHub Actions | CI/CD pipeline |

---

## Framework Highlights

- Cross-browser execution (Chromium, Firefox, WebKit)
- Parallel test execution using ThreadLocal
- Retry mechanism for flaky tests
- Screenshot capture on failure
- Playwright trace generation
- Failure video recording
- Dual reporting — Extent + Allure
- Config-driven execution
- Data-driven testing using Excel
- CI/CD integration via GitHub Actions

---

## Application Under Test

```text
https://freelance-learn-automation.vercel.app
```

---

## Framework Architecture

```text
playwright-learn-automation/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── src/
│   ├── main/java/com/playwright/qa/
│   │   ├── base/
│   │   │   ├── BaseTest.java
│   │   │   └── ConfigReader.java
│   │   │
│   │   ├── listener/
│   │   │   ├── ExtentListener.java
│   │   │   ├── ExtentManager.java
│   │   │   ├── RetryAnalyzer.java
│   │   │   ├── RetryListener.java
│   │   │   ├── ArtifactReporter.java
│   │   │   └── AllureReportListener.java
│   │   │
│   │   ├── pages/
│   │   │   ├── LandingPage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── DashboardPage.java
│   │   │   ├── CartPage.java
│   │   │   ├── ManageCoursesPage.java
│   │   │   └── SignUpPage.java
│   │   │
│   │   └── utils/
│   │       ├── ExcelUtil.java
│   │       ├── TestDataProvider.java
│   │       └── AssertUtil.java
│   │
│   └── test/
│       ├── java/com/playwright/qa/test/
│       │   ├── LandingPageTest.java
│       │   ├── LoginTest.java
│       │   ├── DashboardPageTest.java
│       │   ├── CartPageTest.java
│       │   ├── ManageCoursesTest.java
│       │   └── NewUserSignUpTest.java
│       │
│       └── resources/
│           ├── config.properties
│           ├── smoke-suite.xml
│           ├── regression-suite.xml
│           ├── testng.xml
│           └── testData.xlsx
│
├── test-output/
├── target/
└── pom.xml
```

---

## Design Patterns

| Pattern | Where Applied | Why |
|---|---|---|
| Page Object Model | `pages/` package | One change point per UI element; test logic stays clean |
| ThreadLocal | `BaseTest.java` | Each parallel thread gets its own isolated browser instance |
| Listener Pattern | `listener/` package | Reporting, retry, and artifact logic decoupled from tests |
| Factory-style Browser Init | `BaseTest.java` | Single switch block handles all browser types cleanly |
| Utility Reusability | `utils/` package | Shared Excel, assertion, and data logic — no duplication |

---

## Key Implementation Highlights

### ThreadLocal — Safe Parallel Execution

Each thread holds its own `Playwright`, `Browser`, `BrowserContext`, and `Page` — no shared state between parallel tests.

```java
private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

public static Page getPage() {
    return pageThreadLocal.get();
}
```

---

### Retry Mechanism

Failed tests retry up to **2 times** automatically.

Applied globally via `IAnnotationTransformer` — no per-test annotation needed.

```java
public class RetryAnalyzer implements IRetryAnalyzer {

    private int count = 0;
    private static final int MAX_RETRY = 2;

    @Override
    public boolean retry(ITestResult result) {
        return count++ < MAX_RETRY;
    }
}
```

---

### Parallel Suite Configuration

```xml
<suite name="Regression" parallel="classes" thread-count="3">
```

---

### Data-Driven Testing via Excel

`ExcelUtil` reads test data sheets.

`TestDataProvider` supplies rows to TestNG `@DataProvider`.

Each row executes as an independent test iteration.

---

## Configuration

```text
src/test/resources/config.properties
```

Example:

```properties
base.url=https://freelance-learn-automation.vercel.app
browser.type=chromium
headless=false
timeout=10000

user.email=admin@email.com
user.password=admin@123
user.unregemail=singh.j@gmail.com
```

All environment-specific values are externalized here.

Browser type and headless mode can also be overridden using Maven `-D` runtime parameters.

---

## Setup

```bash
git clone https://github.com/NPrad1/playwright-automation.git

cd playwright-automation

mvn clean install

npx playwright install
```

---

## Test Execution

| Command | Description |
|---|---|
| `mvn clean test` | Run complete suite |
| `mvn test -DsuiteFile=src/test/resources/smoke-suite.xml` | Run smoke suite |
| `mvn test -DsuiteFile=src/test/resources/regression-suite.xml` | Run regression suite |
| `mvn test -Dbrowser=firefox` | Run tests on Firefox |
| `mvn test -Dheadless=true` | Run in headless mode |

---

## Reporting

### Extent Report

Single-file HTML report shareable without additional setup.

```text
test-output/ExtentReport.html
```

Contains:
- execution summary
- pass/fail status
- screenshots
- execution logs

---

### Allure Report

Deep-debug report containing:
- screenshots
- videos
- Playwright traces
- step-level execution details

Generate report:

```bash
allure serve target/allure-results
```

Both reports are generated from the same execution.

- Extent Report → stakeholder-friendly summary
- Allure Report → engineering/debugging analysis

---

## Playwright Artifacts

| Artifact | Location |
|---|---|
| Failure videos | `test-output/videos/` |
| Playwright traces | `test-output/traces/` |

Open trace manually:

```bash
npx playwright show-trace test-output/traces/<test-name>.zip
```

---

## CI/CD — GitHub Actions

```yaml
- name: Install Playwright browsers
  run: npx playwright install --with-deps

- name: Run smoke suite
  run: mvn test -DsuiteFile=src/test/resources/smoke-suite.xml -Dheadless=true

- name: Upload Extent Report
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: extent-report
    path: test-output/ExtentReport.html

- name: Upload failure traces
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: failure-traces
    path: test-output/traces/
```

Workflow file:

```text
.github/workflows/ci.yml
```

---

## Logging

Framework uses Log4j2 for centralized logging.

Configuration file:

```text
src/test/resources/log4j2.xml
```

Logs capture:
- browser initialization
- navigation
- actions
- failures

Logs are also appended to Extent Report entries through listeners.

---

## Current Capabilities

- UI Automation
- Cross Browser Testing
- Parallel Execution
- Retry Handling
- Screenshot Capture
- Video Recording
- Playwright Tracing
- Extent + Allure Reporting
- CI/CD Execution
- Excel Data Handling

---

## Planned Improvements

- API Automation Integration
- Docker-based Execution
- Jenkins Pipeline Integration
- Cloud Execution (BrowserStack / LambdaTest)
- Database Validation
- Accessibility Testing
- Visual Regression Testing

---

## Author

Pradeep Kumar

GitHub:
https://github.com/NPrad1