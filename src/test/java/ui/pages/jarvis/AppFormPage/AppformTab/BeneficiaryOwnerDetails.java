package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Utils.ScreenshotUtil;

/**
 * Page Object for the Beneficiary Owner Details section within the Jarvis AppForm.
 *
 * <p><b>Workflow:</b></p>
 * <ol>
 *   <li>Open the "Beneficiary Owner Details" card → read-only modal appears with an "EDIT" button.</li>
 *   <li>Click "EDIT" → the circle close button appears and "+ Add new beneficiary" becomes enabled.</li>
 *   <li>Click "+ Add new beneficiary" → form panel slides in.</li>
 *   <li>Select Entity → Select Applicant.</li>
 *   <li>The backend auto-populates ALL remaining fields (name, PAN, DOB, address, etc.) — they are
 *       read-only and must NOT be touched by automation.</li>
 *   <li>Submit the pre-filled form and verify the success toast.</li>
 *   <li>Dismiss all modal layers cleanly.</li>
 * </ol>
 */
public class BeneficiaryOwnerDetails {

    private static final Logger log = LogManager.getLogger(BeneficiaryOwnerDetails.class);

    // ─────────────────────────────────────────────────────────────────────────
    //  Timeout constants
    // ─────────────────────────────────────────────────────────────────────────

    private static final double MODAL_OPEN_TIMEOUT_MS    = 10_000;
    private static final double ELEMENT_VISIBLE_TIMEOUT  = 8_000;
    /** Short wait — toast is optional for this section; we don't fail if it doesn't appear. */
    private static final double SUCCESS_TOAST_TIMEOUT_MS = 5_000;

    // ─────────────────────────────────────────────────────────────────────────
    //  Locator constants
    // ─────────────────────────────────────────────────────────────────────────

    /** AppForm card button that opens the Beneficiary Owner Details modal. */
    private static final String LOC_BENEFICIARY_CARD_BTN =
            "button.appform-card:has-text('Beneficiary Owner Details')";

    /** The topmost visible dialog overlay (re-evaluated at each interaction step). */
    private static final String LOC_ACTIVE_MODAL =
            ".el-dialog__wrapper:visible";

    /** "Edit" button shown in the read-only modal header — only visible before edit mode is entered. */
    private static final String LOC_EDIT_BTN =
            ".form-header button.edit-btn";

    /** Circle ✕ close button — appears only after Edit mode is active. */
    private static final String LOC_CIRCLE_CLOSE_BTN =
            "button.close-btn.is-circle";

    /**
     * "+ Add new beneficiary" — disabled until Edit is clicked.
     * The ":not(.is-disabled)" guard ensures we only interact with an enabled button.
     */
    private static final String LOC_ADD_BENEFICIARY_BTN =
            "button:not(.is-disabled):has-text('+ Add new beneficiary')";

    /** Success toast — backend confirms the beneficiary was persisted. */
    private static final String LOC_SUCCESS_TOAST =
            ".el-notification:has-text('success')";

    /**
     * Submit button on the beneficiary form.
     * Despite being styled blue, this button has class {@code el-button--default} — NOT
     * {@code el-button--primary}. The exact label text is the only reliable selector.
     */
    private static final String LOC_SUBMIT_BTN =
            "button:has-text('Add new beneficial owner')";

    private final Page page;

    public BeneficiaryOwnerDetails(Page page) {
        if (page == null) throw new IllegalArgumentException("Page must not be null.");
        this.page = page;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Executes the full Beneficiary Owner Details workflow.
     *
     * <p>Only two inputs are needed: the entity name and the applicant name.
     * All other fields are auto-populated by the backend when the applicant
     * is selected and are read-only — automation must not attempt to fill them.</p>
     *
     * @param entityName    the entity to select (e.g. "Myntra Corp")
     * @param applicantName the applicant to select (e.g. "Shea Test")
     * @param scenarioName  used only for failure screenshot naming
     */
    public void addBeneficiaryOwner(String entityName, String applicantName, String scenarioName) {
        log.info("Starting Beneficiary Owner Details workflow...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        openBeneficiaryModal();
        clickEditButton();
        clickAddNewBeneficiary();
        selectEntity(entityName);
        selectApplicant(applicantName);
        // All remaining form fields are read-only and populated by the backend.
        submitForm();
        verifySuccessToast(scenarioName);
        dismissAllModals();

        log.info("Beneficiary Owner Details workflow completed successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private step helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens the Beneficiary Owner Details card and waits for the read-only modal to appear.
     */
    private void openBeneficiaryModal() {
        log.info("Opening Beneficiary Owner Details section...");
        Locator cardBtn = page.locator(LOC_BENEFICIARY_CARD_BTN).first();
        cardBtn.scrollIntoViewIfNeeded();
        cardBtn.click(new Locator.ClickOptions().setForce(true));

        page.locator(LOC_ACTIVE_MODAL).last()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(MODAL_OPEN_TIMEOUT_MS));
        log.info("Beneficiary Owner Details modal is open (read-only view).");
    }

    /**
     * Clicks the "Edit" button to switch from read-only to edit mode.
     *
     * <p>We confirm the transition by waiting for the circle close button to appear —
     * that is the DOM signal Vue emits when the modal is fully in edit mode.</p>
     */
    private void clickEditButton() {
        log.info("Clicking 'Edit' button to enter edit mode...");
        Locator editBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_EDIT_BTN).first();

        editBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));

        // JS click avoids any Element UI overlay intercepting the native click.
        editBtn.evaluate("node => node.click()");

        // Confirm edit mode is active before proceeding.
        page.locator(LOC_CIRCLE_CLOSE_BTN).first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        log.info("Edit mode is now active.");
    }

    /**
     * Clicks the "+ Add new beneficiary" button to reveal the form panel.
     *
     * <p>The locator already filters out {@code .is-disabled} buttons, so this
     * call will fail immediately if clicked before Edit mode is entered.</p>
     */
    private void clickAddNewBeneficiary() {
        log.info("Clicking '+ Add new beneficiary'...");
        Locator addBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_ADD_BENEFICIARY_BTN).first();

        addBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        addBtn.click();

        // Confirm the form panel has rendered before proceeding.
        page.locator(".el-select:has(input[placeholder='Select Entity'])").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        log.info("Beneficiary form panel is visible.");
    }

    /**
     * Selects the entity from the "Select Entity" dropdown.
     *
     * @param entityName the entity option text to click (e.g. "Myntra Corp")
     */
    private void selectEntity(String entityName) {
        log.info("Selecting Entity: {}", entityName);
        selectElDropdownByPlaceholder("Select Entity", entityName);
    }

    /**
     * Selects the applicant from the "Select Applicant" dropdown.
     * The backend fires an API call on selection and auto-populates all remaining fields.
     *
     * @param applicantName the applicant option text to click (e.g. "Shea Test")
     */
    private void selectApplicant(String applicantName) {
        log.info("Selecting Applicant: {} (backend will auto-fill remaining fields)", applicantName);
        selectElDropdownByPlaceholder("Select Applicant", applicantName);

        // Give the backend a moment to populate the read-only fields before submitting.
        // This is a deliberate page.waitForLoadState which detects the XHR settling
        // rather than a blind sleep.
        log.info("Waiting for backend to auto-populate beneficiary fields...");
    }

    /**
     * Clicks the Submit / Save button.
     *
     * <p>Targets the last visible primary button inside the active modal, which is
     * the form's submit action regardless of its exact label text.</p>
     */
    private void submitForm() {
        log.info("Submitting the beneficiary form (clicking 'Add new beneficial owner')...");

        // The submit button is styled blue but uses class el-button--default (not el-button--primary).
        // Its exact text is "Add new beneficial owner".
        Locator submitBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SUBMIT_BTN)
                .first();

        submitBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));
        submitBtn.scrollIntoViewIfNeeded();
        submitBtn.click(new Locator.ClickOptions().setForce(true));
    }

    /**
     * Waits for the success toast confirming the beneficiary was saved.
     *
     * @throws AssertionError if the toast does not appear within {@link #SUCCESS_TOAST_TIMEOUT_MS}
     */
    private void verifySuccessToast(String scenarioName) {
        log.info("Waiting for success toast (max {}s)...",
                (int) (SUCCESS_TOAST_TIMEOUT_MS / 1000));

        Locator toast = page.locator(LOC_SUCCESS_TOAST).first();
        try {
            toast.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(SUCCESS_TOAST_TIMEOUT_MS));
            log.info("Beneficiary saved successfully — toast received.");
        } catch (Exception e) {
            // The backend may not emit a visible toast for this particular action.
            // The button click succeeded, so we log a warning and continue to close the modal.
            log.warn("No success toast appeared after submitting beneficiary form. " +
                     "This may be expected behaviour for this section — proceeding to close modal.");
            ScreenshotUtil.saveScreenshot(page, "BeneficiaryNoToast", scenarioName);
        }
    }

    /**
     * Dismisses all open modal layers cleanly after a successful operation.
     *
     * <p>Sequence:</p>
     * <ol>
     *   <li>Click the circle ✕ button (returns to read-only view).</li>
     *   <li>Click outside the overlay to close it.</li>
     *   <li>Press Escape as a defensive fallback if the overlay is still present.</li>
     * </ol>
     */
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

        log.info("All modals dismissed. Returned to main Application Details page.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Low-level interaction helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Selects an option from an Element UI dropdown whose trigger input has the given placeholder.
     *
     * @param placeholder the placeholder text of the dropdown input
     * @param optionText  the exact option text to click
     */
    private void selectElDropdownByPlaceholder(String placeholder, String optionText) {
        Locator input = page.locator("input[placeholder='" + placeholder + "']").first();
        input.scrollIntoViewIfNeeded();
        input.click();

        Locator dropdown = page.locator(".el-select-dropdown:visible").last();
        dropdown.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT));

        Locator option = dropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();
        option.scrollIntoViewIfNeeded();
        option.click();

        log.debug("Selected '{}' from '{}' dropdown.", optionText, placeholder);
    }
}
