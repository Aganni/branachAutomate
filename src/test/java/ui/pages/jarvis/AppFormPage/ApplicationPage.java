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
     * Waits for the backend to sync and the UI to update the Assignee name.
     * @param expectedAssignee The name of the user expected to own the application (e.g., "Tenjin")
     */
    private void waitForAssignee(String expectedAssignee) {
        log.info("Waiting for backend to assign application to: [{}]...", expectedAssignee);

        Locator assigneeLabel = page.locator(".app-layout-infos .appId")
                .filter(new Locator.FilterOptions().setHasText("Assigned to : " + expectedAssignee))
                .first();

        assigneeLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        log.info("Application is now successfully assigned to: [{}]", expectedAssignee);

        page.waitForTimeout(1000);
    }

    /**
     * Selects an action and handles the Accept Modal or Authorization Errors.
     * @param actionName The text of the action from the feature file
     * @param scenarioName Used for saving screenshots if it fails
     */
    public void selectApplicationActionAndAccept(String actionName, String scenarioName) {
        // Skip wait if moving to Login Desk
        if (!"Move to Login Desk".equalsIgnoreCase(actionName)) {
            waitForAssignee("Tenjin");
        }

        log.info("Selecting Application Action: [{}]", actionName);

        Locator actionsDropdown = page.getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));

        Locator actionOption = page.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click();
        log.info("Successfully clicked action: [{}]", actionName);

        // Handle the intermittent FI Warning popup
        handleOptionalFIWarning();

        handleActionResponse(actionName, scenarioName);
    }

    /**
     * Selects an action (like Move to Credit Approval) that requires Level and Assignee selection.
     */
    public void selectActionWithAssignment(String actionName, String level, String assigneeEmail, String scenarioName) {
        // Skip wait if moving to Login Desk
        if (!"Move to Login Desk".equalsIgnoreCase(actionName)) {
            waitForAssignee("Tenjin");
        }

        log.info("Selecting Application Action: [{}] with assignment to [{}]", actionName, assigneeEmail);

        Locator actionsDropdown = page.getByPlaceholder("Application Actions");
        actionsDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.click(new Locator.ClickOptions().setForce(true));

        Locator actionOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click(new Locator.ClickOptions().setForce(true));

        // Handle the intermittent FI Warning popup
        handleOptionalFIWarning();

        page.waitForTimeout(1000);

        log.info("Selecting Level: {}", level);
        Locator levelInput = page.locator("//label[text()='Level']/following-sibling::div//input").first();
        levelInput.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);

        Locator levelOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(level)).first();
        levelOption.click(new Locator.ClickOptions().setForce(true));

        log.info("Searching and selecting Assignee: {}", assigneeEmail);
        Locator assignInput = page.locator("//label[text()='Assigned to']/following-sibling::div//input").first();
        assignInput.click(new Locator.ClickOptions().setForce(true));

        page.keyboard().type("tenjin");
        page.waitForTimeout(1000);

        Locator assigneeOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(assigneeEmail)).first();
        assigneeOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assigneeOption.click(new Locator.ClickOptions().setForce(true));

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

        Locator errorToast = page.locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Authorization Error"));
        Locator acceptModal = page.locator(".el-dialog__body .accept-btn"); // The Accept button in your HTML
        Locator successToast = page.locator(".el-notification__title").filter(new Locator.FilterOptions().setHasText("Success"));

        try {
            acceptModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));

            log.info("Confirmation modal appeared. Clicking Accept...");
            acceptModal.click();
            successToast.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            log.info("Success toast verified. Workflow updated successfully.");

            page.waitForLoadState(LoadState.NETWORKIDLE);

        } catch (Exception e) {
            if (errorToast.isVisible()) {
                String errorMsg = page.locator(".el-notification__content").innerText();
                log.error("Authorization Error blocked the workflow: {}", errorMsg);

                ScreenshotUtil.saveScreenshot(page, "AuthError_" + actionName.replace(" ", ""), scenarioName);

                throw new AssertionError("Failed to move workflow. Authorization Error: " + errorMsg);
            } else {
                log.error("Unknown error occurred after clicking Application Action.");
                ScreenshotUtil.saveScreenshot(page, "UnknownError_" + actionName.replace(" ", ""), scenarioName);
                throw new RuntimeException("Expected confirmation modal did not appear for action: " + actionName, e);
            }
        }
    }

    /**
     * Checks for the intermittent "No FI has been triggered" warning popup.
     * Clicks "Yes" if it appears, otherwise safely proceeds.
     */
    private void handleOptionalFIWarning() {
        // Strictly locate the message box containing the specific warning text
        Locator fiWarningBox = page.locator(".el-message-box")
                .filter(new Locator.FilterOptions().setHasText("No FI has been triggered"));

        try {
            // Wait up to 3 seconds for this specific popup to appear
            fiWarningBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            log.info("FI Warning popup appeared: 'No FI has been triggered...'. Clicking 'Yes'.");

            // Strictly locate the "Yes" primary button inside that specific message box
            Locator yesBtn = fiWarningBox.locator(".el-message-box__btns button.el-button--primary")
                    .filter(new Locator.FilterOptions().setHasText("Yes"))
                    .first();
            yesBtn.click(new Locator.ClickOptions().setForce(true));

            // Wait a moment for the fade-out animation before the next modal renders
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("No FI warning popup detected. Proceeding with standard flow.");
        }
    }
}