package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class ApplicationPage extends BaseTest {

    // Sidebar link to the Applications section
    private static final String APPLICATION_SIDEBAR_LINK = "a[href='/application']";

    // Search-by dropdown trigger (the filter pill)
    private static final String SEARCH_TYPE_DROPDOWN = ".search-by-filter .el-input__inner";

    // The option inside the dropdown (e.g. "Partner LID", "App ID")
    private static final String SEARCH_OPTION_XPATH = "//li[contains(@class,'el-select-dropdown__item')]//span[text()='%s']";

    // The actual text-input that appears after a search-type is chosen
    private static final String SEARCH_INPUT = ".search-by-filter input[type='text']:not([readonly])";

    // The APP ID cell in the results table (visible text link / p tag)
    private static final String FIRST_ROW_APP_ID_LINK = "table tbody tr:first-child td .app-id p, " +
            "table tbody tr:first-child [class*='app-id'], " +
            "table tbody tr:first-child td:nth-child(2) p";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void navigateToApplicationTab() {
        log.info("Navigating to Application tab in Jarvis sidebar");
        getPage().locator(APPLICATION_SIDEBAR_LINK).click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Application tab loaded");
    }

    /**
     * Selects a search type from the filter dropdown and types the search value.
     * 
     * @param searchType  e.g. "Partner LID"
     * @param searchValue the actual ID/value to search for
     */
    public void searchByCriteria(String searchType, String searchValue) {
        log.info("Searching by [{}] with value: {}", searchType, searchValue);

        // 1. Open the filter-type dropdown
        Locator dropdownTrigger = getPage().locator(".search-by-filter .el-input__inner").first();
        dropdownTrigger.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dropdownTrigger.click();

        // 2. Select the right option
        String optionXpath = "//li[contains(@class,'el-select-dropdown__item')]//span[text()='" + searchType + "']";
        getPage().locator(optionXpath).click();
        log.info("Selected [{}] from search criteria dropdown", searchType);

        // 3. Type in the search box
        Locator searchInput = getPage().locator(".search-by-filter input[type='text']:not([readonly])").last();
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill(searchValue);

        log.info("Triggering search. Waiting for table to refresh...");
        getPage().keyboard().press("Enter");

        // --- THE FIX: Handle the Race Condition ---

        // A. Wait for the Element UI loading mask to disappear (if it appears)
        Locator loadingMask = getPage().locator(".el-loading-mask").first();
        try {
            loadingMask.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10000));
        } catch (Exception e) {
            log.info("No loading mask detected or it disappeared instantly.");
        }

        // B. Wait for the network API calls to finish fetching the filtered data
        getPage().waitForLoadState(LoadState.NETWORKIDLE);

        // C. The Pragmatic Wait: Give the frontend framework (Vue.js) a brief moment to redraw the HTML table
        getPage().waitForTimeout(1500);

        log.info("Search results loaded and table updated.");
    }

    /**
     * Reads the App ID text from the first result row and stores it.
     * 
     * @return the App ID string (e.g. "e878034a-bc51-4d01-af6c-e6af61d5dcdc")
     */
    public String getAppFormIdFromFirstRow() {
        log.info("Extracting App ID from the first row...");

        // Based on your HTML: <div class="app-id"><p class="app-id-ellipse">...
        Locator appIdLocator = getPage().locator("table tbody tr:first-child .app-id p.app-id-ellipse").first();

        appIdLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Grab the text directly without looping through all columns
        String appId = appIdLocator.innerText().trim();

        if (appId.isEmpty()) {
            throw new RuntimeException("App ID was found but the text was empty!");
        }

        log.info("Captured App ID from first row: {}", appId);
        return appId;
    }

    /**
     * Clicks the first appform row to open its detail view.
     */
    public void openFirstApplication() throws InterruptedException {
        log.info("Clicking to open the first application in the list...");
        Locator firstRow = getPage().locator(FIRST_ROW_APP_ID_LINK).first();
        firstRow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRow.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(3000);// Extra wait to ensure the detail page is fully loaded
        log.info("Application form opened successfully.");
    }
}
