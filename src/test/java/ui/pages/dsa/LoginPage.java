package ui.pages.dsa;

import com.microsoft.playwright.Page;
import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BaseTest {
    // Locators
    private final String externalLoginLink = "span:text-is('External Login')";
    private final String emailInput = "input[placeholder*='Email']";
    private final String sendOtpButton = "button:has-text('SEND OTP')";// Assuming standard OTP component
    private final String verifyOtpButton = "button:has-text('VERIFY OTP')";
    private final String dsaDashboardTitle = "KSF DSA Portal";// More likely for the new portal
    private final String dsaDashboardUrl = "https://portal.uat.creditsaison.xyz/dashboard";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void navigateToPortal(String url) {
        getPage().navigate(url);
        log.info("Navigated to DSA Portal: {}", url);
    }

    public void clickExternalLogin() {
        getPage().click(externalLoginLink);
        log.info("Clicked on external login button");
    }

    public void loginWithEmailAndOtp(String email, String otp) {
        getPage().fill(emailInput, email);
        getPage().click(sendOtpButton);
        log.info("Entered email: {} and clicked GET OTP", email);
        
        // Handling OTP inputs (often multiple if it's an OTP component)
        getPage().waitForSelector("#otp-box-0");

        // Logic to fill each individual box
        for (int i = 0; i < otp.length(); i++) {
            String digit = String.valueOf(otp.charAt(i));
            // Targets id="otp-box-0", id="otp-box-1", etc.
            getPage().locator("#otp-box-" + i).fill(digit);
        }

        // Click Verify
        getPage().click(verifyOtpButton);
        log.info("Entered OTP: {} and clicked VERIFY", otp);
    }

    public void verifyRedirectedToDsaDashboard() {
        // This assertion will wait up to 5 seconds (default) for the URL to match
        assertThat(getPage()).hasURL(dsaDashboardUrl);
        assertThat(getPage()).hasTitle(dsaDashboardTitle);

        log.info("Successfully redirected to dashboard: {}", getPage().url());
    }
}
