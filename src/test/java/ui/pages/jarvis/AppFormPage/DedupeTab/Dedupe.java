package ui.pages.jarvis.AppFormPage.DedupeTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class Dedupe {

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
}