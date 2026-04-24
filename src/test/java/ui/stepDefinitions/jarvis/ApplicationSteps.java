package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.Dashboard.ApplicationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;

public class ApplicationSteps extends BaseTest {

    // Hardcoded Partner LID — update here when test data changes
    private static final String PARTNER_LID = "dsa-4cbeb803-cc75-4f6d-9e67-f2bbaa4a0ae2";


    private final ApplicationDashboardPage applicationDashboardPage = new ApplicationDashboardPage();
    private final DashboardPage dashboardPage = new DashboardPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) throws InterruptedException {
        dashboardPage.navigateToApplicationTab();

        applicationDashboardPage.searchByCriteria(searchType, PARTNER_LID);

        // Capture and store App ID before clicking open — needed for Allocation
        DynamicDataClass.setValue("appFormId", applicationDashboardPage.getAppFormIdFromFirstRow());
        log.info("Appform ID [{}]", DynamicDataClass.getValue("appFormId"));

        applicationDashboardPage.openFirstApplication();
    }
}
