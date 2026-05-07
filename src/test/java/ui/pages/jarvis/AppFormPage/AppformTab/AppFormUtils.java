package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Utils.ScreenshotUtil;

/**
 * Shared static utilities for AppForm page-object classes.
 * Centralises the three patterns duplicated across every section:
 * toast verification, modal dismissal, and appform-card opening.
 */
public final class AppFormUtils {

    private static final Logger log = LogManager.getLogger(AppFormUtils.class);

    // ── Timeouts ────────────────────────────────────────────────────────────
    static final double TOAST_TIMEOUT_MS   = 8_000;
    static final double VISIBLE_TIMEOUT_MS = 5_000;

    // ── Shared locator fragments ─────────────────────────────────────────────
    static final String LOC_TOAST_TITLE   = ".el-notification__title";
    static final String LOC_TOAST_CONTENT = ".el-notification__content";
    static final String LOC_ACTIVE_MODAL  = ".el-dialog__wrapper:visible";
    static final String LOC_APPFORM_CARD  = "button.appform-card";

    /**
     * Waits for a notification toast and fails the test if an Error toast appears.
     * Logs a warning and continues if no toast appears within the timeout.
     *
     * @param page         active Playwright page
     * @param screenshotId prefix for the failure screenshot filename
     * @param scenarioName folder name used by {@link ScreenshotUtil}
     */
    public static void verifyToast(Page page, String screenshotId, String scenarioName) {
        Locator toastTitle = page.locator(LOC_TOAST_TITLE).first();
        try {
            toastTitle.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(TOAST_TIMEOUT_MS));

            String title = toastTitle.innerText().trim();
            if (title.contains("Error")) {
                Locator content = page.locator(LOC_TOAST_CONTENT).first();
                String msg = content.isVisible() ? content.innerText().trim() : "Unknown error";
                log.error("Submission failed — toast error: {}", msg);
                ScreenshotUtil.saveScreenshot(page, screenshotId + "Error", scenarioName);
                throw new AssertionError("Form submission failed: " + msg);
            }
            log.info("Submission confirmed via toast: {}", title);

        } catch (AssertionError e) {
            throw e; // re-throw assertion as-is
        } catch (Exception e) {
            // No toast is not always fatal — caller decides
            log.warn("No toast appeared within {}ms for [{}]. Continuing.", (int) TOAST_TIMEOUT_MS, screenshotId);
        }
    }

    /**
     * Dismisses the active modal: click outside (10,10) first, then Escape as fallback.
     *
     * @param page        active Playwright page
     * @param activeModal a scoped locator to the visible dialog (used for the visibility check)
     */
    public static void dismissModal(Page page, Locator activeModal) {
        page.mouse().click(10, 10);
        page.waitForTimeout(800);
        if (activeModal.isVisible()) {
            page.keyboard().press("Escape");
            page.waitForTimeout(800);
        }
        log.info("Modal dismissed.");
    }

    /**
     * Opens the AppForm card that matches {@code cardText} and waits for the dialog to appear.
     *
     * @param page     active Playwright page
     * @param cardText visible label of the card button (e.g. "Bank Details")
     */
    public static void openAppFormCard(Page page, String cardText) {
        Locator card = page.locator(LOC_APPFORM_CARD)
                .filter(new Locator.FilterOptions().setHasText(cardText))
                .first();
        card.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        card.scrollIntoViewIfNeeded();
        card.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Selects an option from a label-anchored Element UI dropdown.
     * Uses JS click on the input to bypass overlay intercepts.
     *
     * @param page       active Playwright page
     * @param labelText  full label text (XPath {@code contains} match)
     * @param optionText exact option text to click in the dropdown list
     */
    public static void selectDropdown(Page page, String labelText, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, labelText);
        Locator input = page.locator(
                "xpath=//label[contains(normalize-space(text()),'" + labelText + "')]/following-sibling::div//input"
        ).first();
        input.scrollIntoViewIfNeeded();
        try {
            input.click(new Locator.ClickOptions().setTimeout(3_000));
        } catch (Exception e) {
            input.evaluate("node => node.click()"); // fallback JS click
        }

        Locator dropdown = page.locator(".el-select-dropdown:visible").last();
        try {
            dropdown.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(VISIBLE_TIMEOUT_MS));
        } catch (Exception e) {
            throw new RuntimeException("Dropdown for '" + labelText + "' failed to open.", e);
        }

        Locator option = dropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();
        option.scrollIntoViewIfNeeded();
        option.click(new Locator.ClickOptions().setForce(true));
        log.info("Selected '{}'.", optionText);
        page.waitForTimeout(300);
    }
}
