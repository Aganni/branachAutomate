package ui.pages.jarvis.AppFormPage.DedupeTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class Dedupe extends BaseTest {

    private final Page page;

    public Dedupe(Page page) {
        this.page = page;
    }

    public void selectDedupeTab() {
        System.out.println("Navigating to Dedupe tab...");

        page.locator("//a[@class='tab-item tab-item-ubl']//p[@class='tab-item-ubl-title']",
                        new Page.LocatorOptions().setHasText("Dedupe"))
                .click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        System.out.println("Validate Dedupe is resolved...");

        page.locator("//a[contains(@class, 'nuxt-link-active')]//span[normalize-space()='Resolved']")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        System.out.println("Dedupe is resolved...");
    }

    /**
     * Navigates back to the main App Form tab.
     */
    public void navigateToAppFormTab() {
        log.info("Navigating back to App Form tab...");

        // Find the tab by its specific title class and text
        Locator appFormTab = page.locator("//p[@class='tab-item-title' and normalize-space()='App Form']").first();

        appFormTab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        appFormTab.click(new Locator.ClickOptions().setForce(true));

        // Wait for the Application Details page to fully render
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully switched to the App Form tab.");
    }
}