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

    /**
     * Opens the E-Sign Of Documents section.
     */
    public void openESignSection() {
        log.info("Opening E-Sign Of Documents section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);

        Locator eSignCard = page.locator("button.appform-card")
                .filter(new Locator.FilterOptions().setHasText("E-Sign Of Documents"))
                .first();
        eSignCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        eSignCard.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Checks if document is generated, waits if needed, opts in for offline signatures,
     * handles the warning, and returns back.
     */
    public void optInForOfflineSignatures(String scenarioName) {
        log.info("Starting Offline Signatures flow...");

        // Give Vue a brief moment to render the table DOM
        page.waitForTimeout(1000);

        // Strict XPath: Looks for the label that DOES NOT have the 'is-disabled' class
        String enabledCheckboxXpath = "//label[contains(@class, 'el-checkbox') and not(contains(@class, 'is-disabled')) and .//span[contains(normalize-space(text()), 'Opt in for offline signatures')]]";
        Locator enabledOfflineLabel = page.locator(enabledCheckboxXpath).first();

        // --- 1. SMART GENERATION LOGIC ---
        // If the checkbox is not already enabled, we need to click Generate and wait
        if (!enabledOfflineLabel.isVisible()) {
            log.info("Document not generated yet. Looking for 'Generate' button...");

            Locator generateBtn = page.locator("xpath=//button[contains(@class, 'el-button') and .//span[normalize-space(text())='Generate']]").first();
            if (generateBtn.isVisible()) {
                generateBtn.click(new Locator.ClickOptions().setForce(true));
                log.info("Clicked 'Generate' button. Starting polling...");
            }

            // POLLING LOGIC (3 loops of 30 seconds)
            boolean isEnabled = false;
            for (int i = 1; i <= 3; i++) {
                log.info("Polling attempt {}/3: Waiting up to 30 seconds for document generation to complete...", i);
                try {
                    enabledOfflineLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
                    isEnabled = true;
                    log.info("Document generated! Checkbox is now enabled.");
                    break;
                } catch (Exception e) {
                    log.warn("Server still processing. Checkbox not enabled after {} seconds...", (i * 30));
                }
            }

            if (!isEnabled) {
                log.error("Backend generation timed out after 90 seconds.");
                ScreenshotUtil.saveScreenshot(page, "DocGenerationTimeout", scenarioName);
                throw new AssertionError("Document generation timed out. 'Opt in for offline signatures' checkbox remained disabled.");
            }
        } else {
            log.info("Document was already generated. Skipping Generation step.");
        }

        // --- 2. CLICK THE CHECKBOX SAFELY ---
        log.info("Clicking the 'Opt in for offline signatures' checkbox...");

        // CRITICAL FIX: Target the actual native <input> element and use JS to force the click
        Locator nativeCheckbox = enabledOfflineLabel.locator("xpath=.//input[@type='checkbox']").first();
        nativeCheckbox.evaluate("node => node.click()");

        // --- 3. HANDLE WARNING POPUP ---
        log.info("Waiting for Warning popup...");
        Locator warningPopup = page.locator("xpath=//div[contains(@class, 'el-message-box') and .//p[contains(text(), 'This will save the changes. Continue?')]]").first();

        try {
            warningPopup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            log.info("Warning popup appeared. Clicking 'OK'...");

            Locator okBtn = warningPopup.locator("xpath=.//button[contains(@class, 'el-button--primary') and .//span[normalize-space(text())='OK']]").first();
            okBtn.evaluate("node => node.click()"); // JS click for maximum reliability
        } catch (Exception e) {
            log.error("Warning popup failed to appear. The checkbox click likely did not register.");
            ScreenshotUtil.saveScreenshot(page, "WarningPopupTimeout", scenarioName);
            throw new AssertionError("Failed to opt-in for offline signatures. Warning popup never appeared.", e);
        }

        // --- 4. VERIFY SUCCESS TOAST ---
        log.info("Waiting for 'Saved successfully' middle toast to appear...");
        Locator successToast = page.locator("xpath=//*[contains(@class, 'el-message') and contains(normalize-space(text()), 'Saved successfully')]").first();

        try {
            successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            log.info("Success toast verified!");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.error("Success toast 'Saved successfully' did not appear.");
            ScreenshotUtil.saveScreenshot(page, "OfflineOptInToastError", scenarioName);
            throw new AssertionError("Failed to save changes. Success toast did not appear.", e);
        }

        // --- 5. CLICK BACK BUTTON ---
        log.info("Clicking the 'Back' button...");
        Locator backBtn = page.locator("xpath=//button[contains(@class, 'el-button--text') and .//span[contains(normalize-space(text()), 'Back')]]").first();
        backBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        backBtn.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully returned to the previous page.");
    }
}