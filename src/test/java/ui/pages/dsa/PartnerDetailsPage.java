package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hooks.BaseTest;

import java.util.Map;

public class PartnerDetailsPage extends BaseTest{

    // Element for the Partner Details form
    private static final String BRANCH_INPUT_ID = "#branch";
    private static final String SALES_MANAGER_INPUT_ID = "#SalesManager";
    private static final String SCHEME_SELECT_ID = "#scheme";

    // Buttons
    private static final String SAVE_AND_NEXT_BTN = "button:has-text('SAVE AND NEXT')";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Helper for MuiAutocomplete: Clicks, types to filter, and selects the option.
     */
    private void fillAutocomplete(String locator, String value) {
        log.info("Filling autocomplete '{}' with value: {}", locator, value);
        Locator input = getPage().locator(locator);
        input.click();
        input.clear();
        input.fill(value); // Type to filter

        // Wait for the option to appear in the listbox and click it
        getPage().getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true)).click();
    }

    /**
     * Helper for MuiSelect: Clicks the div to open the menu, then selects the option.
     */
    private void selectDropdown(String locator, String value) {
        log.info("Selecting dropdown '{}' with value: {}", locator, value);
        getPage().locator(locator).click(); // Click to open dropdown

        // Wait for the option to appear in the listbox and click it
        getPage().getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true)).click();
    }

    /**
     * Reads the Cucumber Map and fills only the provided mandatory fields.
     */
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
        getPage().locator(SAVE_AND_NEXT_BTN).click();
        log.info("Clicked SAVE AND NEXT button");
    }
}
