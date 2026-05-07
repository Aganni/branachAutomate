package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

import java.util.Map;

public class BankDetails extends BaseTest {

    private final Page page;

    public BankDetails(Page page) {
        this.page = page;
    }

    /** Opens the Bank Details card and clicks the Edit button. */
    public void openBankDetailsAndEdit() {
        log.info("Opening Bank Details section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, "Bank Details");

        log.info("Clicking Edit on Bank Details modal...");
        Locator editBtn = page.locator(".el-dialog__body")
                .filter(new Locator.FilterOptions().setHasText("Bank Details"))
                .locator("button.edit-btn")
                .first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /** Fills Disbursal (index 0) and Collection (index 1) bank details, submits, and closes the modal. */
    public void fillBankDetails(Map<String, String> details, String scenarioName) {
        log.info("Filling Bank Details dynamically...");

        if (details.containsKey("Disbursal Account Number")) selectBankDropdownOption("ACCOUNT NUMBER", 0, details.get("Disbursal Account Number"));
        if (details.containsKey("Disbursal Account Type"))   selectBankDropdownOption("ACCOUNT TYPE",   0, details.get("Disbursal Account Type"));
        if (details.containsKey("Disbursal IFSC Code"))      page.locator("xpath=//label[text()='IFSC CODE']/following-sibling::div//input").nth(0).fill(details.get("Disbursal IFSC Code"));

        if (details.containsKey("Collection Account Number")) selectBankDropdownOption("ACCOUNT NUMBER", 1, details.get("Collection Account Number"));
        if (details.containsKey("Collection Account Type"))   selectBankDropdownOption("ACCOUNT TYPE",   1, details.get("Collection Account Type"));
        if (details.containsKey("Collection IFSC Code"))      page.locator("xpath=//label[text()='IFSC CODE']/following-sibling::div//input").nth(1).fill(details.get("Collection IFSC Code"));

        log.info("Clicking Submit floating button...");
        Locator submitBtn = page.locator(".cs-fab:visible").first();
        submitBtn.hover(new Locator.HoverOptions().setForce(true));
        submitBtn.click(new Locator.ClickOptions().setForce(true));

        AppFormUtils.verifyToast(page, "BankDetails", scenarioName);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        // Dismiss modal after successful submit
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);
    }

    /** Selects a dropdown option scoped to the nth occurrence of a labelled field. */
    private void selectBankDropdownOption(String label, int index, String optionText) {
        log.info("Selecting '{}' for '{}' (index: {})", optionText, label, index);
        Locator input = page.locator("xpath=//label[text()='" + label + "']/following-sibling::div//input").nth(index);
        input.hover(new Locator.HoverOptions().setForce(true));
        input.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);

        // :visible scopes to only the currently open dropdown, ignoring hidden ones
        Locator option = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click(new Locator.ClickOptions().setForce(true));
        log.info("Selected '{}'.", optionText);
    }
}
