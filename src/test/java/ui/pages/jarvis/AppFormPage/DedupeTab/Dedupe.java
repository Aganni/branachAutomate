package ui.pages.jarvis.AppFormPage.DedupeTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.pages.jarvis.AppFormPage.AppformTab.AppFormTabNavigator;

public class Dedupe extends BaseTest {

    private final Page page;

    public Dedupe(Page page) {
        this.page = page;
    }

    public void selectDedupeTab() {
        log.info("Navigating to Dedupe tab...");

        // 1. Click the Dedupe Tab
        Locator dedupeTab = page.locator("a.tab-item").filter(new Locator.FilterOptions().setHasText("Dedupe")).first();
        dedupeTab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dedupeTab.click(new Locator.ClickOptions().setForce(true));

        // 2. Wait for URL to route to Dedupe
        log.info("Waiting for URL to route to /dedupe...");
        page.waitForURL("**/dedupe*");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(3000);

        // 3. Refresh to ensure latest status is fetched from backend
        log.info("Refreshing the Dedupe page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); // Give DOM a second to re-attach

        // 4. Validate it is Resolved
        log.info("Validating Dedupe is resolved...");

        // Strict locator: Find the Dedupe tab again, then look for the "Resolved" span inside it
        Locator resolvedTag = page.locator("a.tab-item")
                .filter(new Locator.FilterOptions().setHasText("Dedupe"))
                .locator("span")
                .filter(new Locator.FilterOptions().setHasText("Resolved"))
                .first();

        resolvedTag.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("Dedupe is resolved.");
    }

    /**
     * Navigates to the App Form tab if not already there.
     * Delegates to {@link AppFormTabNavigator#ensureOnAppFormTab(Page)} so that
     * the URL-check logic lives in exactly one place.
     */
    public void navigateToAppFormTab() {
        AppFormTabNavigator.ensureOnAppFormTab(page);
    }
}