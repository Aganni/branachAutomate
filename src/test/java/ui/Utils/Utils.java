package ui.Utils;

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
}
