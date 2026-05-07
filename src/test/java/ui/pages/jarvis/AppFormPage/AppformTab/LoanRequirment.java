package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class LoanRequirment extends BaseTest {

    private final Page page;

    public LoanRequirment(Page page) {
        this.page = page;
    }

    /** Opens the Loan Requirements card and clicks Edit. */
    public void openLoanRequirementsAndEdit() {
        log.info("Opening Loan Requirements section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, "Loan Requirements");

        log.info("Clicking Edit on Loan Requirements modal...");
        Locator editBtn = page.locator(".el-dialog__body")
                .filter(new Locator.FilterOptions().setHasText("Loan Requirements & Terms"))
                .locator("button.edit-btn")
                .first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /** Reads RACC rates, fills ROI + PF, selects banks, submits, and closes the modal. */
    public void fillLoanRequirementsAndSubmit(String scenarioName) {
        log.info("Filling Loan Requirements dynamically...");

        // Read RACC-displayed ROI and fill it into the input (Vue requires pressSequentially to trigger v-model)
        Locator roiIndicator = page.locator("xpath=//label[contains(text(),'INTEREST RATE')]/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String roiValue = roiIndicator.textContent().trim().replaceAll("[^0-9.]", "");
        if (roiValue.isEmpty()) roiValue = "19.50";
        log.info("ROI from RACC: {}", roiValue);

        Locator roiInput = page.getByPlaceholder("Enter the interest rate").first();
        roiInput.scrollIntoViewIfNeeded();
        roiInput.click();
        roiInput.fill("");
        roiInput.pressSequentially(roiValue, new Locator.PressSequentiallyOptions().setDelay(100));
        roiInput.press("Tab");
        page.waitForTimeout(500);

        // Read RACC-displayed PF and fill it into the input
        Locator pfIndicator = page.locator("xpath=//label[contains(text(),'PROCESSING FEE EXCLUSIVE GST')]/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String pfValue = pfIndicator.textContent().trim().replaceAll("[^0-9.]", "");
        if (pfValue.isEmpty()) pfValue = "2.00";
        log.info("PF from RACC: {}", pfValue);

        Locator pfInput = page.getByPlaceholder("Enter the processing fee percentage").first();
        pfInput.scrollIntoViewIfNeeded();
        pfInput.click();
        pfInput.fill("");
        pfInput.pressSequentially(pfValue, new Locator.PressSequentiallyOptions().setDelay(100));
        pfInput.press("Tab");
        page.waitForTimeout(500);

        selectMultipleBanks("IDBI", "DCB", "CBI");
        page.waitForTimeout(1000);

        log.info("Clicking Submit floating button...");
        page.locator(".cs-fab:visible").first().click(new Locator.ClickOptions().setForce(true));

        AppFormUtils.verifyToast(page, "LoanRequirements", scenarioName);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        AppFormUtils.dismissModal(page, page.locator(AppFormUtils.LOC_ACTIVE_MODAL).last());
        log.info("Successfully returned to the main Application Details page.");
    }

    /** Selects multiple options from the CLM2 Eligible Banks multi-select dropdown. */
    private void selectMultipleBanks(String... banks) {
        log.info("Selecting CLM2 Eligible Banks...");
        Locator dropdownWrapper = page.locator("xpath=//label[contains(text(), 'CLM2 Eligible Banks')]/following-sibling::div//div[contains(@class, 'el-select')]").first();
        dropdownWrapper.scrollIntoViewIfNeeded();
        dropdownWrapper.click();
        page.waitForTimeout(500);

        for (String bank : banks) {
            Locator option = page.locator("li.el-select-dropdown__item")
                    .filter(new Locator.FilterOptions().setHasText(bank))
                    .last();
            if (option.isVisible()) {
                option.scrollIntoViewIfNeeded();
                option.click();
                log.info("Selected bank: {}", bank);
                page.waitForTimeout(500);
            } else {
                log.warn("Bank '{}' not found in dropdown.", bank);
            }
        }
        page.keyboard().press("Escape");
        page.waitForTimeout(500);
    }

    /** Initiates Credit Approval from inside the Loan Requirements modal. */
    public void initiateCreditApproval(String reason) {
        log.info("Initiating Credit Approval with reason: {}", reason);
        page.waitForTimeout(1000);

        Locator initiateBtn = page.locator(".credit-approval-container button")
                .filter(new Locator.FilterOptions().setHasText("Initiate"))
                .first();
        initiateBtn.scrollIntoViewIfNeeded();

        Locator reasonInput = page.locator(".credit-approval-form-container input[type='text']").first();

        // Try standard click; fall back to JS click if Element UI absorbs it
        try {
            initiateBtn.click(new Locator.ClickOptions().setForce(true));
            reasonInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
        } catch (Exception e) {
            log.warn("Standard click ignored — forcing JS click on Initiate button.");
            initiateBtn.evaluate("node => node.click()");
            reasonInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        }

        reasonInput.fill(reason);
        log.info("Filled approval reason: {}", reason);

        Locator saveBtn = page.locator(".credit-approval-form-container button")
                .filter(new Locator.FilterOptions().setHasText("Save"))
                .first();
        saveBtn.click(new Locator.ClickOptions().setForce(true));

        // Wait for the disabled state to clear before clicking the final submit
        Locator finalSubmitBtn = page.locator(".credit-approval-form-container button:not(.is-disabled)")
                .filter(new Locator.FilterOptions().setHasText("Initiate Credit Approval"))
                .first();
        finalSubmitBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        finalSubmitBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked final 'Initiate Credit Approval' button.");

        page.waitForTimeout(10_000); // backend processes the approval asynchronously

        // Close via circle-X button first, then outside-click + Escape fallback
        Locator circleCloseBtn = page.locator("button.close-btn.is-circle").first();
        if (circleCloseBtn.isVisible()) {
            circleCloseBtn.click();
            page.waitForTimeout(1000);
        }

        AppFormUtils.dismissModal(page, page.locator(AppFormUtils.LOC_ACTIVE_MODAL).last());
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully returned to the main Application Details page.");
    }
}
