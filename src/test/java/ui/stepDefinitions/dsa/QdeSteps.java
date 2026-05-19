package ui.stepDefinitions.dsa;

import backend.constants.Constants;
import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.QdePage;

import static dynamicData.DynamicDataClass.getValue;

public class QdeSteps extends BaseTest {

    @And("User completes QDE with co-applicant details and submits")
    public void completeQde() {
        QdePage page = new QdePage(BaseTest.getPage());

        page.enterPanAndVerify((String) getValue(Constants.PAN_CARD));
        page.verifyAutoPopulatedName(TestDataProvider.get("dsa.qde.pan_profile"));
        page.enterMobileNumber((String) getValue(Constants.MOBILE_NUMBER));
        page.enterShareholdingPercentage(TestDataProvider.get("dsa.qde.shareholding"));
        page.clickSaveButton();
        page.clickSubmitButton();
    }
}
