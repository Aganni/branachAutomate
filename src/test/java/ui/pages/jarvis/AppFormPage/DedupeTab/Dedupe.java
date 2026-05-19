package ui.pages.jarvis.AppFormPage.DedupeTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.pages.jarvis.AppFormPage.AppformTab.AppFormTabNavigator;

public class Dedupe extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String DEDUPE_TAB_LOCATOR = "a.tab-item";
    private static final String DEDUPE_TEXT = "Dedupe";
    private static final String RESOLVED_TEXT = "Resolved";

    public Dedupe(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void selectDedupeTab() {
        log.info("Navigating to Dedupe tab...");

        Locator dedupeTab = page.locator(DEDUPE_TAB_LOCATOR).filter(new Locator.FilterOptions().setHasText(DEDUPE_TEXT)).first();
        dedupeTab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dedupeTab.click(new Locator.ClickOptions().setForce(true));

        log.info("Waiting for URL to route to /dedupe...");
        page.waitForURL("**/dedupe*");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(3000);

        log.info("Refreshing the Dedupe page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); 

        log.info("Validating Dedupe is resolved...");

        Locator resolvedTag = page.locator(DEDUPE_TAB_LOCATOR)
                .filter(new Locator.FilterOptions().setHasText(DEDUPE_TEXT))
                .locator("span")
                .filter(new Locator.FilterOptions().setHasText(RESOLVED_TEXT))
                .first();

        resolvedTag.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("Dedupe is resolved.");
    }

    public void navigateToAppFormTab() {
        AppFormTabNavigator.ensureOnAppFormTab(page);
    }
}