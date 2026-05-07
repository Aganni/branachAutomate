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

    public void openAppFormOwnerShipAndEdit() {
        log.info("Opening Appform Ownership Details section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        Locator ownershipCard = page.locator("button.appform-card")
                .filter(new Locator.FilterOptions().setHasText("Appform Ownership Details"))
                .first();
        ownershipCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        ownershipCard.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Clicking Edit on Appform Ownership Details...");
        Locator editBtn = page.locator("[role='dialog']:visible button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));
    }

    /**
     * Selects the Credit Approver/Manager, submits, and exits the modal.
     */
    /**
     * Selects the Credit Approver/Manager, submits, and exits the modal.
     */
    public void selectCreditApproverAndSubmit(Map<String, String> details, String scenarioName) {
        String approverEmail = details.get("UserEmail");
        log.info("Assigning Credit Approver: [{}] for scenario: [{}]", approverEmail, scenarioName);

        // 1. SCOPE TO THE VISIBLE MODAL ONLY!
        // This prevents Playwright from accidentally clicking hidden inputs in the background
        Locator activeModal = page.locator(".el-dialog__wrapper:visible").last();

        // 2. Locate the input strictly inside the active modal
        Locator approverInput = activeModal.locator("//label[contains(text(), 'Credit Manager') or contains(text(), 'Credit Approver')]/following-sibling::div//input").first();

        // 3. Click the input box
        approverInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        try {
            approverInput.click();
        } catch (Exception e) {
            log.warn("Standard click failed, forcing JavaScript click...");
            approverInput.evaluate("node => node.click()");
        }

        // CRITICAL: Give the heavy DOM half a second to render the 300+ list items
        page.waitForTimeout(1000);

        // 4. Locate the visible dropdown floating at the bottom of the HTML
        Locator activeDropdown = page.locator(".el-select-dropdown:visible").last();

        // 5. Find the exact email in the list and scroll to it internally.
        Locator optionToSelect = activeDropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(approverEmail))
                .first();

        // scrollIntoViewIfNeeded handles Element UI's internal scrollbars perfectly
        optionToSelect.scrollIntoViewIfNeeded();
        optionToSelect.click(new Locator.ClickOptions().setForce(true));
        log.info("Successfully selected email from the dropdown without typing.");

        // 6. Submit via the active Floating Action Button inside the modal
        log.info("Submitting Appform Ownership Details...");
        Locator submitBtn = activeModal.locator(".cs-fab:visible").first();
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

        // 8. Click outside the modal to close it and go back
        log.info("Clicking outside the modal to dismiss it...");
        page.mouse().click(10, 10);
        page.waitForTimeout(1000);

        // Safety fallback: if the dialog is still open, smash the Escape key
        if (activeModal.isVisible()) {
            log.info("Modal didn't close, pressing 'Escape' key...");
            page.keyboard().press("Escape");
            page.waitForTimeout(1000);
        }

        log.info("Successfully returned to the main Application Details page.");
    }
}