package ui.pages.jarvis.AppFormPage.VerificationTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class VerificationTab extends BaseTest {

    private final Page page;

    public VerificationTab(Page page) {
        this.page = page;
    }

    // Finds the Verification tab precisely
    private static final String VERIFICATION_TAB = "//p[@class='tab-item-title' and normalize-space()='Verification']";

    // Finds the specific Edit button that belongs ONLY to the Uan/Udyam section
    private static final String UDYAM_EDIT_BTN = "//label[contains(text(),'Uan/Udyam id')]/ancestor::div[contains(@class,'verificationEntityDetail')]//button[contains(@class,'kycEditBtn')]";

    // Finds the Resolve button strictly inside the "Edit KYC" modal
    private static final String EDIT_KYC_RESOLVE_BTN = "//div[@role='dialog' and .//*[normalize-space(text())='Edit KYC']]//div[contains(@class, 'el-dialog__footer')]//button[contains(@class,'el-button--primary')]";

    // Finds the Resolve button strictly inside the "Resolve Kyc" confirmation modal
    private static final String CONFIRM_RESOLVE_BTN = "//div[@role='dialog' and @aria-label='Resolve Kyc']//div[contains(@class, 'el-dialog__footer')]//button[contains(@class,'el-button--primary')]";


    /**
     * Navigates to the Verification tab and forces a page refresh to ensure data is fully synced.
     */
    public void navigateToVerificationTab() {
        log.info("Navigating to Verification tab...");

        Locator tab = getPage().locator(VERIFICATION_TAB).first();
        tab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        tab.click(new Locator.ClickOptions().setForce(true));

        // --- CRITICAL FIX: Wait for the URL to actually change to the Verification route ---
        log.info("Waiting for URL to route to /verification...");
        getPage().waitForURL("**/verification*");

        // Wait for the initial routing and network calls to finish
        getPage().waitForLoadState(LoadState.NETWORKIDLE);

        // --- ADDED REFRESH LOGIC ---
        log.info("Refreshing the Verification page to ensure backend data and event listeners are fully loaded...");
        getPage().reload(new Page.ReloadOptions().setTimeout(60000));

        // Wait for the network to settle again after the refresh
        getPage().waitForLoadState(LoadState.NETWORKIDLE);

        // A small 1-second buffer for Element UI components to fully render after the network stops
        getPage().waitForTimeout(1000);

        log.info("Verification Page successfully reloaded and network is idle.");
    }

    /**
     * Clicks Edit on the Udyam ID, then clicks Resolve on the first popup, 
     * and Resolve again on the confirmation popup.
     */
    public void resolveUdyamKyc(String scenarioName) {
        log.info("Starting Udyam KYC Resolution flow...");

        // 1. Click the Edit button next to Udyam ID
        Locator editBtn = page.locator(UDYAM_EDIT_BTN).first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Edit on Udyam KYC.");
        page.waitForTimeout(3000);

        // 2. Wait for the 'Edit KYC' modal and click Resolve
        Locator firstResolveBtn = page.locator(EDIT_KYC_RESOLVE_BTN).first();
        firstResolveBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        firstResolveBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Resolve on the Edit KYC modal.");

        page.waitForTimeout(3000);
        // 3. Wait for the 'Resolve Kyc' confirmation modal and click Resolve again
        Locator secondResolveBtn = page.locator(CONFIRM_RESOLVE_BTN).first();
        secondResolveBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        secondResolveBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Resolve on the Confirmation modal.");

        page.waitForTimeout(3000);
        log.info("Waiting for system response after resolving KYC...");
        Locator notificationTitle = page.locator(".el-notification__title").first();

        try {
            // Wait up to 8 seconds for ANY toast to appear
            notificationTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = notificationTitle.innerText().trim();

            // Grab the actual message inside the toast
            Locator notificationContent = page.locator(".el-notification__content").first();
            String msgText = notificationContent.isVisible() ? notificationContent.innerText().trim() : "";

            if (titleText.contains("Success")) {
                log.info("Success popup verified! Message: '{}'", msgText);

                // Optional: You can uncomment the line below to fail the test if the message isn't exactly right
                // Assert.assertTrue(msgText.contains("Kyc SuccessFully edited"), "Unexpected success message: " + msgText);

                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(1000); // Give Vue a second to update the UI status to 'Ok'

            } else if (titleText.contains("Error")) {
                log.error("Backend Error blocked KYC resolution: {}", msgText);
                ScreenshotUtil.saveScreenshot(page, "KycResolveError", scenarioName);
                throw new AssertionError("Failed to resolve KYC. Backend Error: " + msgText);
            }

        } catch (Exception e) {// Re-throw the assertion if it failed above

            log.error("Timeout waiting for success toast after resolving KYC.");
            ScreenshotUtil.saveScreenshot(page, "UnknownKycError", scenarioName);
            throw new RuntimeException("Expected success toast did not appear for KYC resolution.", e);
        }
    }
}
