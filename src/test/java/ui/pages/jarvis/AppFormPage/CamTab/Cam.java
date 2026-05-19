package ui.pages.jarvis.AppFormPage.CamTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class Cam extends BaseTest {

    private final Page page;

    // ── Locator constants ─────────────────────────────────────────────────────
    private static final String LOC_CAM_TAB         = "a.tab-item";
    private static final String LOC_START_CAM_BTN   = "button.download-cam";
    private static final String LOC_ENABLED_CAM_BTN = "button.download-cam:not(.is-disabled)";
    private static final String LOC_GO_TO_APPFORM   = "span.back-to-application";

    public Cam(Page page) {
        this.page = page;
    }

    /**
     * Navigates to the CAM tab, waits for the Start CAM button to become enabled
     * (retrying up to 2 page refreshes), clicks it, handles the new tab, and returns.
     */
    public void selectCamTab() {
        log.info("Navigating to CAM tab...");
        Locator camTabLink = page.locator(LOC_CAM_TAB)
                .filter(new Locator.FilterOptions().setHasText("CAM"))
                .first();
        camTabLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        camTabLink.click(new Locator.ClickOptions().setForce(true));

        page.waitForURL("**/cam*");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(3000);

        // Button renders immediately but stays disabled while backend calculates CAM data.
        // Poll for the enabled state; refresh up to twice if still disabled.
        boolean isEnabled = waitForStartCamEnabled(3000);

        if (!isEnabled) {
            log.info("Start CAM button disabled after initial load — triggering 1st refresh...");
            page.reload(new Page.ReloadOptions().setTimeout(60_000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);
            isEnabled = waitForStartCamEnabled(5000);
            if (isEnabled) log.info("Start CAM button enabled after 1st refresh.");
            else           log.warn("Still disabled after 1st refresh — trying 2nd...");
        }

        if (!isEnabled) {
            page.waitForTimeout(5000); // extra backend processing time before 2nd refresh
            page.reload(new Page.ReloadOptions().setTimeout(60_000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);
            isEnabled = waitForStartCamEnabled(10_000);
            if (!isEnabled) throw new AssertionError(
                    "Start CAM button is still DISABLED after two refreshes — backend may not have finished.");
            log.info("Start CAM button enabled after 2nd refresh.");
        }

        // Click the enabled button and handle the popup tab it opens
        Locator startCamBtn = page.locator(LOC_ENABLED_CAM_BTN).first();
        log.info("Clicking 'Start CAM'...");
        Page newCamTab = page.waitForPopup(() ->
                startCamBtn.click(new Locator.ClickOptions().setForce(true)));

        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Switched to CAM tab: {}", newCamTab.url());

        // Click 'Go to Appform' in the popup tab to return context to the main application
        Locator goToAppFormBtn = newCamTab.locator(LOC_GO_TO_APPFORM).first();
        goToAppFormBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        goToAppFormBtn.click(new Locator.ClickOptions().setForce(true));

        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        newCamTab.waitForTimeout(1000);
        if (!newCamTab.isClosed()) {
            newCamTab.close();
            log.info("Closed CAM popup tab.");
        }

        page.bringToFront();
        log.info("Returned focus to the main Application Details tab.");
    }

    /**
     * Waits up to {@code timeoutMs} for the Start CAM button to be enabled (no {@code is-disabled} class).
     *
     * @return {@code true} if button became enabled within the timeout
     */
    private boolean waitForStartCamEnabled(double timeoutMs) {
        try {
            log.info("Waiting {}ms for Start CAM to become enabled...", (int) timeoutMs);
            page.locator(LOC_ENABLED_CAM_BTN).first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            log.warn("Start CAM still disabled after {}ms.", (int) timeoutMs);
            return false;
        }
    }
}