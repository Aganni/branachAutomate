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

        log.info("Refreshing the CAM page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);

        log.info("Checking for 'Start CAM' button...");
        Locator startCamBtn = page.locator("button.download-cam:visible").first();

        // --- THE DOUBLE REFRESH LOGIC ---
        try {
            // Wait up to 5 seconds for the button to appear after the first refresh
            startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        } catch (Exception e) {
            log.warn("Start CAM button not visible yet. Backend is likely still processing. Waiting 3 seconds and refreshing again...");
            page.waitForTimeout(3000); // Give the backend time to finish generating the CAM
            page.reload(new Page.ReloadOptions().setTimeout(60000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);

            // Try one more time, this time failing the test if it still isn't there
            startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        }

        log.info("Clicking on 'Start CAM' and waiting for the new tab to open...");
        Page newCamTab = page.waitForPopup(() -> {
            startCamBtn.click(new Locator.ClickOptions().setForce(true));
        });

        // Switch focus to the new tab
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully switched to new CAM tab: " + newCamTab.url());

        // Click 'Go to Appform' inside the NEW tab
        log.info("Clicking 'Go to Appform' in the new tab...");
        Locator goToAppFormBtn = newCamTab.locator("span.back-to-application:visible").first();
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