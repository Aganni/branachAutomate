package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormPage.ApplicationPage;

public class AppFormActionsSteps {

    @And("User selects {string} from Application Actions and moves appForm next stage")
    public void userSelectsApplicationAction(String action) throws InterruptedException {
        ApplicationPage appFormActionsPage = new ApplicationPage(BaseTest.getPage());
        appFormActionsPage.selectApplicationActionAndAccept(action,"Moving_AppFrom");
    }

    @And("User moves application to {string} assigned to level {string} and user {string}")
    public void moveApplicationWithAssignment(String action, String level, String email) {
        ApplicationPage appFormActionsPage = new ApplicationPage(BaseTest.getPage());
        appFormActionsPage.selectActionWithAssignment(action, level, email, "Moving_AppFrom");
    }
}
