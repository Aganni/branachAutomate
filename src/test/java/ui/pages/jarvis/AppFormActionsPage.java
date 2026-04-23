package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class AppFormActionsPage extends BaseTest {

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void selectApplicationAction(String actionName) {
        log.info("Selecting Application Action: [{}]", actionName);

        // 1. Target the Application Actions dropdown by its placeholder
        Locator actionsDropdown = getPage().getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Force click is used here to bypass any Element UI hidden overlays
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));
        log.info("Application Actions dropdown opened.");

        // 2. Dynamically locate the specific action from the floating list
        // We use li.el-select-dropdown__item and filter by the exact text passed from the feature file
        Locator actionOption = getPage().locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();

        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click();
        log.info("Successfully clicked action: [{}]", actionName);

        // 3. Wait for the page to react (usually opens a modal or triggers a network call)
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }
}
