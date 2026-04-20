package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ConsentPage extends BaseTest {

    // Locators
    private static final String SEND_OTP_BTN = "button:has-text('SEND OTP')";
    private static final String OTP_INPUT_GROUP = ".MuiOtpInput-Box input"; // Targets all OTP inputs
    private static final String VERIFY_BTN = "button:has-text('VERIFY')";
    private static final String EMAIL_INPUT = "input[name='emailentity']";
    private static final String SEND_EMAIL_BTN = "button[name='entity']:has-text('Send')";
    private static final String SUBMIT_CONSENT_BTN = "button:has-text('SUBMIT')";
    private static final String NEXT_TAB_BTN = "button:has-text('Next')";

    // Status texts to wait for
    private static final String CONSENT_RECEIVED_TEXT = "text='Consent Received'";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void clickSendOtp() {
        log.info("Clicking Send OTP for Primary Applicant");
        getPage().locator(SEND_OTP_BTN).click();
    }

    public void fillOtp(String otp) {
        log.info("Filling OTP: {}", otp);
        Locator otpInputs = getPage().locator(OTP_INPUT_GROUP);
        for (int i = 0; i < otp.length(); i++) {
            otpInputs.nth(i).fill(String.valueOf(otp.charAt(i)));
        }
        log.info("OTP verified successfully. Consent Received.");
    }

    public void enterEmail(String email) {
        Locator emailField = getPage().locator(EMAIL_INPUT);

        // 1. Check if the field is disabled (auto-filled from Details)
        if (emailField.isDisabled()) {
            String currentValue = emailField.inputValue();
            // If it already matches what we want, just exit the method and proceed to click SEND!
            if (currentValue.equals(email)) {
                log.info("Email is already auto-filled correctly with: {}. Skipping edit.", currentValue);
                return;
            } else {
                log.info("Email is locked but needs to be changed. Clicking Edit icon...");
                // Navigates to the parent element of the input to specifically find ITS edit icon
                emailField.locator("xpath=..").getByTestId("EditIcon").click();
            }
        }
        // 2. Now the field is enabled, so we can safely click and fill
        log.info("Entering Entity Email: {}", email);
        emailField.click();
        emailField.fill(email);
        // 3. Click the Save icon (only appears after editing)
        Locator saveIcon = emailField.locator("xpath=..").getByTestId("SaveIcon");
        if (saveIcon.isVisible()) {
            saveIcon.click();
            log.info("Clicked Save icon for email.");
        }
    }

    public void clickSendEmail() {
        log.info("Clicking Send button for Entity Email");
        getPage().locator(SEND_EMAIL_BTN).click();
        getPage().waitForTimeout(3000);
    }

    public void clickSubmitConsent() {
        log.info("Waiting for backend SQS processing...");
        getPage().waitForTimeout(3000);

        log.info("Refreshing the page to fetch the latest consent status...");
        getPage().reload();

        log.info("Waiting for Submit button to enable and clicking it.");
        getPage().locator(SUBMIT_CONSENT_BTN).click(new Locator.ClickOptions().setTimeout(15000));

        log.info("Successfully clicked Submit on Consent Page");
    }
}
