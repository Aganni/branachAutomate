package ui.Utils;

import com.microsoft.playwright.Page;
import hooks.BaseTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenshotUtil extends BaseTest {

        // --- Screenshot Helper Method ---

        public static void saveScreenshot(Page page, String name, String featureScenarioName) {
            if (page == null) {
                log.warn("Cannot save screenshot: Page instance is null.");
                return;
            }
            try {
                String uniqueFileName = name+ "_" +".png";
                // Path is "screenshots/featureScenarioName/uniqueFileName.png"
                Path folder = Paths.get("screenshots", featureScenarioName);
                Files.createDirectories(folder);
                Path file = folder.resolve(uniqueFileName);

                page.screenshot(new Page.ScreenshotOptions().setPath(file).setFullPage(false));
                log.info("Screenshot saved successfully to: " + file);
            } catch (Exception e) {
                log.error("Failed to save screenshot: " + e.getMessage(), e);
            }
        }
}
