package ui.stepDefinitions.dsa;

import io.cucumber.java.en.Then;
import ui.pages.dsa.EligibilityCheckPage;

public class EligibilityCheckSteps {

    private final EligibilityCheckPage eligibilityCheckPage = new EligibilityCheckPage();

    @Then("User waits for the Eligibility Prechecks to complete and clicks Next")
    public void userWaitsForEligibilityPrechecksAndClicksNext() {
        eligibilityCheckPage.waitForPrechecksAndClickNext();
    }
}