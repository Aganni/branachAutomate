package ui.pages.dsa;

import hooks.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class DdePage extends BaseTest {
    // --- Static Locators for Buttons ---
    private static final String SAVE_BTN = "button:has-text('Save')";
    private static final String SUBMIT_BTN = "button:has-text('Submit')";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Generic method to fill any text field by its ID or Name
     */
    public void fillField(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;
        getPage().locator("#" + fieldId).fill(value);
        log.info("Filled field [{}] with value: {}", fieldId, value);
    }

    /**
     * Generic method for Material UI Autocomplete/Dropdowns
     */
    public void selectFromDropdown(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;
        Locator input = getPage().locator("#" + fieldId);
        input.click();
        input.fill(value);

        // Select the first matching option from the listbox that appears
        Locator option = getPage().getByRole(AriaRole.LISTBOX).getByText(value, new Locator.GetByTextOptions().setExact(false)).first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();
        log.info("Selected [{}] from dropdown [{}]", value, fieldId);
    }

    /**
     * Generic method for Radio Buttons (Yes/No)
     */
    public void selectRadio(String groupName, String option) {
        // Targets the radio input based on name and value (yes/no)
        getPage().locator("input[name='" + groupName + "'][value='" + option.toLowerCase() + "']").click();
        log.info("Selected [{}] for radio group [{}]", option, groupName);
    }

    public void selectRadixPopover(String labelText, String optionValue) {
        if (optionValue == null || optionValue.isEmpty()) return;

        log.info("Selecting [{}] for Radix Popover [{}]", optionValue, labelText);

        String xpath = "//label[text()='" + labelText + "']/ancestor::div[1]//button";

        Locator popoverTrigger = getPage().locator(xpath).first();

        // Ensure it's visible before clicking
        popoverTrigger.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        popoverTrigger.click();

        Locator option = getPage().locator("[role='option'], [data-radix-collection-item], button")
                .filter(new Locator.FilterOptions().setHasText(optionValue))
                .last(); // Sometimes hidden versions exist, 'last()' usually gets the active one

        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();

        log.info("Successfully selected {}", optionValue);
    }

    public void clickSave() {
        getPage().locator(SAVE_BTN).click();
        log.info("Clicked Save button");
    }

    public void fillCorrespondenceAddress(String partialAddress) {
        log.info("Filling Correspondence Address: {}", partialAddress);

        Locator addressInput = getPage().getByPlaceholder("Correspondence Address (Line 1)");

        addressInput.click();
        addressInput.fill(partialAddress);

        Locator suggestion = getPage().locator("div").filter(new Locator.FilterOptions().setHasText(partialAddress)).last();
        suggestion.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        suggestion.click();

        log.info("Address suggestion selected.");
    }

    public void selectEmail(String email) {
        log.info("Selecting Email: {}", email);

        Locator emailInput = getPage().getByPlaceholder("Select Email");
        emailInput.click();
        emailInput.fill(email);
    }

    public void selectValueFromMuiSelect(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;

        log.info("Opening MuiSelect dropdown [{}]", fieldId);

        // 1. Click the DIV to open the list (Do NOT use .fill() here)
        Locator selectTrigger = getPage().locator("#" + fieldId);
        selectTrigger.click();

        // 2. The list appears in a Popover/Menu.
        // We look for the role 'option' that matches our text.
        log.info("Selecting option: {}", value);
        Locator option = getPage().getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(value).setExact(true));

        // If exact match fails, try a broader search within the menu
        if (option.count() == 0) {
            option = getPage().locator("li[role='option']").filter(new Locator.FilterOptions().setHasText(value)).first();
        }

        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();

        log.info("Successfully selected [{}]", value);
    }

    public void clickSubmit() {
        // Wait for submit to be enabled after Save
        getPage().locator(SUBMIT_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        getPage().locator(SUBMIT_BTN).click();
        log.info("Clicked Submit button");
    }
}
