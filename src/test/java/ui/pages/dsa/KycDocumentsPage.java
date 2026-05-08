package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import hooks.BaseTest;

public class KycDocumentsPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String SUBMIT_DOCS_BTN = "button:has-text('Submit')";

    public KycDocumentsPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    public void submitDocuments() {
        log.info("On KYC Documents page. All fields are non-mandatory. Proceeding to submit...");

        Locator submitBtn = page.locator(SUBMIT_DOCS_BTN);
        submitBtn.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        submitBtn.click();
        log.info("Clicked Submit on KYC Documents page.");
    }
}