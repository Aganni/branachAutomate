package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.Dashboard.ApplicationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;
import ui.pages.jarvis.LoginPage;
import ui.pages.jarvis.RiskCategoryPage;

import java.util.List;
import java.util.Map;

/**
 * UI step definitions for Risk Category badge validation in Jarvis.
 * API validation steps are in backend.stepDefinitions.RiskCategoryApiSteps.
 */
public class RiskCategorySteps extends BaseTest {

    private RiskCategoryPage riskCategoryPage;
    private ApplicationDashboardPage appDashboard;

    // ═══════════════════════════════════════════════════════════════════════════
    //  BACKGROUND STEPS
    // ═══════════════════════════════════════════════════════════════════════════

    @Given("User logs into Jarvis portal")
    public void userLogsIntoJarvisPortal() throws Exception {
        BaseTest.getCredentials("jarvis");
        String jarvisUrl = BaseTest.initializeEnvironment("jarvisUrl");
        BaseTest.getPage().navigate(jarvisUrl);

        LoginPage jarvisLogin = new LoginPage(BaseTest.getPage());
        jarvisLogin.login();
        log.info("Successfully logged into Jarvis portal");
    }

    @And("User navigates to Application Dashboard")
    public void userNavigatesToApplicationDashboard() {
        DashboardPage dashboardPage = new DashboardPage(BaseTest.getPage());
        dashboardPage.navigateToApplicationTab();
        appDashboard = new ApplicationDashboardPage(BaseTest.getPage());
        log.info("Navigated to Application Dashboard");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SEARCH & NAVIGATION STEPS
    // ═══════════════════════════════════════════════════════════════════════════

    @Then("User clears the date filter")
    public void userClearsTheDateFilter() {
        appDashboard = new ApplicationDashboardPage(BaseTest.getPage());
        appDashboard.clearDateFilter();
    }

    @When("User searches application by {string} with value {string}")
    public void userSearchesApplicationByWithValue(String searchType, String searchValue) {
        appDashboard = new ApplicationDashboardPage(BaseTest.getPage());
        DynamicDataClass.setValue("appFormId", searchValue);
        appDashboard.searchByCriteria(searchType, searchValue);
        log.info("Searched by [{}] with value: {}", searchType, searchValue);
    }

    @And("User opens the first application from search results")
    public void userOpensTheFirstApplicationFromSearchResults() throws InterruptedException {
        appDashboard.openFirstApplication();
        riskCategoryPage = new RiskCategoryPage(BaseTest.getPage());
        log.info("Opened first application from search results");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  RISK CATEGORY BADGE UI STEPS
    // ═══════════════════════════════════════════════════════════════════════════

    @Then("Risk Category badge is visible on appForm header")
    public void riskCategoryBadgeIsVisibleOnAppFormHeader() {
        riskCategoryPage = new RiskCategoryPage(BaseTest.getPage());
        riskCategoryPage.verifyRiskCategoryBadgeVisible();
    }

    @When("User hovers on Risk Category badge")
    public void userHoversOnRiskCategoryBadge() {
        riskCategoryPage.hoverOnRiskCategoryBadge();
    }

    @Then("Risk Category popover is displayed with applicant table")
    public void riskCategoryPopoverIsDisplayedWithApplicantTable() {
        riskCategoryPage.verifyPopoverTableDisplayed();
    }

    @And("Popover table has columns {string} {string} {string}")
    public void popoverTableHasColumns(String col1, String col2, String col3) {
        riskCategoryPage.verifyPopoverTableColumns(col1, col2, col3);
    }

    @And("Risk category {string} with {string} badge is shown for applicant")
    public void riskCategoryWithBadgeIsShownForApplicant(String expectedRiskCategory, String expectedColor) {
        riskCategoryPage.verifyRiskCategoryColor(expectedRiskCategory, expectedColor);
        // Store UI data for API comparison step
        storeUiPopoverData();
    }

    @And("Popover displays multiple applicants with individual risk categories")
    public void popoverDisplaysMultipleApplicantsWithIndividualRiskCategories() {
        riskCategoryPage.verifyMultipleApplicantsWithIndividualRiskCategories();
        // Store UI data for API comparison step
        storeUiPopoverData();
    }

    @Then("Risk Category popover displays {string} message")
    public void riskCategoryPopoverDisplaysMessage(String expectedMessage) {
        riskCategoryPage.verifyRiskCategoryNotAvailable();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Extracts popover data from UI and stores in DynamicDataClass
     * for cross-validation with API responses.
     */
    private void storeUiPopoverData() {
        List<Map<String, String>> uiData = riskCategoryPage.getRiskCategoryPopoverData();
        DynamicDataClass.setValue("uiRiskCategoryData", uiData);
        log.info("Stored {} applicant records from UI popover for API validation", uiData.size());
    }
}
