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
     * Dynamically reads RACC rates, inputs them, selects banks, and submits.
     */
    public void fillLoanRequirementsAndSubmit(String scenarioName) {
        log.info("Filling Loan Requirements dynamically...");

        // 1. Read and Fill Interest Rate (ROI)
        Locator roiIndicator = page.locator("//label[text()='INTEREST RATE (%)']/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String roiText = roiIndicator.innerText().trim(); // Example: "RACC ROI 19.50%"
        String roiValue = extractNumberFromText(roiText);
        log.info("Extracted ROI value: {}", roiValue);

        Locator roiInput = page.getByPlaceholder("Enter the interest rate").first();
        roiInput.fill(roiValue);

        // 2. Read and Fill Processing Fee (PF)
        Locator pfIndicator = page.locator("//label[contains(text(),'PROCESSING FEE EXCLUSIVE GST')]/following-sibling::div//div[contains(@class,'racc-indicator')]").first();
        String pfText = pfIndicator.innerText().trim(); // Example: "RACC PF 2.00%"
        String pfValue = extractNumberFromText(pfText);
        log.info("Extracted PF value: {}", pfValue);

        Locator pfInput = page.getByPlaceholder("Enter the processing fee percentage").first();
        pfInput.fill(pfValue);

        // 3. Select the first 3 banks from CLM2 Eligible Banks
        selectMultipleBanks("IDBI", "DCB", "CBI");
        page.waitForTimeout(3000);

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
                // Optional: You can explicitly assert the text here if you want to be extra strict
                // Assert.assertTrue(msgText.contains("Updated loan details"), "Unexpected success message");
            } else if (titleText.contains("Error")) {
                log.error("Failed to update Loan Requirements: {}", msgText);
                ScreenshotUtil.saveScreenshot(page, "LoanReqError", scenarioName);
                throw new AssertionError("Loan Requirements submission failed: " + msgText);
            }
        } catch (Exception e) {
            log.error("No toast appeared after submitting Loan Requirements.");
            ScreenshotUtil.saveScreenshot(page, "NoToast_LoanReq", scenarioName);
            throw new RuntimeException("Timeout waiting for submission response.", e);
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        // --- 5. GO BACK (Click outside the form) ---
        log.info("Clicking outside the form to close it and go back...");

        // 1. Click the absolute top-left pixel of the browser (this hits the grey background mask)
        page.mouse().click(10, 10);
        page.waitForTimeout(1000); // Wait 1 second for the CSS fade-out animation to finish

        // 2. Safety Fallback: Check if the modal is somehow still visible
        Locator activeDialog = page.locator(".el-dialog__wrapper:visible").last();
        if (activeDialog.isVisible()) {
            log.info("Modal didn't close from mouse click. Pressing 'Escape' key...");
            page.keyboard().press("Escape"); // Element UI natively supports Esc to close modals
            page.waitForTimeout(1000);
        }

        log.info("Successfully returned to the main Application Details page.");
    }

    /**
     * Helper to extract just the numbers (e.g., "19.50" from "RACC ROI 19.50%")
     */
    private String extractNumberFromText(String text) {
        // Replaces everything that is NOT a digit or a decimal point with an empty string
        return text.replaceAll("[^0-9.]", "");
    }

    /**
     * Helper to handle multi-select dropdowns in Element UI
     */
    private void selectMultipleBanks(String... banksToSelect) {
        log.info("Selecting CLM2 Eligible Banks...");

        // Open the dropdown
        Locator dropdownInput = page.locator("//label[text()='CLM2 Eligible Banks']/following-sibling::div//input").first();
        dropdownInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500); // Allow animation

        // Loop through and click each requested bank
        for (String bank : banksToSelect) {
            Locator option = page.locator("li.el-select-dropdown__item:visible").filter(new Locator.FilterOptions().setHasText(bank)).first();
            option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            option.click(new Locator.ClickOptions().setForce(true));
            log.info("Selected bank: {}", bank);
        }

        // Press Escape to close the multi-select dropdown list cleanly
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
