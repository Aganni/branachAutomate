package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import io.cucumber.java.en.*;
import ui.pages.jarvis.ApplicationPage;

public class ApplicationSteps {

    // Hardcoded Partner LID — update here when test data changes
    private static final String PARTNER_LID = "dsa-3d3e3f75-38d3-4dcf-b2e5-3c3c1dc279ad";

    private final ApplicationPage jarvisAppPage = new ApplicationPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) {
        jarvisAppPage.navigateToApplicationTab();
        jarvisAppPage.searchByCriteria(searchType, PARTNER_LID);

        // Capture and store App ID before clicking open — needed for Allocation
        // Dashboard
        String appFormId = jarvisAppPage.getAppFormIdFromFirstRow();
        DynamicDataClass.setValue("appFormId", appFormId);

        jarvisAppPage.openFirstApplication();
    }
}
