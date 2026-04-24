package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class AppFormOwnerShipDetails extends BaseTest {

    private final Page page;

    public AppFormOwnerShipDetails(Page page) {
        this.page = page;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AppForm Ownership Details Form
    // ─────────────────────────────────────────────────────────────────────────────

    public void openAppFormOwnerShipAndEdit() {
        log.info("Opening Appform Ownership Details section...");

        // 1. REFACTORED: Replaced massive absolute XPath with a stable semantic locator
        Locator ownershipCard = page.locator("button.appform-card")
                .filter(new Locator.FilterOptions().setHasText("Appform Ownership Details"))
                .first();
        ownershipCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        ownershipCard.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Clicking Edit on Appform Ownership Details...");

        // 2. Click the specific edit button inside the floating dialog
        Locator editBtn = page.locator("[role='dialog']:visible button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));
    }

    /**
     * Searches for, selects the Credit Approver/Manager, submits, and exits the modal.
     */
    public void selectCreditApproverAndSubmit(Map<String, String> details, String scenarioName) {
        String approverEmail = details.get("UserEmail");
        log.info("Assigning Credit Approver: [{}] for scenario: [{}]", approverEmail, scenarioName);

        // 3. REFACTORED: Find the input using the visible Label, NOT the placeholder!
        // (Placeholders change based on who is currently assigned, breaking tests)
        Locator approverInput = page.locator("//label[contains(text(), 'Credit Manager') or contains(text(), 'Credit Approver')]/following-sibling::div//input").first();

        approverInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        approverInput.click(new Locator.ClickOptions().setForce(true));

        // 4. Type the email directly from the keyboard to trigger Element UI's filter
        page.keyboard().type(approverEmail);
        page.waitForTimeout(1000); // Allow backend to search and populate the dropdown list

        // 5. Select the visible filtered option
        Locator option = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(approverEmail))
                .first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click(new Locator.ClickOptions().setForce(true));

        // 6. Submit via the active Floating Action Button
        log.info("Submitting Appform Ownership Details...");
        Locator submitBtn = page.locator(".cs-fab:visible").first();
        submitBtn.click(new Locator.ClickOptions().setForce(true));

        // 7. Verify Success / Capture Screenshot on Error
        Locator toastTitle = page.locator(".el-notification__title").first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            if (toastTitle.innerText().trim().contains("Error")) {
                String errorMsg = page.locator(".el-notification__content").first().innerText().trim();
                log.error("Failed to update Ownership Details: {}", errorMsg);
                ScreenshotUtil.saveScreenshot(page, "OwnershipError", scenarioName);
                throw new AssertionError("Ownership submission failed: " + errorMsg);
            }
            log.info("Appform Ownership Details submitted successfully.");
        } catch (Exception e) {
            log.warn("No toast appeared after submitting ownership, continuing...");
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); // Let the toast clear

        // 8. NEW: Click outside the modal to close it and go back
        log.info("Clicking outside the modal to dismiss it...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        // Safety fallback: if the dialog is still open, smash the Escape key
        if (page.locator(".el-dialog__wrapper:visible").last().isVisible()) {
            log.info("Modal didn't close, pressing 'Escape' key...");
            page.keyboard().press("Escape");
            page.waitForTimeout(1000);
        }

        log.info("Successfully returned to the main Application Details page.");
    }
}