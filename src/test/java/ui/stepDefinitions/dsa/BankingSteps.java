package ui.stepDefinitions.dsa;

import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import ui.pages.dsa.BankingPage;

public class BankingSteps {

    @And("User completes Banking details and submits")
    public void completeBankingDetails() {
        String bankName = TestDataProvider.get("dsa.banking.bank_name");
        BankingPage bankingPage = new BankingPage(BaseTest.getPage());
        bankingPage.selectBankAndProceed(bankName);
        bankingPage.submitAndMoveToNext();
    }
}
