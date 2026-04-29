package ui.pages.jarvis.AppFormPage.RegCheckTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class RegCheck extends BaseTest {

    private final Page page;

    public RegCheck(Page page) {
        this.page = page;
    }

    public void selectRegCheckTabAndValidate() {
        log.info("Navigating to Reg. Check tab...");

        // 1. Click the Reg. Check Tab
        Locator regCheckTab = page.locator("a.tab-item").filter(new Locator.FilterOptions().setHasText("Reg. Check")).first();
        regCheckTab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        regCheckTab.click(new Locator.ClickOptions().setForce(true));

        // 2. Wait for URL to route to Reg. Check
        log.info("Waiting for URL to route to /regulatoryCheck...");
        page.waitForURL("**/regulatoryCheck*");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // 3. Refresh to ensure latest status is fetched from backend
        log.info("Refreshing the Reg. Check page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        // 4. Validate it is Resolved
        log.info("Validating Reg. Check is resolved...");

        Locator resolvedTag = page.locator("a.tab-item")
                .filter(new Locator.FilterOptions().setHasText("Reg. Check"))
                .locator("span")
                .filter(new Locator.FilterOptions().setHasText("Resolved"))
                .first();

        resolvedTag.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("Reg. Check is resolved.");
    }
}