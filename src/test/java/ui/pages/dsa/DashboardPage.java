package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardPage {
    private final Page page;
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);
    // New Constants for Application Initiation
    private static final String LOAN_TYPE_DROPDOWN = ".ant-select-selector";
    private static final String DROPDOWN_PLACEHOLDER = "Choose Loan Type";
    private static final String SELECT_ITEM_OPTION = ".ant-select-item-option";
    private static final String ADD_APPLICATION_BTN_TEXT = "Add Application";

    public DashboardPage(Page page) {
        this.page = page;
    }

    /**
     * Selects a loan type from the Ant Design dropdown and clicks Add Application.
     * @param loanType The exact text of the loan (e.g., "Business Loans")
     */
    public void initiateApplication(String loanType) {
        log.info("Attempting to initiate application for loan type: {}", loanType);

        // 1. Click the dropdown trigger
        page.locator(LOAN_TYPE_DROPDOWN)
                .filter(new Locator.FilterOptions().setHasText(DROPDOWN_PLACEHOLDER))
                .click();

        // 2. Click the specific option in the dropdown list
        page.locator(SELECT_ITEM_OPTION)
                .filter(new Locator.FilterOptions().setHasText(loanType))
                .click();

        // 3. Click the Add Application button
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(ADD_APPLICATION_BTN_TEXT))
                .click();

        log.info("Successfully selected {} and clicked {} button", loanType, ADD_APPLICATION_BTN_TEXT);
    }
}
