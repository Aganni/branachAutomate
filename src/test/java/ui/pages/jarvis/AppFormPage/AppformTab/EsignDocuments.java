package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

public class EsignDocuments extends BaseTest {

    private final Page page;

    public EsignDocuments(Page page) {
        this.page = page;
    }

    /**
     * Opens the E-Sign Of Documents section.
     */
    public void openESignSection() {
        log.info("Opening E-Sign Of Documents section...");

        Locator eSignCard = page.locator("button.appform-card")
                .filter(new Locator.FilterOptions().setHasText("E-Sign Of Documents"))
                .first();
        eSignCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        eSignCard.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Clicks Generate, opts for offline signature, handles the warning, and returns.
     */
    public void generateAndOptInOfflineSignatures() {
        log.info("Starting E-Sign Generation and Offline Opt-in flow...");

        // 1. Click the Generate button
        Locator generateBtn = page.locator("button:has-text('Generate')").first();
        generateBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        generateBtn.click(new Locator.ClickOptions().setForce(true));

        log.info("Clicked Generate. Waiting for document generation to complete...");

        // Document generation usually involves heavy backend polling, so we wait for network to settle
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(3000); // Buffer for UI to refresh after generation

        // 2. Click the 'Opt in for offline signatures' checkbox
        // We click the label element directly to bypass Element UI's hidden input tricks
        log.info("Clicking 'Opt in for offline signatures' checkbox...");
        Locator offlineCheckboxLabel = page.locator("label.el-checkbox")
                .filter(new Locator.FilterOptions().setHasText("Opt in for offline signatures"))
                .first();
        offlineCheckboxLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        offlineCheckboxLabel.click(new Locator.ClickOptions().setForce(true));

        // 3. Handle the Warning Message Box
        log.info("Waiting for Warning message box...");
        Locator warningOkBtn = page.locator(".el-message-box__btns button.el-button--primary").first();
        warningOkBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        warningOkBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked OK on Warning message box.");

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); // Allow message box fade-out

        // 4. Click the Back button to return to the App Form details
        log.info("Clicking Back button...");
        Locator backBtn = page.locator("button.el-button--text").filter(new Locator.FilterOptions().setHasText("Back")).first();
        backBtn.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Successfully returned to the main Application Details page.");
    }
}