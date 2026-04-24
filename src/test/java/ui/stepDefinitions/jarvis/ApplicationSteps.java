package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.ApplicationPage;

public class ApplicationSteps extends BaseTest {

    // Hardcoded Partner LID — update here when test data changes
    private static final String PARTNER_LID = "dsa-44c52e72-0ede-4598-bd4f-2dc9dd89fe3f";

    private final ApplicationPage jarvisAppPage = new ApplicationPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) throws InterruptedException {
        jarvisAppPage.navigateToApplicationTab();
        jarvisAppPage.searchByCriteria(searchType, "dsa-defbd0ef-d77c-4af0-ac4b-635a513b1a7f");

        // Capture and store App ID before clicking open — needed for Allocation
        DynamicDataClass.setValue("appFormId", jarvisAppPage.getAppFormIdFromFirstRow());
        log.info("Appform ID [{}]", DynamicDataClass.getValue("appFormId"));

        jarvisAppPage.openFirstApplication();
    }
}
