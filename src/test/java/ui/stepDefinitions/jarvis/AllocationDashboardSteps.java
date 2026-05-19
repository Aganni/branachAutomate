package ui.stepDefinitions.jarvis;

import data.TestDataProvider;
import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormPage.ApplicationPage;
import ui.pages.jarvis.Dashboard.AllocationDashboardPage;
import ui.pages.jarvis.Dashboard.DashboardPage;

public class AllocationDashboardSteps extends BaseTest {

    @And("User assigns appForm and moves to CAM stage")
    public void assignAndMoveToCam() throws InterruptedException {
        allocateAndOpenAppForm();
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectApplicationActionAndAccept("Move to CAM", "Moving_AppFrom");
    }

    @And("User assigns appForm and moves to Credit Review")
    public void assignAndMoveToCreditReview() throws InterruptedException {
        allocateAndOpenAppForm();
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectApplicationActionAndAccept("Move to Credit Review", "Moving_AppFrom");
    }

    @And("User assigns appForm and moves to Credit Approval")
    public void assignAndMoveToCreditApproval() throws InterruptedException {
        String level = TestDataProvider.get("jarvis.assignment.level");
        String user = TestDataProvider.get("jarvis.assignment.user");

        allocateAndOpenAppForm();
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectActionWithAssignment("Move to Credit Approval", level, user, "Moving_AppFrom");
    }

    @And("User moves to Sanction Approval")
    public void moveToSanctionApproval() {
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectApplicationActionAndAccept("Move to Sanction Approval", "Moving_AppFrom");
    }

    @And("User reassigns and moves to QC Review")
    public void reassignAndMoveToQcReview() {
        String level = TestDataProvider.get("jarvis.assignment.level");
        String user = TestDataProvider.get("jarvis.assignment.user");

        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.reassignApplication(level, user, "ReAssign_AppForm");
        appPage.selectApplicationActionAndAccept("Move to QC Review", "Moving_AppFrom");
    }

    @And("User approves the application and triggers disbursal")
    public void approveAndDisburse() {
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectApplicationActionAndAccept("Approve this Application", "Moving_AppFrom");
    }

    // ── Helper ──
    private void allocateAndOpenAppForm() throws InterruptedException {
        String tenjinEmail = TestDataProvider.get("jarvis.assignment.user");

        AllocationDashboardPage allocationPage = new AllocationDashboardPage(BaseTest.getPage());
        DashboardPage dashboardPage = new DashboardPage(BaseTest.getPage());

        dashboardPage.navigateToAllocationDashboard();
        allocationPage.switchToTeamView();

        String appFormId = (String) DynamicDataClass.getValue("appFormId");
        allocationPage.searchByAppId(appFormId);
        Thread.sleep(1000);

        allocationPage.selectFirstRowCheckbox();
        allocationPage.clickAllocate();
        allocationPage.clickProceedOnAlert();
        allocationPage.manualAssignAndFinish(tenjinEmail);
        allocationPage.verifyAssignedUser("Tenjin User");
        allocationPage.openAppFormByApplicantName();
    }
}
