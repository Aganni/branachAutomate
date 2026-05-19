package ui.pages.jarvis.AppFormPage.VerificationTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class VerificationTab extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String VERIFICATION_TAB = "//p[@class='tab-item-title' and normalize-space()='Verification']";
    private static final String UDYAM_EDIT_BTN = "//label[contains(text(),'Uan/Udyam id')]/ancestor::div[contains(@class,'verificationEntityDetail')]//button[contains(@class,'kycEditBtn')]";
    private static final String EDIT_KYC_RESOLVE_BTN = "//div[@role='dialog' and .//*[normalize-space(text())='Edit KYC']]//div[contains(@class, 'el-dialog__footer')]//button[contains(@class,'el-button--primary')]";
    private static final String CONFIRM_RESOLVE_BTN = "//div[@role='dialog' and @aria-label='Resolve Kyc']//div[contains(@class, 'el-dialog__footer')]//button[contains(@class,'el-button--primary')]";

    public VerificationTab(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void navigateToVerificationTab() {
        log.info("Navigating to Verification tab...");

        Locator tab = page.locator(VERIFICATION_TAB).first();
        tab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        tab.click(new Locator.ClickOptions().setForce(true));

        log.info("Waiting for URL to route to /verification...");
        page.waitForURL("**/verification*");

        page.waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Refreshing the Verification page to ensure backend data and event listeners are fully loaded...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); 

        log.info("Verification Page successfully reloaded and network is idle.");
    }

    public void resolveUdyamKyc(String scenarioName) {
        log.info("Starting Udyam KYC Resolution flow...");

        Locator editBtn = page.locator(UDYAM_EDIT_BTN).first();
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Edit on Udyam KYC.");
        page.waitForTimeout(3000);

        Locator firstResolveBtn = page.locator(EDIT_KYC_RESOLVE_BTN).first();
        firstResolveBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        firstResolveBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Resolve on the Edit KYC modal.");

        page.waitForTimeout(3000);
        
        Locator secondResolveBtn = page.locator(CONFIRM_RESOLVE_BTN).first();
        secondResolveBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        secondResolveBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Resolve on the Confirmation modal.");

        page.waitForTimeout(3000);
        log.info("Waiting for system response after resolving KYC...");
        Locator notificationTitle = page.locator(".el-notification__title").first();

        try {
            notificationTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = notificationTitle.innerText().trim();

            Locator notificationContent = page.locator(".el-notification__content").first();
            String msgText = notificationContent.isVisible() ? notificationContent.innerText().trim() : "";

            if (titleText.contains("Success")) {
                log.info("Success popup verified! Message: '{}'", msgText);
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(1000); 
            } else if (titleText.contains("Error")) {
                log.error("Backend Error blocked KYC resolution: {}", msgText);
                ScreenshotUtil.saveScreenshot(page, "KycResolveError", scenarioName);
                throw new AssertionError("Failed to resolve KYC. Backend Error: " + msgText);
            }

        } catch (Exception e) {
            log.error("Timeout waiting for success toast after resolving KYC.");
            ScreenshotUtil.saveScreenshot(page, "UnknownKycError", scenarioName);
            throw new RuntimeException("Expected success toast did not appear for KYC resolution.", e);
        }
    }
}
