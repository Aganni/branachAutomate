package ui.pages.jarvis;

import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BaseTest {
    private final String emailInput = "Email or phone";
    private final String passwordInput = "Enter your password";
    private final String nextText = "Next";

    public void login() {

        log.info("Starting Jarvis Login process...");

        getPage().getByLabel(emailInput).last().fill(getUserEmail());
        getPage().getByText(nextText).click();

        getPage().getByLabel(passwordInput).fill(getsetUserPassWord());
        getPage().getByText(nextText).last().click();

        log.info("Jarvis Login submitted.");

        getPage().waitForTimeout(10000);
        assertThat(getPage()).hasTitle("jarvis");
        log.info("Successfully landed on Jarvis Dashboard");
    }
}
