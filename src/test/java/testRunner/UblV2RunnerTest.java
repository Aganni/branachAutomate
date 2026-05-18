package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Primary UBL test runner.
 * Uses combined step definitions with YAML-driven test data.
 *
 * Run with: mvn test -Denv=uat
 * Run on INT: mvn test -Denv=int
 */
@CucumberOptions(features = "features/ubl.feature", glue = {
                "hooks",
                "data",
                "ui.stepDefinitions.dsa",
                "ui.stepDefinitions.jarvis",
                "backend.stepDefinitions"
}, tags = "@UBL")
public class UblV2RunnerTest extends AbstractTestNGCucumberTests {
}
