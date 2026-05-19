package ui.stepDefinitions.jarvis;

import io.cucumber.java.en.*;
import ui.pages.jarvis.DocumentsPage;

public class DocumentsSteps {

    private final DocumentsPage documentsPage = new DocumentsPage();

    @And("User uploads mandatory documents and marks OSV in Documents tab")
    public void uploadMandatoryDocumentsAndMarkOsv() {
        documentsPage.uploadMandatoryDocumentsAndMarkOsv();
    }

    @And("User approves the KYC checklist in Documents tab")
    public void approveKycChecklist() {
        documentsPage.approveKycChecklist();
    }
}
