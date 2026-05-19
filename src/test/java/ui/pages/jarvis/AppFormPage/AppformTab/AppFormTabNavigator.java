package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AppFormTabNavigator {

    private static final Logger log = LogManager.getLogger(AppFormTabNavigator.class);

    /** URL pattern that identifies the AppForm tab. */
    private static final String APPFORM_URL_PATTERN = "**/appForm**";

    /** URL segment used for a fast in-memory check before issuing a click. */
    private static final String APPFORM_URL_SEGMENT = "/appForm";

    private static final double TAB_VISIBLE_TIMEOUT_MS = 8_000;

    public static void ensureOnAppFormTab(Page page) {
        String currentUrl = page.url();

        if (currentUrl.contains(APPFORM_URL_SEGMENT)) {
            log.debug("Already on AppForm tab (URL: {}). Skipping navigation.", currentUrl);
            return;
        }

        log.info("Not on AppForm tab (current URL: {}). Navigating to AppForm tab...", currentUrl);

        Locator appFormTab = page.locator("a.tab-item")
                .filter(new Locator.FilterOptions().setHasText("App Form"))
                .first();

        appFormTab.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(TAB_VISIBLE_TIMEOUT_MS));

        appFormTab.click(new Locator.ClickOptions().setForce(true));

        page.waitForURL(APPFORM_URL_PATTERN);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        log.info("Successfully navigated to AppForm tab.");
    }
}
