package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.ApplicationPage;

import static dynamicData.DynamicDataClass.get;

public class ApplicationSteps {

    public ApplicationPage jarvisAppPage = new ApplicationPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) {
        jarvisAppPage.navigateToApplicationTab();
        jarvisAppPage.searchByCriteria(searchType, "dsa-3d3e3f75-38d3-4dcf-b2e5-3c3c1dc279ad");
        jarvisAppPage.openFirstApplication();
    }
}
