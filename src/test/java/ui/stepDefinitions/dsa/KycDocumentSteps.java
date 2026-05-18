package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.KycDocumentsPage;

public class KycDocumentSteps {

    @And("User submits KYC Documents page")
    public void submitKycDocuments() {
        KycDocumentsPage page = new KycDocumentsPage(BaseTest.getPage());
        page.submitDocuments();
    }
}
