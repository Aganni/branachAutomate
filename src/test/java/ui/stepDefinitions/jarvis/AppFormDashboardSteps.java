package ui.stepDefinitions.jarvis;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;
import io.cucumber.java.en.And;
import ui.pages.jarvis.AllocationDashboardPage;
import ui.pages.jarvis.tabs.CamTab;
import ui.pages.jarvis.tabs.DedupeTab;
import ui.pages.jarvis.tabs.RegCheckTab;

public class AppFormDashboardSteps extends BaseTest {

    private final DedupeTab dedupeTab = new DedupeTab(getPage());
    private final RegCheckTab regCheckTab = new RegCheckTab(getPage());
    private final CamTab camTab = new CamTab(getPage());

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
