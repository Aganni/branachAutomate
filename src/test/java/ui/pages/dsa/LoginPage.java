package ui.pages.dsa;

import com.microsoft.playwright.Page;
import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String EXTERNAL_LOGIN_LINK = "span:text-is('External Login')";
    private static final String EMAIL_INPUT = "input[placeholder*='Email']";
    private static final String SEND_OTP_BTN = "button:has-text('SEND OTP')";
    private static final String VERIFY_OTP_BTN = "button:has-text('VERIFY OTP')";
    private static final String DSA_DASHBOARD_TITLE = "KSF DSA Portal";
    private static final String DSA_DASHBOARD_URL = "https://portal.uat.creditsaison.xyz/dashboard";

    public LoginPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void navigateToPortal(String url) {
        page.navigate(url);
        log.info("Navigated to DSA Portal: {}", url);
    }

    public void clickExternalLogin() {
        page.click(EXTERNAL_LOGIN_LINK);
        log.info("Clicked on external login button");
    }

    public void loginWithEmailAndOtp(String email, String otp) {
        page.fill(EMAIL_INPUT, email);
        page.click(SEND_OTP_BTN);
        log.info("Entered email: {} and clicked GET OTP", email);
        
        page.waitForSelector("#otp-box-0");

        for (int i = 0; i < otp.length(); i++) {
            String digit = String.valueOf(otp.charAt(i));
            page.locator("#otp-box-" + i).fill(digit);
        }

        page.click(VERIFY_OTP_BTN);
        log.info("Entered OTP: {} and clicked VERIFY", otp);
    }

    public void verifyRedirectedToDsaDashboard() {
        assertThat(page).hasURL(DSA_DASHBOARD_URL);
        assertThat(page).hasTitle(DSA_DASHBOARD_TITLE);

        log.info("Successfully redirected to dashboard: {}", page.url());
    }
}
