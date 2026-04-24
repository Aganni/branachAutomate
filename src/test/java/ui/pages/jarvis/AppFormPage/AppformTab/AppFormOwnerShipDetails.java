package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class AppFormOwnerShipDetails extends BaseTest{
    private final Page page;

    public AppFormOwnerShipDetails(Page page) {
        this.page = page;
    }

    // ─────────────────────────────────────────────────────────────────────────────
      // AppForm OwnerShip Details FORM
    // ─────────────────────────────────────────────────────────────────────────────

    public void openAppFormOwnerShipAndEdit() {
        log.info("Opening Appform Ownership Details section...");

        Locator appFormOwnerShipDetails = page.locator("//body/div[@id='__nuxt']/div[@id='__layout']/div/section[@class='el-container app-container']/section[@class='el-container content-wrapper']/main[@class='el-main dashboard-main-slot']/div[@class='application-layout ubl-layout']/div[@class='application-page-component']/div[@class='application-main-slot-with-side-bar application-main-slot-default']/div[@id='mainView']/div/div/div[@class='appform-section-container']/div[@id='DSA Details']/div/button[1]");
        appFormOwnerShipDetails.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        appFormOwnerShipDetails.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Select Credit Approver from the dropdown based on the email provided in the data map.
     */
    public void selectCreditApprover(Map<String, String> details, String scenarioName) {
        log.info("Clicking Edit to select Credit Approver for scenario: [{}]...", scenarioName);
        clickEditButton();
        String approverEmail = details.get("UserEmail");
        log.info("Selecting Credit Approver: [{}] for scenario: [{}]", approverEmail, scenarioName);

        // 1. Click the dropdown input.
        // Using a more stable selector that doesn't rely on 'is-focus'
        page.locator("input[placeholder='Select']").click();

        // 2. Select the specific email from the dropdown list.
        // Element UI typically renders options in 'li' tags with this class.
        page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(approverEmail))
                .click();

        clickSubmitButton();
        log.info("Successfully selected Credit Approver email.");
    }

    private void clickSubmitButton() {
        log.info("Submitting Appform Ownership Details...");

        Locator submitBtn = page.locator("//div[@class='cs-fab']");
        submitBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        submitBtn.click();

        log.info("Appform Ownership Details submitted successfully.");
    }

    private void clickEditButton() {
        log.info("Clicking Edit on Appform Ownership Details...");
        Locator editBtn = page.locator("button[class='el-button edit-btn el-button--primary'] span");
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();
    }


//    /**
//     * Helper method to handle Element UI dropdowns reliably
//     */
//    private void selectDropdownOption(String label, String optionText) {
//        log.info("Selecting '{}' for '{}'", optionText, label);
//
//        // Click the input box under the specific label
//        Locator input = getPage().locator("//label[text()='" + label + "']/following-sibling::div//input").first();
//        input.click(new Locator.ClickOptions().setForce(true));
//
//        getPage().waitForTimeout(500); // Allow Element UI dropdown animation to finish
//
//        // Find and click the option from the floating list
//        Locator option = getPage().locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(optionText)).first();
//        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//        option.click(new Locator.ClickOptions().setForce(true));
//    }


}
