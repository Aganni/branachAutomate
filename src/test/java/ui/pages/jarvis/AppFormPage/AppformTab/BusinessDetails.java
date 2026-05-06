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

    // ─────────────────────────────────────────────────────────────────────────────
      // BUSINESS DETAILS FORM (Login Desk Updates)
    // ─────────────────────────────────────────────────────────────────────────────

    public void openBusinessDetailsAndEdit() {
        log.info("Opening Business Details section...");

        Locator businessCard = page.locator("button.appform-card").filter(new Locator.FilterOptions().setHasText("Business Details")).first();
        businessCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        businessCard.click();

        log.info("Clicking Edit on Company Details...");
        Locator editBtn = page.locator(".el-card__body")
                .filter(new Locator.FilterOptions().setHasText("Company Details"))
                .locator("button:has-text('Edit')");
        editBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        editBtn.click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
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
            Locator empInput = page.locator("//label[text()='NO OF EMPLOYEES']/following-sibling::div//input").first();
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
        page.locator(".cs-fab").click();

        // 7. Handle Submission Response (Success vs Error Toasts)
        log.info("Waiting for form submission response...");
        Locator toastTitle = page.locator(".el-notification__title").first();

        try {
            // Wait up to 8 seconds for ANY toast to appear
            toastTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(8000));
            String titleText = toastTitle.innerText().trim();

            if (titleText.contains("Success")) {
                log.info("Business details submitted successfully!");
                page.waitForLoadState(LoadState.NETWORKIDLE);

            } else if (titleText.contains("Error")) {
                // If the toast says Error, grab the specific error message text
                Locator toastMessage = page.locator(".el-notification__content").first();
                String errorMsg = toastMessage.isVisible() ? toastMessage.innerText().trim() : "Unknown Form Error";

                log.error("Failed to update Business Details: {}", errorMsg);

                // Capture Screenshot
                ScreenshotUtil.saveScreenshot(page, "BusinessDetailsError", scenarioName);

                // Fail the test cleanly
                throw new AssertionError("Business Details submission failed: " + errorMsg);
            } else {
                log.warn("An unexpected toast appeared: {}", titleText);
            }

        } catch (Exception e) {
            log.error("No success or error toast appeared within the timeout period.");
            ScreenshotUtil.saveScreenshot(page, "NoResponseToast_BusinessDetails", scenarioName);
            throw new RuntimeException("Timeout waiting for submission response toast.", e);
        }
    }

     /**
     * Helper method to handle Element UI dropdowns reliably without hitting hidden ghost elements.
     */
    private void selectDropdownOption(String label, String optionText) {
        log.info("Selecting '{}' for '{}'", optionText, label);

        // 1. Find the input box (using 'contains' just in case there are hidden spaces in the label)
        Locator input = page.locator("//label[contains(normalize-space(text()), '" + label + "')]/following-sibling::div//input").first();
        input.scrollIntoViewIfNeeded();

        // 2. Click the input box (NO force click! If it's covered, we want it to fail over to JS)
        try {
            // Try a normal human click first
            input.click(new Locator.ClickOptions().setTimeout(3000));
        } catch (Exception e) {
            log.warn("Standard click intercepted for '{}'. Forcing JS click...", label);
            // Bypass the virtual mouse entirely and force the browser to click the element
            input.evaluate("node => node.click()");
        }

        // 3. CRITICAL: Wait for the dropdown menu to ACTUALLY appear on the screen
        Locator activeDropdown = page.locator(".el-select-dropdown:visible").last();
        try {
            activeDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        } catch (Exception e) {
            throw new RuntimeException("The dropdown menu for '" + label + "' failed to open after clicking!", e);
        }

        // 4. Find the option STRICTLY inside the active dropdown
        Locator option = activeDropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();

        // 5. Scroll to the option inside the dropdown and click it
        option.scrollIntoViewIfNeeded();
        option.click(new Locator.ClickOptions().setForce(true));

        log.info("Successfully selected '{}'.", optionText);
        page.waitForTimeout(300); // Small buffer before moving to the next dropdown
    }

    /**
     * Dedicated method to handle the Udyam Typing, Clicking, and Verify Alert
     */
    private void handleUdyamVerification(String udyamNumber) {
        log.info("Handling Udyam Verification for: {}", udyamNumber);

        // 1. Click the Udyam input box to activate the dropdown
        Locator uanInput = page.locator("//label[text()='ENTITY UAN/UDYAM NUMBER']/following-sibling::div//input").first();
        uanInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        uanInput.click(new Locator.ClickOptions().setForce(true));

        // This fires the native JavaScript events Vue needs to trigger the dropdown filter
        page.keyboard().type(udyamNumber, new Keyboard.TypeOptions().setDelay(50));

        // 3. Wait dynamically for the specific option to render in the floating list
        Locator udyamOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(udyamNumber))
                .first();

        try {
            // Wait up to 5 seconds for the list to populate (handles network/debounce delays)
            udyamOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            udyamOption.click(new Locator.ClickOptions().setForce(true));
            log.info("Selected Udyam Number from dropdown.");
        } catch (Exception e) {
            log.warn("Dropdown option did not appear in time. Forcing 'Enter' key as fallback...");
            page.keyboard().press("Enter");
        }

        // Give the UI a split second to register the selection before clicking Verify
        page.waitForTimeout(500);

        // 4. Click Verify
        Locator verifyBtn = page.locator(".verify-btn").first();
        verifyBtn.click(new Locator.ClickOptions().setForce(true));
        log.info("Clicked Verify button.");

        // 5. Handle the specific Element UI Message Box popup ("Are you sure...")
        try {
            Locator messageBox = page.locator(".el-message-box").first();
            messageBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));

            Locator okBtn = messageBox.locator(".el-message-box__btns button").filter(new Locator.FilterOptions().setHasText("OK")).first();
            okBtn.click();
            log.info("Clicked OK on the Udyam verification popup.");

            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("Udyam confirmation popup did not appear, proceeding...");
        }
    }

    /**
     * Clicks the back arrow to exit the Company Details form, then clicks the main
     * Business Details back arrow to return to the main Application Dashboard.
     */
    public void navigateBackToAppDetails() {
        log.info("Navigating back to the main Application Details page...");

        // --- Step 1: Close the inner "Company Details" Modal ---
        log.info("Closing inner Company Details modal...");
        Locator firstBackBtn = getPage().locator(".el-dialog__body button.close-btn:has(.el-icon-arrow-left)").last();

        try {
            if (firstBackBtn.isVisible()) {
                firstBackBtn.hover(new Locator.HoverOptions().setForce(true));
                firstBackBtn.click(new Locator.ClickOptions().setForce(true));
                log.info("Clicked inner modal back arrow.");
                getPage().waitForTimeout(1000); // Allow Element UI transition to finish
            }
        } catch (Exception e) {
            log.warn("Could not click inner modal back arrow, or it was already closed. " + e.getMessage());
        }

        // --- Step 2: Dismiss any intermediate floating popups (Safety catch) ---
        log.info("Clicking outside to dismiss any floating menus...");
        getPage().mouse().click(10, 10);
        getPage().waitForTimeout(500);

        // --- Step 3: Click the main "Business Details" Back Arrow ---
        log.info("Clicking the main Business Details back arrow...");

        // This targets the specific arrow next to the "Business Details" header text
        Locator mainBackBtn = getPage().locator(".business-heading button.close-btn:has(.el-icon-arrow-left)").first();

        try {
            // Force hover is critical here because the CSS hides the button until the mouse is over the header
            mainBackBtn.hover(new Locator.HoverOptions().setForce(true));
            mainBackBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
            mainBackBtn.click(new Locator.ClickOptions().setForce(true));
            log.info("Successfully clicked main Business Details back arrow.");
        } catch (Exception e) {
            log.error("Failed to click the main back arrow. Try pressing Escape as fallback.");
            getPage().keyboard().press("Escape");
        }

        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Returned to main Application Details screen.");
    }

}
