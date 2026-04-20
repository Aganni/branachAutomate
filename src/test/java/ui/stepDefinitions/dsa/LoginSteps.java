package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.LoginPage;

public class LoginSteps extends BaseTest {
    private final LoginPage loginPage = new LoginPage();

    @Given("User navigates to the DSA Portal")
    public void navigateToDsaPortal() throws Exception {
        String url = BaseTest.initializeEnvironment("dsaPortalUrl");
        loginPage.navigateToPortal(url);
    }

    @When("User sign in via external login option with a registered email Id and lands on the DSA Dashboard")
    public void signInViaExternalLogin() throws Exception {
        BaseTest.getCredentials("dsa");
        
        if (BaseTest.getUserEmail() == null || BaseTest.getOtp() == null) {
            throw new RuntimeException("Email or OTP missing in uatCredentials.properties! email=" + BaseTest.getUserEmail() + ", otp=" + BaseTest.getOtp());
        }
        loginPage.clickExternalLogin();
        loginPage.loginWithEmailAndOtp(BaseTest.getUserEmail(), BaseTest.getOtp());
        log.info("Successfully signed in via external email/OTP option with email: {}", BaseTest.getUserEmail());

        loginPage.verifyRedirectedToDsaDashboard();
    }
}
