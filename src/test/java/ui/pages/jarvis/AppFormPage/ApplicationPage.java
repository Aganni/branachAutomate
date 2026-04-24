package ui.pages.jarvis.AppFormPage;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class ApplicationPage extends BaseTest {

    private final Page page;

    public ApplicationPage(Page page) {
        this.page = page;
    }

    /**
     * Selects an action and handles the Accept Modal or Authorization Errors.
     * @param actionName The text of the action from the feature file
     * @param scenarioName Used for saving screenshots if it fails
     */
    public void selectApplicationActionAndAccept(String actionName, String scenarioName) {
        log.info("Selecting Application Action: [{}]", actionName);

        // 1. Open the Dropdown
        Locator actionsDropdown = page.getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));

        // 2. Click the specific action
        Locator actionOption = page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click();
        log.info("Successfully clicked action: [{}]", actionName);

        // 3. Handle the UI Response (Error Toast vs Accept Modal)
        handleActionResponse(actionName, scenarioName);
    }

    /**
     * Selects an action (like Move to Credit Approval) that requires Level and Assignee selection.
     */
    public void selectActionWithAssignment(String actionName, String level, String assigneeEmail, String scenarioName) {
        log.info("Selecting Application Action: [{}] with assignment to [{}]", actionName, assigneeEmail);

        // 1. Open the Action Dropdown
        Locator actionsDropdown = page.getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));

        // 2. Click the specific action
        Locator actionOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click(new Locator.ClickOptions().setForce(true));

        // Wait for the modal to fully render
        page.waitForTimeout(1000);

        // 3. Handle the "Level" Dropdown
        log.info("Selecting Level: {}", level);
        Locator levelInput = page.locator("//label[text()='Level']/following-sibling::div//input").first();
        levelInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500); // Allow dropdown animation

        Locator levelOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(level)).first();
        levelOption.click(new Locator.ClickOptions().setForce(true));

        // 4. Handle the "Assigned to" Searchable Dropdown
        log.info("Searching and selecting Assignee: {}", assigneeEmail);
        Locator assignInput = page.locator("//label[text()='Assigned to']/following-sibling::div//input").first();
        assignInput.click(new Locator.ClickOptions().setForce(true));

        // Use raw keyboard typing to bypass Element UI's "readonly" search mask
        page.keyboard().type("tenjin");
        page.waitForTimeout(1000); // Wait for the backend to filter the emails

        Locator assigneeOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(assigneeEmail)).first();
        assigneeOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assigneeOption.click(new Locator.ClickOptions().setForce(true));

        // 5. Submit and verify using your existing handler
        handleActionResponse(actionName, scenarioName);
      
        page.waitForTimeout(1000);

        log.info("Refreshing the page...");
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Page reloaded and network is idle.");
    }

    /**
     * Internal method to check if the action succeeded or threw an Auth Error.
     */
    private void handleActionResponse(String actionName, String scenarioName) {
        log.info("Waiting for system response after clicking action...");

        // Define locators for what might appear on screen
        Locator errorToast = page.locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Authorization Error"));
        Locator acceptModal = page.locator(".el-dialog__body .accept-btn"); // The Accept button in your HTML
        Locator successToast = page.locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Success"));

        try {
            // Wait up to 5 seconds for the "Accept" modal to appear
            acceptModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

            // If we reach here, the modal opened successfully. Click Accept.
            log.info("Confirmation modal appeared. Clicking Accept...");
            acceptModal.click();

            // Wait for the Success toast to appear
            successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            log.info("Success toast verified. Workflow updated successfully.");

            page.waitForLoadState(LoadState.NETWORKIDLE);

        } catch (Exception e) {
            // If the Accept modal did NOT appear within 5 seconds, check for the Authorization Error
            if (errorToast.isVisible()) {
                String errorMsg = page.locator(".el-notification__content").innerText();
                log.error("Authorization Error blocked the workflow: {}", errorMsg);

                // Capture Screenshot using your Utility
                ScreenshotUtil.saveScreenshot(page, "AuthError_" + actionName.replace(" ", ""), scenarioName);

                throw new AssertionError("Failed to move workflow. Authorization Error: " + errorMsg);
            } else {
                // Some other unknown error occurred (neither modal nor expected error toast appeared)
                log.error("Unknown error occurred after clicking Application Action.");
                ScreenshotUtil.saveScreenshot(page, "UnknownError_" + actionName.replace(" ", ""), scenarioName);
                throw new RuntimeException("Expected confirmation modal did not appear for action: " + actionName, e);
            }
        }
    }


}
