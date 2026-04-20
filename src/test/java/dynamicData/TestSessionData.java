package dynamicData;

import io.restassured.path.json.JsonPath;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class TestSessionData {

    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

    private String lpc;
    private String appFormId;
    private String partnerLoanId;
    private String apiPayload;
    private String apiResponse;
}