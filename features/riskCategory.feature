@RiskCategory @Regression
Feature: Risk Category Badge Validation in Jarvis

  Background: Login to Jarvis and navigate to Application Dashboard
    Given User logs into Jarvis portal
    And User navigates to Application Dashboard
    Then User clears the date filter

  @Regression
  Scenario Outline: Risk Category badge is displayed and validated for appForm
    When User searches application by "App ID" with value <appFormId>
    And User opens the first application from search results
    Then Risk Category badge is visible on appForm header
    When User hovers on Risk Category badge
    Then Risk Category popover is displayed with applicant table
    And Popover table has columns "Applicant ID" "Applicant Name" "Risk Category"
    And Risk category <expectedRiskCategory> with <expectedColor> badge is shown for applicant
    # API Validation
    Then User validates risk category from Helios API for appFormId <appFormId>
    And User validates applicant name from Shield API for appFormId <appFormId>
    And UI risk category data matches API response for all applicants
    Examples:
      | appFormId                                | expectedRiskCategory | expectedColor |
      | "499e41b0-e897-42c4-aa99-72405d1f6c12"  | "Low"                | "green"       |
      | "b1487409-94cf-4548-9362-552883581284"  | "Medium"             | "yellow"      |
      | "93c2abd9-5337-4351-9fd7-c00b87960f6f"  | "High"               | "red"         |

  @Regression
  Scenario Outline: Risk Category badge with multiple applicants is displayed and validated
    When User searches application by "App ID" with value <appFormId>
    And User opens the first application from search results
    Then Risk Category badge is visible on appForm header
    When User hovers on Risk Category badge
    Then Risk Category popover is displayed with applicant table
    And Popover table has columns "Applicant ID" "Applicant Name" "Risk Category"
    And Popover displays multiple applicants with individual risk categories
    # API Validation
    Then User validates risk category from Helios API for appFormId <appFormId>
    And User validates applicant name from Shield API for appFormId <appFormId>
    And UI risk category data matches API response for all applicants
    Examples:
      | appFormId                                |
      | "9657e90b-f712-4f02-9f26-6d34379ec473"  |

  @Regression
  Scenario Outline: Risk Category badge shows error when data is not available
    When User searches application by "App ID" with value <appFormId>
    And User opens the first application from search results
    Then Risk Category badge is visible on appForm header
    When User hovers on Risk Category badge
    Then Risk Category popover displays "Not able to fetch Risk Category" message
    Examples:
      | appFormId                                |
      | "f49349bf-8501-4919-8776-bde097a7ba2d"  |
