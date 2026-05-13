package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import ui.pages.jarvis.AppFormPage.CamTab.Cam;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;
import ui.pages.jarvis.AppFormPage.RegCheckTab.RegCheck;
import ui.pages.jarvis.AppFormPage.VerificationTab.VerificationTab;

public class AppFormDashboardSteps extends BaseTest {

    @And("User navigates to the Dedupe tab in Application Dashboard")
    public void navigateDedupe()  {
        Dedupe dedupeTab = new Dedupe(getPage());
        dedupeTab.selectDedupeTab();
    }

    @And("User navigates to the RegCheck tab in Application Dashboard")
    public void navigateToRegCheckTab() {
        RegCheck regCheckTab = new RegCheck(getPage());
        regCheckTab.selectRegCheckTabAndValidate();
    }

    @And("User navigates to the CAM tab in Application Dashboard and start the cam")
    public void navigateToCamTab() {
        Cam camTab = new Cam(getPage());
        camTab.selectCamTab();
    }

    @And("User navigates to the Verification tab and resolves the Udyam KYC status")
    public void userNavigatesToVerificationTab() {
        VerificationTab verificationPage = new VerificationTab(getPage());
        verificationPage.navigateToVerificationTab();
        verificationPage.resolveUdyamKyc("Resolving Udyam KYC status");
    }


}
