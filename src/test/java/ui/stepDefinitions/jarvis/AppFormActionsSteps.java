package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormPage.ApplicationPage;

public class AppFormActionsSteps {

    private final ApplicationPage appFormActionsPage = new ApplicationPage();

    @And("User selects {string} from Application Actions and moves appForm next stage")
    public void userSelectsApplicationAction(String action) throws InterruptedException {
        appFormActionsPage.selectApplicationActionAndAccept(action,"Moving_AppFrom");
    }
}
