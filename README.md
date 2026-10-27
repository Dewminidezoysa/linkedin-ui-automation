# LinkedIn UI Automation

Automated UI tests for the IT3153 Software Quality Assurance assignment at General Sir John Kotelawala Defence University.

## Tools

- Java 21
- Maven
- Selenium WebDriver 4.51.0
- TestNG 7.11.0
- Maven Surefire Plugin 3.5.2
- Google Chrome

The project is configured to compile with Java 21. Chrome and an internet connection are required. Selenium Manager handles ChromeDriver setup automatically.

## Test Coverage

| Test method | Purpose |
|---|---|
| BrowserTest.loginPageLoads | Checks that the login page displays a visible email field |
| LoginTest.validLogin | Checks that valid credentials open the feed |
| LoginTest.invalidPasswordShowsError | Checks that an invalid password displays the expected error |
| SearchTest.searchDisplaysResults | Searches for Microsoft and checks the results URL and a visible result |
| SearchTest.firstResultOpensCompanyPage | Opens the first visible View page result and checks the Microsoft company URL |

## Project Structure

- src/test/java/base: browser setup and cleanup
- src/test/java/pages: page objects for login and searching
- src/test/java/tests: TestNG test classes
- src/test/resources: reserved for test resources
- pom.xml: dependencies and build configuration

## Configure Credentials

Use only your own LinkedIn account. Do not put credentials in source files or commit them to Git.

Open PowerShell in the project folder and run:

```powershell
$env:LINKEDIN_EMAIL = (Read-Host "Enter your LinkedIn email").Trim()
```

Then run:

```powershell
$linkedinSecret = Read-Host "Enter your LinkedIn password" -AsSecureString
$env:LINKEDIN_PASSWORD = [System.Net.NetworkCredential]::new("", $linkedinSecret).Password
Remove-Variable linkedinSecret
```

Run the tests from this same terminal. These environment variables apply to the current terminal session and must be configured again in a new session.

The invalid-password test uses a deliberately incorrect test password and checks that it differs from the configured valid password.

## Run the Tests

Run all five tests:

```powershell
mvn test
```

Run individual tests:

```powershell
mvn test "-Dtest=BrowserTest"
mvn test "-Dtest=LoginTest#validLogin"
mvn test "-Dtest=LoginTest#invalidPasswordShowsError"
mvn test "-Dtest=SearchTest#searchDisplaysResults"
mvn test "-Dtest=SearchTest#firstResultOpensCompanyPage"
```

To rebuild from saved source files:

```powershell
mvn clean test
```

A successful full run reports five tests with zero failures, errors, or skipped tests.

## Test Design

The project uses the Page Object Model to separate page interactions from test assertions.

BaseTest starts a new Chrome session before each test and closes it afterwards. Explicit waits check conditions instead of using fixed sleep delays.

LinkedIn can contain hidden duplicate fields. The page objects select visible, enabled elements. Search interactions retry when LinkedIn replaces an element and Selenium reports a stale element reference.

## Screenshots and Reports

Tests automatically save timestamped screenshots in the evidence folder. Screenshots are captured in finally blocks, including when a test fails after entering the block.

Maven Surefire writes test results to:

```text
target/surefire-reports/
```

The evidence and target folders are excluded from Git. Retain screenshots locally for the assignment report and hide personal information before sharing.

Running mvn clean removes target and its reports, but does not remove evidence.

## Login Security and Limitations

LinkedIn restricts automated access and may present CAPTCHA, verification, or account restrictions. Do not bypass these controls.

The valid-login test and the login step in search tests allow up to 120 seconds for the feed to open. If verification appears, complete it manually in the automated browser during that period. Record any manual intervention in the report.

Stop repeated attempts if LinkedIn rejects credentials or restricts the account.

These tests cover a small set of flows on a live application. LinkedIn may change its layout, wording, locators, or result order. The result-opening test expects Microsoft's company page for the chosen search term; a changed first result will require investigation.

## Troubleshooting

- Missing credentials: configure both environment variables in the terminal used to run Maven.
- Compilation errors: save all files and read the first compiler error.
- Hidden elements: inspect matching elements and select a visible, enabled one.
- Stale elements: locate the element again after a page update.
- Login rejected: verify email/password login manually before retrying.
- Failed test: inspect the terminal output, Surefire report, and automatic screenshot.