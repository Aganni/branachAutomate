package ui.stepDefinitions.dsa;

import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.PartnerDetailsPage;

import java.util.LinkedHashMap;
import java.util.Map;

public class PartnerDetailsSteps extends BaseTest {

    @Then("User completes Partner Details and proceeds")
    public void completePartnerDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Branch", TestDataProvider.get("dsa.partner_details.branch"));
        details.put("Sales Manager", TestDataProvider.get("dsa.partner_details.sales_manager"));
        details.put("Scheme", TestDataProvider.get("dsa.partner_details.scheme"));

        PartnerDetailsPage page = new PartnerDetailsPage(BaseTest.getPage());
        page.fillMandatoryDetails(details);
        page.clickSaveAndNext();
    }
}
