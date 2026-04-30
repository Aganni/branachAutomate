package ui.stepDefinitions.dsa;

import io.cucumber.java.en.*;
import ui.pages.dsa.BankingPage;

import static hooks.BaseTest.getPage;

public class BankingSteps {

    private final BankingPage bankingPage;

    public BankingSteps(){
        this.bankingPage = new BankingPage(getPage());
    }

    @And("User selects bank {string} and completes statement upload in the new tab")
    public void selectBankAndUpload(String bankName) {
        bankingPage.selectBankAndProceed(bankName);
    }

    @Then("User submits banking details and proceeds to the next page")
    public void submitBankingAndProceed() {
        bankingPage.submitAndMoveToNext();
    }
}
