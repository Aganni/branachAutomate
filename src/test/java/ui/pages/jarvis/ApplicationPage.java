package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import hooks.BaseTest;

public class ApplicationPage extends BaseTest {
    private final String APPLICATION_SIDEBAR_LINK = "a[href='/application']";
    private final String SEARCH_TYPE_DROPDOWN = ".search-by-filter .el-input__inner";
    private final String SEARCH_INPUT_NAME = "Enter App ID)"; // Generic placeholder
    private final String FIRST_ROW_APP_ID = "tr.appform-row .app-id p";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void navigateToApplicationTab() {
        log.info("Navigating to Application Tab in Jarvis sidebar");
        getPage().locator(APPLICATION_SIDEBAR_LINK).click();
        getPage().waitForLoadState();
    }

    public void searchByCriteria(String searchType, String searchValue) {
        log.info("Searching by [{}] with value: {}", searchType, searchValue);

        getPage().locator(SEARCH_TYPE_DROPDOWN).first().click();

        String dynamicOptionXpath = "//li[contains(@class,'el-select-dropdown__item')]//span[text()='" + searchType + "']";

        getPage().locator(dynamicOptionXpath).click();
        log.info("Selected [{}] from search criteria dropdown", searchType);

        getPage().getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(SEARCH_INPUT_NAME))
                .fill(searchValue);
        getPage().keyboard().press("Enter");

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void openFirstApplication() {
        log.info("Opening the first application in the list...");

        // Wait for the first row to be visible
        Locator firstRow = getPage().locator(FIRST_ROW_APP_ID).first();
        firstRow.waitFor();

        // Clicking the App ID usually opens the detail view/app form
        firstRow.click();
        log.info("Application form opened successfully.");
    }
}
