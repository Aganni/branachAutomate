package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class AppFormActionsPage extends BaseTest {

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Selects an action and handles the Accept Modal or Authorization Errors.
     * @param actionName The text of the action from the feature file
     * @param scenarioName Used for saving screenshots if it fails
     */
    public void selectApplicationActionAndAccept(String actionName, String scenarioName) {
        log.info("Selecting Application Action: [{}]", actionName);

        // 1. Open the Dropdown
        Locator actionsDropdown = getPage().getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));

        // 2. Click the specific action
        Locator actionOption = getPage().locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click();
        log.info("Successfully clicked action: [{}]", actionName);

        // 3. Handle the UI Response (Error Toast vs Accept Modal)
        handleActionResponse(actionName, scenarioName);
    }

    /**
     * Internal method to check if the action succeeded or threw an Auth Error.
     */
    private void handleActionResponse(String actionName, String scenarioName) {
        log.info("Waiting for system response after clicking action...");

        // Define locators for what might appear on screen
        Locator errorToast = getPage().locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Authorization Error"));
        Locator acceptModal = getPage().locator(".el-dialog__body .accept-btn"); // The Accept button in your HTML
        Locator successToast = getPage().locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Success"));

        try {
            // Wait up to 5 seconds for the "Accept" modal to appear
            acceptModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

            // If we reach here, the modal opened successfully. Click Accept.
            log.info("Confirmation modal appeared. Clicking Accept...");
            acceptModal.click();

            // Wait for the Success toast to appear
            successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            log.info("Success toast verified. Workflow updated successfully.");

            getPage().waitForLoadState(LoadState.NETWORKIDLE);

        } catch (Exception e) {
            // If the Accept modal did NOT appear within 5 seconds, check for the Authorization Error
            if (errorToast.isVisible()) {
                String errorMsg = getPage().locator(".el-notification__content").innerText();
                log.error("Authorization Error blocked the workflow: {}", errorMsg);

                // Capture Screenshot using your Utility
                ScreenshotUtil.saveScreenshot(getPage(), "AuthError_" + actionName.replace(" ", ""), scenarioName);

                throw new AssertionError("Failed to move workflow. Authorization Error: " + errorMsg);
            } else {
                // Some other unknown error occurred (neither modal nor expected error toast appeared)
                log.error("Unknown error occurred after clicking Application Action.");
                ScreenshotUtil.saveScreenshot(getPage(), "UnknownError_" + actionName.replace(" ", ""), scenarioName);
                throw new RuntimeException("Expected confirmation modal did not appear for action: " + actionName, e);
            }
        }
    }


}
