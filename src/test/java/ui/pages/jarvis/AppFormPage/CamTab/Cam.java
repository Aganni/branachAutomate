package ui.pages.jarvis.AppFormPage.CamTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class Cam extends BaseTest {

    private final Page page;

    public Cam(Page page) {
        this.page = page;
    }

    public void selectCamTab() {
        log.info("Navigating to CAM tab...");

        Locator camTabLink = page.locator("a.tab-item").filter(new Locator.FilterOptions().setHasText("CAM")).first();
        camTabLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        camTabLink.click(new Locator.ClickOptions().setForce(true));

        log.info("Waiting for URL to route to /cam...");
        page.waitForURL("**/cam*");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(3000);

        // The Start CAM button renders immediately but stays DISABLED (grey) while the backend
        // calculates CAM data. We must wait for :not(.is-disabled) — the ENABLED (blue) state.
        // A plain :visible check is insufficient because the disabled button is also visible.
        boolean isBtnEnabled = waitForStartCamEnabled(3000);

        // --- 1ST REFRESH (if button still disabled) ---
        if (!isBtnEnabled) {
            log.info("Start CAM button is disabled after page load. Triggering 1st refresh...");
            page.reload(new Page.ReloadOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            isBtnEnabled = waitForStartCamEnabled(5000);
            if (isBtnEnabled) {
                log.info("Start CAM button is ENABLED after 1st refresh!");
            } else {
                log.warn("Start CAM button still disabled after 1st refresh. Trying 2nd refresh...");
            }
        }

        // --- 2ND REFRESH (if still disabled) ---
        if (!isBtnEnabled) {
            page.waitForTimeout(5000); // give backend more time before 2nd refresh
            log.info("Triggering 2nd refresh...");
            page.reload(new Page.ReloadOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            isBtnEnabled = waitForStartCamEnabled(10000);
            if (!isBtnEnabled) {
                throw new AssertionError(
                        "Start CAM button is still DISABLED after two refreshes. " +
                        "Backend may not have finished calculating CAM data.");
            }
            log.info("Start CAM button is ENABLED after 2nd refresh!");
        }

        // --- CLICK ENABLED BUTTON ---
        Locator startCamBtn = page.locator("button.download-cam:not(.is-disabled)").first();
        log.info("Clicking on 'Start CAM' and waiting for the new tab to open...");
        Page newCamTab = page.waitForPopup(() -> {
            startCamBtn.click(new Locator.ClickOptions().setForce(true));
        });

        // Switch focus to the new tab
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully switched to new CAM tab: " + newCamTab.url());

        // Click 'Go to Appform' inside the NEW tab
        log.info("Clicking 'Go to Appform' in the new tab...");
        Locator goToAppFormBtn = newCamTab.locator("span.back-to-application").first();
        goToAppFormBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        goToAppFormBtn.click(new Locator.ClickOptions().setForce(true));

        // Handle Tab Closing
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        newCamTab.waitForTimeout(1000);

        if (!newCamTab.isClosed()) {
            newCamTab.close();
            log.info("Closed the CAM tab manually.");
        }

        page.bringToFront();
        log.info("Returned focus to the original Application Details tab.");
    }

    /**
     * Waits up to {@code timeoutMs} for the Start CAM button to be in an ENABLED (not disabled) state.
     *
     * <p>The button is always present in the DOM and visible, but carries {@code is-disabled}
     * while the backend is calculating. We poll for {@code :not(.is-disabled)} to confirm
     * the backend has finished and the button is safe to click.</p>
     *
     * @param timeoutMs max wait in milliseconds
     * @return {@code true} if button became enabled within the timeout, {@code false} otherwise
     */
    private boolean waitForStartCamEnabled(double timeoutMs) {
        try {
            log.info("Waiting up to {}ms for Start CAM button to become enabled...", (int) timeoutMs);
            page.locator("button.download-cam:not(.is-disabled)")
                    .first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(timeoutMs));
            log.info("Start CAM button is ENABLED (not disabled).");
            return true;
        } catch (Exception e) {
            log.warn("Start CAM button is still DISABLED after {}ms wait.", (int) timeoutMs);
            return false;
        }
    }