package ui.pages.jarvis.tabs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class CamTab {

    private final Page page;

    public CamTab(Page page) {
        this.page = page;
    }

    public void selectCamTab() throws InterruptedException {
        System.out.println("Navigating to CAM tab...");

        // Fixed syntax: Removed the redundant Options object since the text is already in the XPath
        page.locator("//p[@class='tab-item-title'][normalize-space()='CAM']")
                .click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        System.out.println("Click on Start CAM");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("View CAM"))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        System.out.println("CAM is in progress...");
        Thread.sleep(2000);
    }
}