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

        applicationDashboardPage.searchByCriteria(searchType, "dsa-db1fc6a0-b1a9-48f7-8a06-cb3ed1ac5044");

        // Capture and store App ID before clicking open — needed for Allocation
        Thread.sleep(800);
        DynamicDataClass.setValue("appFormId", applicationDashboardPage.getAppFormIdFromFirstRow());
        log.info("Appform ID [{}]", DynamicDataClass.getValue("appFormId"));
        applicationDashboardPage.openFirstApplication();
    }
}
