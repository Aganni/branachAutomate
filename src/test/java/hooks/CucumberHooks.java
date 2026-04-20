package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CucumberHooks {
    private static final Logger log = LogManager.getLogger(CucumberHooks.class);

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("Starting new browser for scenario: " + scenario.getName());
        BaseTest.startBrowserInstance();
    }

    @After
    public void afterScenario(Scenario scenario) {
        log.info("Tearing down browser for scenario: " + scenario.getName());
        BaseTest.teardownBrowserInstance();
        BaseTest.cleanupStorageStates();
    }
}
