package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormActionsPage;

public class AppFormActionsSteps {

    private final AppFormActionsPage appFormActionsPage = new AppFormActionsPage();

    @And("User selects {string} from Application Actions and moves appForm next stage")
    public void userSelectsApplicationAction(String action) throws InterruptedException {
        appFormActionsPage.selectApplicationActionAndAccept(action,"Moving_AppFrom");
    }
}
