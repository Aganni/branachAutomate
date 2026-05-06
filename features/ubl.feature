@UBL @Regression
Feature: Unsecured Business Loan End-to-End Workflow

  Scenario: Complete DSA Portal to Jarvis Portal Handoff using Context Bridge
#     Data Setup and whitelisting pan in mystique
    Given Generates required test data and Whitelist PAN for profile "Shea Test" in Mystique
## Step 1: DSA Portal Intake
    Given User navigates to the DSA Portal
    When User sign in via external login option with a registered email Id and lands on the DSA Dashboard
    Then User selects "Business Loans" from the dropdown menu and initiates a new application
## Step 2 : Fill out the mandatory details in partner details
    Then User fills the mandatory details in partner details page and clicks on Save and Next button
      | Branch        | 91, SPRINGBOARD - NEW DELHI |
      | Sales Manager | devi (devi@gmail.com)       |
      | Scheme        | Income program- Cash profit |
## Step 3: Business Details - PAN Verification
    Then User enters Entity PAN "AAECM9636S" in business details, clicks the verification button, and verifies that entity name is "Myntra Corp"
    And User clicks on the "Continue to fetch details linked to this PAN" button
##     Business Details - Filling Operational Address
    And User select Entity Operational Address line 1 from the dropdown and verifies the auto-populated address details
      | Operational Address (Line 2) | AGARWAL NGR CAVES RD                     |
      | Pincode                      | 400060                                   |
      | City                         | MMB                                      |
      | State                        | MH                                       |
    And User selects "Family Owned" as the Ownership type
##     Business Details - Filling Registered Address
    And User selects "Yes" for Same as Operational Address for Registered Address
##     Business Details - Filling more Business Details
    Then User fills the More Business Details and selects "Service" as the Industry Type
      | Entity Email          | adarsh.gani@creditsaison-in.com |
      | Entity Contact Number | 8937879820                      |
      | Date of Registration  | 12/07/1998                      |
      | Last Year's Turnover  | 1100000                         |
##     Business Details - Filling Loan Requirements
    Then User fills the Loan Requirements and clicks on the Submit button on business details page
      | Tenure      | 12                 |
      | Loan Amount | 700000             |
      | End Use     | Business Expansion |
## Step 4: Eligibility Check
    Then User waits for the Eligibility Prechecks to complete and clicks Next
## Step 5: QDE - Primary Co-applicant Details
    Then User verifies the generated PAN in QDE page and checks auto-populated name "Shea Test" for the Primary Co-applicant
    And User fills remaining basic details in QDE page with "45" shareholding, saves, and submits
## Step 6: Consent Page
    Then User sends and verifies OTP "123456" for Primary Applicant
    And User provides email "adarsh.gani@creditsaison-in.com" for Entity, sends consent, and submits
    When User bypasses email verification by pushing "adarsh.gani@creditsaison-in.com" consent to SQS
    Then User submits the Consent page
## Step 7: QDE 1 decision Check
    Then User waits for the Eligibility Prechecks to complete and clicks Next
## Step 8: Banking Details
    And User selects bank "IndusInd Bank, India" and completes statement upload in the new tab
    Then User submits banking details and proceeds to the next page
## Step 9: QDE 2 decision Check
    Then User waits for the Eligibility Prechecks to complete and clicks Next
    And User fills the DDE form with following details
      | TradeRef1 Name         | Varun         |
      | TradeRef1 Relation     | Customer      |
      | TradeRef1 Mobile       | 9886779302    |
      | TradeRef2 Name         | Rothan      |
      | TradeRef2 Relation     | Customer      |
      | TradeRef2 Mobile  | 9839099092    |
      | Email             | adarsh.gani@gmail.com |
      | Correspondence Address   |  AGARWAL NGR CAVES RD   |
      | Correspondence Ownership |  Self Owned             |
      | Father First Name | Vikas        |
      | Father Last Name  | Kamble        |
      | Caste             | GENERAL       |
      | Religion          | HINDU         |
      | Designation       | Individual    |
      | Annual Income     | 400000       |
      | Permanent Same    | Yes           |
      | Current Same      | Yes           |
## Step 9: KYC Documents
    Then User submits the KYC Documents page without additional uploads
# Jarvis flow starts here
    And User switches to Jarvis portal and logins
    And User navigates to the Applications tab searches appFrom using "Partner LID" and opens the appform
    And User selects "Move to Login Desk" from Application Actions and moves appForm next stage
    And User updates the Business Details with the following data:
      |CLASSIFICATION   |                                                   CORPORATE|
      | Udyam Number    | UDYAM-AD-12-1234567                                      |
      | Industry Sector | Industrial hardware/machinery tools/Engineering goods |
      | Sub Sector      | Trader- Wholesale                                 |
      | No Of Employees | 50                                               |
      | Business Type   | Small                                             |
##    |     Entity CGTMSE    |   yes                                                |
    And User updates the Bank Details with the following data:
      | Disbursal Account Number  | 159590886867 |
      | Disbursal Account Type    | SAVINGS      |
      | Disbursal IFSC Code       | HDFC0000094  |
      | Collection Account Number | 159590886867 |
      | Collection Account Type   | SAVINGS      |
      | Collection IFSC Code      | HDFC0000094  |
# Allocation Dashboard flow
    And User navigates to the Allocation Dashboard and assign appform to self using app ID
    And User selects "Move to CAM" from Application Actions and moves appForm next stage
    And User navigates to the CAM tab in Application Dashboard and start the cam
# Allocation Dashboard flow
    And User navigates to the Allocation Dashboard and assign appform to self using app ID
    And User selects "Move to Credit Review" from Application Actions and moves appForm next stage
    And User opens the Loan Requirements section and updates the details
    And User navigates to the Dedupe tab in Application Dashboard
    And User navigates to the Verification tab and resolves the Udyam KYC status
# Allocation Dashboard flow
    And User navigates to the Allocation Dashboard and assign appform to self using app ID
    And User moves application to "Move to Credit Approval" assigned to level "L5" and user "tenjin.user@creditsaison-in.com"
    And User navigates to the RegCheck tab in Application Dashboard
    And User updates the Appform Ownership Details with the following data:
      | UserEmail | harshit.verma@partner.creditsaison-in.com |
# Loan Requirements flow
    And User opens the Loan Requirements section and initiates Credit Approval with reason "Approve"
    And User moves appForm to "TERMS" stage
#  Document Generation and E-Sign flow
    And User opens E-Sign section, generates documents, and opts for offline signatures
# Insurance Details flow
    And User opens Insurance Details and updates Insurance and Nominee Details with the following data
      | Provider     | Acko Group Health Insurance         |
      | Tenure       | 10 Months                           |
      | NomineeName  | Adarsh Gani                         |
      | Relationship | brother                             |
      | DOB          | 2001-08-24                          |
      | Gender       | Male                                |
      | Mobile       | 7848043529                          |
      | Email        | adarsh.gani@creditsaison-in.com     |
    And User Update the repayment details
    And User selects "Move to Sanction Approval" from Application Actions and moves appForm next stage



