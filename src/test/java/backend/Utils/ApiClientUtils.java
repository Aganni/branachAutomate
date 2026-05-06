package backend.Utils;

import backend.constants.Constants;
import hooks.BaseTest;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static backend.Utils.RequestSpecManager.getBaseSpec;
import static backend.constants.Headers.*;
import static io.restassured.RestAssured.given;

public class ApiClientUtils extends BaseTest {

    public static Response doPostKyc(String baseUri, String apiEndPoints, String requestBody) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(
                SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        String xKarzaApi = Constants.UAT.equalsIgnoreCase(environment) ? X_KARZA_API_UAT : X_KARZA_API;
        RequestSpecification apiRequest = given().spec(getBaseSpec())
                .queryParam(Constants.KYC_TYPE, Constants.PANCARD)
                .header(X_KARZA_KEY, xKarzaApi).baseUri(initializeEnvironment(baseUri))
                .body(requestBody);

        // Retry logic for Lambda call
        int maxRetries = 3;
        int attempt = 0;
        int statusCode = 0;
        Response response = null;

        while (attempt < maxRetries) {
            response = apiRequest.when().post(apiEndPoints).then().extract().response();

            statusCode = response.getStatusCode();
            // Exit loop on success or client error (don't retry 4xx)
            if (statusCode < 500) {
                break;
            }

            attempt++;
            if (attempt < maxRetries) {
                Thread.sleep(2000); // 2-second delay between retries
            }
        }
        return response;
    }

    /**
     * Executes a POST request with Basic Auth and no payload body.
     */
    public static Response doPostEmptyBodyWithBasicAuth(String baseUri, String endpoint, String basicAuthToken) throws Exception {
        RestAssured.baseURI = initializeEnvironment(baseUri);
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Authorization", "Basic " + basicAuthToken)
                .header("Content-Type", "application/json")
                .post(endpoint);
    }

    /**
     * Executes a POST request with Basic Auth and a payload body.
     */
    public static Response doPostWithBasicAuth(String baseUri, String endpoint, String requestBody, String basicAuthToken) throws Exception {
        RestAssured.baseURI = initializeEnvironment(baseUri);
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Authorization", "Basic " + basicAuthToken)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(endpoint);
    }
}