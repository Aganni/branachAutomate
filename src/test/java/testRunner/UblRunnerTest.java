package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = "features/ubl.feature", glue = {
                "hooks",
                "ui.stepDefinitions.dsa",
                "ui.stepDefinitions.jarvis",
                "backend.stepDefinitions"
}, tags = "@UBL")
public class UblRunnerTest extends AbstractTestNGCucumberTests {
}
