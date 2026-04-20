package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormActionsPage;

public class AppFormActionsSteps {

    private final AppFormActionsPage appFormActionsPage = new AppFormActionsPage();

    @And("User moves the application to CM via Application Actions")
    public void moveApplicationToCM() {
        appFormActionsPage.moveToCAM();
    }
}
