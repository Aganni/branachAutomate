package ui.pages.jarvis.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class ApplicationDashboardPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String APPLICATION_SIDEBAR_LINK = "a[href='/application']";
    private static final String SEARCH_TYPE_DROPDOWN = ".search-by-filter .el-input__inner";
    private static final String SEARCH_INPUT = ".search-by-filter input[type='text']:not([readonly])";
    private static final String FIRST_ROW_APP_ID_LINK = "table tbody tr:first-child td .app-id p, " +
            "table tbody tr:first-child [class*='app-id'], " +
            "table tbody tr:first-child td:nth-child(2) p";

    public ApplicationDashboardPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void searchByCriteria(String searchType, String searchValue) {
        log.info("Searching by [{}] with value: {}", searchType, searchValue);

        Locator dropdownTrigger = page.locator(SEARCH_TYPE_DROPDOWN).first();
        dropdownTrigger.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dropdownTrigger.click();

        String optionXpath = "//li[contains(@class,'el-select-dropdown__item')]//span[text()='" + searchType + "']";
        page.locator(optionXpath).click();
        log.info("Selected [{}] from search criteria dropdown", searchType);

        Locator searchInput = page.locator(SEARCH_INPUT).last();
        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchInput.fill(searchValue);

        log.info("Triggering search. Waiting for table to refresh...");
        page.keyboard().press("Enter");

        Locator loadingMask = page.locator(".el-loading-mask").first();
        try {
            loadingMask.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10000));
        } catch (Exception e) {
            log.info("No loading mask detected or it disappeared instantly.");
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1500);

        log.info("Search results loaded and table updated.");
    }

    public String getAppFormIdFromFirstRow() {
        log.info("Extracting App ID from the first row...");

        Locator appIdLocator = page.locator("table tbody tr:first-child .app-id p.app-id-ellipse").first();
        appIdLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String appId = appIdLocator.innerText().trim();

        if (appId.isEmpty()) {
            throw new RuntimeException("App ID was found but the text was empty!");
        }

        log.info("Captured App ID from first row: {}", appId);
        return appId;
    }

    public void openFirstApplication() throws InterruptedException {
        log.info("Clicking to open the first application in the list...");
        Locator firstRow = page.locator(FIRST_ROW_APP_ID_LINK).first();
        firstRow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRow.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(3000);
        log.info("Application form opened successfully.");
    }
}
