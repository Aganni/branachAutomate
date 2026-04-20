package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class AppFormActionsPage extends BaseTest {

    // ── "Application Actions" dropdown in the appform header ─────────
    // From screenshot: top-right dropdown button labelled "Application Actions"
    private static final String APP_ACTIONS_DROPDOWN = "button:has-text('Application Actions'), [class*='action'] button:has-text('Actions')";

    // ── "Move to CM" option inside the dropdown ───────────────────────
    private static final String MOVE_TO_CM_OPTION = "li:has-text('Move to CM'), [role='menuitem']:has-text('Move to CM'), "
            +
            "//li[contains(text(),'Move to CM')]";

    // ── Optional confirmation popup (if any) ─────────────────────────
    private static final String CONFIRM_BUTTON = "[role='dialog'] button:has-text('Confirm'), [role='dialog'] button:has-text('Yes'), "
            +
            ".el-dialog button:has-text('Confirm')";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Clicks the "Application Actions" dropdown in the appform header
     * and selects "Move to CM".
     */
    public void moveToCAM() {
        log.info("Clicking 'Application Actions' dropdown...");

        Locator dropdown = getPage().locator(APP_ACTIONS_DROPDOWN).first();
        dropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dropdown.click();
        log.info("Application Actions dropdown opened.");

        // Select "Move to CM"
        Locator moveToCm = getPage().locator(MOVE_TO_CM_OPTION).first();
        moveToCm.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        moveToCm.click();
        log.info("Clicked 'Move to CM' from Application Actions dropdown.");

        // Handle any confirmation popup that may appear
        Locator confirmBtn = getPage().locator(CONFIRM_BUTTON);
        if (confirmBtn.count() > 0) {
            confirmBtn.first().click();
            log.info("Confirmation popup dismissed.");
        }

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Move to CM action completed.");
    }
}
