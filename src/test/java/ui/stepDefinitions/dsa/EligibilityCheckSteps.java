package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.EligibilityCheckPage;

public class EligibilityCheckSteps {

    @And("User passes Eligibility Prechecks")
    public void passEligibilityPrechecks() {
        EligibilityCheckPage page = new EligibilityCheckPage(BaseTest.getPage());
        page.waitForPrechecksAndClickNext();
    }
}
