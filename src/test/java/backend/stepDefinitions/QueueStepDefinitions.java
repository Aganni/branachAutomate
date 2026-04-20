package backend.stepDefinitions;

import backend.Utils.AwsSqsUtils;
import backend.constants.Constants;
import backend.payload.SqsQueuePayload;
import io.cucumber.java.en.*;

import static dynamicData.DynamicDataClass.get;
import static dynamicData.DynamicDataClass.getValue;

public class QueueStepDefinitions {

    @When("User bypasses email verification by pushing {string} consent to SQS")
    public void pushConsentToSqs(String email) throws InterruptedException {
        // 1. Fetch the partnerLoanId we extracted from the URL earlier
        String partnerLoanId = get().getPartnerLoanId();

        if (partnerLoanId == null) {
            throw new IllegalStateException("partnerLoanId is null! Did you forget to extract it from the URL?");
        }

        // 2. Generate the payload
        String payload = SqsQueuePayload.buildCustomerConsentPayload(email, partnerLoanId);

        // 3. Push to AWS SQS
        AwsSqsUtils.pushMessageToQueue(payload);
        Thread.sleep(2000);
    }
}
