package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import hooks.BaseTest;

public class ConsentPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String SEND_OTP_BTN = "button:has-text('SEND OTP')";
    private static final String OTP_INPUT_GROUP = ".MuiOtpInput-Box input"; 
    private static final String EMAIL_INPUT = "input[name='emailentity']";
    private static final String SEND_EMAIL_BTN = "button[name='entity']:has-text('Send')";
    private static final String SUBMIT_CONSENT_BTN = "button:has-text('SUBMIT')";

    public ConsentPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void clickSendOtp() {
        log.info("Clicking Send OTP for Primary Applicant");
        page.locator(SEND_OTP_BTN).click();
    }

    public void fillOtp(String otp) {
        log.info("Filling OTP: {}", otp);
        Locator otpInputs = page.locator(OTP_INPUT_GROUP);
        for (int i = 0; i < otp.length(); i++) {
            otpInputs.nth(i).fill(String.valueOf(otp.charAt(i)));
        }
        log.info("OTP verified successfully. Consent Received.");
    }

    public void enterEmail(String email) {
        Locator emailField = page.locator(EMAIL_INPUT);

        if (emailField.isDisabled()) {
            String currentValue = emailField.inputValue();
            if (currentValue.equals(email)) {
                log.info("Email is already auto-filled correctly with: {}. Skipping edit.", currentValue);
                return;
            } else {
                log.info("Email is locked but needs to be changed. Clicking Edit icon...");
                emailField.locator("xpath=..").getByTestId("EditIcon").click();
            }
        }
        
        log.info("Entering Entity Email: {}", email);
        emailField.click();
        emailField.fill(email);
        
        Locator saveIcon = emailField.locator("xpath=..").getByTestId("SaveIcon");
        if (saveIcon.isVisible()) {
            saveIcon.click();
            log.info("Clicked Save icon for email.");
        }
    }

    public void clickSendEmail() {
        log.info("Clicking Send button for Entity Email");
        page.locator(SEND_EMAIL_BTN).click();
        page.waitForTimeout(3000);
    }

    public void clickSubmitConsent() {
        log.info("Waiting for backend SQS processing...");
        page.waitForTimeout(3000);

        log.info("Refreshing the page to fetch the latest consent status...");
        page.reload();

        log.info("Waiting for Submit button to enable and clicking it.");
        page.locator(SUBMIT_CONSENT_BTN).click(new Locator.ClickOptions().setTimeout(15000));

        log.info("Successfully clicked Submit on Consent Page");
    }
}
