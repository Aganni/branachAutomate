package ui.stepDefinitions.dsa;

import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.Utils.Utils;
import ui.pages.dsa.BusinessDetailsPage;

import java.util.LinkedHashMap;
import java.util.Map;

public class BusinessDetailsSteps extends BaseTest {

    @And("User completes Business Details with PAN verification and submits")
    public void completeBusinessDetails() {
        BusinessDetailsPage page = new BusinessDetailsPage(BaseTest.getPage());

        // Extract partner loan ID from URL
        Utils.extractAndStorePartnerLoanId();

        // PAN verification
        page.enterPanAndVerify(TestDataProvider.get("dsa.business_details.entity_pan"));
        page.verifyAutoPopulatedEntityName(TestDataProvider.get("dsa.business_details.entity_name"));
        page.clickContinueToFetchDetails();

        // Operational address
        page.fillOperationalAddress();
        Map<String, String> addressData = new LinkedHashMap<>();
        addressData.put("Operational Address (Line 2)", TestDataProvider.get("dsa.business_details.operational_address.line2"));
        addressData.put("State", TestDataProvider.get("dsa.business_details.operational_address.state"));
        page.verifyAutoPopulatedAddress(addressData);

        // Ownership + registered address
        page.selectOwnership(TestDataProvider.get("dsa.business_details.ownership"));
        page.selectSameAsOperationalAddress(TestDataProvider.get("dsa.business_details.same_as_operational"));

        // More business details
        Map<String, String> moreDetails = new LinkedHashMap<>();
        moreDetails.put("Entity Email", TestDataProvider.get("dsa.business_details.entity_email"));
        moreDetails.put("Entity Contact Number", TestDataProvider.get("dsa.business_details.entity_contact"));
        moreDetails.put("Date of Registration", TestDataProvider.get("dsa.business_details.date_of_registration"));
        moreDetails.put("Last Year's Turnover", TestDataProvider.get("dsa.business_details.last_year_turnover"));
        page.fillMoreBusinessDetails(moreDetails);
        page.selectIndustrySubSector(TestDataProvider.get("dsa.business_details.industry_type"));

        // Loan requirements + submit
        Map<String, String> loanReq = new LinkedHashMap<>();
        loanReq.put("Tenure", TestDataProvider.get("dsa.loan_requirements.tenure"));
        loanReq.put("Loan Amount", TestDataProvider.get("dsa.loan_requirements.loan_amount"));
        loanReq.put("End Use", TestDataProvider.get("dsa.loan_requirements.end_use"));
        page.fillLoanRequirements(loanReq);
        page.clickSubmit();
    }
}
