package ui.stepDefinitions.dsa;

import io.cucumber.java.en.*;
import ui.pages.dsa.BankingPage;

public class BankingSteps {

    private final BankingPage bankingPage = new BankingPage();

    @And("User selects bank {string} and completes statement upload in the new tab")
    public void selectBankAndUpload(String bankName) {
        bankingPage.selectBankAndProceed(bankName);
    }

    @Then("User submits banking details and proceeds to the next page")
    public void submitBankingAndProceed() {
        bankingPage.submitAndMoveToNext();
    }
}
