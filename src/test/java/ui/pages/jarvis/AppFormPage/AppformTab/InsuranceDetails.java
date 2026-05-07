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

    // ── Locator constants ────────────────────────────────────────────────────
    private static final String LOC_INSURANCE_CARD    = "Insurance Details";
    private static final String LOC_EDIT_BTN          = "xpath=//div[contains(@class, 'form-header')]//button[contains(@class, 'edit-btn')]";
    private static final String LOC_HOLDER_DROPDOWN   = "xpath=//label[contains(normalize-space(text()), 'Policy Holder Name')]/following-sibling::div//div[contains(@class, 'el-select')]";
    private static final String LOC_NOMINEE_DROPDOWN  = "xpath=//label[contains(normalize-space(text()), 'Nominee Name')]/following-sibling::div//div[contains(@class, 'el-select')]";
    private static final String LOC_PREMIUM_INPUT     = "xpath=//label[contains(normalize-space(text()),'Insurance Premium including GST')]/following-sibling::div//input";
    private static final String LOC_SUBMIT_BTN        = "xpath=//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='SUBMIT']]";

    public InsuranceDetails(Page page) {
        this.page = page;
    }

    /** Opens the Insurance Details card and clicks Edit. */
    public void openInsuranceAndEdit() {
        log.info("Opening Insurance Details section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, LOC_INSURANCE_CARD);

        log.info("Clicking Edit on Insurance modal...");
        Locator editBtn = page.locator(LOC_EDIT_BTN).first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        editBtn.evaluate("node => node.click()"); // JS click bypasses any overlay

        page.waitForTimeout(1000); // allow Vue to enable all fields after edit mode activates
    }

    /** Fills all insurance fields, adds a nominee, submits, and verifies the result. */
    public void fillInsuranceAndSubmit(Map<String, String> data, String scenarioName) {
        log.info("Filling Insurance Details...");

        AppFormUtils.selectDropdown(page, "Insurance Provider",               data.getOrDefault("Provider", "Acko Group Health Insurance"));
        AppFormUtils.selectDropdown(page, "Policy Insurance Tenure (Months)", data.getOrDefault("Tenure", "10 Months"));

        // Premium input has no placeholder — located via label sibling XPath
        fillInsurancePremium(data.getOrDefault("InsurancePremium", "70000"));

        // Policy Holder — click the el-select wrapper (not the inner read-only input)
        selectViaSelectWrapper(LOC_HOLDER_DROPDOWN, null); // picks first non-disabled option

        // Nominee — open dropdown and click "+ Add" to reveal the inline form
        Locator nomineeWrapper = page.locator(LOC_NOMINEE_DROPDOWN).first();
        nomineeWrapper.scrollIntoViewIfNeeded();
        nomineeWrapper.click();
        Locator nomineeDropdown = page.locator(".el-select-dropdown:visible").last();
        nomineeDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        nomineeDropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText("+ Add"))
                .first()
                .click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(1000);

        fillNomineeDetails(data);

        log.info("Submitting Insurance form...");
        page.locator(LOC_SUBMIT_BTN).first().scrollIntoViewIfNeeded();
        page.locator(LOC_SUBMIT_BTN).first().evaluate("node => node.click()");

        verifyAndCloseInsuranceModal(scenarioName);
    }

    /** Fills the Insurance Premium (Rupees) text field. Has no placeholder — uses label XPath. */
    private void fillInsurancePremium(String amount) {
        log.info("Filling Insurance Premium including GST: {}", amount);
        Locator premiumInput = page.locator(LOC_PREMIUM_INPUT).first();
        premiumInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        premiumInput.scrollIntoViewIfNeeded();
        premiumInput.clear();
        premiumInput.fill(amount);
        premiumInput.press("Tab"); // trigger Vue v-model update
    }

    /**
     * Clicks an el-select wrapper div to open its dropdown and picks the first non-disabled option.
     * Used for read-only dropdowns that cannot be opened via their inner input.
     */
    private void selectViaSelectWrapper(String wrapperXpath, String optionText) {
        Locator wrapper = page.locator(wrapperXpath).first();
        wrapper.scrollIntoViewIfNeeded();
        wrapper.click();
        Locator dropdown = page.locator(".el-select-dropdown:visible").last();
        dropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

        Locator option = (optionText != null)
                ? dropdown.locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(optionText)).first()
                : dropdown.locator("li.el-select-dropdown__item:not(.is-disabled)").first();

        option.scrollIntoViewIfNeeded();
        option.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
    }

    /** Fills all fields in the inline Add Nominee form. */
    private void fillNomineeDetails(Map<String, String> data) {
        log.info("Filling Nominee details...");

        page.locator("xpath=//input[@placeholder='Enter new nominee name']").first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        page.locator("xpath=//input[@placeholder='Enter new nominee name']").first()
                .fill(data.getOrDefault("NomineeName", "Test Nominee"));

        // Relationship dropdown
        AppFormUtils.selectDropdown(page, "Relationship", data.getOrDefault("Relationship", "brother"));

        // DOB — opens an Element UI calendar picker
        log.info("Selecting DOB from calendar...");
        page.locator("xpath=//input[@placeholder='DOB']").first()
                .click(new Locator.ClickOptions().setForce(true));
        ui.Utils.Utils.selectDateFromElementUICalendar(data.getOrDefault("DOB", "1990-01-01"));

        // Gender dropdown
        AppFormUtils.selectDropdown(page, "Gender", data.getOrDefault("Gender", "Male"));

        // Address text area (hardcoded default — no field in feature file)
        page.locator("xpath=//textarea[@placeholder='Address']").first().fill("Manayata\nHebala\nFamily Owned");

        // Pincode triggers auto-population of City/State via API
        Locator pincodeInput = page.locator("xpath=//input[@placeholder='Enter the Pincode']").first();
        pincodeInput.fill("560045");
        pincodeInput.press("Tab");

        Locator cityInput = page.locator("xpath=//input[@placeholder='Enter the City']").first();
        try {
            // Wait for City to auto-populate; fall through to manual selection if it doesn't
            page.waitForCondition(() -> !cityInput.inputValue().isEmpty() || !cityInput.getAttribute("class").contains("is-disabled"),
                    new Page.WaitForConditionOptions().setTimeout(3000));
        } catch (Exception e) {
            log.warn("City did not auto-populate — attempting manual selection.");
            clickFirstDropdownOption("xpath=//label[text()='CITY']/following-sibling::div//div[contains(@class, 'el-select')]");
            clickFirstDropdownOption("xpath=//label[text()='STATE']/following-sibling::div//div[contains(@class, 'el-select')]");
        }

        page.locator("xpath=//input[@placeholder='Contact Number']").first().fill(data.getOrDefault("Mobile", "9876543210"));
        page.locator("xpath=//input[@placeholder='Email ID']").first().fill(data.getOrDefault("Email", "test.nominee@creditsaison-in.com"));

        log.info("Clicking Add Nominee button...");
        Locator addBtn = page.locator("xpath=//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='Add']]").first();
        if (addBtn.getAttribute("class").contains("is-disabled")) {
            ScreenshotUtil.saveScreenshot(page, "NomineeFormValidationFailed", "AddNominee");
            throw new AssertionError("'Add' button is disabled — a mandatory nominee field is missing or invalid.");
        }
        addBtn.scrollIntoViewIfNeeded();
        addBtn.evaluate("node => node.click()");
        page.waitForTimeout(1000);
    }

    /** Clicks a wrapper div to open its dropdown and picks the first non-disabled item. */
    private void clickFirstDropdownOption(String wrapperXpath) {
        Locator wrapper = page.locator(wrapperXpath).first();
        wrapper.click();
        page.waitForTimeout(500);
        page.locator(".el-select-dropdown:visible").last()
                .locator("li.el-select-dropdown__item:not(.is-disabled)")
                .first()
                .evaluate("node => node.click()");
    }

    /** Waits for a toast/error, closes the circle-X button, then dismisses the modal. */
    private void verifyAndCloseInsuranceModal(String scenarioName) {
        Locator toastTitle = page.locator(AppFormUtils.LOC_TOAST_TITLE).first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            if (toastTitle.innerText().trim().contains("Error")) {
                String msg = page.locator(AppFormUtils.LOC_TOAST_CONTENT).first().innerText().trim();
                ScreenshotUtil.saveScreenshot(page, "InsuranceError", scenarioName);
                throw new AssertionError("Insurance submission failed: " + msg);
            }
            log.info("Insurance Details submitted successfully.");
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            log.warn("No toast appeared after Insurance submit — continuing.");
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        // Close the circle-X button first (returns to read-only view)
        Locator circleCloseBtn = page.locator("button.close-btn.is-circle").first();
        if (circleCloseBtn.isVisible()) {
            circleCloseBtn.click();
            page.waitForTimeout(1000);
        }

        AppFormUtils.dismissModal(page, page.locator(AppFormUtils.LOC_ACTIVE_MODAL).last());
        log.info("Successfully returned to the main Application Details page.");
    }
}