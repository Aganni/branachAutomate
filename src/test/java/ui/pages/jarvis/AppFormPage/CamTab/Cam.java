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

        // --- 1ST REFRESH ---
        log.info("Refreshing the CAM page (1st Attempt)...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);

        // Defined without ':visible' so Playwright dynamically evaluates the DOM state during the wait
        Locator startCamBtn = page.locator("button.download-cam").first();
        boolean isBtnVisible = false;

        try {
            log.info("Checking for 'Start CAM' button...");
            // Wait up to 5 seconds for the button to appear after the first refresh
            startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            isBtnVisible = true;
            log.info("Start CAM button found after 1st refresh!");
        } catch (Exception e) {
            log.warn("Start CAM button NOT found after 1st refresh. Backend is likely still calculating.");
        }

        // --- 2ND REFRESH (ONLY IF NEEDED) ---
        if (!isBtnVisible) {
            log.info("Waiting 5 seconds before triggering a 2nd refresh...");
            page.waitForTimeout(5000); // Give the backend more time to finish generating the CAM

            log.info("Refreshing the CAM page (2nd Attempt)...");
            page.reload(new Page.ReloadOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            try {
                log.info("Checking for 'Start CAM' button again...");
                // Try one more time, this time waiting up to 10 seconds, failing the test if it still isn't there
                startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
                log.info("Start CAM button found after 2nd refresh!");
            } catch (Exception e) {
                throw new AssertionError("Start CAM button failed to appear even after two refreshes and extended waiting.");
            }
        }

        // --- CLICK AND PROCEED ---
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
}