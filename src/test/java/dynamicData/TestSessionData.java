package dynamicData;

import lombok.Data;
import java.util.LinkedHashMap;

@Data
public class TestSessionData {

    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

    private String lpc;
    private String appFormId;
    private String partnerLoanId;
    private String apiPayload;
    private String apiResponse;
}