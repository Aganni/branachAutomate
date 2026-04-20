package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class AllocationDashboardPage extends BaseTest {

    // ── Sidebar navigation ──────────────────────────────────────────
    // Using href pattern matching the project convention (same as
    // a[href='/application'])
    // Multiple fallbacks in case the exact href path differs per deployment
    private static final String ALLOCATION_DASHBOARD_LINK = "a[href='/allocation-dashboard'], " +
            "a[href*='allocation'], " +
            "//a[contains(normalize-space(),'Allocation Dashboard')], " +
            "//li //a[contains(.,'Allocation Dashboard')]";

    // ── Team View toggle ─────────────────────────────────────────────
    // From screenshot: top-right "Team View" toggle/button
    private static final String TEAM_VIEW_TOGGLE = "button:has-text('Team View'), label:has-text('Team View')";

    // ── Search area ──────────────────────────────────────────────────
    // The search-by dropdown trigger on the Allocation Dashboard
    private static final String SEARCH_DROPDOWN_TRIGGER = ".el-input__inner, [class*='search'] input";

    // The "App ID" option in the dropdown
    private static final String APP_ID_OPTION_XPATH = "//li[contains(@class,'el-select-dropdown__item')]//span[text()='App ID'] | "
            +
            "//div[contains(@class,'dropdown')]//span[text()='App ID'] | " +
            "//ul//li[text()='App ID']";

    // The search text-input box (after 'App ID' is selected)
    private static final String SEARCH_INPUT = "input[placeholder*='app'], input[placeholder*='App'], " +
            ".el-input__inner[type='text']:not([readonly])";

    // ── Results table ────────────────────────────────────────────────
    // Checkbox next to the first row in the results table
    private static final String FIRST_ROW_CHECKBOX = "table tbody tr:first-child input[type='checkbox'], " +
            "table tbody tr:first-child .el-checkbox__input";

    // "Allocate" action button that appears above the table after selecting a row
    private static final String ALLOCATE_BUTTON = "button:has-text('Allocate')";

    // ── Alert / Proceed popup ────────────────────────────────────────
    private static final String PROCEED_BUTTON = "button:has-text('Proceed')";

    // ── Allocation popup (assign user) ───────────────────────────────
    // Email input inside the allocation popup/dialog
    private static final String ALLOCATION_EMAIL_INPUT = "[role='dialog'] input[type='email'], [role='dialog'] input[type='text'], "
            +
            ".el-dialog input[type='text']";

    // The dropdown option that appears after typing the email
    private static final String EMAIL_DROPDOWN_OPTION_XPATH = "//li[contains(@class,'el-select-dropdown__item') or contains(@class,'option')] | "
            +
            "//*[contains(@class,'dropdown-item')]";

    // "Assign" button inside the allocation popup
    private static final String ASSIGN_BUTTON = "[role='dialog'] button:has-text('Assign'), .el-dialog button:has-text('Assign')";

    // "Finish" button inside the allocation popup
    private static final String FINISH_BUTTON = "[role='dialog'] button:has-text('Finish'), .el-dialog button:has-text('Finish')";

    // ── App ID row link (to re-open appform after allocation) ────────
    private static final String APP_ID_ROW_LINK = "table tbody tr:first-child td a, table tbody tr:first-child td [class*='app-id']";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Navigates to the Allocation Dashboard.
     * Uses direct URL navigation (most reliable when coming from the appform detail
     * page
     * where the sidebar may be collapsed/hidden).
     * Falls back to clicking the sidebar link if the URL-based navigation is not
     * available.
     */
    public void navigateToAllocationDashboard() {
        log.info("Navigating to Allocation Dashboard...");
        try {
            // Primary: use the jarvis base URL + allocation-dashboard path
            String jarvisUrl = BaseTest.initializeEnvironment("jarvisUrl");
            String allocationUrl = jarvisUrl + "/allocation-dashboard";
            getPage().navigate(allocationUrl);
            getPage().waitForLoadState(LoadState.NETWORKIDLE);
            log.info("Navigated to Allocation Dashboard via URL: {}", allocationUrl);
        } catch (Exception e) {
            // Fallback: try clicking sidebar link
            log.warn("URL navigation failed ({}), trying sidebar click...", e.getMessage());
            getPage().locator(ALLOCATION_DASHBOARD_LINK).first().click();
            getPage().waitForLoadState(LoadState.NETWORKIDLE);
            log.info("Navigated to Allocation Dashboard via sidebar link.");
        }
    }

    /**
     * Clicks the "Team View" toggle to switch from My View → Team View.
     * The toggle is in the top-right corner of the Allocation Dashboard.
     */
    public void switchToTeamView() {
        log.info("Switching to Team View...");

        // --- DEBUG: Print all labels, spans, and buttons so we know what is on the
        // screen ---
        getPage().waitForTimeout(5000); // Give the dashboard 5 seconds to load UI elements
        java.util.List<String> labels = getPage()
                .locator("label, button, .el-switch, .el-tabs__item, span[class*='text']").allInnerTexts();
        log.info("UI Elements found: {}", labels);
        // ---------------------------------------------------------------------------------

        Locator toggle = getPage().getByText("Team View").first();
        toggle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        // Force click in case it's a span wrapped by an invisible input/label structure
        toggle.click(new Locator.ClickOptions().setForce(true));
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Switched to Team View.");
    }

    /**
     * Selects "App ID" from the search dropdown, fills in the appFormId,
     * and presses Enter to trigger the search.
     */
    public void searchByAppId(String appFormId) {
        log.info("Searching Allocation Dashboard by App ID: {}", appFormId);

        // 1. Open the search-type dropdown
        Locator dropdownTrigger = getPage().locator(SEARCH_DROPDOWN_TRIGGER).first();
        dropdownTrigger.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dropdownTrigger.click();

        // 2. Select "App ID" from the dropdown options
        getPage().locator(APP_ID_OPTION_XPATH).first().click();
        log.info("Selected 'App ID' from search dropdown.");

        // 3. Fill the search input with the appFormId
        Locator searchInput = getPage().locator(SEARCH_INPUT).last();
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill(appFormId);
        getPage().keyboard().press("Enter");

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Allocation Dashboard search executed for App ID: {}", appFormId);
    }

    /**
     * Selects the checkbox next to the first result row.
     */
    public void selectFirstRowCheckbox() {
        log.info("Selecting the checkbox for the first result row...");
        Locator checkbox = getPage().locator(FIRST_ROW_CHECKBOX).first();
        checkbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        checkbox.click();
        log.info("Checkbox selected.");
    }

    /**
     * Clicks the "Allocate" button that appears after a row is selected.
     */
    public void clickAllocate() {
        log.info("Clicking Allocate button...");
        Locator allocateBtn = getPage().locator(ALLOCATE_BUTTON);
        allocateBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        allocateBtn.click();
        log.info("Allocate button clicked.");
    }

    /**
     * Clicks "Proceed" on the Alert popup:
     * "You have selected 1 assigned case. Do you want to proceed for allocation?"
     */
    public void clickProceedOnAlert() {
        log.info("Clicking Proceed on the allocation alert popup...");
        Locator proceedBtn = getPage().locator(PROCEED_BUTTON);
        proceedBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        proceedBtn.click();
        log.info("Clicked Proceed on alert.");
    }

    /**
     * In the Allocation popup:
     * 1. Types the email address in the input
     * 2. Selects the first dropdown suggestion
     * 3. Clicks Assign
     * 4. Clicks Finish
     */
    public void assignAndFinish(String email) {
        log.info("Assigning allocation to: {}", email);

        // 1. Fill the email input inside the dialog
        Locator emailInput = getPage().locator(ALLOCATION_EMAIL_INPUT).first();
        emailInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        emailInput.fill(email);

        // 2. Select the first suggestion from the dropdown
        Locator emailOption = getPage().locator(EMAIL_DROPDOWN_OPTION_XPATH).first();
        emailOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        emailOption.click();
        log.info("Email option selected from dropdown.");

        // 3. Click Assign
        Locator assignBtn = getPage().locator(ASSIGN_BUTTON);
        assignBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assignBtn.click();
        log.info("Clicked Assign button.");

        // 4. Click Finish
        Locator finishBtn = getPage().locator(FINISH_BUTTON);
        finishBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        finishBtn.click();
        log.info("Clicked Finish button. Allocation complete.");

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * After allocation, clicks the App ID row link again to re-open the appform.
     */
    public void openAppFormFromRow() {
        log.info("Re-opening appform from the row link...");
        Locator rowLink = getPage().locator(APP_ID_ROW_LINK).first();
        rowLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        rowLink.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Appform opened from table row.");
    }
}
