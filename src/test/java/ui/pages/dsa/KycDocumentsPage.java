package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import hooks.BaseTest;

public class KycDocumentsPage extends BaseTest {

    public static Page getPage() {
        return BaseTest.getPage();
    }

    private static final String SUBMIT_DOCS_BTN = "button:has-text('Submit')";

    public void submitDocuments() {
        log.info("On KYC Documents page. All fields are non-mandatory. Proceeding to submit...");

        // Ensure the button is visible and enabled
        Locator submitBtn = getPage().locator(SUBMIT_DOCS_BTN);
        submitBtn.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        submitBtn.click();
        log.info("Clicked Submit on KYC Documents page.");
    }
}