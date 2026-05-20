package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page object for Risk Category badge and popover interactions in Jarvis.
 * The badge uses Element UI (el-tag, el-popover) unlike Apollo's Mantine components.
 */
public class RiskCategoryPage extends BaseTest {

    private final Page page;

    // ── Locators ─────────────────────────────────────────────────────────────
    private static final String RISK_CATEGORY_BADGE = ".risk-category-badge-wrapper .risk-category-tag";
    private static final String RISK_CATEGORY_POPOVER = ".risk-category-popover";
    private static final String POPOVER_TABLE = ".risk-category-popover .risk-popover-table";
    private static final String POPOVER_TABLE_HEADERS = ".risk-popover-table thead th";
    private static final String POPOVER_TABLE_ROWS = ".risk-popover-table tbody tr";
    private static final String RISK_NOT_FOUND_TEXT = "Not able to fetch Risk Category";

    public RiskCategoryPage(Page page) {
        if (page == null) throw new IllegalArgumentException("Page instance cannot be null");
        this.page = page;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Verifies that the Risk Category badge is visible on the app form header.
     */
    public void verifyRiskCategoryBadgeVisible() {
        Locator badge = page.locator(RISK_CATEGORY_BADGE);
        badge.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        Assert.assertTrue(badge.isVisible(), "Risk Category badge is not visible on appForm header");
        log.info("Risk Category badge is visible on appForm header");
    }

    /**
     * Hovers over the Risk Category badge to trigger the el-popover.
     * Waits for the popover to become visible.
     */
    public void hoverOnRiskCategoryBadge() {
        Locator badge = page.locator(RISK_CATEGORY_BADGE);
        badge.hover();
        // Wait for the popover to appear (aria-hidden becomes false, display:none removed)
        page.locator(RISK_CATEGORY_POPOVER)
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        log.info("Hovered on Risk Category badge, popover is now visible");
    }

    /**
     * Verifies the popover table is displayed with correct structure.
     */
    public void verifyPopoverTableDisplayed() {
        Locator table = page.locator(POPOVER_TABLE);
        table.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        Assert.assertTrue(table.isVisible(), "Risk Category popover table is not visible");
        log.info("Risk Category popover table is displayed");
    }

    /**
     * Verifies the popover table has the expected column headers.
     */
    public void verifyPopoverTableColumns(String col1, String col2, String col3) {
        Locator headers = page.locator(POPOVER_TABLE_HEADERS);
        int headerCount = headers.count();
        Assert.assertTrue(headerCount >= 3, "Expected at least 3 columns but found " + headerCount);

        String actualCol1 = headers.nth(0).textContent().trim();
        String actualCol2 = headers.nth(1).textContent().trim();
        String actualCol3 = headers.nth(2).textContent().trim();

        Assert.assertEquals(actualCol1, col1, "First column header mismatch");
        Assert.assertEquals(actualCol2, col2, "Second column header mismatch");
        Assert.assertEquals(actualCol3, col3, "Third column header mismatch");
        log.info("Popover table columns verified: {}, {}, {}", col1, col2, col3);
    }

    /**
     * Verifies that a specific risk category with expected color badge is shown.
     * Color is determined by the CSS class: risk-low (green), risk-medium (yellow), risk-high (red).
     */
    public void verifyRiskCategoryColor(String expectedRiskCategory, String expectedColor) {
        Locator rows = page.locator(POPOVER_TABLE_ROWS);
        int rowCount = rows.count();
        boolean found = false;

        String expectedCssClass = mapColorToCssClass(expectedColor);

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            Locator riskBadge = row.locator("td").nth(2).locator("span.risk-badge-inline");
            String badgeText = riskBadge.textContent().trim();

            if (badgeText.equalsIgnoreCase(expectedRiskCategory)) {
                found = true;
                // Verify the CSS class matches expected color
                String classAttr = riskBadge.getAttribute("class");
                Assert.assertTrue(classAttr != null && classAttr.contains(expectedCssClass),
                        "Expected CSS class '" + expectedCssClass + "' for risk category '" + expectedRiskCategory
                                + "' but found class: " + classAttr);
                log.info("Found risk badge '{}' with class '{}' in popover row {}", badgeText, expectedCssClass, i);
                break;
            }
        }
        Assert.assertTrue(found, "Expected risk category '" + expectedRiskCategory + "' not found in popover");
        log.info("Verified risk category '{}' with color '{}' is displayed", expectedRiskCategory, expectedColor);
    }

    /**
     * Gets all applicant data from the Risk Category popover table.
     * Returns a list of maps with keys: applicantId, name, riskCategory
     */
    public List<Map<String, String>> getRiskCategoryPopoverData() {
        List<Map<String, String>> applicants = new ArrayList<>();
        Locator rows = page.locator(POPOVER_TABLE_ROWS);
        int rowCount = rows.count();

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            Locator cells = row.locator("td");

            String applicantId = cells.nth(0).textContent().trim();
            String name = cells.nth(1).textContent().trim();
            String riskCategory = cells.nth(2).locator("span.risk-badge-inline").textContent().trim();

            Map<String, String> applicant = new HashMap<>();
            applicant.put("applicantId", applicantId);
            applicant.put("name", name);
            applicant.put("riskCategory", riskCategory);
            applicants.add(applicant);
        }
        log.info("Extracted {} applicants from Risk Category popover", applicants.size());
        return applicants;
    }

    /**
     * Verifies that the popover displays multiple applicants, each with their own risk category.
     */
    public void verifyMultipleApplicantsWithIndividualRiskCategories() {
        Locator rows = page.locator(POPOVER_TABLE_ROWS);
        int rowCount = rows.count();

        Assert.assertTrue(rowCount > 1,
                "Expected multiple applicants in Risk Category popover but found " + rowCount);
        log.info("Popover displays {} applicants (multiple)", rowCount);

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            Locator cells = row.locator("td");

            String applicantId = cells.nth(0).textContent().trim();
            String name = cells.nth(1).textContent().trim();
            String riskCategory = cells.nth(2).locator("span.risk-badge-inline").textContent().trim();

            Assert.assertFalse(applicantId.isEmpty(), "Applicant ID is empty for row " + i);
            Assert.assertFalse(name.isEmpty(), "Name is empty for row " + i);
            Assert.assertFalse(riskCategory.isEmpty(), "Risk Category is empty for row " + i);

            log.info("Row {}: Applicant ID={}, Name={}, Risk Category={}", i, applicantId, name, riskCategory);
        }
        log.info("All {} applicants have individual risk categories displayed", rowCount);
    }

    /**
     * Verifies that the popover shows "Not able to fetch Risk Category" message.
     */
    public void verifyRiskCategoryNotAvailable() {
        Locator popover = page.locator(RISK_CATEGORY_POPOVER);
        Locator notFoundMessage = popover.locator("text=" + RISK_NOT_FOUND_TEXT);
        notFoundMessage.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        Assert.assertTrue(notFoundMessage.isVisible(),
                "Expected '" + RISK_NOT_FOUND_TEXT + "' message but it was not visible");
        log.info("Verified '{}' message is displayed in popover", RISK_NOT_FOUND_TEXT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private String mapColorToCssClass(String color) {
        return switch (color.toLowerCase()) {
            case "green" -> "risk-low";
            case "yellow" -> "risk-medium";
            case "red" -> "risk-high";
            default -> throw new IllegalArgumentException("Unknown risk color: " + color);
        };
    }
}
