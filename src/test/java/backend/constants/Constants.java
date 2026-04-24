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

    String START_PROCESS_ENDPOINT = "/api/v1/UBL/%s/start-process/%s";
}
