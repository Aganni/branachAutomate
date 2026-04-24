package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class BankDetails extends BaseTest {

    private final Page page;

    public BankDetails(Page page) {
        this.page = page;
    }
    // ─────────────────────────────────────────────────────────────────────────────
    //  5. BANK DETAILS FLOW
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Opens the Bank Details accordion and clicks the Edit button.
     */
    public void openBankDetailsAndEdit() {
        log.info("Opening Bank Details section...");

        // Click the Bank Details accordion card
        Locator bankCard = page.locator("button.appform-card").filter(new Locator.FilterOptions().setHasText("Bank Details")).first();
        bankCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        bankCard.click();

        log.info("Clicking Edit on Bank Details modal...");
        Locator editBtn = page.locator(".el-dialog__body").filter(new Locator.FilterOptions().setHasText("Bank Details"))
                .locator("button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Fills both the Disbursal and Collection Bank details dynamically.
     * @param details Map containing the feature file data
     * @param scenarioName For capturing screenshots on failure
     */
    public void fillBankDetails(Map<String, String> details, String scenarioName) {
        log.info("Filling Bank Details dynamically...");

        // --- 1. DISBURSAL BANK DETAILS ---
        String disbursalScope = "//h3[contains(text(),'Disbursal Bank Details')]/ancestor::div[contains(@class,'el-row')]/following-sibling::div[1]";

        if (details.containsKey("Disbursal Account Number")) {
            selectBankDropdownOption(disbursalScope, "ACCOUNT NUMBER", details.get("Disbursal Account Number"));
        }
        if (details.containsKey("Disbursal Account Type")) {
            selectBankDropdownOption(disbursalScope, "ACCOUNT TYPE", details.get("Disbursal Account Type"));
        }
        if (details.containsKey("Disbursal IFSC Code")) {
            Locator ifscInput = page.locator(disbursalScope + "//label[text()='IFSC CODE']/following-sibling::div//input").first();
            ifscInput.fill(details.get("Disbursal IFSC Code"));
        }

        // --- 2. COLLECTION BANK DETAILS ---
        String collectionScope = "//h3[contains(text(),'Collection Bank Details')]/ancestor::div[contains(@class,'el-row')]/following-sibling::div[1]";

        if (details.containsKey("Collection Account Number")) {
            selectBankDropdownOption(collectionScope, "ACCOUNT NUMBER", details.get("Collection Account Number"));
        }
        if (details.containsKey("Collection Account Type")) {
            selectBankDropdownOption(collectionScope, "ACCOUNT TYPE", details.get("Collection Account Type"));
        }
        if (details.containsKey("Collection IFSC Code")) {
            Locator ifscInput = page.locator(collectionScope + "//label[text()='IFSC CODE']/following-sibling::div//input").first();
            ifscInput.fill(details.get("Collection IFSC Code"));
        }

        // --- 3. SUBMIT & VERIFY ---
        log.info("Clicking Submit floating button...");
        page.locator(".cs-fab:not(.disabled)").last().click();

        Locator toastTitle = page.locator(".el-notification__title").first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = toastTitle.innerText().trim();

            if (titleText.contains("Success")) {
                log.info("Bank details submitted successfully!");
            } else if (titleText.contains("Error")) {
                Locator toastMessage = page.locator(".el-notification__content").first();
                String errorMsg = toastMessage.isVisible() ? toastMessage.innerText().trim() : "Validation Error";
                log.error("Failed to update Bank Details: {}", errorMsg);
                ScreenshotUtil.saveScreenshot(page, "BankDetailsError", scenarioName);
                throw new AssertionError("Bank Details submission failed: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("No toast appeared after submitting Bank details.");
            ScreenshotUtil.saveScreenshot(page, "NoToast_BankDetails", scenarioName);
            throw new RuntimeException("Timeout waiting for submission response.", e);
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); // Allow toast to clear

        // --- 4. CLOSE MODAL ---
        log.info("Clicking outside the modal to dismiss it...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);
    }

    /**
     * Specialized helper for Bank Details dropdowns, restricted to a specific scope (Disbursal vs Collection)
     */
    private void selectBankDropdownOption(String scopeXpath, String label, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, label);

        // Find the input strictly within the provided scope (Disbursal or Collection block)
        Locator input = page.locator(scopeXpath + "//label[text()='" + label + "']/following-sibling::div//input").first();
        input.click(new Locator.ClickOptions().setForce(true));

        page.waitForTimeout(500); // Allow Element UI dropdown animation

        // Find and click the option from the floating list
        Locator option = page.locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(optionText)).first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click(new Locator.ClickOptions().setForce(true));
    }
}
