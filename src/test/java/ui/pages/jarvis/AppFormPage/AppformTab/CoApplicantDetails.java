package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class CoApplicantDetails extends BaseTest {

    private final Page page;

    // ── Timeouts ─────────────────────────────────────────────────────────────
    private static final double MODAL_OPEN_TIMEOUT_MS        = 10_000;
    private static final double ELEMENT_VISIBLE_TIMEOUT_MS   = 8_000;
    private static final double AADHAAR_SEED_TOAST_TIMEOUT_MS = 30_000;

    // ── Locator constants ─────────────────────────────────────────────────────
    private static final String LOC_CO_APPLICANT_CARD_BTN  = "xpath=//div[@id='Co-Applicant Details']//button";
    private static final String LOC_ACTIVE_MODAL           = ".el-dialog__wrapper:visible";
    private static final String LOC_ADD_AADHAAR_BTN        = "button.view-btn:has(span:has-text('ADD AADHAAR'))";
    private static final String LOC_AADHAAR_INPUT          = "input[placeholder='Enter the Aadhaar']";
    private static final String LOC_SUBMIT_AADHAAR_BTN     = ".yes-button-aadhar button";
    private static final String LOC_SEED_CONFIRM_YES_BTN   = "button.accept-btn:has(span:has-text('Yes'))";
    private static final String LOC_SUCCESS_TOAST          = ".el-notification:has-text('Aadhaar Added successfully')";
    private static final String LOC_CIRCLE_CLOSE_BTN       = "button.close-btn.is-circle";

    public CoApplicantDetails(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance must not be null.");
        this.page = page;
    }

    /** Opens the Co-Applicant modal, seeds the Aadhaar, confirms, verifies, and dismisses. */
    public void addAadhaarToCoApplicant(String aadhaarNumber, String scenarioName) {
        log.info("Starting Co-Applicant Aadhaar seeding...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        openCoApplicantModal();
        clickAddAadhaarButton();
        enterAadhaarNumber(aadhaarNumber);
        submitAadhaarEntry();
        confirmSeedAadhaar();
        verifySuccessToast(scenarioName);
        dismissAllModals();

        log.info("Co-Applicant Aadhaar seeding completed successfully.");
    }

    private void openCoApplicantModal() {
        log.info("Opening Co-Applicant Details section...");
        Locator cardBtn = page.locator(LOC_CO_APPLICANT_CARD_BTN).first();
        cardBtn.scrollIntoViewIfNeeded();
        cardBtn.click();
        page.locator(LOC_ACTIVE_MODAL).last()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(MODAL_OPEN_TIMEOUT_MS));
        log.info("Co-Applicant Details modal is open.");
    }

    private void clickAddAadhaarButton() {
        log.info("Clicking 'ADD AADHAAR' button...");
        Locator addAadhaarBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_ADD_AADHAAR_BTN).first();
        addAadhaarBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        addAadhaarBtn.click();
    }

    private void enterAadhaarNumber(String aadhaarNumber) {
        log.info("Entering Aadhaar number...");
        Locator aadhaarInput = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_AADHAAR_INPUT).first();
        aadhaarInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        aadhaarInput.fill(aadhaarNumber);
    }

    private void submitAadhaarEntry() {
        log.info("Submitting Aadhaar entry...");
        page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SUBMIT_AADHAAR_BTN).first()
                .click();
    }

    private void confirmSeedAadhaar() {
        log.info("Confirming Seed Aadhaar dialog...");
        Locator yesBtn = page.locator(LOC_ACTIVE_MODAL).last()
                .locator(LOC_SEED_CONFIRM_YES_BTN).first();
        yesBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        yesBtn.click();
    }

    /** Waits up to 30s for the Aadhaar-seeded success toast — fails test if it doesn't arrive. */
    private void verifySuccessToast(String scenarioName) {
        log.info("Waiting for 'Aadhaar Added successfully' toast (max {}s)...",
                (int) (AADHAAR_SEED_TOAST_TIMEOUT_MS / 1000));
        try {
            page.locator(LOC_SUCCESS_TOAST).first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(AADHAAR_SEED_TOAST_TIMEOUT_MS));
            log.info("Aadhaar seeded successfully — toast received.");
        } catch (Exception e) {
            ScreenshotUtil.saveScreenshot(page, "AadhaarAddError", scenarioName);
            throw new AssertionError("Co-Applicant Aadhaar seeding failed or timed out: " + e.getMessage(), e);
        }
    }

    private void dismissAllModals() {
        log.info("Dismissing all open modals...");
        Locator circleCloseBtn = page.locator(LOC_CIRCLE_CLOSE_BTN).first();
        if (circleCloseBtn.isVisible()) {
            circleCloseBtn.click();
            circleCloseBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        }

        page.mouse().click(10, 10);

        Locator remainingModal = page.locator(LOC_ACTIVE_MODAL).last();
        if (remainingModal.isVisible()) {
            log.warn("Modal still visible — pressing Escape as fallback.");
            page.keyboard().press("Escape");
            remainingModal.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(ELEMENT_VISIBLE_TIMEOUT_MS));
        }
        log.info("All modals dismissed.");
    }
}
