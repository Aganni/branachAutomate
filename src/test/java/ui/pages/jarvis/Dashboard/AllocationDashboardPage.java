package ui.pages.jarvis.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AllocationDashboardPage extends BaseTest {

    // ── Locators ──────────────────────────────────────────────────────
    private static final String TEAM_VIEW_SWITCH = ".download-csv-container-sme .el-switch";
    private static final String FIRST_ROW_CHECKBOX = ".el-table__body tbody tr:first-child .el-checkbox__original, .el-table__body tbody tr:first-child .el-checkbox";

    // Locators for verification and opening the form
    private static final String FIRST_ROW_ASSIGNEE_NAME = ".el-table__body tbody tr:first-child .assignee-container p.applicant-name";
    private static final String FIRST_ROW_APPLICANT_NAME = ".el-table__body tbody tr:first-child .appform-row p.word-ellipsis";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void switchToTeamView() {
        log.info("Switching to Team View...");
        Locator teamViewSwitch = getPage().locator(TEAM_VIEW_SWITCH).first();
        teamViewSwitch.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        teamViewSwitch.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void searchByAppId(String appFormId) {
        log.info("Searching Allocation Dashboard by App ID: {}", appFormId);

        getPage().getByPlaceholder("Search application by").click();

        Locator appIdOption = getPage().locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText("App ID"))
                .first();
        appIdOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        appIdOption.click();

        Locator searchInput = getPage().getByPlaceholder("Enter App ID");
        searchInput.waitFor();
        searchInput.fill(appFormId);
        getPage().keyboard().press("Enter");

        // Wait for the Element UI loading mask to disappear, then wait for network
        try {
            getPage().locator(".el-loading-mask").first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignore) {}
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        getPage().waitForTimeout(1000); // Brief pause to allow Vue.js to re-render the table row
    }

    public void selectFirstRowCheckbox() {
        log.info("Selecting the checkbox for the first result row...");
        Locator checkbox = getPage().locator(FIRST_ROW_CHECKBOX).first();
        checkbox.waitFor();
        checkbox.click(new Locator.ClickOptions().setForce(true));
    }

    public void clickAllocate() {
        log.info("Clicking Allocate button...");
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allocate")).click();
    }

    public void clickProceedOnAlert() {
        // If an alert pops up before the main modal, click proceed
        try {
            Locator proceed = getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Proceed"));
            proceed.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            proceed.click();
            log.info("Clicked Proceed on alert.");
        } catch (Exception e) {
            log.info("No Proceed alert detected, moving directly to allocation modal.");
        }
    }

    /**
     * Executes the specific manual assignment flow shown in the HTML
     */
    public void manualAssignAndFinish(String email) {
        log.info("Performing Manual Assignment to: {}", email);

        // 1. Click "Manual assign" radio button
        Locator manualRadio = getPage().locator("label").filter(new Locator.FilterOptions().setHasText("Manual assign"));
        manualRadio.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        manualRadio.click();

        // 2. Open the "Select User" dropdown
        Locator userInput = getPage().getByPlaceholder("Select User");
        userInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // FIX 1: Force click to bypass the "intercepts pointer events" block from the div wrapper
        userInput.click(new Locator.ClickOptions().setForce(true));

        // FIX 2: Do NOT use .fill(email) because the input is readonly="readonly".
        // Just opening it is enough, Playwright will find the option in the list.

        // 3. Click the specific email option from the Element UI dropdown list
        log.info("Looking for user in dropdown list: {}", email);
        Locator userOption = getPage().locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(email))
                .first();

        // Playwright will automatically scroll the internal list to find this element
        userOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        userOption.click();

        // 4. Click "Assign & Finish"
        Locator assignFinishBtn = getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Assign & Finish"));
        assignFinishBtn.click();

        log.info("Allocation submitted. Waiting for table refresh...");
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        getPage().waitForTimeout(1500); // Brief wait for the modal to close and row to update
    }

    /**
     * Verifies the 'Assigned To' column in the first row matches the expected name
     */
    public void verifyAssignedUser(String expectedUserName) {
        log.info("Verifying assignee is updated to: {}", expectedUserName);
        Locator assigneeName = getPage().locator(FIRST_ROW_ASSIGNEE_NAME).first();
        assigneeName.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Assert that the text contains the expected user's name
        assertThat(assigneeName).containsText(expectedUserName);
        log.info("Verified! Assigned user is correctly showing as {}", expectedUserName);
    }

    /**
     * Opens the appform by clicking the Application Name (Applicant Name)
     */
    public void openAppFormByApplicantName() {
        log.info("Opening appform by clicking Applicant Name...");
        Locator applicantNameLink = getPage().locator(FIRST_ROW_APPLICANT_NAME).first();
        applicantNameLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        applicantNameLink.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Appform opened successfully.");
    }
}