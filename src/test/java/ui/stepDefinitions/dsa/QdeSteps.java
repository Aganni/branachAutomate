package ui.stepDefinitions.dsa;

import backend.constants.Constants;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.QdePage;

import static dynamicData.DynamicDataClass.getValue;

public class QdeSteps extends BaseTest {

    private final QdePage qdePage = new QdePage();

    @Then("User verifies the generated PAN in QDE page and checks auto-populated name {string} for the Primary Co-applicant")
    public void verifyPanInQde(String expectedName) {
        qdePage.enterPanAndVerify((String) getValue(Constants.PAN_CARD));
        qdePage.verifyAutoPopulatedName(expectedName);
    }

    @Then("User fills remaining basic details in QDE page with {string} shareholding, saves, and submits")
    public void fillRemainingQdeDetailsAndSave(String shareholding) {
        qdePage.enterMobileNumber((String) getValue(Constants.MOBILE_NUMBER));
        qdePage.enterShareholdingPercentage(shareholding);

        qdePage.clickSaveButton();
        qdePage.clickSubmitButton();
    }
}