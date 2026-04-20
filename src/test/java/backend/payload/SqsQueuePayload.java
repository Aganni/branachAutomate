package backend.payload;

public class SqsQueuePayload {

    public static String buildCustomerConsentPayload(String email, String partnerLoanId) {
        return "{\n" +
                "  \"consentId\": \"" + email + "\",\n" +
                "  \"entityId\": \"" + partnerLoanId + "\",\n" +
                "  \"entityType\": \"partnerLoanId\",\n" +
                "  \"consentType\": \"clickWrapEmail\",\n" +
                "  \"userConsent\": null,\n" +
                "  \"customerType\": \"businessEntity\",\n" +
                "  \"referenceId\": null,\n" +
                "  \"providerName\": null,\n" +
                "  \"providerId\": null,\n" +
                "  \"lpc\": null,\n" +
                "  \"consentStartDate\": null,\n" +
                "  \"consentEndDate\": null,\n" +
                "  \"consentValid\": false,\n" +
                "  \"id\": null,\n" +
                "  \"markedBy\": null\n" +
                "}";
    }
}
