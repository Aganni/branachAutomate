package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hooks.BaseTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class QdePage extends BaseTest {

    // Input Locators
    private static final String PAN_INPUT = "input[name='panNumber']";
    private static final String NAME_INPUT = "input[name='name']";
    private static final String PHONE_INPUT = "input[placeholder='Phone No.']";
    private static final String SHAREHOLDING_INPUT = "input[name='shareHolding']";

    // Button Names
    private static final String VERIFY_BTN_NAME = "Verify";
    private static final String SAVE_BTN_NAME = "Save";
    private static final String SUBMIT_BTN_NAME = "Submit";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    public void enterPanAndVerify(String panNumber) {
        log.info("Entering Primary applicant PAN: {}", panNumber);
        getPage().locator(PAN_INPUT).fill(panNumber);

        // Clicks the verify button using the constant
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(VERIFY_BTN_NAME)).click();
        log.info("Clicked '{}' button for Primary applicant PAN", VERIFY_BTN_NAME);
    }

    public void verifyAutoPopulatedName(String expectedName) {
        log.info("Verifying auto-populated Primary applicant Name is: {}", expectedName);
        Locator nameField = getPage().locator(NAME_INPUT);

        // Playwright's auto-wait magic: waits until the API populates the field
        assertThat(nameField).hasValue(expectedName);
        log.info("Primary applicant Name verified successfully!");
    }

    public void enterMobileNumber(String mobileNumber) {
        log.info("Entering Primary applicant Mobile Number: {}", mobileNumber);
        getPage().locator(PHONE_INPUT).fill(mobileNumber);
    }

    public void enterShareholdingPercentage(String percentage) {
        log.info("Entering Shareholding Percentage: {}%", percentage);
        getPage().locator(SHAREHOLDING_INPUT).fill(percentage);
    }

    public void clickSaveButton() {
        log.info("Clicking '{}' button on QDE page", SAVE_BTN_NAME);
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(SAVE_BTN_NAME).setExact(true)).click();
    }

    public void clickSubmitButton() {
        log.info("Clicking '{}' button to submit QDE details", SUBMIT_BTN_NAME);
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(SUBMIT_BTN_NAME).setExact(true)).click();
    }
}