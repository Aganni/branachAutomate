package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import ui.Utils.Utils;
import ui.pages.dsa.BusinessDetailsPage;

public class BusinessDetailsSteps {
    private final BusinessDetailsPage businessDetailsPage;

    public BusinessDetailsSteps(){
       this.businessDetailsPage=new BusinessDetailsPage(BaseTest.getPage());
    }

    @Then("User enters Entity PAN {string} in business details, clicks the verification button, and verifies that entity name is {string}")
    public void userEntersPanAndVerifiesEntity(String panNumber, String expectedEntityName) {
        Utils.extractAndStorePartnerLoanId();
        businessDetailsPage.enterPanAndVerify(panNumber);
        businessDetailsPage.verifyAutoPopulatedEntityName(expectedEntityName);
    }

    @And("User clicks on the {string} button")
    public void userClicksOnTheButton(String buttonText) {
        if (buttonText.equals("Continue to fetch details linked to this PAN")) {
            businessDetailsPage.clickContinueToFetchDetails();
        }
    }

    @And("User select Entity Operational Address line 1 from the dropdown and verifies the auto-populated address details")
    public void userVerifiesTheAutoPopulatedAddressDetails(DataTable dataTable) {
        businessDetailsPage.fillOperationalAddress();
        businessDetailsPage.verifyAutoPopulatedAddress(dataTable.asMap(String.class, String.class));
    }

    @And("User selects {string} as the Ownership type")
    public void userSelectsAsTheOwnershipType(String ownership) {
        businessDetailsPage.selectOwnership(ownership);
    }

    @And("User selects {string} for Same as Operational Address for Registered Address")
    public void userSelectsForSameAsOperationalAddress(String option) {
        businessDetailsPage.selectSameAsOperationalAddress(option);
    }

    @Then("User fills the More Business Details and selects {string} as the Industry Type")
    public void userFillsTheMoreBusinessDetails(String sector,DataTable dataTable) {
        businessDetailsPage.fillMoreBusinessDetails(dataTable.asMap(String.class, String.class));
        businessDetailsPage.selectIndustrySubSector(sector);
    }

    @Then("User fills the Loan Requirements and clicks on the Submit button on business details page")
    public void userFillsTheLoanRequirements(DataTable dataTable) {
        businessDetailsPage.fillLoanRequirements(dataTable.asMap(String.class, String.class));
        businessDetailsPage.clickSubmit();
    }
}
