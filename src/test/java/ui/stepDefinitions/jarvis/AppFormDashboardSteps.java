package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import ui.pages.jarvis.AppFormPage.CamTab.Cam;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;
import ui.pages.jarvis.AppFormPage.RegCheckTab.RegCheck;

public class AppFormDashboardSteps extends BaseTest {

    private final Dedupe dedupeTab = new Dedupe(getPage());
    private final RegCheck regCheckTab = new RegCheck(getPage());
    private final Cam camTab = new Cam(getPage());

    @And("User navigates to the Dedupe tab in Application Dashboard")
    public void navigateDedupe() {
        dedupeTab.selectDedupeTab();
    }

    @And("User navigates to the RegCheck tab in Application Dashboard")
    public void navigateToRegCheckTab() {
        regCheckTab.selectRegCheckTab();
    }

    @And("User navigates to the CAM tab in Application Dashboard")
    public void navigateToCamTab() throws InterruptedException {
         camTab.selectCamTab();
    }
}
