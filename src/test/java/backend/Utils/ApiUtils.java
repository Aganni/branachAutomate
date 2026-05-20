package backend.Utils;

import backend.constants.Constants;
import backend.constants.Headers;
import backend.payload.ApiPayload;
import hooks.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dynamicData.DynamicDataClass.get;
import static dynamicData.DynamicDataClass.getValue;

public class ApiUtils extends BaseTest {

    public static void updatePanInMystique(String panProfile) {

        // Create the payload
        get().setApiPayload(ApiPayload.whitelistPanCardInMystique((String) getValue(Constants.PAN_CARD), panProfile)); // Storing for test session logs

        try {
            //  Execute the call ( APIEndPoints.WHITELIST_PANCARD_IN_MYSTIQUE = "/addKyc")
            Response response = ApiClientUtils.doPostKyc(Constants.MYSTIQUE_BASE_URI, Constants.WHITELIST_PANCARD_IN_MYSTIQUE, get().getApiPayload());

            //  Assert Status Code is 200
            Assert.assertEquals(response.getStatusCode(), 200, "Mystique PAN Whitelist API failed!");

            //  Assert Response Message
            JsonPath jsonPath = response.jsonPath();
            String expectedMessage = "Added a new kyc of type pancard with id- " + getValue(Constants.PAN_CARD);
            Assert.assertEquals(jsonPath.getString("message"), expectedMessage, "API Response message mismatch!");

        } catch (Exception e) {
            log.error("Failed to update PAN in Mystique", e);
            Assert.fail("Exception during Mystique PAN Whitelist: " + e.getMessage());
        }
    }

    public static void moveAppFormToStage(String stage) {
        // Fetch the dynamic Application ID stored in the current session
        String appId = getValue("appFormId").toString();

        if (appId == null || appId.isEmpty()) {
            throw new AssertionError("Cannot move to stage: appFormId is missing in TestSessionData.");
        }

        // Formats the endpoint: e.g., /api/v1/UBL/TERMS/start-process/1b945302...
        String endpoint = String.format(Constants.START_PROCESS_ENDPOINT, stage, appId);

        try {
            log.info("Triggering Nebula API to move App ID: [{}] to Stage: [{}]", appId, stage);

            // Execute the API call
            Response response = ApiClientUtils.doPostEmptyBodyWithBasicAuth(
                    Constants.NEBULA_BASE_URI , endpoint, Headers.BASIC_AUTH
            );

            // Assert Success Status Code (Assuming 200 or 204 for successful process start)
            int statusCode = response.getStatusCode();
            log.info("API Response Status Code: {}", statusCode);
            log.info("API Response Body: {}", response.getBody().asString());

            Assert.assertTrue(statusCode == 200 || statusCode == 204,
                    "Failed to move app to stage " + stage + ". API returned status: " + statusCode);

        } catch (Exception e) {
            log.error("Failed to execute Nebula start-process API", e);
            Assert.fail("Exception during moving app to stage " + stage + ": " + e.getMessage());
        }
    }

    public static void updateRepaymentDetails() {
        // Fetch the dynamic Application ID stored in the current session
        String appId = getValue("appFormId").toString();

        if (appId == null || appId.isEmpty()) {
            throw new AssertionError("Cannot update repayment details: appFormId is missing in TestSessionData.");
        }

        String endpoint = String.format(Constants.REPAYMENT_DETAILS_ENDPOINT, appId);
        String payload = ApiPayload.getRepaymentDetailsPayload(appId);

        try {
            log.info("Triggering Lannister API to update Repayment Details for App ID: [{}]", appId);

            // Execute the API call
            Response response = ApiClientUtils.doPostWithBasicAuth(Constants.LANNISTER_BASE_URI, endpoint, payload, Headers.BASIC_AUTH);

            // Assert Success Status Code (Assuming 200 or 201 for success)
            int statusCode = response.getStatusCode();
            log.info("API Response Status Code: {}", statusCode);
            log.info("API Response Body: {}", response.getBody().asString());

            Assert.assertTrue(statusCode == 200 || statusCode == 201,
                    "Failed to update repayment details. API returned status: " + statusCode);

        } catch (Exception e) {
            log.error("Failed to execute Lannister repayment details API", e);
            Assert.fail("Exception during repayment details API: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  RISK CATEGORY HELPERS (Helios + Shield)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Extracts applicant risk data from Helios API response.
     * The response structure has a "result" map containing lists of applicant data.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extractHeliosRiskData(JsonPath heliosResponse) {
        List<Map<String, Object>> apiApplicants = new ArrayList<>();
        Map<String, Object> result = heliosResponse.getMap("result");

        if (result != null) {
            for (Object value : result.values()) {
                if (value instanceof List) {
                    List<Map<String, Object>> applicantList = (List<Map<String, Object>>) value;
                    apiApplicants.addAll(applicantList);
                }
            }
        }
        return apiApplicants;
    }

    /**
     * Finds a Helios applicant record by applicant_id.
     */
    public static Map<String, Object> findHeliosApplicantById(String applicantId, List<Map<String, Object>> heliosData) {
        for (Map<String, Object> heliosApplicant : heliosData) {
            String heliosApplicantId = String.valueOf(heliosApplicant.get("applicant_id"));
            if (heliosApplicantId.equals(applicantId)) {
                return heliosApplicant;
            }
        }
        return null;
    }

    /**
     * Finds applicant full name from Shield linkedIndividuals by matching applicant ID.
     */
    @SuppressWarnings("unchecked")
    public static String findApplicantNameFromShield(String applicantId, List<Map<String, Object>> shieldData) {
        for (Map<String, Object> linked : shieldData) {
            String linkedId = String.valueOf(linked.get("id"));
            if (linkedId.equals(applicantId)) {
                Map<String, Object> individual = (Map<String, Object>) linked.get("individual");
                if (individual != null) {
                    String firstName = individual.get("firstName") != null ? String.valueOf(individual.get("firstName")) : "";
                    String middleName = individual.get("middleName") != null
                            && !"null".equals(String.valueOf(individual.get("middleName")))
                            ? String.valueOf(individual.get("middleName")) : "";
                    String lastName = individual.get("lastName") != null ? String.valueOf(individual.get("lastName")) : "";
                    return (firstName + " " + middleName + " " + lastName).replaceAll("\\s+", " ").trim();
                }
            }
        }
        return "Unknown";
    }
}