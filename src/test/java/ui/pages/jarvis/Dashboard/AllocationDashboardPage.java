package ui.pages.jarvis.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AllocationDashboardPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String TEAM_VIEW_SWITCH = ".download-csv-container-sme .el-switch";
    private static final String FIRST_ROW_CHECKBOX = ".el-table__body tbody tr:first-child .el-checkbox__original, .el-table__body tbody tr:first-child .el-checkbox";
    private static final String FIRST_ROW_ASSIGNEE_NAME = ".el-table__body tbody tr:first-child .assignee-container p.applicant-name";
    private static final String FIRST_ROW_APPLICANT_NAME = ".el-table__body tbody tr:first-child .appform-row p.word-ellipsis";

    public AllocationDashboardPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void switchToTeamView() {
        log.info("Switching to Team View...");
        Locator teamViewSwitch = page.locator(TEAM_VIEW_SWITCH).first();
        teamViewSwitch.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        teamViewSwitch.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void searchByAppId(String appFormId) {
        log.info("Searching Allocation Dashboard by App ID: {}", appFormId);

        page.getByPlaceholder("Search application by").click();

        Locator appIdOption = page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText("App ID"))
                .first();
        appIdOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        appIdOption.click();

        Locator searchInput = page.getByPlaceholder("Enter App ID");
        searchInput.waitFor();
        searchInput.fill(appFormId);
        page.keyboard().press("Enter");

        try {
            page.locator(".el-loading-mask").first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignore) {}
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); 
    }

    public void selectFirstRowCheckbox() {
        log.info("Selecting the checkbox for the first result row...");
        Locator checkbox = page.locator(FIRST_ROW_CHECKBOX).first();
        checkbox.waitFor();
        checkbox.click(new Locator.ClickOptions().setForce(true));
    }

    public void clickAllocate() {
        log.info("Clicking Allocate button...");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allocate")).click();
    }

    public void clickProceedOnAlert() {
        try {
            Locator proceed = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Proceed"));
            proceed.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            proceed.click();
            log.info("Clicked Proceed on alert.");
        } catch (Exception e) {
            log.info("No Proceed alert detected, moving directly to allocation modal.");
        }
    }

    public void manualAssignAndFinish(String email) {
        log.info("Performing Manual Assignment to: {}", email);

        Locator manualRadio = page.locator("label").filter(new Locator.FilterOptions().setHasText("Manual assign"));
        manualRadio.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        manualRadio.click();

        Locator userInput = page.getByPlaceholder("Select User");
        userInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        userInput.click(new Locator.ClickOptions().setForce(true));

        log.info("Looking for user in dropdown list: {}", email);
        Locator userOption = page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(email))
                .first();

        userOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        userOption.click();

        Locator assignFinishBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Assign & Finish"));
        assignFinishBtn.click();

        log.info("Allocation submitted. Waiting for table refresh...");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1500); 
    }

    public void verifyAssignedUser(String expectedUserName) {
        page.waitForTimeout(1500);
        log.info("Verifying assignee is updated to: {}", expectedUserName);
        Locator assigneeName = page.locator(FIRST_ROW_ASSIGNEE_NAME).first();
        assigneeName.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        assertThat(assigneeName).containsText(expectedUserName);
        log.info("Verified! Assigned user is correctly showing as {}", expectedUserName);
    }

    public void openAppFormByApplicantName() {
        log.info("Opening appform by clicking Applicant Name...");
        Locator applicantNameLink = page.locator(FIRST_ROW_APPLICANT_NAME).first();
        applicantNameLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        applicantNameLink.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Appform opened successfully.");
    }
}