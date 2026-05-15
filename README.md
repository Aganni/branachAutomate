<div align="center">
  <img src="./logo.png" alt="branchBot Logo" width="300"/>
  <h1>branchBot - Jarvis & DSA UI Automation Framework</h1>
  <p>A highly scalable, modern UI test automation framework built for enterprise applications.</p>
</div>

---

## 🏗 Architecture & Design Patterns

The branchBot framework is designed with modularity, stability, and speed in mind. It strictly follows industry-standard design patterns to ensure maintainable and robust automated test scripts.

- **Page Object Model (POM):** We enforce a strict POM architecture where each web page is represented by a dedicated class. All locators and action methods are encapsulated within these classes.
- **Constructor-Based Dependency Injection:** State (the Playwright `Page` instance) is securely injected into Page Objects via constructors (e.g., `new LoginPage(BaseTest.getPage())`). This eliminates brittle static locators and allows for safe parallel test execution.
- **ThreadLocal State Management:** To support parallel execution, WebDriver/Playwright contexts are managed using `ThreadLocal`, ensuring complete isolation between concurrent test threads.
- **Behavior-Driven Development (BDD):** Tests are written in plain-text Gherkin syntax using Cucumber, creating living documentation that bridging the gap between business requirements and technical implementation.

---

## 🛠 Technology Stack

This framework leverages modern automation tools for optimal performance:

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 | The core programming language, leveraging modern features like Records, switch expressions, and improved memory management. |
| **Browser Automation** | Playwright (v1.47+) | Fast, reliable, and capable of handling dynamic Single Page Applications (Vue.js/React). |
| **BDD Framework** | Cucumber (v7.14+) | Enables writing executable specifications in Gherkin syntax. |
| **Test Runner** | TestNG | Provides powerful parallel execution capabilities, data providers, and test suite configuration. |
| **Build Tool** | Maven | Manages dependencies and build lifecycles seamlessly. |
| **Reporting** | ExtentReports | Generates rich, interactive HTML reports with screenshots for test failures. |

---

## 🚀 Setup & Execution

### Prerequisites
- **JDK 21** installed and configured in your `JAVA_HOME`.
- **Maven** installed.
- Access to the internal company network or VPN (for UAT environments).

### Running Tests
To compile and run the test suite locally:
```bash
# Clean and compile the project
mvn clean compile

# Run the full test suite
mvn test
```

---

## 📞 Contact Members

For any queries, access requests, or contributions to this repository, please reach out to the core maintainers:

*  [Adarsh Gani](https://creditsaison-in.enterprise.slack.com/team/U06EEEM5S9Y)
