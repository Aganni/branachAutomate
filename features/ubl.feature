@UBL @Regression
Feature: Unsecured Business Loan End-to-End Workflow

  Scenario: Complete UBL flow from DSA intake to Disbursal
  # ── Data Initialization ──
    Given Initialize data for "normal" loan of "UBL"
    And User generates test data and whitelists PAN in Mystique
  # ── DSA Portal Flow ──
    When User logs into DSA Portal and initiates a Business Loan application
    Then User completes Partner Details and proceeds
    And User completes Business Details with PAN verification and submits
    And User passes Eligibility Prechecks
    And User completes QDE with co-applicant details and submits
    And User completes Consent page with SQS bypass
    And User passes Eligibility Prechecks
    And User completes Banking details and submits
    And User passes decision check and fills DDE form
    And User submits KYC Documents page
  # ── Jarvis Portal Flow ──
    When User switches to Jarvis and opens the application
    Then User moves appForm to Login Desk and updates Business and Bank details
    And User assigns appForm and moves to CAM stage
    And User starts CAM process
    And User pulls and downloads the commercial cibil report from Bureau View
    And User uploads mandatory documents and marks OSV in Documents tab
    And User assigns appForm and moves to Credit Review
    And User updates Loan Requirements and resolves Dedupe and Verification
    And User assigns appForm and moves to Credit Approval
    And User updates Ownership, initiates Credit Approval, and moves to Terms
    And User generates E-Sign documents and completes Insurance details
    And User updates Repayment, adds Aadhaar, and adds Beneficiary Owner
    And User uploads mandatory documents and marks OSV in Documents tab
    And User moves to Sanction Approval
    And User reassigns and moves to QC Review
    And User moves to QC Approval stage
    And User approves the KYC checklist in Documents tab
    And User approves the application and triggers disbursal
