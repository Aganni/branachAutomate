package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import java.nio.file.Paths;

public class BankingPage extends BaseTest {

    // DSA Portal Locators
    private static final String BANK_NAME_INPUT = "#bankName";
    private static final String CONTINUE_BTN = "button:has-text('Continue')";
    private static final String SUBMIT_BANK_DETAILS_BTN = "button:has-text('Submit Bank Details')";
    private static final String SAVE_AND_NEXT_BTN = "button:has-text('Save and Next')";

    // Perfios Tab Locators
    private static final String I_CONFIRM_BTN = "button.btn-confirm";
    private static final String FILE_INPUT = "input[name='file']";
    private static final String FINISH_BTN = "button:has-text('Or click here to finish')";
    private static final String CLOSE_TAB_BTN = "button:has-text('close Tab')";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void selectBankAndProceed(String bankName) {
        log.info("Selecting bank: {}", bankName);

        getPage().locator(BANK_NAME_INPUT).fill(bankName);
        getPage().keyboard().press("Enter");

        log.info("Setting up listener for new tab...");
        Page perfiosPage = getPage().waitForPopup(() -> {
            getPage().getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Continue").setExact(true))
                    .click();
        });

        log.info("New tab detected. Starting URL assertion loop...");

        // 2. ASSERTION & DEBUG LOOP: Let's see what Playwright actually sees
        boolean urlMatched = false;
        for (int i = 0; i < 30; i++) { // Try for 30 seconds
            String currentUrl = perfiosPage.url();
            log.info("Current Tab URL [Attempt {}]: {}", i, currentUrl);

            if (currentUrl.contains("perfios") || currentUrl.contains("statement")) {
                urlMatched = true;
                log.info("Assertion Passed: URL matches expected pattern.");
                break;
            }
            perfiosPage.waitForTimeout(1000);
        }

        if (!urlMatched) {
            throw new RuntimeException("Assertion Failed: Timed out waiting for Perfios URL. Last seen URL: " + perfiosPage.url());
        }

        // 3. Wait for the button to be TRULY ready
        log.info("Waiting for 'I Confirm' button to be visible...");
        perfiosPage.locator(I_CONFIRM_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

        handlePerfiosUpload(perfiosPage);
    }

    private void handlePerfiosUpload(Page perfiosPage) {
        log.info("Stabilized! Title: {}", perfiosPage.title());

        perfiosPage.locator(I_CONFIRM_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.info("Clicking I Confirm...");
        perfiosPage.locator(I_CONFIRM_BTN).click();

        log.info("Uploading bank statement PDF...");
        // Ensure the file exists at this path!
        perfiosPage.setInputFiles(FILE_INPUT, Paths.get("src/test/resources/testdata/bank_statement.pdf"));

        // Wait for the upload to process and 'Finish' to appear
        log.info("Waiting for analysis to complete...");
        perfiosPage.locator(FINISH_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        perfiosPage.locator(FINISH_BTN).click();

        // Close the tab
        log.info("Closing Perfios tab...");
        perfiosPage.locator(CLOSE_TAB_BTN).click();
    }

    public void submitAndMoveToNext() {
        // Refresh original page to see the 'Added' status
        getPage().reload();
        log.info("Page reloaded. Submitting bank details...");

        // 1. Click Submit Bank Details
        getPage().locator(SUBMIT_BANK_DETAILS_BTN).click();

        // 2. Wait for background workflow and click Save and Next
        log.info("Waiting for Save and Next button to enable...");
        getPage().locator(SAVE_AND_NEXT_BTN).click(new Locator.ClickOptions().setTimeout(30000));
    }
}