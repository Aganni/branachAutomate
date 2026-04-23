package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AllocationDashboardPage;

public class AllocationDashboardSteps extends BaseTest {

    private static final String TENJIN_EMAIL = "tenjin.user@creditsaison-in.com";

    private final AllocationDashboardPage allocationPage = new AllocationDashboardPage();

    @And("User navigates to the Allocation Dashboard and assign appform to self from unassigned queue using app ID")
    public void navigateToAllocationDashboard() {
        allocationPage.navigateToAllocationDashboard();
        allocationPage.switchToTeamView();
        String appFormId = (String) DynamicDataClass.getValue("appFormId");
        log.info("Using stored App ID for allocation search: {}", appFormId);
        allocationPage.searchByAppId(appFormId);
        allocationPage.selectFirstRowCheckbox();
        allocationPage.clickAllocate();
        allocationPage.clickProceedOnAlert();
    }

//    @And("User switches to Team View in the Allocation Dashboard")
//    public void switchToTeamView() {
//        allocationPage.switchToTeamView();
//    }
//
//    @And("User searches for the appform using App ID in Allocation Dashboard")
//    public void searchByAppIdInAllocationDashboard() {
//        // Retrieve the App ID captured earlier from the Applications table
//        String appFormId = (String) DynamicDataClass.getValue("appFormId");
//        log.info("Using stored App ID for allocation search: {}", appFormId);
//        allocationPage.searchByAppId(appFormId);
//    }
//
//    @And("User selects the appform checkbox and clicks Allocate")
//    public void selectCheckboxAndClickAllocate() {
//        allocationPage.selectFirstRowCheckbox();
//        allocationPage.clickAllocate();
//    }
//
//    @And("User clicks Proceed on the allocation alert popup")
//    public void clickProceedOnAlert() {
//        allocationPage.clickProceedOnAlert();
//    }

//    @And("User assigns the appform to Tenjin and finishes allocation")
//    public void assignToTenjinAndFinish() {
//        allocationPage.assignAndFinish(TENJIN_EMAIL);
//    }

    @And("User re-opens the appform from the Allocation Dashboard row")
    public void reOpenAppFormFromRow() {
        allocationPage.openAppFormFromRow();
    }
}
