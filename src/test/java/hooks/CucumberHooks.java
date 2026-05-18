package hooks;

import data.TestDataProvider;
import dynamicData.DynamicDataClass;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.Utils.ScreenshotUtil;

public class CucumberHooks {
    private static final Logger log = LogManager.getLogger(CucumberHooks.class);

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  STARTING SCENARIO: {}", scenario.getName());
        log.info("═══════════════════════════════════════════════════════════════");
        BaseTest.startBrowserInstance();
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Take screenshot on failure
        if (scenario.isFailed()) {
            try {
                ScreenshotUtil.saveScreenshot(BaseTest.getPage(),
                        "FAILED_" + scenario.getName().replaceAll("[^a-zA-Z0-9]", "_"),
                        "failures");
            } catch (Exception e) {
                log.warn("Could not capture failure screenshot: {}", e.getMessage());
            }
        }

        // Print execution summary
        printTestSummary(scenario);

        // Cleanup
        BaseTest.teardownBrowserInstance();
        BaseTest.cleanupStorageStates();
        TestDataProvider.cleanup();
    }

    private void printTestSummary(Scenario scenario) {
        String partnerLoanId = safeGet(() -> DynamicDataClass.get().getPartnerLoanId());
        String appFormId = safeGet(() -> String.valueOf(DynamicDataClass.getValue("appFormId")));
        String panCard = safeGet(() -> String.valueOf(DynamicDataClass.getValue("pan_card")));
        String mobileNumber = safeGet(() -> String.valueOf(DynamicDataClass.getValue("mobile_number")));

        log.info("");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  TEST EXECUTION SUMMARY");
        log.info("  ─────────────────────────────────────────────────────────────");
        log.info("  Scenario        : {}", scenario.getName());
        log.info("  Status          : {}", scenario.getStatus());
        log.info("  ─────────────────────────────────────────────────────────────");
        log.info("  Partner Loan ID : {}", partnerLoanId);
        log.info("  AppForm ID      : {}", appFormId);
        log.info("  PAN Card        : {}", panCard);
        log.info("  Mobile Number   : {}", mobileNumber);
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("");
    }

    private String safeGet(java.util.function.Supplier<String> supplier) {
        try {
            String value = supplier.get();
            return (value == null || "null".equals(value)) ? "N/A" : value;
        } catch (Exception e) {
            return "N/A";
        }
    }
}
