package ui.stepDefinitions.dsa;

import backend.Utils.AwsSqsUtils;
import backend.payload.SqsQueuePayload;
import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.ConsentPage;

import static dynamicData.DynamicDataClass.get;

public class ConsentSteps extends BaseTest {

    @And("User completes Consent page with SQS bypass")
    public void completeConsentWithSqsBypass() throws InterruptedException {
        ConsentPage consentPage = new ConsentPage(BaseTest.getPage());
        String otp = TestDataProvider.get("dsa.consent.otp");
        String email = TestDataProvider.get("dsa.consent.entity_email");

        // Send OTP + verify
        consentPage.clickSendOtp();
        consentPage.fillOtp(otp);

        // Entity email consent
        consentPage.enterEmail(email);
        consentPage.clickSendEmail();

        // SQS bypass for email verification
        String partnerLoanId = get().getPartnerLoanId();
        String payload = SqsQueuePayload.buildCustomerConsentPayload(email, partnerLoanId);
        AwsSqsUtils.pushMessageToQueue(payload);
        Thread.sleep(2000);

        // Submit consent
        consentPage.clickSubmitConsent();
    }
}
