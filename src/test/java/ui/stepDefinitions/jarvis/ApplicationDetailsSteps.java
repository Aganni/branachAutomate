package ui.stepDefinitions.jarvis;

import backend.Utils.ApiUtils;
import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.jarvis.AppFormPage.ApplicationPage;
import ui.pages.jarvis.AppFormPage.AppformTab.*;
import ui.pages.jarvis.AppFormPage.DedupeTab.Dedupe;
import ui.pages.jarvis.AppFormPage.RegCheckTab.RegCheck;
import ui.pages.jarvis.AppFormPage.VerificationTab.VerificationTab;
import ui.pages.jarvis.AppFormPage.CamTab.Cam;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationDetailsSteps extends BaseTest {

    @Then("User moves appForm to Login Desk and updates Business and Bank details")
    public void moveToLoginDeskAndUpdate() {
        ApplicationPage appPage = new ApplicationPage(BaseTest.getPage());
        appPage.selectApplicationActionAndAccept("Move to Login Desk", "Moving_AppFrom");

        // Business Details
        Map<String, String> bizDetails = new LinkedHashMap<>();
        bizDetails.put("CLASSIFICATION", TestDataProvider.get("jarvis.business_details.classification"));
        bizDetails.put("Udyam Number", TestDataProvider.get("jarvis.business_details.udyam_number"));
        bizDetails.put("Industry Sector", TestDataProvider.get("jarvis.business_details.industry_sector"));
        bizDetails.put("Sub Sector", TestDataProvider.get("jarvis.business_details.sub_sector"));
        bizDetails.put("No Of Employees", TestDataProvider.get("jarvis.business_details.no_of_employees"));
        bizDetails.put("Business Type", TestDataProvider.get("jarvis.business_details.business_type"));

        BusinessDetails bizPage = new BusinessDetails(BaseTest.getPage());
        bizPage.openBusinessDetailsAndEdit();
        bizPage.fillCompanyDetails(bizDetails, "Updating_Business_Details");
        bizPage.navigateBackToAppDetails();

        // Bank Details
        Map<String, String> bankDetails = new LinkedHashMap<>();
        bankDetails.put("Disbursal Account Number", TestDataProvider.get("jarvis.bank_details.disbursal_account_number"));
        bankDetails.put("Disbursal Account Type", TestDataProvider.get("jarvis.bank_details.disbursal_account_type"));
        bankDetails.put("Disbursal IFSC Code", TestDataProvider.get("jarvis.bank_details.disbursal_ifsc_code"));
        bankDetails.put("Collection Account Number", TestDataProvider.get("jarvis.bank_details.collection_account_number"));
        bankDetails.put("Collection Account Type", TestDataProvider.get("jarvis.bank_details.collection_account_type"));
        bankDetails.put("Collection IFSC Code", TestDataProvider.get("jarvis.bank_details.collection_ifsc_code"));

        BankDetails bankPage = new BankDetails(BaseTest.getPage());
        bankPage.openBankDetailsAndEdit();
        bankPage.fillBankDetails(bankDetails, "Updating_Bank_Details");
    }

    @And("User updates Loan Requirements and resolves Dedupe and Verification")
    public void updateLoanAndResolveDedupe() {
        Dedupe dedupe = new Dedupe(BaseTest.getPage());
        LoanRequirment loanReq = new LoanRequirment(BaseTest.getPage());

        dedupe.navigateToAppFormTab();
        loanReq.openLoanRequirementsAndEdit();
        loanReq.fillLoanRequirementsAndSubmit("Updating_Loan_Requirements");

        dedupe.selectDedupeTab();

        VerificationTab verificationTab = new VerificationTab(BaseTest.getPage());
        verificationTab.navigateToVerificationTab();
        verificationTab.resolveUdyamKyc("Resolving_Udyam_KYC");
    }

    @And("User updates Ownership, initiates Credit Approval, and moves to Terms")
    public void updateOwnershipAndApprove() {
        String approver = TestDataProvider.get("jarvis.ownership.credit_approver");
        Map<String, String> ownershipDetails = new LinkedHashMap<>();
        ownershipDetails.put("UserEmail", approver);

        Dedupe dedupe = new Dedupe(BaseTest.getPage());
        dedupe.navigateToAppFormTab();

        AppFormOwnerShipDetails ownershipPage = new AppFormOwnerShipDetails(BaseTest.getPage());
        ownershipPage.openAppFormOwnerShipAndEdit();
        ownershipPage.selectCreditApproverAndSubmit(ownershipDetails, "Updating_Ownership");

        RegCheck regCheck = new RegCheck(BaseTest.getPage());
        regCheck.selectRegCheckTabAndValidate();

        String reason = TestDataProvider.get("jarvis.credit_approval.reason");
        LoanRequirment loanReq = new LoanRequirment(BaseTest.getPage());
        loanReq.openLoanRequirementsAndEdit();
        loanReq.initiateCreditApproval(reason);

        ApiUtils.moveAppFormToStage("TERMS");
    }

    @And("User generates E-Sign documents and completes Insurance details")
    public void eSignAndInsurance() {
        EsignDocuments eSign = new EsignDocuments(BaseTest.getPage());
        eSign.openESignSection();
        eSign.optInForOfflineSignatures("ESign_Offline");

        Map<String, String> insuranceData = new LinkedHashMap<>();
        insuranceData.put("Provider", TestDataProvider.get("jarvis.insurance.provider"));
        insuranceData.put("Tenure", TestDataProvider.get("jarvis.insurance.tenure"));
        insuranceData.put("NomineeName", TestDataProvider.get("jarvis.insurance.nominee_name"));
        insuranceData.put("Relationship", TestDataProvider.get("jarvis.insurance.relationship"));
        insuranceData.put("DOB", TestDataProvider.get("jarvis.insurance.dob"));
        insuranceData.put("Gender", TestDataProvider.get("jarvis.insurance.gender"));
        insuranceData.put("Mobile", TestDataProvider.get("jarvis.insurance.mobile"));
        insuranceData.put("Email", TestDataProvider.get("jarvis.insurance.email"));

        InsuranceDetails insurancePage = new InsuranceDetails(BaseTest.getPage());
        insurancePage.openInsuranceAndEdit();
        insurancePage.fillInsuranceAndSubmit(insuranceData, "Updating_Insurance");
    }

    @And("User updates Repayment, adds Aadhaar, and adds Beneficiary Owner")
    public void repaymentAadhaarBeneficiary() {
        ApiUtils.updateRepaymentDetails();

        String aadhaar = TestDataProvider.get("jarvis.co_applicant.aadhaar");
        CoApplicantDetails coApp = new CoApplicantDetails(BaseTest.getPage());
        coApp.addAadhaarToCoApplicant(aadhaar, "Adding_Aadhaar");

        String entity = TestDataProvider.get("jarvis.beneficiary.entity");
        String applicant = TestDataProvider.get("jarvis.beneficiary.applicant");
        BeneficiaryOwnerDetails beneficiary = new BeneficiaryOwnerDetails(BaseTest.getPage());
        beneficiary.addBeneficiaryOwner(entity, applicant, "Adding_Beneficiary");
    }

    @And("User starts CAM process")
    public void startCam() {
        Cam camTab = new Cam(BaseTest.getPage());
        camTab.selectCamTab();
    }
}
