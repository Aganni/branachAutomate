package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.LoginPage;

public class LoginSteps extends BaseTest{
    private LoginPage jarvisPage = new LoginPage();

    @And("User switches to Jarvis portal and logins")
    public void switchToJarvisAndLogin() throws Exception {
        log.info("Switching to Jarvis portal and performing login...");
        BaseTest.getCredentials("jarvis");

        BaseTest.switchToJarvisPortal();

        jarvisPage.login();
    }
}
