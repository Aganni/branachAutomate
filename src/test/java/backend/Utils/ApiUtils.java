package backend.Utils;

import backend.constants.Constants;
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
}