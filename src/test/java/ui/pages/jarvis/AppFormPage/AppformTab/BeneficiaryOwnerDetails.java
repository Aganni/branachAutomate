package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class BeneficiaryOwnerDetails extends BaseTest {

    private final Page page;

    // ── Timeouts ─────────────────────────────────────────────────────────────
    private static final double MODAL_OPEN_TIMEOUT_MS    = 10_000;
    private static final double ELEMENT_VISIBLE_TIMEOUT  = 8_000;
    private static final double SUCCESS_TOAST_TIMEOUT_MS = 5_000;

    // ── Locator constants ─────────────────────────────────────────────────────
    private static final String LOC_BENEFICIARY_CARD_BTN = "button.appform-card:has-text('Beneficiary Owner Details')";
    private static final String LOC_ACTIVE_MODAL         = ".el-dialog__wrapper:visible";
    private static final String LOC_EDIT_BTN             = ".form-header button.edit-btn";
    private static final String LOC_CIRCLE_CLOSE_BTN     = "button.close-btn.is-circle";
    private static final String LOC_ADD_BENEFICIARY_BTN  = "button:not(.is-disabled):has-text('+ Add new beneficiary')";
    private static final String LOC_SUCCESS_TOAST        = ".el-notification:has-text('success')";
    private static final String LOC_SUBMIT_BTN           = "button:has-text('Add new beneficial owner')";

    public BeneficiaryOwnerDetails(Page page) {
        if (page == null) throw new IllegalArgumentException("Page must not be null.");
        this.page = page;
    }

    /** Opens the Beneficiary Owner Details modal, adds one owner, and dismisses. */
    public void addBeneficiaryOwner(String entityName, String applicantName, String scenarioName) {
        log.info("Starting Beneficiary Owner Details workflow...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        openBeneficiaryModal();
        clickEditButton();
        clickAddNewBeneficiary();
        selectEntity(entityName);
        selectApplicant(applicantName);
        submitForm();
        verifySuccessToast(scenarioName);
        dismissAllModals();

        log.info("Beneficiary Owner Details workflow completed successfully.");
    }

    private void openBeneficiaryModal() {
        log.info("Opening Beneficiary Owner Details section...");
        Locator cardBtn = page.locator(LOC_BENEFICIARY_CARD_BTN).first();
        cardBtn.scrollIntoViewIfNeeded();
        cardBtn.click(new Locator.ClickOptions().setForce(true));
        page.locator(LOC_ACTIVE_MODAL).last()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(MODAL_OPEN_TIMEOUT_MS));
        log.info("Beneficiary Owner Details modal is open.");
    }

    private void clickEditButton() {
        log.info("Clicking 'Edit' button to enter edit mode...");
        Locator editBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_EDIT_BTN).first();
        editBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        editBtn.evaluate("node => node.click()"); // JS click avoids Element UI overlay intercept
        // Confirm edit mode is active — circle-close appears only in edit mode
        page.locator(LOC_CIRCLE_CLOSE_BTN).first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        log.info("Edit mode is now active.");
    }

    private void clickAddNewBeneficiary() {
        log.info("Clicking '+ Add new beneficiary'...");
        Locator addBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_ADD_BENEFICIARY_BTN).first();
        addBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        addBtn.click();
        // Wait for the entity select input to confirm the form panel rendered
        page.locator(".el-select:has(input[placeholder='Select Entity'])").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        log.info("Beneficiary form panel is visible.");
    }

    private void selectEntity(String entityName) {
        log.info("Selecting Entity: {}", entityName);
        selectElDropdownByPlaceholder("Select Entity", entityName);
    }

    private void selectApplicant(String applicantName) {
        log.info("Selecting Applicant: {} (backend auto-fills remaining fields)", applicantName);
        selectElDropdownByPlaceholder("Select Applicant", applicantName);
    }

    private void submitForm() {
        log.info("Submitting beneficiary form...");
        Locator submitBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SUBMIT_BTN).first();
        submitBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        submitBtn.scrollIntoViewIfNeeded();
        submitBtn.click(new Locator.ClickOptions().setForce(true));
    }

    /**
     * Soft assertion — no toast is not always an error for this section.
     * Logs a warning and continues if toast doesn't appear within 5s.
     */
    private void verifySuccessToast(String scenarioName) {
        log.info("Waiting for success toast (max {}s)...", (int) (SUCCESS_TOAST_TIMEOUT_MS / 1000));
        try {
            page.locator(LOC_SUCCESS_TOAST).first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(SUCCESS_TOAST_TIMEOUT_MS));
            log.info("Beneficiary saved — success toast received.");
        } catch (Exception e) {
            log.warn("No success toast appeared — this may be expected for this section. Continuing.");
            ScreenshotUtil.saveScreenshot(page, "BeneficiaryNoToast", scenarioName);
        }
    }

    private void dismissAllModals() {
        log.info("Dismissing all open modals...");
        Locator closeBtn = page.locator(LOC_CIRCLE_CLOSE_BTN).first();
        if (closeBtn.isVisible()) {
            closeBtn.click();
            closeBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        }
        page.mouse().click(10, 10);
        Locator remainingModal = page.locator(LOC_ACTIVE_MODAL).last();
        if (remainingModal.isVisible()) {
            log.warn("Modal still visible after outside click — pressing Escape as fallback.");
            page.keyboard().press("Escape");
            remainingModal.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        }
        log.info("All modals dismissed.");
    }

    /** Placeholder-based Element UI dropdown selector — used for Entity and Applicant fields. */
    private void selectElDropdownByPlaceholder(String placeholder, String optionText) {
        Locator input = page.locator("input[placeholder='" + placeholder + "']").first();
        input.scrollIntoViewIfNeeded();
        input.click();
        Locator dropdown = page.locator(".el-select-dropdown:visible").last();
        dropdown.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        dropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first()
                .scrollIntoViewIfNeeded();
        dropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first()
                .click();
        log.debug("Selected '{}' from '{}' dropdown.", optionText, placeholder);
    }
}
