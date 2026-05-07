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
// ─────────────────────────────────────────────────────────────────────────────
    //  6. LOAN REQUIREMENTS FLOW
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Opens the Loan Requirements accordion and clicks Edit.
     */
    public void openLoanRequirementsAndEdit() {
        log.info("Opening Loan Requirements section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        Locator loanReqCard = page.locator("button.appform-card").filter(new Locator.FilterOptions().setHasText("Loan Requirements")).first();
        loanReqCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        loanReqCard.click();

        log.info("Clicking Edit on Loan Requirements modal...");
        Locator editBtn = page.locator(".el-dialog__body").filter(new Locator.FilterOptions().setHasText("Loan Requirements & Terms"))
                .locator("button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Dynamically reads RACC rates, inputs values, selects banks, and submits.
     */
    public void fillLoanRequirementsAndSubmit(String scenarioName) {
        log.info("Filling Loan Requirements dynamically...");

        // 1. EXTRACT AND FILL ROI
        Locator roiIndicator = page.locator("//label[contains(text(),'INTEREST RATE')]/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String roiText = roiIndicator.textContent().trim();
        String roiValue = roiText.replaceAll("[^0-9.]", ""); // Extracts just "19.50"

        if (roiValue.isEmpty()) roiValue = "19.50"; // Fallback just in case
        log.info("Extracted ROI value to input: {}", roiValue);

        Locator roiInput = page.getByPlaceholder("Enter the interest rate").first();
        roiInput.scrollIntoViewIfNeeded();
        
        // Force inject value using native typing to trigger Vue v-model updates
        roiInput.click();
        roiInput.fill(""); // Clear native value
        roiInput.pressSequentially(roiValue, new Locator.PressSequentiallyOptions().setDelay(100));
        roiInput.press("Tab"); // Trigger native blur

        page.waitForTimeout(500);
        log.info("ROI Field Value After Evaluate: {}", roiInput.inputValue());

        // 2. EXTRACT AND FILL PF
        Locator pfIndicator = page.locator("//label[contains(text(),'PROCESSING FEE EXCLUSIVE GST')]/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String pfText = pfIndicator.textContent().trim();
        String pfValue = pfText.replaceAll("[^0-9.]", ""); // Extracts just "2.00"

        if (pfValue.isEmpty()) pfValue = "2.00"; // Fallback just in case
        log.info("Extracted PF value to input: {}", pfValue);

        Locator pfInput = page.getByPlaceholder("Enter the processing fee percentage").first();
        pfInput.scrollIntoViewIfNeeded();
        
        // Force inject value using native typing to trigger Vue v-model updates
        pfInput.click();
        pfInput.fill(""); // Clear native value
        pfInput.pressSequentially(pfValue, new Locator.PressSequentiallyOptions().setDelay(100));
        pfInput.press("Tab"); // Trigger native blur

        page.waitForTimeout(500);
        log.info("PF Field Value After Evaluate: {}", pfInput.inputValue());

        // 3. SELECT MULTIPLE BANKS
        selectMultipleBanks("IDBI", "DCB", "CBI");
        page.waitForTimeout(1000); // Give the UI a second to clear any validation errors

        // --- 4. SUBMIT & VERIFY ---
        log.info("Clicking Submit floating button...");
        page.locator(".cs-fab:visible").first().click(new Locator.ClickOptions().setForce(true));

        log.info("Waiting for Success/Error toast to appear...");
        Locator toastTitle = page.locator(".el-notification__title").first();

        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = toastTitle.innerText().trim();
            Locator toastMessage = page.locator(".el-notification__content").first();
            String msgText = toastMessage.isVisible() ? toastMessage.innerText().trim() : "";

            if (titleText.contains("Success")) {
                log.info("Success popup verified! Message: '{}'", msgText);
            } else if (titleText.contains("Error")) {
                log.error("Failed to update Loan Requirements: {}", msgText);
                ScreenshotUtil.saveScreenshot(page, "LoanReqError", scenarioName);
                throw new AssertionError("Loan Requirements submission failed: " + msgText);
            }
        } catch (Exception e) {
            log.error("No toast appeared. The form likely has validation errors preventing submission.");
            ScreenshotUtil.saveScreenshot(page, "NoToast_LoanReq", scenarioName);
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("loan_req_error2.html"), page.content().getBytes());
            } catch(Exception ex) {
                log.error("Failed to write html", ex);
            }
            throw new RuntimeException("Timeout waiting for submission response.", e);
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        // --- 5. GO BACK ---
        log.info("Clicking outside the form to close it and go back...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        Locator activeDialog = page.locator(".el-dialog__wrapper:visible").last();
        if (activeDialog.isVisible()) {
            page.keyboard().press("Escape");
            page.waitForTimeout(1000);
        }

        log.info("Successfully returned to the main Application Details page.");
    }

    /**
     * Reliably selects multiple options from the Element UI CLM2 Banks dropdown.
     */
    /**
     * Reliably selects multiple options from the Element UI CLM2 Banks dropdown.
     */
    private void selectMultipleBanks(String... banks) {
        log.info("Selecting CLM2 Eligible Banks...");

        Locator dropdownWrapper = page.locator("//label[contains(text(), 'CLM2 Eligible Banks')]/following-sibling::div//div[contains(@class, 'el-select')]").first();
        dropdownWrapper.scrollIntoViewIfNeeded();
        dropdownWrapper.click();

        page.waitForTimeout(500); // Wait for animation

        for (String bank : banks) {
            log.info("Attempting to select bank: {}", bank);
            
            Locator option = page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(bank))
                .last();

            if (option.isVisible()) {
                option.scrollIntoViewIfNeeded();
                option.click();
                log.info("Successfully selected bank: {}", bank);
                page.waitForTimeout(500); 
            } else {
                log.warn("Bank not found in dropdown list: {}", bank);
            }
        }
        
        page.keyboard().press("Escape");
        page.waitForTimeout(500);
    }

    /**
     * Initiates the Credit Approval from inside the Loan Requirements modal.
     * @param reason The reason text to type into the approval box.
     */
    public void initiateCreditApproval(String reason) {
        log.info("Initiating Credit Approval with reason: {}", reason);

        // Give the modal 1 second to completely finish all opening animations
        page.waitForTimeout(1000);

        // 1. Locate the initial 'Initiate' button
        Locator initiateBtn = page.locator(".credit-approval-container button").filter(new Locator.FilterOptions().setHasText("Initiate")).first();
        initiateBtn.scrollIntoViewIfNeeded();

        // 2. Locate the input box we expect to appear
        Locator reasonInput = page.locator(".credit-approval-form-container input[type='text']").first();

        // 3. BULLETPROOF CLICK LOGIC: Try standard click. If ignored, force a JS click.
        try {
            initiateBtn.click(new Locator.ClickOptions().setForce(true));
            // Wait just 3 seconds to see if the input appears
            reasonInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
        } catch (Exception e) {
            log.warn("Standard click intercepted or ignored by Element UI. Forcing JavaScript click...");

            // This injects raw JS into the browser to force the click event from the inside out
            initiateBtn.evaluate("node => node.click()");

            // Wait for the input box again
            reasonInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        }

        // 4. Fill the reason
        reasonInput.fill(reason);
        log.info("Filled reason for approval: {}", reason);

        // 5. Click the 'Save' button
        Locator saveBtn = page.locator(".credit-approval-form-container button").filter(new Locator.FilterOptions().setHasText("Save")).first();
        saveBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked 'Save' button.");

        // 6. Click 'Initiate Credit Approval' (Waits for the 'is-disabled' class to vanish!)
        Locator finalSubmitBtn = page.locator(".credit-approval-form-container button:not(.is-disabled)").filter(new Locator.FilterOptions().setHasText("Initiate Credit Approval")).first();
        finalSubmitBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        finalSubmitBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked final 'Initiate Credit Approval' button.");

        // 7. Wait 10 seconds for the backend process to complete
        log.info("Waiting 10 seconds for backend credit approval processing...");
        page.waitForTimeout(10000);

        // 8. Close the modal by clicking outside
        log.info("Clicking outside the modal to dismiss it...");
        
        // Click the top-right circle close button
        Locator circleCloseBtn2 = page.locator("button.close-btn.is-circle").first();
        if (circleCloseBtn2.isVisible()) {
            circleCloseBtn2.click();
            page.waitForTimeout(1000);
        }

        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        // Fallback: If modal is still visible, press Escape
        if (page.locator(".el-dialog__wrapper:visible").last().isVisible()) {
            log.info("Modal didn't close, pressing 'Escape' key...");
            page.keyboard().press("Escape");
            page.waitForTimeout(1000);
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully returned to the main Application Details page.");
    }

}
