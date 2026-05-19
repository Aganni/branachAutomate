package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hooks.BaseTest;

public class DashboardPage extends BaseTest {
    
    private final Page page;
    
    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String LOAN_TYPE_DROPDOWN = ".ant-select-selector";
    private static final String DROPDOWN_PLACEHOLDER = "Choose Loan Type";
    private static final String SELECT_ITEM_OPTION = ".ant-select-item-option";
    private static final String ADD_APPLICATION_BTN_TEXT = "Add Application";

    public DashboardPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    /**
     * Selects a loan type from the Ant Design dropdown and clicks Add Application.
     * @param loanType The exact text of the loan (e.g., "Business Loans")
     */
    public void initiateApplication(String loanType) {
        log.info("Attempting to initiate application for loan type: {}", loanType);

        page.locator(LOAN_TYPE_DROPDOWN)
                .filter(new Locator.FilterOptions().setHasText(DROPDOWN_PLACEHOLDER))
                .click();

        page.locator(SELECT_ITEM_OPTION)
                .filter(new Locator.FilterOptions().setHasText(loanType))
                .click();

        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(ADD_APPLICATION_BTN_TEXT))
                .click();

        log.info("Successfully selected {} and clicked {} button", loanType, ADD_APPLICATION_BTN_TEXT);
    }
}
