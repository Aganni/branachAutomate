package ui.stepDefinitions.jarvis;

import backend.Utils.ApiClientUtils;
import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import ui.pages.jarvis.Dashboard.ApplicationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;
import ui.pages.jarvis.LoginPage;
import ui.pages.jarvis.RiskCategoryPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // Store appFormId for test summary logging
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
    //  RISK CATEGORY BADGE STEPS
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
    }

    @And("Popover displays multiple applicants with individual risk categories")
    public void popoverDisplaysMultipleApplicantsWithIndividualRiskCategories() {
        riskCategoryPage.verifyMultipleApplicantsWithIndividualRiskCategories();
    }

    @Then("Risk Category popover displays {string} message")
    public void riskCategoryPopoverDisplaysMessage(String expectedMessage) {
        riskCategoryPage.verifyRiskCategoryNotAvailable();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  API VALIDATION STEPS
    // ═══════════════════════════════════════════════════════════════════════════

    @Then("User validates risk category from Helios API for appFormId {string}")
    public void userValidatesRiskCategoryFromHeliosAPI(String appFormId) throws Exception {
        JsonPath heliosResponse = ApiClientUtils.doPostHeliosRiskCategory(appFormId);

        // Extract applicant risk data from Helios response
        List<Map<String, Object>> apiApplicants = new ArrayList<>();
        Map<String, Object> result = heliosResponse.getMap("result");

        if (result != null) {
            for (Object value : result.values()) {
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> applicantList = (List<Map<String, Object>>) value;
                    apiApplicants.addAll(applicantList);
                }
            }
        }

        DynamicDataClass.setValue("heliosRiskData", apiApplicants);
        log.info("Helios API returned {} applicants with risk data", apiApplicants.size());
        Assert.assertFalse(apiApplicants.isEmpty(),
                "Helios API returned no risk data for appFormId: " + appFormId);
    }

    @And("User validates applicant name from Shield API for appFormId {string}")
    public void userValidatesApplicantNameFromShieldAPI(String appFormId) throws Exception {
        JsonPath shieldResponse = ApiClientUtils.doGetShieldAppForm(appFormId);

        List<Map<String, Object>> linkedIndividuals = shieldResponse.getList("linkedIndividuals");
        DynamicDataClass.setValue("shieldLinkedIndividuals", linkedIndividuals);
        log.info("Shield API returned {} linked individuals", linkedIndividuals.size());
        Assert.assertFalse(linkedIndividuals.isEmpty(),
                "Shield API returned no linked individuals for appFormId: " + appFormId);
    }

    @And("UI risk category data matches API response for all applicants")
    public void uiRiskCategoryDataMatchesAPIResponse() {
        // Get UI data from popover
        List<Map<String, String>> uiApplicants = riskCategoryPage.getRiskCategoryPopoverData();

        // Get API data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> heliosData =
                (List<Map<String, Object>>) DynamicDataClass.getValue("heliosRiskData");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> shieldData =
                (List<Map<String, Object>>) DynamicDataClass.getValue("shieldLinkedIndividuals");

        // Assert count matches
        Assert.assertEquals(uiApplicants.size(), heliosData.size(),
                "Applicant count mismatch between UI (" + uiApplicants.size()
                        + ") and Helios API (" + heliosData.size() + ")");

        // For each UI applicant, validate against API data
        for (Map<String, String> uiApplicant : uiApplicants) {
            String uiApplicantId = uiApplicant.get("applicantId");
            String uiName = uiApplicant.get("name");
            String uiRiskCategory = uiApplicant.get("riskCategory");

            // Find matching Helios record by applicant_id
            Map<String, Object> matchedHelios = findHeliosApplicant(uiApplicantId, heliosData);
            Assert.assertNotNull(matchedHelios,
                    "Applicant ID " + uiApplicantId + " from UI not found in Helios API response");

            // Assert risk category matches
            String apiRiskCategory = String.valueOf(matchedHelios.get("risk_category"));
            Assert.assertEquals(uiRiskCategory, apiRiskCategory,
                    "Risk category mismatch for applicant " + uiApplicantId);

            // Find matching Shield record to validate name
            String expectedName = findApplicantNameFromShield(uiApplicantId, shieldData);
            if (expectedName != null && !"Unknown".equals(expectedName)) {
                Assert.assertEquals(uiName, expectedName,
                        "Name mismatch for applicant " + uiApplicantId);
            }

            log.info("Validated applicant {} - Name: {}, Risk: {}", uiApplicantId, uiName, uiRiskCategory);
        }
        log.info("All UI data matches API response successfully");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<String, Object> findHeliosApplicant(String applicantId, List<Map<String, Object>> heliosData) {
        for (Map<String, Object> heliosApplicant : heliosData) {
            String heliosApplicantId = String.valueOf(heliosApplicant.get("applicant_id"));
            if (heliosApplicantId.equals(applicantId)) {
                return heliosApplicant;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String findApplicantNameFromShield(String applicantId, List<Map<String, Object>> shieldData) {
        for (Map<String, Object> linked : shieldData) {
            String linkedId = String.valueOf(linked.get("id"));
            if (linkedId.equals(applicantId)) {
                Map<String, Object> individual = (Map<String, Object>) linked.get("individual");
                if (individual != null) {
                    String firstName = individual.get("firstName") != null ? String.valueOf(individual.get("firstName")) : "";
                    String middleName = individual.get("middleName") != null
                            && !"null".equals(String.valueOf(individual.get("middleName")))
                            ? String.valueOf(individual.get("middleName")) : "";
                    String lastName = individual.get("lastName") != null ? String.valueOf(individual.get("lastName")) : "";
                    return (firstName + " " + middleName + " " + lastName).replaceAll("\\s+", " ").trim();
                }
            }
        }
        return "Unknown";
    }
}
