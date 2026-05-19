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

    /** Opens the Appform Ownership Details card and enters edit mode. */
    public void openAppFormOwnerShipAndEdit() {
        log.info("Opening Appform Ownership Details section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, "Appform Ownership Details");

        log.info("Clicking Edit on Appform Ownership Details...");
        Locator editBtn = page.locator("[role='dialog']:visible button.edit-btn").first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));
    }

    /** Selects the Credit Approver from the dropdown, submits, and closes the modal. */
    public void selectCreditApproverAndSubmit(Map<String, String> details, String scenarioName) {
        String approverEmail = details.get("UserEmail");
        log.info("Assigning Credit Approver: [{}]", approverEmail);

        // Scope all interactions to the currently visible modal to avoid hidden background inputs
        Locator activeModal = page.locator(AppFormUtils.LOC_ACTIVE_MODAL).last();

        Locator approverInput = activeModal.locator(
                "xpath=//label[contains(text(), 'Credit Manager') or contains(text(), 'Credit Approver')]/following-sibling::div//input"
        ).first();
        approverInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        try {
            approverInput.click();
        } catch (Exception e) {
            log.warn("Standard click failed — forcing JS click on approver input.");
            approverInput.evaluate("node => node.click()");
        }

        page.waitForTimeout(1000); // allow the large dropdown list (~300 items) to render

        // Select the email option from the visible dropdown
        page.locator(".el-select-dropdown:visible").last()
                .locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(approverEmail))
                .first()
                .scrollIntoViewIfNeeded();
        page.locator(".el-select-dropdown:visible").last()
                .locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(approverEmail))
                .first()
                .click(new Locator.ClickOptions().setForce(true));
        log.info("Credit approver selected.");

        log.info("Submitting Appform Ownership Details...");
        activeModal.locator(".cs-fab:visible").first().click(new Locator.ClickOptions().setForce(true));

        AppFormUtils.verifyToast(page, "OwnershipDetails", scenarioName);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        AppFormUtils.dismissModal(page, activeModal);
        log.info("Successfully returned to the main Application Details page.");
    }
}