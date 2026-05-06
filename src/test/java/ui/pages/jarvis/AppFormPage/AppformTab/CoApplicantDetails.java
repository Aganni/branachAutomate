package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class CoApplicantDetails extends BaseTest {

    private Page page;

    public CoApplicantDetails(Page page) {
        this.page = page;
    }

    public void addAadhaarToCoApplicant(String aadhaarNumber, String scenarioName) {
        log.info("Opening Co-Applicant Details section...");

        // 1. Open Co-Applicant Details Section
        Locator coApplicantSection = page.locator("xpath=//div[@id='Co-Applicant Details']//button").first();
        coApplicantSection.scrollIntoViewIfNeeded();
        coApplicantSection.click();

        // Wait for modal
        Locator activeModal = page.locator(".el-dialog__wrapper:visible").last();
        activeModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        // 2. Click "ADD AADHAAR"
        log.info("Clicking ADD AADHAAR button...");
        Locator addAadhaarBtn = activeModal.locator("button.view-btn:has(span:has-text('ADD AADHAAR'))").first();
        addAadhaarBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        addAadhaarBtn.click();

        // 3. Enter Aadhaar
        log.info("Entering Aadhaar number...");
        Locator addAadhaarModal = page.locator(".el-dialog__wrapper:visible").last();
        Locator aadhaarInput = addAadhaarModal.locator("input[placeholder='Enter the Aadhaar']").first();
        aadhaarInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        aadhaarInput.fill(aadhaarNumber);

        // 4. Click Submit
        log.info("Clicking Submit on Add Aadhaar modal...");
        Locator submitBtn = addAadhaarModal.locator(".yes-button-aadhar button").first();
        submitBtn.click();

        // 5. Seed Aadhaar "Yes" Confirmation
        log.info("Confirming Seed Aadhaar...");
        Locator seedAadhaarModal = page.locator(".el-dialog__wrapper:visible").last();
        Locator yesBtn = seedAadhaarModal.locator("button.accept-btn:has(span:has-text('Yes'))").first();
        yesBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        yesBtn.click();

        // 6. Handle Dynamic Wait for Toast
        log.info("Waiting dynamically for Aadhaar Added successfully toast...");
        Locator toastMessage = page.locator(".el-notification:has-text('Aadhaar Added successfully')").first();

        try {
            // Wait up to 30 seconds for the backend service response (Playwright will proceed immediately once it appears)
            toastMessage.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
            log.info("Success popup verified! Message: 'Aadhaar Added successfully'");
        } catch (Exception e) {
            log.error("Failed to add Aadhaar or timeout waiting for success toast.");
            ScreenshotUtil.saveScreenshot(page, "AadhaarAddError", scenarioName);
            throw new AssertionError("Aadhaar addition failed or timed out: " + e.getMessage());
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);

        log.info("Successfully added Aadhaar and returned to the main Application Details page.");

        page.waitForTimeout(3000);

        log.info("Refreshing the page...");
        page.reload(new Page.ReloadOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
