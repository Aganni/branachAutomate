package data;

import io.cucumber.java.en.Given;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataInitializationSteps {

    private static final Logger log = LogManager.getLogger(DataInitializationSteps.class);

    @Given("Initialize data for {string} loan of {string}")
    public void initializeTestData(String loanType, String lpc) {
        log.info("Initializing test data — LPC: [{}], Loan Type: [{}]", lpc, loanType);
        TestDataProvider.initialize(loanType, lpc);
        log.info("Test data ready. Environment: [{}]", hooks.BaseTest.environment);
    }
}
