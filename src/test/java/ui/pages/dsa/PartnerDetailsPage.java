package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hooks.BaseTest;

import java.util.Map;

public class PartnerDetailsPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String BRANCH_INPUT_ID = "#branch";
    private static final String SALES_MANAGER_INPUT_ID = "#SalesManager";
    private static final String SCHEME_SELECT_ID = "#scheme";
    private static final String SAVE_AND_NEXT_BTN = "button:has-text('SAVE AND NEXT')";

    public PartnerDetailsPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    private void fillAutocomplete(String locator, String value) {
        log.info("Filling autocomplete '{}' with value: {}", locator, value);
        Locator input = page.locator(locator);
        input.click();
        input.clear();
        input.fill(value);

        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true)).click();
    }

    private void selectDropdown(String locator, String value) {
        log.info("Selecting dropdown '{}' with value: {}", locator, value);
        page.locator(locator).click(); 
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true)).click();
    }

    public void fillMandatoryDetails(Map<String, String> details) {
        if (details.containsKey("Branch")) {
            fillAutocomplete(BRANCH_INPUT_ID, details.get("Branch"));
        }
        if (details.containsKey("Sales Manager")) {
            fillAutocomplete(SALES_MANAGER_INPUT_ID, details.get("Sales Manager"));
        }
        if (details.containsKey("Scheme")) {
            selectDropdown(SCHEME_SELECT_ID, details.get("Scheme"));
        }
    }

    public void clickSaveAndNext() {
        page.locator(SAVE_AND_NEXT_BTN).click();
        log.info("Clicked SAVE AND NEXT button");
    }
}
