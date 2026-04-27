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

        // 1. Click the actual <a> tag for the CAM tab
        Locator camTabLink = page.locator("a.tab-item").filter(new Locator.FilterOptions().setHasText("CAM")).first();
        camTabLink.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        camTabLink.click(new Locator.ClickOptions().setForce(true));
        
        log.info("Waiting for URL to route to /cam...");
        page.waitForURL("**/cam*");

        page.waitForLoadState(LoadState.NETWORKIDLE);

        // 3. Now it is 100% safe to refresh the page
        log.info("Refreshing the CAM page to ensure JavaScript event listeners are fully attached...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        log.info("Clicking on 'Start CAM' and waiting for the new tab to open...");

        Locator startCamBtn = page.locator("button.download-cam:visible").first();
        startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 4. Catch the new tab
        Page newCamTab = page.waitForPopup(() -> {
            startCamBtn.click(new Locator.ClickOptions().setForce(true));
        });

        // 5. Switch focus to the new tab
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully switched to new CAM tab: " + newCamTab.url());

        // 6. Click 'Go to Appform' inside the NEW tab
        log.info("Clicking 'Go to Appform' in the new tab...");
        Locator goToAppFormBtn = newCamTab.locator("span.back-to-application:visible").first();
        goToAppFormBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        goToAppFormBtn.click(new Locator.ClickOptions().setForce(true));

        // 7. Handle Tab Closing
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        newCamTab.waitForTimeout(1000);

        if (!newCamTab.isClosed()) {
            newCamTab.close();
            log.info("Closed the CAM tab manually.");
        }

        // 8. Bring focus back to the original Application tab
        page.bringToFront();
        log.info("Returned focus to the original Application Details tab.");
    }
}