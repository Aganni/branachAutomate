package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.Dashboard.ApplicationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;
import ui.pages.jarvis.LoginPage;

import static dynamicData.DynamicDataClass.get;

public class LoginSteps extends BaseTest {

    @When("User switches to Jarvis and opens the application")
    public void switchToJarvisAndOpenApp() throws Exception {
        BaseTest.getCredentials("jarvis");
        BaseTest.switchToJarvisPortal();

        LoginPage jarvisLogin = new LoginPage(BaseTest.getPage());
        jarvisLogin.login();

        DashboardPage dashboardPage = new DashboardPage(BaseTest.getPage());
        dashboardPage.navigateToApplicationTab();

        ApplicationDashboardPage appDashboard = new ApplicationDashboardPage(BaseTest.getPage());
        appDashboard.searchByCriteria("Partner LID", get().getPartnerLoanId());
        Thread.sleep(800);
        DynamicDataClass.setValue("appFormId", appDashboard.getAppFormIdFromFirstRow());
        log.info("AppForm ID: [{}]", DynamicDataClass.getValue("appFormId"));
        appDashboard.openFirstApplication();
    }
}
