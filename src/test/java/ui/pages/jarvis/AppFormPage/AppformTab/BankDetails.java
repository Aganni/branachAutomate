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
        AppFormTabNavigator.ensureOnAppFormTab(page);

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

        // --- 1. DISBURSAL BANK DETAILS (Index 0) ---
        if (details.containsKey("Disbursal Account Number")) {
            selectBankDropdownOption("ACCOUNT NUMBER", 0, details.get("Disbursal Account Number"));
        }
        if (details.containsKey("Disbursal Account Type")) {
            selectBankDropdownOption("ACCOUNT TYPE", 0, details.get("Disbursal Account Type"));
        }
        if (details.containsKey("Disbursal IFSC Code")) {
            Locator ifscInput = getPage().locator("//label[text()='IFSC CODE']/following-sibling::div//input").nth(0);
            ifscInput.fill(details.get("Disbursal IFSC Code"));
        }

        // --- 2. COLLECTION BANK DETAILS (Index 1) ---
        if (details.containsKey("Collection Account Number")) {
            selectBankDropdownOption("ACCOUNT NUMBER", 1, details.get("Collection Account Number"));
        }
        if (details.containsKey("Collection Account Type")) {
            selectBankDropdownOption("ACCOUNT TYPE", 1, details.get("Collection Account Type"));
        }
        if (details.containsKey("Collection IFSC Code")) {
            Locator ifscInput = getPage().locator("//label[text()='IFSC CODE']/following-sibling::div//input").nth(1);
            ifscInput.fill(details.get("Collection IFSC Code"));
        }

        // --- 3. SUBMIT & VERIFY ---
        log.info("Clicking Submit floating button...");

        Locator submitBtn = getPage().locator(".cs-fab:visible").first();

// Hover over it to trigger any CSS animations, then force click
        submitBtn.hover(new Locator.HoverOptions().setForce(true));
        submitBtn.click(new Locator.ClickOptions().setForce(true));

        Locator toastTitle = getPage().locator(".el-notification__title").first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = toastTitle.innerText().trim();

            if (titleText.contains("Success")) {
                log.info("Bank details submitted successfully!");
            } else if (titleText.contains("Error")) {
                Locator toastMessage = getPage().locator(".el-notification__content").first();
                String errorMsg = toastMessage.isVisible() ? toastMessage.innerText().trim() : "Validation Error";
                log.error("Failed to update Bank Details: {}", errorMsg);
                ScreenshotUtil.saveScreenshot(getPage(), "BankDetailsError", scenarioName);
                throw new AssertionError("Bank Details submission failed: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("No toast appeared after submitting Bank details.");
            ScreenshotUtil.saveScreenshot(getPage(), "NoToast_BankDetails", scenarioName);
            throw new RuntimeException("Timeout waiting for submission response.", e);
        }

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        getPage().waitForTimeout(1000); // Allow toast to clear

        // --- 4. CLOSE MODAL ---
        log.info("Clicking outside the modal to dismiss it...");
        getPage().mouse().click(10, 10);
        getPage().waitForTimeout(1000);
    }

    private void selectBankDropdownOption(String label, int index, String optionText) {
        log.info("Selecting '{}' for '{}' (Index: {})", optionText, label, index);

        // Find the input based on its label and index
        Locator input = getPage().locator("//label[text()='" + label + "']/following-sibling::div//input").nth(index);

        // Force hover and click to open the dropdown
        input.hover(new Locator.HoverOptions().setForce(true));
        input.click(new Locator.ClickOptions().setForce(true));

        getPage().waitForTimeout(500); // Allow Element UI dropdown animation to finish

        // --- THE FIX ---
        // Added `:visible` so Playwright strictly ignores old, hidden dropdown lists in the DOM!
        Locator option = getPage().locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();

        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click(new Locator.ClickOptions().setForce(true));
        log.info("Successfully clicked option: {}", optionText);
    }
}
