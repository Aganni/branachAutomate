package ui.stepDefinitions.dsa;

import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.DdePage;
import ui.pages.dsa.EligibilityCheckPage;

import java.util.Map;

public class DdeSteps {

    @And("User passes decision check and fills DDE form")
    public void passDecisionAndFillDde() {
        // Pass decision check first
        EligibilityCheckPage eligPage = new EligibilityCheckPage(BaseTest.getPage());
        eligPage.waitForPrechecksAndClickNext();

        // Fill DDE from YAML
        DdePage ddePage = new DdePage(BaseTest.getPage());
        Map<String, String> d = TestDataProvider.getMap("dsa.dde");

        if (d.containsKey("trade_ref1_name")) ddePage.fillField("tradeRef1Name", d.get("trade_ref1_name"));
        if (d.containsKey("trade_ref1_relation")) ddePage.selectFromDropdown("tradeRef1BusinessRelationship", d.get("trade_ref1_relation"));
        if (d.containsKey("trade_ref1_mobile")) ddePage.fillField("tradeRef1MobileNumber", d.get("trade_ref1_mobile"));
        if (d.containsKey("trade_ref2_name")) ddePage.fillField("tradeRef2Name", d.get("trade_ref2_name"));
        if (d.containsKey("trade_ref2_relation")) ddePage.selectFromDropdown("tradeRef2BusinessRelationship", d.get("trade_ref2_relation"));
        if (d.containsKey("trade_ref2_mobile")) ddePage.fillField("tradeRef2MobileNumber", d.get("trade_ref2_mobile"));
        if (d.containsKey("email")) ddePage.selectEmail(d.get("email"));
        if (d.containsKey("correspondence_address")) ddePage.fillCorrespondenceAddress(d.get("correspondence_address"));
        if (d.containsKey("correspondence_ownership")) ddePage.selectValueFromMuiSelect("ownership", d.get("correspondence_ownership"));
        if (d.containsKey("father_first_name")) ddePage.fillField("fatherFirstName", d.get("father_first_name"));
        if (d.containsKey("father_last_name")) ddePage.fillField("fatherLastName", d.get("father_last_name"));
        if (d.containsKey("caste")) ddePage.selectRadixPopover("Caste", d.get("caste"));
        if (d.containsKey("religion")) ddePage.selectRadixPopover("Religion", d.get("religion"));
        if (d.containsKey("designation")) ddePage.selectRadixPopover("Designation", d.get("designation"));
        if (d.containsKey("annual_income")) ddePage.fillField("verifiedIncome", d.get("annual_income"));
        if (d.containsKey("permanent_same")) ddePage.selectRadio("isPermanentAddressSameAsOperationalAddress", d.get("permanent_same"));
        if (d.containsKey("current_same")) ddePage.selectRadio("isCurrentAddressSameAsOperationalAddress", d.get("current_same"));

        ddePage.clickSave();
        ddePage.clickSubmit();
    }
}
