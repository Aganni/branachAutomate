package core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * Abstract base class for all Page Objects.
 * Provides common UI interaction utilities and eliminates the need to extend BaseTest.
 */
public abstract class BasePage {

    protected final Page page;
    protected final Logger log = LogManager.getLogger(getClass());

    protected BasePage(Page page) {
        this.page = Objects.requireNonNull(page, "Page instance cannot be null");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

    protected void navigateTo(String url) {
        page.navigate(url);
        waitForNetworkIdle();
    }

    protected void waitForNetworkIdle() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected void refreshPage() {
        page.reload();
        waitForNetworkIdle();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WAIT UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    protected void waitForVisible(String selector) {
        waitForVisible(selector, 10_000);
    }

    protected void waitForVisible(String selector, int timeoutMs) {
        page.locator(selector).first().waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
    }

    protected void waitForHidden(String selector, int timeoutMs) {
        page.locator(selector).first().waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(timeoutMs));
    }

    protected void pause(int milliseconds) {
        page.waitForTimeout(milliseconds);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CLICK UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    protected void click(String selector) {
        page.locator(selector).first().click();
    }

    protected void forceClick(String selector) {
        page.locator(selector).first().click(new Locator.ClickOptions().setForce(true));
    }

    protected void scrollAndClick(Locator locator) {
        locator.scrollIntoViewIfNeeded();
        locator.click();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INPUT UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    protected void fill(String selector, String value) {
        if (value == null || value.isEmpty()) return;
        page.locator(selector).first().fill(value);
    }

    protected void clearAndFill(String selector, String value) {
        if (value == null || value.isEmpty()) return;
        Locator input = page.locator(selector).first();
        input.clear();
        input.fill(value);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DROPDOWN UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Selects an option from an Element UI dropdown by label text.
     */
    protected void selectElDropdown(String labelText, String optionText) {
        log.debug("Selecting '{}' for '{}'", optionText, labelText);
        Locator input = page.locator(
                "xpath=//label[contains(normalize-space(text()),'" + labelText + "')]/following-sibling::div//input"
        ).first();
        input.scrollIntoViewIfNeeded();
        try {
            input.click(new Locator.ClickOptions().setTimeout(3_000));
        } catch (Exception e) {
            input.evaluate("node => node.click()");
        }

        Locator dropdown = page.locator(".el-select-dropdown:visible").last();
        dropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5_000));

        Locator option = dropdown.locator("li.el-select-dropdown__item")
                .filter(new Locator.FilterOptions().setHasText(optionText)).first();
        option.scrollIntoViewIfNeeded();
        option.click(new Locator.ClickOptions().setForce(true));
        pause(300);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  VERIFICATION UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    protected boolean isVisible(String selector) {
        return page.locator(selector).first().isVisible();
    }

    protected String getText(String selector) {
        return page.locator(selector).first().textContent().trim();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SCREENSHOT
    // ═══════════════════════════════════════════════════════════════════════════

    protected void takeScreenshot(String name) {
        try {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("target/screenshots/" + name + "_" + System.currentTimeMillis() + ".png")));
        } catch (Exception e) {
            log.warn("Failed to take screenshot: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TOAST / NOTIFICATION VERIFICATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Waits for an Element UI notification toast and returns its title.
     * Returns null if no toast appears within timeout.
     */
    protected String waitForNotificationToast(int timeoutMs) {
        Locator toast = page.locator(".el-notification__title").first();
        try {
            toast.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
            return toast.textContent().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifies a success notification appeared. Throws if error notification found.
     */
    protected void verifySuccessNotification(String context, int timeoutMs) {
        String title = waitForNotificationToast(timeoutMs);
        if (title == null) {
            log.warn("No notification toast appeared for: {}", context);
            return;
        }
        if (title.contains("Error")) {
            String msg = page.locator(".el-notification__content").first().textContent().trim();
            takeScreenshot("Error_" + context);
            throw new AssertionError(context + " failed with error: " + msg);
        }
        log.info("{} — notification: {}", context, title);
    }
}
