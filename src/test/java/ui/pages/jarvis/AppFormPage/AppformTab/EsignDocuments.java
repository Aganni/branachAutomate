package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class EsignDocuments extends BaseTest {

    private final Page page;

    public EsignDocuments(Page page) {
        this.page = page;
    }

    /** Opens the E-Sign Of Documents card. */
    public void openESignSection() {
        log.info("Opening E-Sign Of Documents section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, "E-Sign Of Documents");
    }

    /**
     * Generates documents if not yet done, opts in for offline signatures,
     * confirms the warning popup, verifies the success toast, and clicks Back.
     */
    public void optInForOfflineSignatures(String scenarioName) {
        log.info("Starting Offline Signatures flow...");
        page.waitForTimeout(1000); // allow Vue to render the checkbox table

        // XPath finds the enabled (non-disabled) offline-signatures checkbox label
        String enabledCheckboxXpath = "xpath=//label[contains(@class, 'el-checkbox') " +
                "and not(contains(@class, 'is-disabled')) " +
                "and .//span[contains(normalize-space(text()), 'Opt in for offline signatures')]]";
        Locator enabledOfflineLabel = page.locator(enabledCheckboxXpath).first();

        if (!enabledOfflineLabel.isVisible()) {
            // Click Generate if checkbox is still disabled (documents not yet created)
            Locator generateBtn = page.locator("xpath=//button[contains(@class, 'el-button') and .//span[normalize-space(text())='Generate']]").first();
            if (generateBtn.isVisible()) {
                generateBtn.click(new Locator.ClickOptions().setForce(true));
                log.info("Clicked 'Generate' — polling for document generation completion...");
            }

            // Poll up to 90s (3 × 30s) for the checkbox to become enabled
            boolean isEnabled = false;
            for (int i = 1; i <= 3 && !isEnabled; i++) {
                log.info("Poll attempt {}/3...", i);
                try {
                    enabledOfflineLabel.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE).setTimeout(30_000));
                    isEnabled = true;
                    log.info("Document generated — checkbox is now enabled.");
                } catch (Exception e) {
                    log.warn("Still generating after {}s...", i * 30);
                }
            }

            if (!isEnabled) {
                ScreenshotUtil.saveScreenshot(page, "DocGenerationTimeout", scenarioName);
                throw new AssertionError("Document generation timed out after 90s — checkbox remained disabled.");
            }
        } else {
            log.info("Documents already generated — skipping generation step.");
        }

        log.info("Clicking 'Opt in for offline signatures' checkbox...");
        // JS click on the native <input> bypasses Element UI's wrapper intercept
        enabledOfflineLabel.locator("xpath=.//input[@type='checkbox']").first().evaluate("node => node.click()");

        // Confirm the "This will save the changes" warning popup
        log.info("Waiting for warning popup...");
        Locator warningPopup = page.locator("xpath=//div[contains(@class, 'el-message-box') and .//p[contains(text(), 'This will save the changes. Continue?')]]").first();
        try {
            warningPopup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            warningPopup.locator("xpath=.//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='OK']]")
                    .first()
                    .evaluate("node => node.click()");
            log.info("Warning popup confirmed.");
        } catch (Exception e) {
            ScreenshotUtil.saveScreenshot(page, "WarningPopupTimeout", scenarioName);
            throw new AssertionError("Warning popup did not appear — checkbox click may not have registered.", e);
        }

        // Wait for the 'Saved successfully' middle toast
        Locator successToast = page.locator("xpath=//*[contains(@class, 'el-message') and contains(normalize-space(text()), 'Saved successfully')]").first();
        try {
            successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15_000));
            log.info("Success toast verified.");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            ScreenshotUtil.saveScreenshot(page, "OfflineOptInToastError", scenarioName);
            throw new AssertionError("'Saved successfully' toast did not appear.", e);
        }

        log.info("Clicking Back button...");
        Locator backBtn = page.locator("xpath=//button[contains(@class, 'el-button--text') and .//span[contains(normalize-space(text()), 'Back')]]").first();
        backBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        backBtn.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully returned to the previous page.");
    }
}