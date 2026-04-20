package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import ui.Utils.Utils;
import ui.pages.dsa.PartnerDetailsPage;

import java.util.Map;

import static dynamicData.DynamicDataClass.get;

public class PartnerDetailsSteps extends BaseTest{
    private final PartnerDetailsPage partnerDetailsPage = new PartnerDetailsPage();

    @Then("User fills the mandatory details in partner details page and clicks on Save and Next button")
    public void userFillsTheMandatoryPartnerDetails(DataTable dataTable) {
        // Convert the Cucumber DataTable to a Map <String, String>
        Map<String, String> details = dataTable.asMap(String.class, String.class);
        partnerDetailsPage.fillMandatoryDetails(details);

        partnerDetailsPage.clickSaveAndNext();
    }
}
