package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import java.util.Map;

import ui.pages.jarvis.AppFormPage.AppformTab.BankDetails;
import ui.pages.jarvis.AppFormPage.AppformTab.BusinessDetails;



public class ApplicationDetailsSteps {

    private final BusinessDetails businessDetailsPage;

    private final BankDetails bankDetailsPage;

    public ApplicationDetailsSteps() {
        this.businessDetailsPage = new BusinessDetails(BaseTest.getPage());
        this.bankDetailsPage = new BankDetails(BaseTest.getPage());
    }

    @And("User updates the Business Details with the following data:")
    public void updateBusinessDetails(Map<String, String> details) {
        businessDetailsPage.openBusinessDetailsAndEdit();
        businessDetailsPage.fillCompanyDetails(details,"Updating_Business_Details");
        businessDetailsPage.navigateBackToAppDetails();
    }

    @And("User updates the Bank Details with the following data:")
    public void updateBankDetailsWithData(Map<String, String> details) {
        bankDetailsPage.openBankDetailsAndEdit();
        bankDetailsPage.fillBankDetails(details, "Updating_Bank_Details");
    }
}
