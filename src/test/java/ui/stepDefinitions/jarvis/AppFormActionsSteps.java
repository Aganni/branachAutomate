package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormPage.ApplicationPage;

public class AppFormActionsSteps {

    private final ApplicationPage appFormActionsPage ;

        public AppFormActionsSteps() {
            this.appFormActionsPage = new ApplicationPage(BaseTest.getPage());
        }

    @And("User selects {string} from Application Actions and moves appForm next stage")
    public void userSelectsApplicationAction(String action) throws InterruptedException {
        appFormActionsPage.selectApplicationActionAndAccept(action,"Moving_AppFrom");
    }

    @And("User moves application to {string} assigned to level {string} and user {string}")
    public void moveApplicationWithAssignment(String action, String level, String email) {
        appFormActionsPage.selectActionWithAssignment(action, level, email, "Moving_AppFrom");
    }
}
