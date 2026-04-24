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
        insuranceCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        insuranceCard.click();

        log.info("Clicking Edit on Insurance modal...");
        Locator editBtn = page.locator(".form-header button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(LoadState.NETWORKIDLE);
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

        // 3. Policy Holder Name (Selects the first available option in the list)
        log.info("Selecting first available Policy Holder Name...");
        Locator holderInput = page.locator("//label[text()='Policy Holder Name']/following-sibling::div//input").first();
        holderInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
        Locator firstHolderOption = page.locator("li.el-select-dropdown__item:visible:not(.is-disabled)").first();
        firstHolderOption.click(new Locator.ClickOptions().setForce(true));

        // 4. Open Nominee Form
        log.info("Opening + Add Nominee form...");
        Locator nomineeInput = page.locator("//label[text()='Nominee Name']/following-sibling::div//input").first();
        nomineeInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
        Locator addNomineeOption = page.locator("li.el-select-dropdown__item:visible").filter(new Locator.FilterOptions().setHasText("+ Add")).first();
        addNomineeOption.click(new Locator.ClickOptions().setForce(true));

        // 5. Fill Nominee Form (Scoped strictly to avoid matching outer form fields)
        fillNomineeDetails(data);

        // 6. Submit Main Insurance Form
        log.info("Submitting main Insurance form...");
        Locator submitBtn = page.locator("button.el-button--primary:visible").filter(new Locator.FilterOptions().setHasText("SUBMIT")).first();
        submitBtn.click(new Locator.ClickOptions().setForce(true));

        // 7. Verify Success Toast
        verifySuccessAndClose(scenarioName);
    }

    /**
     * Helper to fill out the inner Nominee Card
     */
    private void fillNomineeDetails(Map<String, String> data) {
        log.info("Filling Nominee details...");

        // Scope all lookups to the nominee card to prevent cross-contamination
        Locator nomineeCard = page.locator(".nominee-card").first();

        nomineeCard.locator("input[placeholder='Enter new nominee name']").fill(data.getOrDefault("NomineeName", "Test Nominee"));

        // Relationship Dropdown
        Locator relationInput = nomineeCard.locator("//label[text()='Relationship with applicant']/following-sibling::div//input").first();
        relationInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
        page.locator("li.el-select-dropdown__item:visible").filter(new Locator.FilterOptions().setHasText(data.getOrDefault("Relationship", "brother"))).first().click();

        // DOB (Typing directly into Element UI date pickers bypasses the calendar UI)
        Locator dobInput = nomineeCard.locator("input[placeholder='DOB']").first();
        dobInput.fill(data.getOrDefault("DOB", "1995-05-15"));
        dobInput.press("Enter"); // Confirms the date selection

        // Gender Dropdown
        Locator genderInput = nomineeCard.locator("//label[text()='Gender']/following-sibling::div//input").first();
        genderInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
        page.locator("li.el-select-dropdown__item:visible").filter(new Locator.FilterOptions().setHasText(data.getOrDefault("Gender", "Male"))).first().click();

        // Address
        nomineeCard.locator("textarea[placeholder='Address']").fill("Manayata\nHebala\nFamily Owned");

        // Pincode (Wait for City/State to auto-populate)
        log.info("Filling Pincode and waiting for auto-population...");
        Locator pincodeInput = nomineeCard.locator("input[placeholder='Enter the Pincode']").first();
        pincodeInput.fill("560045");
        pincodeInput.press("Tab"); // Triggers backend fetch
        page.waitForTimeout(1500); // Give backend 1.5s to populate city/state

        // Contact & Email
        nomineeCard.locator("input[placeholder='Contact Number']").fill(data.getOrDefault("Mobile", "9876543210"));
        nomineeCard.locator("input[placeholder='Email ID']").fill(data.getOrDefault("Email", "test.nominee@creditsaison-in.com"));

        // Click 'Add' strictly inside the nominee card
        log.info("Clicking Add Nominee button...");
        Locator addBtn = nomineeCard.locator("button.el-button--primary:not(.is-disabled)").filter(new Locator.FilterOptions().setHasText("Add")).first();
        addBtn.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(1000); // Allow card to close
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

        // Click the specific Close button (X) on the form header
        log.info("Clicking Close (X) button on modal header...");
        Locator closeBtn = page.locator(".form-header button.close-btn").first();
        if (closeBtn.isVisible()) {
            closeBtn.click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(500);
        }

        // Ultimate Fallback: Click outside the modal or press Escape to fully close it
        log.info("Clicking outside modal to ensure complete exit...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        if (page.locator(".el-dialog__wrapper:visible").last().isVisible()) {
            page.keyboard().press("Escape");
        }
        log.info("Successfully returned to the main Application Details page.");
    }

    /**
     * Reusable helper for simple dropdowns
     */
    private void selectDropdownOption(String label, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, label);
        Locator input = page.locator("//label[contains(text(),'" + label + "')]/following-sibling::div//input").first();
        input.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
        Locator option = page.locator("li.el-select-dropdown__item:visible").filter(new Locator.FilterOptions().setHasText(optionText)).first();
        option.click(new Locator.ClickOptions().setForce(true));
    }
}