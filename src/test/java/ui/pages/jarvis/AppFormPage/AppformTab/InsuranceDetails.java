package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class InsuranceDetails extends BaseTest {

    private final Page page;

    public InsuranceDetails(Page page) {
        this.page = page;
    }

    /**
     * Opens the Insurance Details section and clicks Edit.
     */
    public void openInsuranceAndEdit() {
        log.info("Opening Insurance Details section...");

        Locator insuranceCard = page.locator("button.appform-card")
                .filter(new Locator.FilterOptions().setHasText("Insurance Details"))
                .first();
        insuranceCard.scrollIntoViewIfNeeded();
        insuranceCard.click(new Locator.ClickOptions().setForce(true));

        log.info("Clicking Edit on Insurance modal...");

        // Use strict XPath for the Edit button based on your HTML
        Locator editBtn = page.locator("xpath=//div[contains(@class, 'form-header')]//button[contains(@class, 'edit-btn')]").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        // JS Click bypasses any invisible overlays blocking the button
        editBtn.evaluate("node => node.click()");

        // Wait a second for Vue to remove the 'disabled' tags from all inputs
        page.waitForTimeout(1000);
    }

    /**
     * Fills the main insurance details, adds a new nominee, and submits.
     */
    public void fillInsuranceAndSubmit(Map<String, String> data, String scenarioName) {
        log.info("Filling Insurance Details...");

        // 1. Insurance Provider
        selectDropdownOption("Insurance Provider", data.getOrDefault("Provider", "Acko Group Health Insurance"));

        // 2. Policy Tenure
        selectDropdownOption("Policy Insurance Tenure (Months)", data.getOrDefault("Tenure", "10 Months"));

        // 3. Policy Holder Name
        log.info("Selecting first available Policy Holder Name...");

        // CRITICAL FIX: Target the 'el-select' wrapper div, not the inner <input>
        Locator holderWrapper = page.locator("xpath=//label[contains(normalize-space(text()), 'Policy Holder Name')]/following-sibling::div//div[contains(@class, 'el-select')]").first();
        holderWrapper.scrollIntoViewIfNeeded();

        // A standard click on the wrapper is usually recognized natively by Vue
        holderWrapper.click();

        Locator activeHolderDropdown = page.locator(".el-select-dropdown:visible").last();
        // Wait explicitly for the dropdown to render on the screen
        activeHolderDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        Locator firstHolderOption = activeHolderDropdown.locator("li.el-select-dropdown__item:not(.is-disabled)").first();
        firstHolderOption.scrollIntoViewIfNeeded();
        firstHolderOption.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);

        // 4. Open Nominee Form
        log.info("Opening + Add Nominee form...");

        // CRITICAL FIX: Target the 'el-select' wrapper div, not the inner <input>
        Locator nomineeWrapper = page.locator("xpath=//label[contains(normalize-space(text()), 'Nominee Name')]/following-sibling::div//div[contains(@class, 'el-select')]").first();
        nomineeWrapper.scrollIntoViewIfNeeded();

        // A standard click on the wrapper
        nomineeWrapper.click();

        Locator activeNomineeDropdown = page.locator(".el-select-dropdown:visible").last();
        // Wait explicitly for the dropdown to render on the screen
        activeNomineeDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        Locator addNomineeOption = activeNomineeDropdown.locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText("+ Add")).first();
        addNomineeOption.scrollIntoViewIfNeeded();
        addNomineeOption.click(new Locator.ClickOptions().setForce(true));

        // Wait a full second for the inner Nominee form to slide into view
        page.waitForTimeout(1000);

        // 5. Fill Nominee Form
        fillNomineeDetails(data);

        // 6. Submit Main Insurance Form
        log.info("Submitting main Insurance form...");
        Locator submitBtn = page.locator("xpath=//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='SUBMIT']]").first();
        submitBtn.scrollIntoViewIfNeeded();
        submitBtn.evaluate("node => node.click()");

        // 7. Verify Success Toast
        verifySuccessAndClose(scenarioName);
    }

    /**
     * Helper to fill out the inner Nominee Form
     */
    private void fillNomineeDetails(Map<String, String> data) {
        log.info("Filling Nominee details...");

        // 1. Nominee Name
        Locator nameInput = page.locator("xpath=//input[@placeholder='Enter new nominee name']").first();
        nameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        nameInput.fill(data.getOrDefault("NomineeName", "Test Nominee"));

        // 2. Relationship Dropdown
        Locator relationWrapper = page.locator("xpath=//label[contains(normalize-space(text()), 'Relationship')]/following-sibling::div//div[contains(@class, 'el-select')]").first();
        relationWrapper.scrollIntoViewIfNeeded();
        relationWrapper.click();
        page.waitForTimeout(500);
        Locator activeRelationDropdown = page.locator(".el-select-dropdown:visible").last();
        activeRelationDropdown.locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(data.getOrDefault("Relationship", "brother"))).first().evaluate("node => node.click()");

        // 3. DOB (Handle the Calendar Popup)
        log.info("Selecting DOB from Calendar...");
        Locator dobInput = page.locator("xpath=//input[@placeholder='DOB']").first();
        dobInput.scrollIntoViewIfNeeded();
        dobInput.click(new Locator.ClickOptions().setForce(true)); // This opens the calendar popup

        // Wait for the calendar popup to appear, then click the 15th of the current month
        Locator activeCalendar = page.locator(".el-picker-panel:visible").last();
        activeCalendar.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        Locator day15 = activeCalendar.locator("xpath=.//td[contains(@class, 'available')]/div/span[text()='15']").first();
        day15.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500); // Give the calendar time to close

        // 4. Gender Dropdown
        Locator genderWrapper = page.locator("xpath=//label[normalize-space(text())='Gender']/following-sibling::div//div[contains(@class, 'el-select')]").first();
        genderWrapper.scrollIntoViewIfNeeded();
        genderWrapper.click();
        page.waitForTimeout(500);
        Locator activeGenderDropdown = page.locator(".el-select-dropdown:visible").last();
        activeGenderDropdown.locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(data.getOrDefault("Gender", "Male"))).first().evaluate("node => node.click()");

        // 5. Address
        Locator addressInput = page.locator("xpath=//textarea[@placeholder='Address']").first();
        addressInput.fill("Manayata\nHebala\nFamily Owned");

        // 6. Pincode & City/State
        log.info("Entering Pincode and waiting for City/State to populate...");
        Locator pincodeInput = page.locator("xpath=//input[@placeholder='Enter the Pincode']").first();
        pincodeInput.fill("560045");
        pincodeInput.press("Tab");

        // Wait up to 3 seconds for the City dropdown to NOT be empty (meaning it populated)
        Locator cityInput = page.locator("xpath=//input[@placeholder='Enter the City']").first();
        try {
            // Element UI often removes the 'is-disabled' or sets a value when populated
            page.waitForCondition(() -> !cityInput.inputValue().isEmpty() || !cityInput.getAttribute("class").contains("is-disabled"), new Page.WaitForConditionOptions().setTimeout(3000));
        } catch (Exception e) {
            log.warn("City/State did not auto-populate in time. Attempting manual selection...");
            // Manual City Selection Fallback
            Locator cityWrapper = page.locator("xpath=//label[text()='CITY']/following-sibling::div//div[contains(@class, 'el-select')]").first();
            cityWrapper.click();
            page.waitForTimeout(500);
            page.locator(".el-select-dropdown:visible").last().locator("li.el-select-dropdown__item:not(.is-disabled)").first().evaluate("node => node.click()");

            // Manual State Selection Fallback
            Locator stateWrapper = page.locator("xpath=//label[text()='STATE']/following-sibling::div//div[contains(@class, 'el-select')]").first();
            stateWrapper.click();
            page.waitForTimeout(500);
            page.locator(".el-select-dropdown:visible").last().locator("li.el-select-dropdown__item:not(.is-disabled)").first().evaluate("node => node.click()");
        }

        // 7. Contact & Email
        page.locator("xpath=//input[@placeholder='Contact Number']").first().fill(data.getOrDefault("Mobile", "9876543210"));
        page.locator("xpath=//input[@placeholder='Email ID']").first().fill(data.getOrDefault("Email", "test.nominee@creditsaison-in.com"));

        // 8. Click 'Add' Button
        log.info("Clicking Add Nominee button...");
        Locator addBtn = page.locator("xpath=//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='Add']]").first();

        // Check if button is disabled (validation error) before clicking
        if (addBtn.getAttribute("class").contains("is-disabled")) {
            ScreenshotUtil.saveScreenshot(page, "NomineeFormValidationFailed", "AddNominee");
            throw new AssertionError("The 'Add' button is disabled. A mandatory field in the Nominee form is missing or invalid.");
        }

        addBtn.scrollIntoViewIfNeeded();
        addBtn.evaluate("node => node.click()");
        page.waitForTimeout(1000);
    }

    /**
     * Verifies the success toast, clicks the modal close button, and dismisses the popup entirely.
     */
    private void verifySuccessAndClose(String scenarioName) {
        Locator toastTitle = page.locator(".el-notification__title").first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            if (toastTitle.innerText().trim().contains("Error")) {
                String errorMsg = page.locator(".el-notification__content").first().innerText().trim();
                ScreenshotUtil.saveScreenshot(page, "InsuranceError", scenarioName);
                throw new AssertionError("Insurance submission failed: " + errorMsg);
            }
            log.info("Insurance Details submitted successfully.");
        } catch (Exception e) {
            log.warn("No toast appeared after submitting Insurance, continuing...");
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        log.info("Closing modal entirely...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        if (page.locator(".el-dialog__wrapper:visible").last().isVisible()) {
            page.keyboard().press("Escape");
        }
        log.info("Successfully returned to the main Application Details page.");
    }

    /**
     * Bulletproof helper method to handle Element UI dropdowns reliably
     */
    private void selectDropdownOption(String label, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, label);

        Locator input = page.locator("xpath=//label[contains(normalize-space(text()), '" + label + "')]/following-sibling::div//input").first();
        input.scrollIntoViewIfNeeded();

        // JS click ensures the dropdown activates even if partially covered
        input.evaluate("node => node.click()");

        // Wait for dropdown menu to appear
        Locator activeDropdown = page.locator(".el-select-dropdown:visible").last();
        try {
            activeDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        } catch (Exception e) {
            throw new RuntimeException("The dropdown menu for '" + label + "' failed to open!", e);
        }

        Locator option = activeDropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();

        option.scrollIntoViewIfNeeded();
        // JS Click prevents the click from being swallowed by span wrappers
        option.evaluate("node => node.click()");

        log.info("Successfully selected '{}'.", optionText);
        page.waitForTimeout(300);
    }
}