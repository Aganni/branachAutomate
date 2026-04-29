package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import ui.pages.jarvis.AppFormPage.CamTab.Cam;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;
import ui.pages.jarvis.AppFormPage.RegCheckTab.RegCheck;
import ui.pages.jarvis.AppFormPage.VerificationTab.VerificationTab;

public class AppFormDashboardSteps extends BaseTest {

    private final Dedupe dedupeTab ;
    private final RegCheck regCheckTab;
    private final Cam camTab ;
    private final VerificationTab verificationPage ;

    public AppFormDashboardSteps() {
        this.dedupeTab = new Dedupe(getPage());
        this.regCheckTab = new RegCheck(getPage());
        this.camTab = new Cam(getPage());
        this.verificationPage = new VerificationTab(getPage());
    }

    @And("User navigates to the Dedupe tab in Application Dashboard")
    public void navigateDedupe() {
        dedupeTab.selectDedupeTab();
    }

    @And("User navigates to the RegCheck tab in Application Dashboard")
    public void navigateToRegCheckTab() {
        regCheckTab.selectRegCheckTabAndValidate();
    }

    @And("User navigates to the CAM tab in Application Dashboard and start the cam")
    public void navigateToCamTab() throws InterruptedException {
         camTab.selectCamTab();
    }

    @And("User navigates to the Verification tab and resolves the Udyam KYC status")
    public void userNavigatesToVerificationTab() {
        verificationPage.navigateToVerificationTab();
        verificationPage.resolveUdyamKyc("Resolving Udyam KYC status");
    }


}
