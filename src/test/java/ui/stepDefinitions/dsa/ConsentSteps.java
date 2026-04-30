package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.ConsentPage;

public class ConsentSteps extends BaseTest {
    private final ConsentPage consentPage ;

    public ConsentSteps(){
        this.consentPage = new ConsentPage(BaseTest.getPage());
    }

    @Then("User sends and verifies OTP {string} for Primary Applicant")
    public void sendAndVerifyOtp(String otp) {
        consentPage.clickSendOtp();
        consentPage.fillOtp(otp);
    }

    @Then("User provides email {string} for Entity, sends consent, and submits")
    public void provideEmailAndSubmit(String email) {
        consentPage.enterEmail(email);
        consentPage.clickSendEmail();
    }

    @Then("User submits the Consent page")
    public void userSubmitsTheConsentPage() {
        consentPage.clickSubmitConsent();
    }
}
