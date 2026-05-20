package backend.stepDefinitions;

import backend.Utils.ApiClientUtils;
import backend.Utils.ApiUtils;
import backend.Utils.DataGeneratorUtils;
import backend.constants.Constants;
import data.TestDataProvider;
import hooks.BaseTest;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

import static dynamicData.DynamicDataClass.*;

public class ApiStepDefinitions extends BaseTest {

    @Given("User generates test data and whitelists PAN in Mystique")
    public void generateDataAndWhitelistPan() {
        String profile = TestDataProvider.get("dsa.qde.pan_profile");

        setValue(Constants.MOBILE_NUMBER, DataGeneratorUtils.generateMobileNumber());
        log.info("Generated mobile: {}", getValue(Constants.MOBILE_NUMBER));

        setValue(Constants.PAN_CARD, DataGeneratorUtils.generatePanNumber());
        log.info("Generated PAN: {}", getValue(Constants.PAN_CARD));

        ApiUtils.updatePanInMystique(profile);
    }

    @And("User moves to QC Approval stage")
    public void moveToQcApproval() {
        ApiUtils.moveAppFormToStage("QC_APPROVE");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  RISK CATEGORY API VALIDATION STEPS
    // ═══════════════════════════════════════════════════════════════════════════

    @Then("User validates risk category from Helios API for appFormId {string}")
    public void userValidatesRiskCategoryFromHeliosAPI(String appFormId) throws Exception {
        JsonPath heliosResponse = ApiClientUtils.doPostHeliosRiskCategory(appFormId);
        List<Map<String, Object>> apiApplicants = ApiUtils.extractHeliosRiskData(heliosResponse);

        setValue("heliosRiskData", apiApplicants);
        log.info("Helios API returned {} applicants with risk data", apiApplicants.size());
        Assert.assertFalse(apiApplicants.isEmpty(),
                "Helios API returned no risk data for appFormId: " + appFormId);
    }

    @And("User validates applicant name from Shield API for appFormId {string}")
    public void userValidatesApplicantNameFromShieldAPI(String appFormId) throws Exception {
        JsonPath shieldResponse = ApiClientUtils.doGetShieldAppForm(appFormId);
        List<Map<String, Object>> linkedIndividuals = shieldResponse.getList("linkedIndividuals");

        setValue("shieldLinkedIndividuals", linkedIndividuals);
        log.info("Shield API returned {} linked individuals", linkedIndividuals.size());
        Assert.assertFalse(linkedIndividuals.isEmpty(),
                "Shield API returned no linked individuals for appFormId: " + appFormId);
    }

    @SuppressWarnings("unchecked")
    @And("UI risk category data matches API response for all applicants")
    public void uiRiskCategoryDataMatchesAPIResponse() {
        List<Map<String, String>> uiApplicants =
                (List<Map<String, String>>) getValue("uiRiskCategoryData");
        List<Map<String, Object>> heliosData =
                (List<Map<String, Object>>) getValue("heliosRiskData");
        List<Map<String, Object>> shieldData =
                (List<Map<String, Object>>) getValue("shieldLinkedIndividuals");

        Assert.assertEquals(uiApplicants.size(), heliosData.size(),
                "Applicant count mismatch between UI (" + uiApplicants.size()
                        + ") and Helios API (" + heliosData.size() + ")");

        for (Map<String, String> uiApplicant : uiApplicants) {
            String uiApplicantId = uiApplicant.get("applicantId");
            String uiName = uiApplicant.get("name");
            String uiRiskCategory = uiApplicant.get("riskCategory");

            Map<String, Object> matchedHelios = ApiUtils.findHeliosApplicantById(uiApplicantId, heliosData);
            Assert.assertNotNull(matchedHelios,
                    "Applicant ID " + uiApplicantId + " from UI not found in Helios API response");

            String apiRiskCategory = String.valueOf(matchedHelios.get("risk_category"));
            Assert.assertEquals(uiRiskCategory, apiRiskCategory,
                    "Risk category mismatch for applicant " + uiApplicantId);

            String expectedName = ApiUtils.findApplicantNameFromShield(uiApplicantId, shieldData);
            if (expectedName != null && !"Unknown".equals(expectedName)) {
                Assert.assertEquals(uiName, expectedName,
                        "Name mismatch for applicant " + uiApplicantId);
            }

            log.info("Validated applicant {} - Name: {}, Risk: {}", uiApplicantId, uiName, uiRiskCategory);
        }
        log.info("All UI data matches API response successfully");
    }
}
