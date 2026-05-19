package ui.pages.jarvis;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import hooks.BaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BureauViewPage extends BaseTest {

    // Tab navigation
    private static final String BUREAU_TAB_LINK = "a.tab-item:has(p:text('Bureau View'))";

    // Sidebar entity selectors
    private static final String SIDEBAR_ENTITY_BUSINESS = ".applicant-detail:has(.sidebarTypeText:text('Business'))";

    // Bureau action buttons
    private static final String PULL_COMMERCIAL_BUREAU_BTN = "button.el-button--success:has-text('Pull commercial bureau report')";
    private static final String VIEW_REPORT_BTN = "button.el-button--success:has-text('view report')";

    // Storage path for downloaded reports
    private static final String REPORT_DOWNLOAD_DIR = System.getProperty("user.dir") + "/src/test/resources/testdata/bureau-reports";

    /**
     * Returns a thread-safe file name for the commercial cibil report.
     * Uses thread ID to avoid conflicts during parallel execution.
     */
    private String getReportFileName(String suggestedName) {
        if (suggestedName != null && !suggestedName.isEmpty()) {
            // Prefix with thread ID for parallel safety
            return "thread_" + Thread.currentThread().threadId() + "_" + suggestedName;
        }
        return "commercial_cibil_thread_" + Thread.currentThread().threadId() + ".html";
    }

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Navigates to the Bureau View tab from the application tabs.
     */
    public void navigateToBureauViewTab() {
        log.info("Navigating to Bureau View tab...");
        getPage().locator(BUREAU_TAB_LINK).click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Bureau View tab loaded.");
    }

    /**
     * Clicks on the Business entity in the sidebar to switch to Commercial Cibil view.
     */
    public void selectBusinessEntity() {
        log.info("Selecting Business entity from sidebar...");
        Locator businessEntity = getPage().locator(SIDEBAR_ENTITY_BUSINESS).first();
        businessEntity.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        businessEntity.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Business entity selected. Commercial Cibil view should be active.");
    }

    /**
     * Clicks the "Pull commercial bureau report" button and waits for the report to be ready.
     * Waits up to 60 seconds for "view report" button to appear.
     */
    public void pullCommercialBureauReport() {
        log.info("Clicking 'Pull commercial bureau report' button...");

        Locator pullBtn = getPage().locator(PULL_COMMERCIAL_BUREAU_BTN);
        pullBtn.waitFor(new Locator.WaitForOptions().setTimeout(15_000));
        pullBtn.click();
        log.info("Pull initiated. Waiting for report to be ready...");

        // Wait for "view report" button to appear with 60s timeout
        Locator viewReportBtn = getPage().locator(VIEW_REPORT_BTN);
        viewReportBtn.waitFor(new Locator.WaitForOptions().setTimeout(60_000));
        log.info("'view report' button is now visible. Report pull completed.");
    }

    /**
     * Clicks the "view report" button which triggers an HTML file download.
     * Captures the download and stores it locally in testdata/bureau-reports/.
     *
     * @return the absolute path to the downloaded HTML report file
     */
    public String downloadCommercialCibilReport() {
        log.info("Clicking 'view report' to download the Commercial Cibil HTML report...");

        // Playwright download handling
        Download download = getPage().waitForDownload(() -> {
            getPage().locator(VIEW_REPORT_BTN).click();
        });

        // Save to testdata directory
        Path downloadDir = Paths.get(REPORT_DOWNLOAD_DIR);
        downloadDir.toFile().mkdirs();

        String fileName = getReportFileName(download.suggestedFilename());

        Path savedPath = downloadDir.resolve(fileName);
        download.saveAs(savedPath);

        log.info("Commercial Cibil report downloaded and saved to: {}", savedPath.toAbsolutePath());
        return savedPath.toAbsolutePath().toString();
    }

    /**
     * Full flow: Navigate to Bureau View -> Select Business -> Pull -> Download report.
     *
     * @return the absolute path to the downloaded HTML report file
     */
    public String pullAndDownloadCommercialCibilReport() {
        navigateToBureauViewTab();
        selectBusinessEntity();
        pullCommercialBureauReport();
        return downloadCommercialCibilReport();
    }
}
