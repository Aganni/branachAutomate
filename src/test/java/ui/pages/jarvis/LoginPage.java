package ui.pages.jarvis;

import com.microsoft.playwright.Page;
import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BaseTest {
    
    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String EMAIL_INPUT_LABEL = "Email or phone";
    private static final String PASSWORD_INPUT_LABEL = "Enter your password";
    private static final String NEXT_TEXT = "Next";

    public LoginPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void login() {
        log.info("Starting Jarvis Login process...");

        page.getByLabel(EMAIL_INPUT_LABEL).last().fill(getUserEmail());
        page.getByText(NEXT_TEXT).click();

        page.getByLabel(PASSWORD_INPUT_LABEL).fill(getsetUserPassWord());
        page.getByText(NEXT_TEXT).last().click();

        log.info("Jarvis Login submitted.");

        page.waitForTimeout(10000);
        assertThat(page).hasTitle("jarvis");
        log.info("Successfully landed on Jarvis Dashboard");
    }
}
