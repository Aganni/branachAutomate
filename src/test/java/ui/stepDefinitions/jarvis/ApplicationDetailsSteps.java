package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import java.util.Map;
import ui.pages.jarvis.AppFormPage.AppformTab.BusinessDetails;



public class ApplicationDetailsSteps {

    private final BusinessDetails appDetailsPage;

    public ApplicationDetailsSteps() {
        this.appDetailsPage = new BusinessDetails(BaseTest.getPage());
    }

    @And("User updates the Business Details with the following data:")
    public void updateBusinessDetails(Map<String, String> details) {
        appDetailsPage.openBusinessDetailsAndEdit();
        appDetailsPage.fillCompanyDetails(details,"Updating_Business_Details");
    }
}
