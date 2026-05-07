package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Utils.ScreenshotUtil;
import ui.pages.jarvis.AppFormPage.AppformTab.AppFormTabNavigator;

/**
 * Page Object for the Co-Applicant Details section within the Jarvis AppForm.
 *
 * <p><b>Design Decisions:</b>
 * <ul>
 *   <li>Does NOT extend {@code BaseTest}. A Page Object's only dependency should be the
 *       {@code Page} instance it operates on. Extending a test base class couples the
 *       page layer to the test lifecycle (a SRP violation).</li>
 *   <li>All Playwright selectors are defined as private constants (the "Locator Repository"
 *       pattern). This means when the UI changes, you update ONE string, not hunt through
 *       method bodies.</li>
 *   <li>All waits are dynamic (element-state based). There are zero calls to
 *       {@code waitForTimeout()} — those are hard sleeps in disguise and cause flakiness.</li>
 *   <li>The public action method is split into focused private helpers, each doing one thing.
 *       This enables future unit-testing of individual interaction sub-steps.</li>
 * </ul>
 */
public class CoApplicantDetails {

    private static final Logger log = LogManager.getLogger(CoApplicantDetails.class);

    // ─────────────────────────────────────────────────────────────────────────
    //  Timeout Constants (centralised so they are easy to tune)
    // ─────────────────────────────────────────────────────────────────────────

    /** Timeout for primary modal animations to complete after a section card click. */
    private static final double MODAL_OPEN_TIMEOUT_MS      = 10_000;

    /** Timeout for inner-modal elements (e.g., Aadhaar input, Seed confirmation). */
    private static final double ELEMENT_VISIBLE_TIMEOUT_MS = 8_000;

    /**
     * Maximum time to wait for the backend Aadhaar seeding service to respond.
     * This is deliberately long (30 s) because the backend call is asynchronous.
     * Playwright exits this wait IMMEDIATELY once the toast appears — it does NOT
     * sleep for the full duration.
     */
    private static final double AADHAAR_SEED_TOAST_TIMEOUT_MS = 30_000;

    // ─────────────────────────────────────────────────────────────────────────
    //  Locator Constants  (change the UI? change ONE string here, not everywhere)
    // ─────────────────────────────────────────────────────────────────────────

    /** Opens the Co-Applicant Details accordion / card on the AppForm. */
    private static final String LOC_CO_APPLICANT_CARD_BTN =
            "xpath=//div[@id='Co-Applicant Details']//button";

    /** The topmost visible dialog overlay — reused at every modal layer. */
    private static final String LOC_ACTIVE_MODAL =
            ".el-dialog__wrapper:visible";

    /** "ADD AADHAAR" trigger button inside the Co-Applicant modal. */
    private static final String LOC_ADD_AADHAAR_BTN =
            "button.view-btn:has(span:has-text('ADD AADHAAR'))";

    /** Text input inside the "Add Aadhaar" inner modal. */
    private static final String LOC_AADHAAR_INPUT =
            "input[placeholder='Enter the Aadhaar']";

    /** Submit button inside the "Add Aadhaar" inner modal. */
    private static final String LOC_SUBMIT_AADHAAR_BTN =
            ".yes-button-aadhar button";

    /** "Yes" acceptance button on the Seed Aadhaar confirmation dialog. */
    private static final String LOC_SEED_CONFIRM_YES_BTN =
            "button.accept-btn:has(span:has-text('Yes'))";

    /**
     * Success toast notification text. Uses the parent .el-notification so the locator
     * is resilient regardless of whether the text lives in the title or the content area.
     */
    private static final String LOC_SUCCESS_TOAST =
            ".el-notification:has-text('Aadhaar Added successfully')";

    /** Circle ✕ close button present on every modal header. */
    private static final String LOC_CIRCLE_CLOSE_BTN =
            "button.close-btn.is-circle";

    // ─────────────────────────────────────────────────────────────────────────
    //  State
    // ─────────────────────────────────────────────────────────────────────────

    private final Page page;

    /**
     * Constructs the page object with the Playwright {@code Page} it will drive.
     *
     * @param page the active Playwright {@link Page}; must not be {@code null}
     */
    public CoApplicantDetails(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page instance must not be null.");
        }
        this.page = page;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Executes the full Co-Applicant Aadhaar seeding workflow:
     * <ol>
     *   <li>Opens the Co-Applicant Details modal.</li>
     *   <li>Clicks "ADD AADHAAR" and enters the Aadhaar number.</li>
     *   <li>Submits and confirms the Seed Aadhaar dialog.</li>
     *   <li>Dynamically waits for the backend success toast (up to 30 s).</li>
     *   <li>Cleanly dismisses all modals.</li>
     * </ol>
     *
     * @param aadhaarNumber  the 12-digit Aadhaar number to seed
     * @param scenarioName   the Cucumber scenario name, used only for screenshot naming on failure
     * @throws AssertionError if the success toast is not received within the timeout
     */
    public void addAadhaarToCoApplicant(String aadhaarNumber, String scenarioName) {
        log.info("Starting Co-Applicant Aadhaar seeding for Aadhaar: [REDACTED]");

        openCoApplicantModal();
        clickAddAadhaarButton();
        enterAadhaarNumber(aadhaarNumber);
        submitAadhaarEntry();
        confirmSeedAadhaar();
        verifySuccessToast(scenarioName);
        dismissAllModals();

        log.info("Co-Applicant Aadhaar seeding completed successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private Step Helpers  (one method = one responsibility)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens the Co-Applicant Details section and waits until the modal is fully rendered.
     *
     * <p>Using {@code scrollIntoViewIfNeeded()} before clicking prevents failures when the
     * card is below the viewport fold — a common source of flakiness on longer AppForms.</p>
     */
    private void openCoApplicantModal() {
        log.info("Opening Co-Applicant Details section...");
        Locator cardBtn = page.locator(LOC_CO_APPLICANT_CARD_BTN).first();
        cardBtn.scrollIntoViewIfNeeded();
        cardBtn.click();

        // Wait for the dialog overlay to be visible — no arbitrary sleep needed.
        page.locator(LOC_ACTIVE_MODAL).last()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(MODAL_OPEN_TIMEOUT_MS));
        log.info("Co-Applicant Details modal is open.");
    }

    /**
     * Locates and clicks the "ADD AADHAAR" button inside the Co-Applicant modal.
     *
     * <p>Re-querying the active modal at each step (rather than caching it from the
     * previous step) prevents stale-element failures caused by Vue re-rendering the DOM
     * between interactions.</p>
     */
    private void clickAddAadhaarButton() {
        log.info("Clicking 'ADD AADHAAR' button...");
        Locator addAadhaarBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_ADD_AADHAAR_BTN).first();

        addAadhaarBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        addAadhaarBtn.click();
    }

    /**
     * Waits for the Aadhaar input field to appear in the inner modal, then fills it.
     *
     * <p>Using {@code fill()} rather than {@code type()} or {@code pressSequentially()}
     * because the input does not rely on Vue v-model keystroke reactivity — it is a simple
     * controlled input that reads its final value on form submission.</p>
     *
     * @param aadhaarNumber the value to populate
     */
    private void enterAadhaarNumber(String aadhaarNumber) {
        log.info("Entering Aadhaar number into input field...");
        Locator aadhaarInput = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_AADHAAR_INPUT).first();

        aadhaarInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        aadhaarInput.fill(aadhaarNumber);
    }

    /**
     * Clicks the Submit button on the "Add Aadhaar" inner modal.
     *
     * <p>The submit button is scoped to the active modal layer to avoid accidentally
     * clicking a submit button from a previously rendered (but now hidden) modal.</p>
     */
    private void submitAadhaarEntry() {
        log.info("Clicking 'Submit' on the Add Aadhaar modal...");
        page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SUBMIT_AADHAAR_BTN).first()
                .click();
    }

    /**
     * Waits for the Seed Aadhaar confirmation dialog and clicks "Yes".
     *
     * <p>This dialog is a <em>new</em> modal layer that appears on top of the existing one.
     * We always query {@code .last()} on the modal wrapper to get the topmost active dialog,
     * which is the most reliable way to handle stacked Element UI dialogs.</p>
     */
    private void confirmSeedAadhaar() {
        log.info("Confirming Seed Aadhaar dialog...");
        Locator yesBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SEED_CONFIRM_YES_BTN).first();

        yesBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        yesBtn.click();
    }

    /**
     * Dynamically waits for the backend Aadhaar-seeding success toast notification.
     *
     * <p><b>Why 30 seconds?</b> The Aadhaar seeding is an async backend call. The timeout
     * here is a <em>maximum ceiling</em>, not a sleep — Playwright polls the DOM and returns
     * the instant the toast appears (typically 2–5 s). If the backend is degraded, the 30 s
     * window prevents a premature test failure on a still-healthy run.</p>
     *
     * @param scenarioName used exclusively for screenshot naming; not exposed outside this class
     * @throws AssertionError with a clear message if the toast does not appear in time
     */
    private void verifySuccessToast(String scenarioName) {
        log.info("Dynamically waiting for 'Aadhaar Added successfully' toast (max {}s)...",
                (int) (AADHAAR_SEED_TOAST_TIMEOUT_MS / 1000));

        Locator successToast = page.locator(LOC_SUCCESS_TOAST).first();

        try {
            successToast.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(AADHAAR_SEED_TOAST_TIMEOUT_MS));
            log.info("Success! 'Aadhaar Added successfully' toast received.");
        } catch (Exception e) {
            log.error("Aadhaar seeding failed — success toast did not appear within {}s. "
                    + "Check backend service logs.", (int) (AADHAAR_SEED_TOAST_TIMEOUT_MS / 1000));
            ScreenshotUtil.saveScreenshot(page, "AadhaarAddError", scenarioName);

            // Wrap with a meaningful AssertionError so Cucumber marks the step as FAILED,
            // not just as ERRORED, giving a cleaner Extent Report entry.
            throw new AssertionError("Co-Applicant Aadhaar seeding failed or timed out. "
                    + "Underlying cause: " + e.getMessage(), e);
        }
    }

    /**
     * Dismisses all open dialog layers cleanly after a successful operation.
     *
     * <p>The sequence is:
     * <ol>
     *   <li>Click the circle ✕ close button (transitions from "submit-mode" to "read-only EDIT mode").</li>
     *   <li>Click outside the modal to dismiss the read-only overlay.</li>
     *   <li>If the overlay is still present (defensive), press Escape as a final fallback.</li>
     * </ol>
     * Each step waits for the previous one to settle before continuing — no arbitrary sleeps.
     * </p>
     */
    private void dismissAllModals() {
        log.info("Dismissing all open modals...");

        // Step 1: Click the circle close button (X) to exit the form-edit view.
        Locator circleCloseBtn = page.locator(LOC_CIRCLE_CLOSE_BTN).first();
        if (circleCloseBtn.isVisible()) {
            circleCloseBtn.click();
            log.debug("Clicked circle close button.");

            // Wait for the button itself to detach before proceeding — this confirms
            // the modal has transitioned to its read-only state.
            circleCloseBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        }

        // Step 2: Click outside to dismiss the read-only modal overlay.
        page.mouse().click(10, 10);

        // Step 3: Defensive fallback — press Escape if any modal layer is still visible.
        Locator remainingModal = page.locator(LOC_ACTIVE_MODAL).last();
        if (remainingModal.isVisible()) {
            log.warn("Modal still visible after outside click — pressing Escape as fallback.");
            page.keyboard().press("Escape");

            // Wait for it to disappear before handing control back to the step definition.
            remainingModal.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        }

        log.info("All modals dismissed. Returned to main Application Details page.");
    }
}
