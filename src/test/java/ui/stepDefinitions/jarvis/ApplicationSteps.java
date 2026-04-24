package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.ApplicationPage;

import static dynamicData.DynamicDataClass.get;

public class ApplicationSteps extends BaseTest {

    private final ApplicationPage jarvisAppPage = new ApplicationPage();

    @And("User navigates to the Applications tab searches appFrom using {string} and opens the appform")
    public void openAppFromInJarvis(String searchType) throws InterruptedException {
        jarvisAppPage.navigateToApplicationTab();
        jarvisAppPage.searchByCriteria(searchType, get().getPartnerLoanId());

        // Capture and store App ID before clicking open — needed for Allocation
        DynamicDataClass.setValue("appFormId", jarvisAppPage.getAppFormIdFromFirstRow());
        log.info("Appform ID [{}]", DynamicDataClass.getValue("appFormId"));

        jarvisAppPage.openFirstApplication();
    }
}
