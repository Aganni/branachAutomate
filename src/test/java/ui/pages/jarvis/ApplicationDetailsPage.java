package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

import java.util.Map;

public class ApplicationDetailsPage extends BaseTest{
    private final Page page;

    public ApplicationDetailsPage(Page page) {
        this.page = page;
    }

    // ─────────────────────────────────────────────────────────────────────────────
      // BUSINESS DETAILS FORM (Login Desk Updates)
    // ─────────────────────────────────────────────────────────────────────────────

    public void openBusinessDetailsAndEdit() {
        log.info("Opening Business Details section...");

        Locator businessCard = getPage().locator("button.appform-card").filter(new Locator.FilterOptions().setHasText("Business Details")).first();
        businessCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        businessCard.click();

        log.info("Clicking Edit on Company Details...");
        Locator editBtn = getPage().locator(".el-card__body")
                .filter(new Locator.FilterOptions().setHasText("Company Details"))
                .locator("button:has-text('Edit')");
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Fills the Company Details dynamically from a Map (Feature File Data)
     */
    public void fillCompanyDetails(Map<String, String> details,String scenarioName) {
        log.info("Filling Company Details dynamically...");

        // 1. Handle Udyam Verification Flow
        if (details.containsKey("Udyam Number")) {
            handleUdyamVerification(details.get("Udyam Number"));
        }

        // 2. Handle Industry Sector
        if (details.containsKey("Industry Sector")) {
            selectDropdownOption("INDUSTRY SECTOR", details.get("Industry Sector"));
        }

        // 3. Handle Sub Sector
        if (details.containsKey("Sub Sector")) {
            selectDropdownOption("SUB SECTOR", details.get("Sub Sector"));
        }

        if (details.containsKey("CLASSIFICATION")) {
            selectDropdownOption("CLASSIFICATION", details.get("CLASSIFICATION"));
        }

        if (details.containsKey("Entity CGTMSE")) {
            selectDropdownOption("Entity CGTMSE", details.get("Entity CGTMSE"));
        }

        // 4. Handle Employees
        if (details.containsKey("No Of Employees")) {
            Locator empInput = getPage().locator("//label[text()='NO OF EMPLOYEES']/following-sibling::div//input").first();
            empInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            empInput.fill(details.get("No Of Employees"));
            log.info("Filled Employees: {}", details.get("No Of Employees"));
        }

        // 5. Handle Business Type
        if (details.containsKey("Business Type")) {
            selectDropdownOption("Business Type", details.get("Business Type"));
        }

        // 6. Click Submit (Floating Action Button)
        log.info("Clicking Submit floating button...");
        getPage().locator(".cs-fab").click();

        // 7. Handle Submission Response (Success vs Error Toasts)
        log.info("Waiting for form submission response...");
        Locator toastTitle = getPage().locator(".el-notification__title").first();

        try {
            // Wait up to 8 seconds for ANY toast to appear
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = toastTitle.innerText().trim();

            if (titleText.contains("Success")) {
                log.info("Business details submitted successfully!");
                getPage().waitForLoadState(LoadState.NETWORKIDLE);

            } else if (titleText.contains("Error")) {
                // If the toast says Error, grab the specific error message text
                Locator toastMessage = getPage().locator(".el-notification__content").first();
                String errorMsg = toastMessage.isVisible() ? toastMessage.innerText().trim() : "Unknown Form Error";

                log.error("Failed to update Business Details: {}", errorMsg);

                // Capture Screenshot
                ScreenshotUtil.saveScreenshot(getPage(), "BusinessDetailsError", scenarioName);

                // Fail the test cleanly
                throw new AssertionError("Business Details submission failed: " + errorMsg);
            } else {
                log.warn("An unexpected toast appeared: {}", titleText);
            }

        } catch (Exception e) {
            log.error("No success or error toast appeared within the timeout period.");
            ScreenshotUtil.saveScreenshot(getPage(), "NoResponseToast_BusinessDetails", scenarioName);
            throw new RuntimeException("Timeout waiting for submission response toast.", e);
        }
    }

    /**
     * Helper method to handle Element UI dropdowns reliably
     */
    private void selectDropdownOption(String label, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, label);

        // Click the input box under the specific label
        Locator input = getPage().locator("//label[text()='" + label + "']/following-sibling::div//input").first();
        input.click(new Locator.ClickOptions().setForce(true));

        getPage().waitForTimeout(500); // Allow Element UI dropdown animation to finish

        // Find and click the option from the floating list
        Locator option = getPage().locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(optionText)).first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click(new Locator.ClickOptions().setForce(true));
    }

    /**
     * Dedicated method to handle the Udyam Typing, Clicking, and Verify Alert
     */
    private void handleUdyamVerification(String udyamNumber) {
        log.info("Handling Udyam Verification for: {}", udyamNumber);

        // 1. Click AND Type the Udyam number into the box
        Locator uanInput = getPage().locator("//label[text()='ENTITY UAN/UDYAM NUMBER']/following-sibling::div//input").first();
        uanInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        uanInput.click(new Locator.ClickOptions().setForce(true));
        uanInput.fill(udyamNumber); // Typing it forces the dropdown to generate the option

        getPage().waitForTimeout(500); // Allow UI to update the dropdown list

        // 2. Select the newly generated option from the dropdown list
        Locator udyamOption = getPage().locator("li.el-select-dropdown__item").filter(new Locator.FilterOptions().setHasText(udyamNumber)).first();
        udyamOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        udyamOption.click(new Locator.ClickOptions().setForce(true));
        log.info("Selected Udyam Number from dropdown.");

        // 3. Click Verify
        Locator verifyBtn = getPage().locator(".verify-btn").first();
        verifyBtn.click();
        log.info("Clicked Verify button.");

        // 4. Handle the specific Element UI Message Box popup ("Are you sure...")
        try {
            Locator messageBox = getPage().locator(".el-message-box").first();
            messageBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));

            Locator okBtn = messageBox.locator(".el-message-box__btns button").filter(new Locator.FilterOptions().setHasText("OK")).first();
            okBtn.click();
            log.info("Clicked OK on the Udyam verification popup.");

            getPage().waitForLoadState(LoadState.NETWORKIDLE);
            getPage().waitForTimeout(1000);
        } catch (Exception e) {
            log.info("Udyam confirmation popup did not appear, proceeding...");
        }
    }
}
