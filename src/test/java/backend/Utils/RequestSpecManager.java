package backend.Utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.SSLConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import static backend.constants.Headers.HEADER_NAME;
import static backend.constants.Headers.HEADER_VALUE;

public class RequestSpecManager {

    // InheritableThreadLocals for Request Specifications
    private static final InheritableThreadLocal<RequestSpecification> baseRequest = new InheritableThreadLocal<>();

    // Global configuration for all RestAssured calls
    static {
        RestAssured.config = RestAssured.config()
                .sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation())
                .logConfig(LogConfig.logConfig()
                        .enablePrettyPrinting(true)
                        .blacklistHeader("Headers", "Cookies", "Authorization", "x-api-key", "signature", "X-API-KEY"));
    }

    /**
     * Custom filter for beautifully formatted console logging.
     */
    private static class CleanConsoleLogFilter implements Filter {

        private static final String RESET = "\u001B[0m";
        private static final String CYAN_BOLD = "\033[1;36m";
        private static final String YELLOW_BOLD = "\033[1;33m";

        @Override
        public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {

            System.out.println("====================================================================\n");
            System.out.println(CYAN_BOLD + "Request method:   " + req.getMethod() + RESET);
            System.out.println("Request URI:      " + req.getURI());

            if (!req.getQueryParams().isEmpty()) System.out.println("Query params:     " + req.getQueryParams());
            if (!req.getFormParams().isEmpty()) System.out.println("Form params:      " + req.getFormParams());
            if (!req.getPathParams().isEmpty()) System.out.println("Path params:      " + req.getPathParams());

            if (req.getBody() != null) {
                System.out.println(YELLOW_BOLD + "Request Body:\n" + req.getBody().toString() + RESET);
            }

            Response resp = ctx.next(req, res);

            System.out.println("\nResponse Status:  " + resp.getStatusLine());
            if (resp.getBody() != null && !resp.getBody().asString().isEmpty()) {
                System.out.println(YELLOW_BOLD + "Response Body:\n" + resp.getBody().asPrettyString() + RESET);
            }
            System.out.println("====================================================================\n");

            return resp;
        }
    }

    public static RequestSpecification getBaseSpec() {
        if (baseRequest.get() == null) {
            baseRequest.set(new RequestSpecBuilder()
                    .setConfig(RestAssured.config)
                    .setContentType(ContentType.JSON)
                    .addFilter(new CleanConsoleLogFilter()) // Only using our clean console filter
                    .addHeader(HEADER_NAME, HEADER_VALUE)
                    .build());
        }
        return baseRequest.get();
    }

    /**
     * Cleans up the ThreadLocals memory when a test finishes.
     */
    public static void resetAllSpecs() {
        baseRequest.remove();
    }
}