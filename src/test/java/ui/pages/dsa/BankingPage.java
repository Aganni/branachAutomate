package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import java.nio.file.Paths;

public class BankingPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String BANK_NAME_INPUT = "#bankName";
    private static final String I_CONFIRM_BTN = "button.btn-confirm";
    private static final String FILE_INPUT = "input[name='file']";
    private static final String FINISH_BTN = "button:has-text('Or click here to finish')";
    private static final String CLOSE_TAB_BTN = "button:has-text('close Tab')";
    private static final String SUBMIT_BANK_DETAILS_BTN = "button:has-text('Submit Bank Details')";
    private static final String SAVE_AND_NEXT_BTN = "button:has-text('Save and Next')";

    public BankingPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void selectBankAndProceed(String bankName) {
        log.info("Selecting bank: {}", bankName);
        page.locator(BANK_NAME_INPUT).fill(bankName);
        page.keyboard().press("Enter");

        log.info("Setting up listener for new tab...");
        Page perfiosPage = page.waitForPopup(() -> {
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Continue").setExact(true))
                    .click();
        });

        log.info("New tab detected. Waiting for Perfios URL...");
        boolean urlMatched = false;
        for (int i = 0; i < 30; i++) {
            String currentUrl = perfiosPage.url();
            if (currentUrl.contains("perfios") || currentUrl.contains("statement")) {
                urlMatched = true;
                break;
            }
            perfiosPage.waitForTimeout(1000);
        }

        if (!urlMatched) {
            throw new RuntimeException("Assertion Failed: Timed out waiting for Perfios URL. Last seen URL: " + perfiosPage.url());
        }

        log.info("Waiting for 'I Confirm' button to be visible...");
        perfiosPage.locator(I_CONFIRM_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        handlePerfiosUpload(perfiosPage);
    }

    private void handlePerfiosUpload(Page perfiosPage) {
        perfiosPage.locator(I_CONFIRM_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.info("Clicking I Confirm...");
        perfiosPage.locator(I_CONFIRM_BTN).click();

        log.info("Uploading bank statement PDF...");
        perfiosPage.setInputFiles(FILE_INPUT, Paths.get("src/test/resources/testdata/bank_statement.pdf"));

        log.info("Waiting for analysis to complete...");
        perfiosPage.locator(FINISH_BTN).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        perfiosPage.locator(FINISH_BTN).click();

        log.info("Closing Perfios tab...");
        perfiosPage.locator(CLOSE_TAB_BTN).click();
    }

    public void submitAndMoveToNext() {
        page.reload();
        log.info("Page reloaded. Submitting bank details...");

        page.locator(SUBMIT_BANK_DETAILS_BTN).click();

        log.info("Waiting for Save and Next button to enable...");
        page.locator(SAVE_AND_NEXT_BTN).click(new Locator.ClickOptions().setTimeout(30000));
    }
}