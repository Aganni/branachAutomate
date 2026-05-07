package ui.Utils;

import dynamicData.DynamicDataClass;
import hooks.BaseTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static dynamicData.DynamicDataClass.get;

public class Utils extends BaseTest {

    public static void extractAndStorePartnerLoanId() {
        BaseTest.log.info("Waiting for URL to update with partnerLoanId...");

        BaseTest.getPage().waitForURL(Pattern.compile(".*partnerLoanId=.*"));

        String currentUrl = BaseTest.getPage().url();
        BaseTest.log.info("Current browser URL fetched by Playwright: {}", currentUrl);

        String partnerLoanId = null;
        Pattern pattern = Pattern.compile("partnerLoanId=([^&]+)");
        Matcher matcher = pattern.matcher(currentUrl);

        if (matcher.find()) {
            partnerLoanId = matcher.group(1);
            BaseTest.log.info("Successfully extracted partnerLoanId: {}", partnerLoanId);
        } else {
            throw new AssertionError("Failed to find 'partnerLoanId' in the URL: " + currentUrl);
        }

        get().setPartnerLoanId(partnerLoanId);
    }

    /**
     * Navigates an open Element UI DatePicker to select the exact target date.
     * @param targetDate The date string in "YYYY-MM-DD" format.
     */
    public static void selectDateFromElementUICalendar(String targetDate) {
        com.microsoft.playwright.Page page = BaseTest.getPage();
        String[] parts = targetDate.split("-");
        String targetYear = parts[0];
        int monthNum = Integer.parseInt(parts[1]);
        String targetMonth = java.time.Month.of(monthNum).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
        String targetDay = String.valueOf(Integer.parseInt(parts[2]));

        BaseTest.log.info("Navigating Element UI Calendar to: {} {} {}", targetYear, targetMonth, targetDay);

        com.microsoft.playwright.Locator activeCalendar = page.locator(".el-picker-panel:visible").last();
        activeCalendar.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(5000));

        // 1. Click Year Header to open Year Selection
        com.microsoft.playwright.Locator yearHeaderBtn = activeCalendar.locator(".el-date-picker__header-label").first();
        yearHeaderBtn.click();
        page.waitForTimeout(500);

        // 2. Navigate Decades until Target Year is visible
        com.microsoft.playwright.Locator yearTable = activeCalendar.locator(".el-year-table:visible").last();
        com.microsoft.playwright.Locator prevDecadeBtn = activeCalendar.locator("button.el-icon-d-arrow-left").first();
        com.microsoft.playwright.Locator nextDecadeBtn = activeCalendar.locator("button.el-icon-d-arrow-right").first();
        
        boolean yearFound = false;
        int attempts = 0;
        while (!yearFound && attempts < 20) {
            com.microsoft.playwright.Locator targetYearCell = yearTable.locator("a.cell").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(targetYear)).first();
            if (targetYearCell.isVisible()) {
                targetYearCell.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
                yearFound = true;
            } else {
                String firstVisibleYear = yearTable.locator("a.cell").first().innerText().trim();
                if (Integer.parseInt(targetYear) < Integer.parseInt(firstVisibleYear)) {
                    prevDecadeBtn.click();
                } else {
                    nextDecadeBtn.click();
                }
                page.waitForTimeout(300);
            }
            attempts++;
        }
        
        if (!yearFound) {
            throw new RuntimeException("Could not find the target year " + targetYear + " in the calendar.");
        }
        page.waitForTimeout(500);

        // 3. Select Month
        com.microsoft.playwright.Locator monthTable = activeCalendar.locator(".el-month-table:visible").last();
        com.microsoft.playwright.Locator targetMonthCell = monthTable.locator("a.cell").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(targetMonth)).first();
        targetMonthCell.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);

        // 4. Select Day
        com.microsoft.playwright.Locator dateTable = activeCalendar.locator(".el-date-table:visible").last();
        com.microsoft.playwright.Locator targetDayCell = dateTable.locator("xpath=.//td[contains(@class, 'available')]/div/span[normalize-space(text())='" + targetDay + "']").first();
        targetDayCell.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        page.waitForTimeout(500);
    }
}
