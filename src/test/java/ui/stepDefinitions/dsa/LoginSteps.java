package ui.stepDefinitions.dsa;

import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.DashboardPage;
import ui.pages.dsa.LoginPage;

public class LoginSteps extends BaseTest {

    @When("User logs into DSA Portal and initiates a Business Loan application")
    public void loginAndInitiateApplication() throws Exception {
        String url = BaseTest.initializeEnvironment("dsaPortalUrl");
        LoginPage loginPage = new LoginPage(BaseTest.getPage());
        loginPage.navigateToPortal(url);

        BaseTest.getCredentials("dsa");
        loginPage.clickExternalLogin();
        loginPage.loginWithEmailAndOtp(BaseTest.getUserEmail(), BaseTest.getOtp());
        loginPage.verifyRedirectedToDsaDashboard();

        String loanType = TestDataProvider.get("dsa.business_details.loan_type");
        DashboardPage dashboardPage = new DashboardPage(BaseTest.getPage());
        dashboardPage.initiateApplication(loanType);
    }
}
