package ui.stepDefinitions.jarvis;

import hooks.BaseTest;
import io.cucumber.java.en.And;
import java.util.Map;

import ui.pages.jarvis.AppFormPage.AppformTab.*;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;


public class ApplicationDetailsSteps {

    @And("User updates the Business Details with the following data:")
    public void updateBusinessDetails(Map<String, String> details) {
        BusinessDetails businessDetailsPage = new BusinessDetails(BaseTest.getPage());
        businessDetailsPage.openBusinessDetailsAndEdit();
        businessDetailsPage.fillCompanyDetails(details,"Updating_Business_Details");
        businessDetailsPage.navigateBackToAppDetails();
    }

    @And("User updates the Bank Details with the following data:")
    public void updateBankDetailsWithData(Map<String, String> details) {
        BankDetails bankDetailsPage = new BankDetails(BaseTest.getPage());
        bankDetailsPage.openBankDetailsAndEdit();
        bankDetailsPage.fillBankDetails(details, "Updating_Bank_Details");
    }

    @And("User opens the Loan Requirements section and updates the details")
    public void userOpensLoanRequirementsAndEdits() {
        Dedupe dedupe = new Dedupe(BaseTest.getPage());
        LoanRequirment loanRequirment = new LoanRequirment(BaseTest.getPage());
        dedupe.navigateToAppFormTab();
        loanRequirment.openLoanRequirementsAndEdit();
        loanRequirment.fillLoanRequirementsAndSubmit("Updating_Loan_Requirements");
    }

    @And("User updates the Appform Ownership Details with the following data:")
    public void updateAppFormOwnershipDetails(Map<String, String> details) {
        Dedupe dedupe = new Dedupe(BaseTest.getPage());
        AppFormOwnerShipDetails appFormOwnerShipDetails = new AppFormOwnerShipDetails(BaseTest.getPage());
        dedupe.navigateToAppFormTab();
        appFormOwnerShipDetails.openAppFormOwnerShipAndEdit();
        appFormOwnerShipDetails.selectCreditApproverAndSubmit(details, "Updating_Appform_Ownership_Details");
    }

    @And("User opens the Loan Requirements section and initiates Credit Approval with reason {string}")
    public void userInitiatesCreditApproval(String reason) {
        LoanRequirment loanRequirment = new LoanRequirment(BaseTest.getPage());
        loanRequirment.openLoanRequirementsAndEdit();
        loanRequirment.initiateCreditApproval(reason);
    }

    @And("User opens E-Sign section, generates documents, and opts for offline signatures")
    public void processESignOffline() {
        EsignDocuments eSignPage = new EsignDocuments(BaseTest.getPage());
        eSignPage.openESignSection();
        eSignPage.optInForOfflineSignatures("ESign_Offline_Signatures");
    }

    @And("User opens Insurance Details and updates Insurance and Nominee Details with the following data")
    public void openInsuranceDetails(Map<String, String> data) {
        InsuranceDetails insurancePage = new InsuranceDetails(BaseTest.getPage());
        insurancePage.openInsuranceAndEdit();
        insurancePage.fillInsuranceAndSubmit(data, "Updating_Insurance_Details");
    }

    @And("User adds Aadhaar number {string} in Co-Applicant Details")
    public void addAadhaarInCoApplicantDetails(String aadhaarNumber) {
        CoApplicantDetails coApplicantDetails = new CoApplicantDetails(BaseTest.getPage());
        coApplicantDetails.addAadhaarToCoApplicant(aadhaarNumber, "Adding_CoApplicant_Aadhaar");
    }

    @And("User adds Beneficiary Owner Details for entity {string} and applicant {string}")
    public void addBeneficiaryOwnerDetails(String entity, String applicant) {
        BeneficiaryOwnerDetails beneficiaryOwnerDetails = new BeneficiaryOwnerDetails(BaseTest.getPage());
        beneficiaryOwnerDetails.addBeneficiaryOwner(entity, applicant, "Adding_Beneficiary_Owner");
    }

}
