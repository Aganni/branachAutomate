package ui.pages.jarvis.AppFormPage.AppformTab;

import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class BusinessDetails extends BaseTest {

    private final Page page;

    public BusinessDetails(Page page) {
        this.page = page;
    }

    /** Opens the Business Details card and clicks Edit on the Company Details modal. */
    public void openBusinessDetailsAndEdit() {
        log.info("Opening Business Details section...");
        AppFormTabNavigator.ensureOnAppFormTab(page);
        AppFormUtils.openAppFormCard(page, "Business Details");

        log.info("Clicking Edit on Company Details...");
        Locator editBtn = page.locator(".el-card__body")
                .filter(new Locator.FilterOptions().setHasText("Company Details"))
                .locator("button:has-text('Edit')");
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /** Fills Company Details dynamically from the feature file data map. */
    public void fillCompanyDetails(Map<String, String> details, String scenarioName) {
        log.info("Filling Company Details dynamically...");

        if (details.containsKey("Udyam Number"))    handleUdyamVerification(details.get("Udyam Number"));
        if (details.containsKey("Industry Sector")) AppFormUtils.selectDropdown(page, "INDUSTRY SECTOR", details.get("Industry Sector"));
        if (details.containsKey("Sub Sector"))      AppFormUtils.selectDropdown(page, "SUB SECTOR", details.get("Sub Sector"));
        if (details.containsKey("CLASSIFICATION"))  AppFormUtils.selectDropdown(page, "CLASSIFICATION", details.get("CLASSIFICATION"));
        if (details.containsKey("Entity CGTMSE"))   AppFormUtils.selectDropdown(page, "Entity CGTMSE", details.get("Entity CGTMSE"));

        if (details.containsKey("No Of Employees")) {
            Locator empInput = page.locator("xpath=//label[text()='NO OF EMPLOYEES']/following-sibling::div//input").first();
            empInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            empInput.fill(details.get("No Of Employees"));
            log.info("Filled No Of Employees: {}", details.get("No Of Employees"));
        }

        if (details.containsKey("Business Type")) AppFormUtils.selectDropdown(page, "Business Type", details.get("Business Type"));

        log.info("Clicking Submit floating button...");
        page.locator(".cs-fab").click();

        AppFormUtils.verifyToast(page, "BusinessDetails", scenarioName);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    /** Types the Udyam number, picks it from the dropdown, then confirms the verification popup. */
    private void handleUdyamVerification(String udyamNumber) {
        log.info("Handling Udyam Verification for: {}", udyamNumber);

        Locator uanInput = page.locator("xpath=//label[text()='ENTITY UAN/UDYAM NUMBER']/following-sibling::div//input").first();
        uanInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        uanInput.click(new Locator.ClickOptions().setForce(true));
        page.keyboard().type(udyamNumber, new Keyboard.TypeOptions().setDelay(50));

        Locator udyamOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(udyamNumber))
                .first();
        try {
            udyamOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            udyamOption.click(new Locator.ClickOptions().setForce(true));
        } catch (Exception e) {
            log.warn("Udyam dropdown did not appear — pressing Enter as fallback.");
            page.keyboard().press("Enter");
        }

        page.waitForTimeout(500);

        Locator verifyBtn = page.locator(".verify-btn").first();
        verifyBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Verify button.");

        // Confirm the "Are you sure?" popup if it appears
        try {
            Locator messageBox = page.locator(".el-message-box").first();
            messageBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            messageBox.locator(".el-message-box__btns button")
                    .filter(new Locator.FilterOptions().setHasText("OK"))
                    .first()
                    .click();
            log.info("Confirmed Udyam verification popup.");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("Udyam confirmation popup did not appear, proceeding...");
        }
    }

    /**
     * Closes the Company Details inner modal then dismisses the Business Details overlay
     * to return to the main Application Details screen.
     */
    public void navigateBackToAppDetails() {
        log.info("Navigating back to the main Application Details page...");

        // Close inner Company Details modal via the back-arrow button
        Locator firstBackBtn = page.locator(".el-dialog__body button.close-btn:has(.el-icon-arrow-left)").last();
        try {
            if (firstBackBtn.isVisible()) {
                firstBackBtn.hover(new Locator.HoverOptions().setForce(true));
                firstBackBtn.click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(1000);
            }
        } catch (Exception e) {
            log.warn("Inner modal back arrow not clickable: {}", e.getMessage());
        }

        // Dismiss any floating menus before clicking the main back arrow
        page.mouse().click(10, 10);
        page.waitForTimeout(500);

        // The main Business Details back arrow is CSS-hidden until hovered
        Locator mainBackBtn = page.locator(".business-heading button.close-btn:has(.el-icon-arrow-left)").first();
        try {
            mainBackBtn.hover(new Locator.HoverOptions().setForce(true));
            mainBackBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
            mainBackBtn.click(new Locator.ClickOptions().setForce(true));
        } catch (Exception e) {
            log.error("Main back arrow not found — pressing Escape as fallback.");
            page.keyboard().press("Escape");
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Returned to main Application Details screen.");
    }
}
