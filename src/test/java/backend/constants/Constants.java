package backend.constants;

public interface Constants {

    String UAT = "uat";
    String PAN_CARD = "pan_card";
    String MOBILE_NUMBER = "mobile_number";

    // Mystique Api keys
    String MYSTIQUE_BASE_URI = "mystiqueUri";
    String WHITELIST_PANCARD_IN_MYSTIQUE = "/addKyc?kycType=pancard";
    String KYC_TYPE = "kycType";
    String PANCARD = "pancard";

    // Nebula API keys
    String NEBULA_BASE_URI = "nebulaUri";
    String START_PROCESS_ENDPOINT = "/api/v1/UBL/%s/start-process/%s";

    // Lannister API keys
    String LANNISTER_BASE_URI = "lannisterUri";
    String REPAYMENT_DETAILS_ENDPOINT = "/api/v1/repaymentdetail/appForm/%s";

    // Helios API keys
    String HELIOS_BASE_URI = "heliosUri";
    String HELIOS_RISK_CATEGORY_ENDPOINT = "/api/v1/risk-category";

    // Shield API keys
    String SHIELD_BASE_URI = "shieldUri";
    String SHIELD_GET_APPFORM_BY_ID_ENDPOINT = "/api/v1/appForm/{appFormId}";

}
