package ui.pages.jarvis.AppFormPage.RegCheckTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class RegCheck {

    private final Page page;

    public RegCheck(Page page) {
        this.page = page;
    }

    public void selectRegCheckTab() {
        System.out.println("Navigating to RegCheck tab...");

        page.locator("//a[@class='tab-item']//p[@class='tab-item-ubl-title']",
                        new Page.LocatorOptions().setHasText("Reg. Check"))
                .click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        System.out.println("Validate Reg. Check is resolved...");

        page.locator("//a[@class='tab-item']//span[@class='tab-item-spacing el-tag el-tag--info el-tag--small el-tag--light'][normalize-space()='Resolved']")
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        
        System.out.println("Reg. Check is resolved...");
    }
}