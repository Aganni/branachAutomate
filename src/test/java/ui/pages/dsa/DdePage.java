package ui.pages.dsa;

import hooks.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class DdePage extends BaseTest {
    
    private final Page page;
    
    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String SAVE_BTN = "button:has-text('Save')";
    private static final String SUBMIT_BTN = "button:has-text('Submit')";

    public DdePage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void fillField(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;
        page.locator("#" + fieldId).fill(value);
        log.info("Filled field [{}] with value: {}", fieldId, value);
    }

    public void selectFromDropdown(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;
        Locator input = page.locator("#" + fieldId);
        input.click();
        input.fill(value);

        Locator option = page.getByRole(AriaRole.LISTBOX).getByText(value, new Locator.GetByTextOptions().setExact(false)).first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();
        log.info("Selected [{}] from dropdown [{}]", value, fieldId);
    }

    public void selectRadio(String groupName, String option) {
        page.locator("input[name='" + groupName + "'][value='" + option.toLowerCase() + "']").click();
        log.info("Selected [{}] for radio group [{}]", option, groupName);
    }

    public void selectRadixPopover(String labelText, String optionValue) {
        if (optionValue == null || optionValue.isEmpty()) return;

        log.info("Selecting [{}] for Radix Popover [{}]", optionValue, labelText);

        String xpath = "//label[text()='" + labelText + "']/ancestor::div[1]//button";
        Locator popoverTrigger = page.locator(xpath).first();

        popoverTrigger.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        popoverTrigger.click();

        Locator option = page.locator("[role='option'], [data-radix-collection-item], button")
                .filter(new Locator.FilterOptions().setHasText(optionValue))
                .last(); 

        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();

        log.info("Successfully selected {}", optionValue);
    }

    public void clickSave() {
        page.locator(SAVE_BTN).click();
        log.info("Clicked Save button");
    }

    public void fillCorrespondenceAddress(String partialAddress) {
        log.info("Filling Correspondence Address: {}", partialAddress);

        Locator addressInput = page.getByPlaceholder("Correspondence Address (Line 1)");

        addressInput.click();
        addressInput.fill(partialAddress);

        Locator suggestion = page.locator("div").filter(new Locator.FilterOptions().setHasText(partialAddress)).last();
        suggestion.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        suggestion.click();

        log.info("Address suggestion selected.");
    }

    public void selectEmail(String email) {
        log.info("Selecting Email: {}", email);

        Locator emailInput = page.getByPlaceholder("Select Email");
        emailInput.click();
        emailInput.fill(email);
    }

    public void selectValueFromMuiSelect(String fieldId, String value) {
        if (value == null || value.isEmpty()) return;

        log.info("Opening MuiSelect dropdown [{}]", fieldId);

        Locator selectTrigger = page.locator("#" + fieldId);
        selectTrigger.click();

        log.info("Selecting option: {}", value);
        Locator option = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(value).setExact(true));

        if (option.count() == 0) {
            option = page.locator("li[role='option']").filter(new Locator.FilterOptions().setHasText(value)).first();
        }

        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();

        log.info("Successfully selected [{}]", value);
    }

    public void clickSubmit() {
        page.locator(SUBMIT_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator(SUBMIT_BTN).click();
        log.info("Clicked Submit button");
    }
}
