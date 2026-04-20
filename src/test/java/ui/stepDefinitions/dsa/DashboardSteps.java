package ui.stepDefinitions.dsa;

import hooks.BaseTest;
import io.cucumber.java.en.When;
import ui.pages.dsa.DashboardPage;

public class DashboardSteps {
    private final DashboardPage dashboardPage;

    public DashboardSteps() {
        this.dashboardPage = new DashboardPage(BaseTest.getPage());
    }
    @When("User selects {string} from the dropdown menu and initiates a new application")
    public void userSelectsLoanAndInitiates(String loanType) {
        dashboardPage.initiateApplication(loanType);
    }
}
