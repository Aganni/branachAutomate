package ui.pages.jarvis.AppFormPage;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import ui.Utils.ScreenshotUtil;

public class ApplicationPage extends BaseTest {

    private final Page page;

    // ── Actions that skip assignee wait ──────────────────────────────────────
    private static final String[] SKIP_ASSIGNEE_WAIT_ACTIONS = {
            "Move to Login Desk", "Move to Sanction Approval", "ReAssign", "Move to QC Approval", "Approve this Application"
    };

    public ApplicationPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Selects an action from Application Actions dropdown and handles the response.
     * Waits for assignee unless action is in the skip list.
     */
    public void selectApplicationActionAndAccept(String actionName, String scenarioName) {
        if (!shouldSkipAssigneeWait(actionName)) {
            waitForAssignee("Tenjin");
        }

        selectActionFromDropdown(actionName);
        handleOptionalFIWarning();
        handleActionResponse(actionName, scenarioName);
    }

    /**
     * Selects an action that requires level + user assignment (e.g., Move to Credit Approval).
     * Opens the assignment modal, selects level and user, then confirms.
     */
    public void selectActionWithAssignment(String actionName, String level, String assigneeEmail, String scenarioName) {
        if (!shouldSkipAssigneeWait(actionName)) {
            waitForAssignee("Tenjin");
        }

        selectActionFromDropdown(actionName);
        handleOptionalFIWarning();
        page.waitForTimeout(1000);

        selectLevelAndAssignee(level, assigneeEmail);
        handleActionResponse(actionName, scenarioName);

        page.waitForTimeout(1000);
        log.info("Refreshing the page...");
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Page reloaded and network is idle.");
    }

    /**
     * Reassigns the application to a specific level and user.
     * Selects "ReAssign" from Application Actions, fills the modal, and confirms.
     * Does NOT wait for assignee before action (since we're reassigning to self).
     */
    public void reassignApplication(String level, String assigneeEmail, String scenarioName) {
        log.info("Reassigning application to level [{}], user [{}]", level, assigneeEmail);

        selectActionFromDropdown("ReAssign");
        page.waitForTimeout(1000);

        // Wait for ReAssign modal
        Locator reassignModal = page.locator(".assign-container");
        reassignModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("ReAssign modal appeared.");

        selectLevelAndAssignee(level, assigneeEmail);

        // Click ReAssign button
        Locator reassignBtn = page.locator(".assign-btn:has-text('ReAssign')");
        reassignBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        reassignBtn.click();
        log.info("Clicked ReAssign button.");

        // Wait for success notification
        Locator successNotification = page.locator(".el-notification__title")
                .filter(new Locator.FilterOptions().setHasText("Success"));
        try {
            successNotification.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            log.info("ReAssign successful — 'Successfully Assigned appForm' notification received.");
        } catch (Exception e) {
            log.error("ReAssign may have failed — no success notification received.");
            ScreenshotUtil.saveScreenshot(page, "ReAssign_NoSuccess", scenarioName);
            throw new AssertionError("ReAssign failed: no success notification appeared.", e);
        }

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean shouldSkipAssigneeWait(String actionName) {
        for (String skipAction : SKIP_ASSIGNEE_WAIT_ACTIONS) {
            if (skipAction.equalsIgnoreCase(actionName)) {
                return true;
            }
        }
        return false;
    }

    private void waitForAssignee(String expectedAssignee) {
        log.info("Waiting for backend to assign application to: [{}]...", expectedAssignee);

        Locator assigneeLabel = page.locator(".app-layout-infos .appId")
                .filter(new Locator.FilterOptions().setHasText("Assigned to : " + expectedAssignee))
                .first();

        assigneeLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        log.info("Application is now successfully assigned to: [{}]", expectedAssignee);
        page.waitForTimeout(1000);
    }

    private void selectActionFromDropdown(String actionName) {
        log.info("Selecting Application Action: [{}]", actionName);

        // Try both possible placeholders for the actions dropdown
        Locator actionsDropdown = page.getByPlaceholder("Application Actions");
        if (actionsDropdown.count() == 0 || !actionsDropdown.first().isVisible()) {
            actionsDropdown = page.getByPlaceholder("moveToNextStage");
        }

        actionsDropdown.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionsDropdown.first().click(new Locator.ClickOptions().setForce(true));

        Locator actionOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(actionName))
                .first();
        actionOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        actionOption.click(new Locator.ClickOptions().setForce(true));
        log.info("Successfully clicked action: [{}]", actionName);
    }

    private void selectLevelAndAssignee(String level, String assigneeEmail) {
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

        // Type partial name to search
        String searchTerm = assigneeEmail.split("@")[0].split("\\.")[0]; // "tenjin" from "tenjin.user@..."
        page.keyboard().type(searchTerm);
        page.waitForTimeout(1000);

        Locator assigneeOption = page.locator("li.el-select-dropdown__item:visible")
                .filter(new Locator.FilterOptions().setHasText(assigneeEmail)).first();
        assigneeOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assigneeOption.click(new Locator.ClickOptions().setForce(true));
        log.info("Assignee [{}] selected.", assigneeEmail);
    }

    private void handleActionResponse(String actionName, String scenarioName) {
        log.info("Waiting for system response after clicking action...");

        Locator errorToast = page.locator(".el-notification__title")
                .filter(new Locator.FilterOptions().setHasText("Authorization Error"));
        Locator acceptModal = page.locator(".el-dialog__body .accept-btn");
        Locator successToast = page.locator(".el-notification__title")
                .filter(new Locator.FilterOptions().setHasText("Success"));

        try {
            acceptModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

            log.info("Confirmation modal appeared. Clicking Accept/Disburse...");
            acceptModal.click();

            // Wait for success notification (appears for 2-4 seconds)
            successToast.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            log.info("Success notification verified. Action '{}' completed successfully.", actionName);

            page.waitForLoadState(LoadState.NETWORKIDLE);

        } catch (Exception e) {
            if (errorToast.isVisible()) {
                String errorMsg = page.locator(".el-notification__content").first().innerText();
                log.error("Authorization Error blocked the workflow: {}", errorMsg);
                ScreenshotUtil.saveScreenshot(page, "AuthError_" + actionName.replace(" ", ""), scenarioName);
                throw new AssertionError("Failed to move workflow. Authorization Error: " + errorMsg);
            } else {
                log.error("Unknown error occurred after clicking Application Action: {}", actionName);
                ScreenshotUtil.saveScreenshot(page, "UnknownError_" + actionName.replace(" ", ""), scenarioName);
                throw new RuntimeException("Expected confirmation modal did not appear for action: " + actionName, e);
            }
        }
    }

    private void handleOptionalFIWarning() {
        Locator fiWarningBox = page.locator(".el-message-box")
                .filter(new Locator.FilterOptions().setHasText("No FI has been triggered"));

        try {
            fiWarningBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            log.info("FI Warning popup appeared. Clicking 'Yes'.");

            Locator yesBtn = fiWarningBox.locator(".el-message-box__btns button.el-button--primary")
                    .filter(new Locator.FilterOptions().setHasText("Yes"))
                    .first();
            yesBtn.click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("No FI warning popup detected. Proceeding.");
        }
    }
}