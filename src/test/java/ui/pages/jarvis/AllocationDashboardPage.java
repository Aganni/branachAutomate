package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class AllocationDashboardPage extends BaseTest {

    // ── Locators ──────────────────────────────────────────────────────

    // Sidebar link matching your exact HTML
    private static final String ALLOCATION_DASHBOARD_LINK = "a[href='/new/dashboard']";

    // Team View switch (Targets the actual Element UI switch component next to the text)
    private static final String TEAM_VIEW_SWITCH = ".download-csv-container-sme .el-switch";

    // Results table locators
    private static final String FIRST_ROW_CHECKBOX = ".el-table__body tbody tr:first-child .el-checkbox__original, .el-table__body tbody tr:first-child .el-checkbox";
    private static final String FIRST_ROW_APP_ID_LINK = ".el-table__body tbody tr:first-child .app-id p";

    // ── Helper ────────────────────────────────────────────────────────
    public static Page getPage() {
        return BaseTest.getPage();
    }

    // ── Methods ───────────────────────────────────────────────────────

    /**
     * Navigates to the Allocation Dashboard using the sidebar link.
     */
    public void navigateToAllocationDashboard() {
        log.info("Navigating to Allocation Dashboard via sidebar...");
        Locator dashboardLink = getPage().locator(ALLOCATION_DASHBOARD_LINK).first();
        dashboardLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dashboardLink.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully navigated to Allocation Dashboard.");
    }

    /**
     * Clicks the actual switch component to change from My View to Team View.
     */
    public void switchToTeamView() {
        log.info("Switching to Team View...");

        // Target the switch component directly rather than the text span
        Locator teamViewSwitch = getPage().locator(TEAM_VIEW_SWITCH).first();
        teamViewSwitch.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        teamViewSwitch.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Switched to Team View.");
    }

    /**
     * Searches for an application using the specific "Search application by" dropdown.
     */
    public void searchByAppId(String appFormId) {
        log.info("Searching Allocation Dashboard by App ID: {}", appFormId);

        // 1. Open the 'Search by' dropdown using its exact placeholder
        getPage().getByPlaceholder("Search application by").click();

        // 2. Select "App ID" from the Element UI dropdown list
        Locator appIdOption = getPage().locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText("App ID"))
                .first();
        appIdOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        appIdOption.click();

        // 3. Fill the new input that appears and press Enter
        Locator searchInput = getPage().getByPlaceholder("Enter App ID");
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill(appFormId);
        getPage().keyboard().press("Enter");

        // Wait for the table filter to complete
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Allocation Dashboard search executed for App ID: {}", appFormId);
    }

    /**
     * Selects the checkbox next to the first result row.
     */
    public void selectFirstRowCheckbox() {
        log.info("Selecting the checkbox for the first result row...");
        // In Element UI, clicking the label wrapper is usually more reliable than the hidden input
        Locator checkbox = getPage().locator(FIRST_ROW_CHECKBOX).first();
        checkbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        checkbox.click(new Locator.ClickOptions().setForce(true)); // Force click to bypass hidden CSS rules
        log.info("Checkbox selected.");
    }

    /**
     * Clicks the "Allocate" action button.
     */
    public void clickAllocate() {
        log.info("Clicking Allocate button...");
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allocate")).click();
        log.info("Allocate button clicked.");
    }

    /**
     * Clicks "Proceed" on the Alert popup.
     */
    public void clickProceedOnAlert() {
        log.info("Clicking Proceed on the allocation alert popup...");
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Proceed")).click();
        log.info("Clicked Proceed on alert.");
    }

    /**
     * Completes the allocation assignment popup flow.
     */
//    public void assignAndFinish(String email) {
//        log.info("Assigning allocation to: {}", email);
//
//        // 1. Target the input inside the active dialog
//        Locator emailInput = getPage().locator("[role='dialog'] input[type='text'], [role='dialog'] input[type='email']").first();
//        emailInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//        emailInput.click();
//        emailInput.fill(email);
//
//        // 2. Select the first suggestion from the dropdown (Element UI list)
//        Locator emailOption = getPage().locator("li.el-select-dropdown__item").first();
//        emailOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//        emailOption.click();
//
//        // 3. Click Assign
//        getPage().locator("[role='dialog']").getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Assign")).click();
//        log.info("Clicked Assign button.");
//
//        // 4. Click Finish
//        getPage().locator("[role='dialog']").getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Finish")).click();
//        log.info("Clicked Finish button. Allocation complete.");
//
//        getPage().waitForLoadState(LoadState.NETWORKIDLE);
//    }

    /**
     * Re-opens the application form by clicking the ID in the first row.
     */
    public void openAppFormFromRow() {
        log.info("Re-opening appform from the row link...");
        Locator rowLink = getPage().locator(FIRST_ROW_APP_ID_LINK).first();
        rowLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        rowLink.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Appform opened from table row.");
    }
}