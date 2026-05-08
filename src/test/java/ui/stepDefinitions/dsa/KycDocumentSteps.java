package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.KycDocumentsPage;

public class KycDocumentSteps {
    private final KycDocumentsPage kycDocumentsPage = new KycDocumentsPage(BaseTest.getPage());

    @Then("User submits the KYC Documents page without additional uploads")
    public void submitKycDocs() {
        kycDocumentsPage.submitDocuments();
    }
}
