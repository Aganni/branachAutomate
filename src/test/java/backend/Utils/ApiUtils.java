package backend.Utils;

import backend.constants.Constants;
import backend.constants.Headers;
import backend.payload.ApiPayload;
import hooks.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;

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
                    "https://nebula.uat.creditsaison.corp" , endpoint, Headers.BASIC_AUTH
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
}