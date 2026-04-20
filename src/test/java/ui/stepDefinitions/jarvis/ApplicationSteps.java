package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.ApplicationPage;

import static dynamicData.DynamicDataClass.get;

public class ApplicationSteps {

    public ApplicationPage jarvisAppPage = new ApplicationPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) {
        jarvisAppPage.navigateToApplicationTab();
        jarvisAppPage.searchByCriteria(searchType, "dsa-1c9e9659-0371-4a9f-ac13-f5013d4556da");
        jarvisAppPage.openFirstApplication();
    }
}
