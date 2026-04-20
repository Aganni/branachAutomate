package backend.stepDefinitions;

import backend.Utils.DataGeneratorUtils;
import backend.Utils.ApiUtils;
import backend.constants.Constants;
import hooks.BaseTest;
import io.cucumber.java.en.*;

import static dynamicData.DynamicDataClass.getValue;
import static dynamicData.DynamicDataClass.setValue;

public class ApiStepDefinitions extends BaseTest {

    @Given("Generates required test data and Whitelist PAN for profile {string} in Mystique")
    public void generateDataAndWhitelistPan(String profileName) {
        // 1. Generate and store Mobile Number
        setValue(Constants.MOBILE_NUMBER, DataGeneratorUtils.generateMobileNumber());
        log.info("Generated and stored mobile number: {}", getValue(Constants.MOBILE_NUMBER));
        // 2. Generate and store PAN
        setValue(Constants.PAN_CARD, DataGeneratorUtils.generatePanNumber());
        log.info("Generated and stored PAN number: {}", getValue(Constants.PAN_CARD));

        // Hits the Mystique API with the profile name and the PAN
        ApiUtils.updatePanInMystique(profileName);
    }
}