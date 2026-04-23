package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AllocationDashboardPage;

public class AllocationDashboardSteps extends BaseTest {

    // Using the email from your uatCredentials and its display name
    private static final String TENJIN_EMAIL = "tenjin.user@creditsaison-in.com";
    private static final String TENJIN_DISPLAY_NAME = "Tenjin User";

    private final AllocationDashboardPage allocationPage = new AllocationDashboardPage();

    @And("User navigates to the Allocation Dashboard and assign appform to self using app ID")
    public void navigateToAllocationDashboard() {
        // 1. Navigate and setup view
        allocationPage.navigateToAllocationDashboard();
        allocationPage.switchToTeamView();

        // 2. Fetch App ID and search
        String appFormId = (String) DynamicDataClass.getValue("appFormId");
        log.info("Using stored App ID for allocation search: {}", appFormId);
        allocationPage.searchByAppId(appFormId);

        // 3. Trigger Allocation
        allocationPage.selectFirstRowCheckbox();
        allocationPage.clickAllocate();
        allocationPage.clickProceedOnAlert();

        // 4. Manual Assign Flow
        allocationPage.manualAssignAndFinish(TENJIN_EMAIL);

        // 5. Verify the row actually updated to Tenjin User before clicking and open the appform
        allocationPage.verifyAssignedUser(TENJIN_DISPLAY_NAME);
        allocationPage.openAppFormByApplicantName();
    }
}
