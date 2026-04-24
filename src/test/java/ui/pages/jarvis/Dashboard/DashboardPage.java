package ui.pages.jarvis.Dashboard;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class DashboardPage extends BaseTest {

    // Sidebar link to the Applications section
    private static final String APPLICATION_SIDEBAR_LINK = "a[href='/application']";
    private static final String ALLOCATION_DASHBOARD_LINK = "a[href='/new/dashboard']";


    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void navigateToApplicationTab() {
        log.info("Navigating to Application tab in Jarvis sidebar");
        getPage().locator(APPLICATION_SIDEBAR_LINK).click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Application tab loaded");
    }

    public void navigateToAllocationDashboard() {
        log.info("Navigating to Allocation Dashboard via sidebar...");
        Locator dashboardLink = getPage().locator(ALLOCATION_DASHBOARD_LINK).first();
        dashboardLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dashboardLink.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

}
