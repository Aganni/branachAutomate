package ui.pages.jarvis.AppFormPage.CamTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class Cam {

    private final Page page;

    public Cam(Page page) {
        this.page = page;
    }

    public void selectCamTab() {
        System.out.println("Navigating to CAM tab...");

        // 1. Click on the CAM tab
        page.locator("//p[@class='tab-item-title'][normalize-space()='CAM']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        System.out.println("Clicking on 'Start CAM' and waiting for the new tab to open...");

        // FIX: Use the exact CSS class from the HTML to bypass whitespace/comment issues
        Locator startCamBtn = page.locator("button.download-cam").first();

        // Ensure it is visible before we try to click it
        startCamBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 2. Catch the new tab using waitForPopup()
        Page newCamTab = page.waitForPopup(() -> {
            // Force click guarantees Playwright clicks it even if Element UI tries to animate it
            startCamBtn.click(new Locator.ClickOptions().setForce(true));
        });

        // 3. Switch focus to the new tab and wait for it to fully load
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        System.out.println("Successfully switched to new CAM tab: " + newCamTab.url());

        // 4. Click 'Go to Appform' inside the NEW tab
        System.out.println("Clicking 'Go to Appform' in the new tab...");
        Locator goToAppFormBtn = newCamTab.locator("span.back-to-application");
        goToAppFormBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        goToAppFormBtn.click();

        // 5. Handle Tab Closing
        newCamTab.waitForLoadState(LoadState.NETWORKIDLE);
        newCamTab.waitForTimeout(1000);

        if (!newCamTab.isClosed()) {
            newCamTab.close();
            System.out.println("Closed the CAM tab manually.");
        }

        // 6. Bring focus back to the original Application tab
        page.bringToFront();
        System.out.println("Returned focus to the original Application Details tab.");
    }
}