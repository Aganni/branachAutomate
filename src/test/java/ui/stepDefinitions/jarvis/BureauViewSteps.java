package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.BureauViewPage;

import static dynamicData.DynamicDataClass.setValue;

public class BureauViewSteps {

    private final BureauViewPage bureauViewPage = new BureauViewPage();

    @And("User navigates to Bureau View tab")
    public void navigateToBureauViewTab() {
        bureauViewPage.navigateToBureauViewTab();
    }

    @And("User selects the Business entity in Bureau sidebar")
    public void selectBusinessEntity() {
        bureauViewPage.selectBusinessEntity();
    }

    @And("User pulls the commercial bureau report and waits for completion")
    public void pullCommercialBureauReport() {
        bureauViewPage.pullCommercialBureauReport();
    }

    @And("User downloads the commercial cibil report")
    public void downloadCommercialCibilReport() {
        String reportPath = bureauViewPage.downloadCommercialCibilReport();
        setValue("commercialCibilReportPath", reportPath);
    }

    @And("User pulls and downloads the commercial cibil report from Bureau View")
    public void pullAndDownloadCommercialCibilReport() {
        String reportPath = bureauViewPage.pullAndDownloadCommercialCibilReport();
        setValue("commercialCibilReportPath", reportPath);
    }
}
