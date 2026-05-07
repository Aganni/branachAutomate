package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import java.util.Map;

import ui.pages.jarvis.AppFormPage.AppformTab.*;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;


public class ApplicationDetailsSteps {

    private final BusinessDetails businessDetailsPage;
    private final BankDetails bankDetailsPage;
    private final LoanRequirment loanRequirment;
    private final AppFormOwnerShipDetails appFormOwnerShipDetails;
    private final Dedupe dedupe;
    private final EsignDocuments eSignPage;
    private final InsuranceDetails insurancePage;
    private final CoApplicantDetails coApplicantDetails;
    private final BeneficiaryOwnerDetails beneficiaryOwnerDetails;

    public ApplicationDetailsSteps() {
        this.businessDetailsPage = new BusinessDetails(BaseTest.getPage());
        this.bankDetailsPage = new BankDetails(BaseTest.getPage());
        this.loanRequirment = new LoanRequirment(BaseTest.getPage());
        this.dedupe = new Dedupe(BaseTest.getPage());
        this.appFormOwnerShipDetails = new AppFormOwnerShipDetails(BaseTest.getPage());
        this.eSignPage = new EsignDocuments(BaseTest.getPage());
        this.insurancePage = new InsuranceDetails(BaseTest.getPage());
        this.coApplicantDetails = new CoApplicantDetails(BaseTest.getPage());
        this.beneficiaryOwnerDetails = new BeneficiaryOwnerDetails(BaseTest.getPage());
    }

    @And("User updates the Business Details with the following data:")
    public void updateBusinessDetails(Map<String, String> details) {
        businessDetailsPage.openBusinessDetailsAndEdit();
        businessDetailsPage.fillCompanyDetails(details,"Updating_Business_Details");
        businessDetailsPage.navigateBackToAppDetails();
    }

    @And("User updates the Bank Details with the following data:")
    public void updateBankDetailsWithData(Map<String, String> details) {
        bankDetailsPage.openBankDetailsAndEdit();
        bankDetailsPage.fillBankDetails(details, "Updating_Bank_Details");
    }

    @And("User opens the Loan Requirements section and updates the details")
    public void userOpensLoanRequirementsAndEdits() {
        dedupe.navigateToAppFormTab();
        loanRequirment.openLoanRequirementsAndEdit();
        loanRequirment.fillLoanRequirementsAndSubmit("Updating_Loan_Requirements");
    }

    @And("User updates the Appform Ownership Details with the following data:")
    public void updateAppFormOwnershipDetails(Map<String, String> details) {
        dedupe.navigateToAppFormTab();
        appFormOwnerShipDetails.openAppFormOwnerShipAndEdit();
        appFormOwnerShipDetails.selectCreditApproverAndSubmit(details, "Updating_Appform_Ownership_Details");
    }

    @And("User opens the Loan Requirements section and initiates Credit Approval with reason {string}")
    public void userInitiatesCreditApproval(String reason) {
        loanRequirment.openLoanRequirementsAndEdit();
        loanRequirment.initiateCreditApproval(reason);
    }

    @And("User opens E-Sign section, generates documents, and opts for offline signatures")
    public void processESignOffline() {
        eSignPage.openESignSection();
        eSignPage.optInForOfflineSignatures("ESign_Offline_Signatures");
    }

    @And("User opens Insurance Details and updates Insurance and Nominee Details with the following data")
    public void openInsuranceDetails(Map<String, String> data) {
        insurancePage.openInsuranceAndEdit();
        insurancePage.fillInsuranceAndSubmit(data, "Updating_Insurance_Details");
    }

    @And("User adds Aadhaar number {string} in Co-Applicant Details")
    public void addAadhaarInCoApplicantDetails(String aadhaarNumber) {
        coApplicantDetails.addAadhaarToCoApplicant(aadhaarNumber, "Adding_CoApplicant_Aadhaar");
    }

    @And("User adds Beneficiary Owner Details for entity {string} and applicant {string}")
    public void addBeneficiaryOwnerDetails(String entity, String applicant) {
        beneficiaryOwnerDetails.addBeneficiaryOwner(entity, applicant, "Adding_Beneficiary_Owner");
    }

}
