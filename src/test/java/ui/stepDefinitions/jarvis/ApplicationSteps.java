package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.Dashboard.ApplicationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;

import static dynamicData.DynamicDataClass.get;

public class ApplicationSteps extends BaseTest {


    private final ApplicationDashboardPage applicationDashboardPage = new ApplicationDashboardPage();
    private final DashboardPage dashboardPage = new DashboardPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) throws InterruptedException {
        dashboardPage.navigateToApplicationTab();

        applicationDashboardPage.searchByCriteria(searchType, "dsa-ae6cc320-3088-4dd0-80cb-65525064a051");

        // Capture and store App ID before clicking open — needed for Allocation
        DynamicDataClass.setValue("appFormId", applicationDashboardPage.getAppFormIdFromFirstRow());
        log.info("Appform ID [{}]", DynamicDataClass.getValue("appFormId"));
        applicationDashboardPage.openFirstApplication();
    }
}
