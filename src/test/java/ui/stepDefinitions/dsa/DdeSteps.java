package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.DdePage;

import java.util.Map;

public class DdeSteps {
    private final DdePage ddePage;

    public DdeSteps(){
        this.ddePage= new DdePage(BaseTest.getPage());
    }

    @And("User fills the DDE form with following details")
    public void fillDdeForm(Map<String, String> details) {
        // We loop through every row you provided in the Feature File
        details.forEach((fieldName, value) -> {
            switch (fieldName) {
                // Case 1: Standard Text Fields (We use ID from HTML)
                case "TradeRef1 Name": ddePage.fillField("tradeRef1Name", value); break;
                case "TradeRef1 Mobile": ddePage.fillField("tradeRef1MobileNumber", value); break;
                case "TradeRef2 Name": ddePage.fillField("tradeRef2Name", value); break;
                case "TradeRef2 Mobile": ddePage.fillField("tradeRef2MobileNumber", value); break;
                case "Father First Name": ddePage.fillField("fatherFirstName", value); break;
                case "Father Last Name": ddePage.fillField("fatherLastName", value); break;
                case "Annual Income": ddePage.fillField("verifiedIncome", value); break;
                // Case 2: Dropdowns
                case "TradeRef1 Relation": ddePage.selectFromDropdown("tradeRef1BusinessRelationship", value); break;
                case "TradeRef2 Relation": ddePage.selectFromDropdown("tradeRef2BusinessRelationship", value); break;
                case "Correspondence Ownership": ddePage.selectValueFromMuiSelect("ownership", value); break;

                // New Radix Popover fields
                case "Designation": ddePage.selectRadixPopover("Designation", value); break;
                case "Caste":       ddePage.selectRadixPopover("Caste", value); break;
                case "Religion":    ddePage.selectRadixPopover("Religion", value); break;

                // Case 3: Radio Buttons (Yes/No)
                case "Permanent Same": ddePage.selectRadio("isPermanentAddressSameAsOperationalAddress", value); break;
                case "Current Same": ddePage.selectRadio("isCurrentAddressSameAsOperationalAddress", value); break;
                case "Correspondence Address": ddePage.fillCorrespondenceAddress(value);break;

                case "Email": ddePage.selectEmail(value);break;
                case "Share Holding": ddePage.fillField("shareHolding", value);break;
            }
        });

        ddePage.clickSave();
        ddePage.clickSubmit();
    }
}
