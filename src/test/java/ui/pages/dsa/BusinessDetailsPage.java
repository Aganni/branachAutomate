package ui.pages.dsa;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BusinessDetailsPage extends BaseTest {

    private static final String ENTITY_PAN_INPUT = "input[name='entityPan']";
    private static final String ENTITY_NAME_INPUT = "input[name='entityName']";

    private static final String CONTINUE_FETCH_BTN = "button:has-text('Continue to fetch details linked to this PAN')";
    private static final String OP_ADDRESS_LINE_1 = "input[placeholder='Operational Address (Line 1)']";
    private static final String OP_ADDRESS_LINE_2 = "textarea[name='operationalAddressLine2']";
    private static final String OP_STATE = "input[name='operationalAddressState']";
    private static final String OWNERSHIP_SELECT = "#operationalAddressOwnership";
    private static final String SAME_ADDRESS_RADIO = "input[name='isResidentialAddressSameAsOperationalAddress']";

    // More Business Details
    private static final String ENTITY_EMAIL = "input[placeholder='Entity Email']";
    private static final String ENTITY_CONTACT = "input[placeholder='Entity Contact Number']";
    private static final String REGISTRATION_DATE = "input[placeholder='Date of Registration']";
    private static final String TURNOVER = "input[placeholder=\"Last Year's Turnover\"]";
    private static final String INDUSTRY_SUB_SECTOR = "#industrySubType";

    // Loan Requirements
    private static final String TENURE = "input[name='tenure']";
    private static final String LOAN_AMOUNT = "input[name='loanAmount']";
    private static final String END_USE = "#enduse";

    private static final String SUBMIT_BTN = "button:has-text('Submit')";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    /**
     * Enters the PAN and clicks the Verify button
     */
    public void enterPanAndVerify(String panNumber) {
        log.info("Entering Entity PAN: {}", panNumber);
        // 1. Fill the PAN input
        getPage().locator(ENTITY_PAN_INPUT).fill(panNumber);
        // 2. Click the Verify button
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Verify")).click();
        log.info("Clicked Verify button for PAN");
    }

    /**
     * Verifies the auto-populated Entity Name matches the expected value
     */
    public void verifyAutoPopulatedEntityName(String expectedName) {
        log.info("Verifying auto-populated Entity Name is: {}", expectedName);
        Locator entityNameField = getPage().locator(ENTITY_NAME_INPUT);
        assertThat(entityNameField).hasValue(expectedName);
        log.info("Entity Name verified successfully!");
    }

    public void clickContinueToFetchDetails() {
        getPage().locator(CONTINUE_FETCH_BTN).click();
        log.info("Clicked Continue to fetch details");
    }


    public void fillOperationalAddress() {
        String addressValue = "2/19 RAM WADI KUNTIDEVI, JOGESHWARI EAST";

        Locator input = getPage().locator(OP_ADDRESS_LINE_1);
        input.click(); // Opens the dropdown

        // Find the option
        Locator dropdownOption = getPage().getByRole(AriaRole.LISTBOX)
                .getByText(addressValue, new Locator.GetByTextOptions().setExact(false))
                .first();
        dropdownOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dropdownOption.click();

        log.info("Selected address from dropdown: {}", addressValue);
    }

    public void verifyAutoPopulatedAddress(Map<String, String> data) {
        if (data.containsKey("Operational Address (Line 2)")) {
            assertThat(getPage().locator(OP_ADDRESS_LINE_2)).hasValue(data.get("Operational Address (Line 2)"));
        }
        if (data.containsKey("State")) {
            assertThat(getPage().locator(OP_STATE)).hasValue(data.get("State"));
        }
        // Add Pincode and City assertions here if you have their correct locators
        log.info("Auto-populated address details verified successfully");
    }

    public void selectOwnership(String ownershipType) {
        getPage().locator(OWNERSHIP_SELECT).click();
        getPage().getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(ownershipType).setExact(true)).click();
        log.info("Selected Ownership: {}", ownershipType);
    }

    public void selectSameAsOperationalAddress(String yesOrNo) {
        // value is lower case "yes" or "no" in the HTML
        String value = yesOrNo.toLowerCase();
        getPage().locator(SAME_ADDRESS_RADIO + "[value='" + value + "']").click();
        log.info("Selected Same as Operational Address: {}", yesOrNo);
    }

    public void fillMoreBusinessDetails(Map<String, String> data) {
        log.info("Filling more business details from provided data");
        if (data.containsKey("Entity Email")) getPage().locator(ENTITY_EMAIL).fill(data.get("Entity Email"));
        if (data.containsKey("Entity Contact Number")) getPage().locator(ENTITY_CONTACT).fill(data.get("Entity Contact Number"));
        if (data.containsKey("Date of Registration")) getPage().locator(REGISTRATION_DATE).fill(data.get("Date of Registration"));
        if (data.containsKey("Last Year's Turnover")) getPage().locator(TURNOVER).fill(data.get("Last Year's Turnover"));
    }

    public void selectIndustrySubSector(String sector) {
        getPage().locator(INDUSTRY_SUB_SECTOR).click();
        getPage().getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(sector).setExact(true)).click();
        log.info("Selected Industry Sub-Sector: {} from sector dropdown", sector);
    }

    public void fillLoanRequirements(Map<String, String> data) {
        if (data.containsKey("Tenure")) getPage().locator(TENURE).fill(data.get("Tenure"));
        if (data.containsKey("Loan Amount")) getPage().locator(LOAN_AMOUNT).fill(data.get("Loan Amount"));
        if (data.containsKey("End Use")) {
            getPage().locator(END_USE).click();
            getPage().getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(data.get("End Use")).setExact(true)).click();
        }
        log.info("Filled loan requirements from provided data");
    }

    public void clickSubmit() {
        getPage().locator(SUBMIT_BTN).click();
        log.info("Clicked Submit button");
    }
}
