package ui.pages.jarvis.AppFormPage.RegCheckTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class RegCheck extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String REG_CHECK_TAB_LOCATOR = "a.tab-item";
    private static final String REG_CHECK_TEXT = "Reg. Check";
    private static final String RESOLVED_TEXT = "Resolved";

    public RegCheck(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void selectRegCheckTabAndValidate() {
        log.info("Navigating to Reg. Check tab...");

        Locator regCheckTab = page.locator(REG_CHECK_TAB_LOCATOR).filter(new Locator.FilterOptions().setHasText(REG_CHECK_TEXT)).first();
        regCheckTab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        regCheckTab.click(new Locator.ClickOptions().setForce(true));

        log.info("Waiting for URL to route to /regulatoryCheck...");
        page.waitForURL("**/regulatoryCheck*");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Refreshing the Reg. Check page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        log.info("Validating Reg. Check is resolved...");

        Locator resolvedTag = page.locator(REG_CHECK_TAB_LOCATOR)
                .filter(new Locator.FilterOptions().setHasText(REG_CHECK_TEXT))
                .locator("span")
                .filter(new Locator.FilterOptions().setHasText(RESOLVED_TEXT))
                .first();

        resolvedTag.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("Reg. Check is resolved.");
    }
}