package ui.stepDefinitions.dsa;

import io.cucumber.java.en.*;
import ui.pages.dsa.KycDocumentsPage;

public class KycDocumentSteps {
    private final KycDocumentsPage kycDocumentsPage = new KycDocumentsPage();

    @Then("User submits the KYC Documents page without additional uploads")
    public void submitKycDocs() {
        kycDocumentsPage.submitDocuments();
    }
}
