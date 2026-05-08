package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.Then;
import ui.pages.dsa.EligibilityCheckPage;

public class EligibilityCheckSteps {

    private final EligibilityCheckPage eligibilityCheckPage = new EligibilityCheckPage(BaseTest.getPage());

    @Then("User waits for the Eligibility Prechecks to complete and clicks Next")
    public void userWaitsForEligibilityPrechecksAndClicksNext() {
        eligibilityCheckPage.waitForPrechecksAndClickNext();
    }
}