package ui.pages.jarvis;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the Documents (DMS) tab in Jarvis Portal.
 * Dynamically reads the KYC Checklist sidebar to determine which documents
 * are mandatory and pending, then uploads and marks OSV for each.
 */
public class DocumentsPage extends BaseTest {

    // ── Tab Navigation ───────────────────────────────────────────────────────
    private static final String DOCUMENTS_TAB_LINK = "a.tab-item:has(p:text('Documents'))";

    // ── KYC Checklist Sidebar ────────────────────────────────────────────────
    private static final String CHECKLIST_ITEMS = ".checklist-main .status-div";
    private static final String CHECKLIST_PASSED_ITEM = ".checklist-main .status-div:has(p.passed)";
    private static final String CHECKLIST_PENDING_ITEM = ".checklist-main .status-div:not(:has(p.passed))";
    private static final String APPROVE_BUTTON = ".checklist-main .approve-button";

    // ── Document Sections (Main Content) ─────────────────────────────────────
    private static final String ENTITY_WRAPPER = ".entity-wrapper";
    private static final String SECTION_WRAPPER = ".section-wrapper";
    private static final String SECTION_NAME = ".section-name";
    private static final String SECTION_EMPTY_CLASS = "is-empty";

    // ── Upload Mechanism ─────────────────────────────────────────────────────
    private static final String HIDDEN_FILE_INPUT = ".upload-main input[type='file']";
    private static final String UPLOAD_BTN_IN_SECTION = ".drop .action-btn:has-text('upload')";

    // ── OSV Modal ────────────────────────────────────────────────────────────
    private static final String OSV_MODAL = ".markosv-modal";
    private static final String OSV_TYPE_DROPDOWN = ".markosv-modal .doctype-select .el-input__inner";
    private static final String OSV_STATUS_DROPDOWN = ".markosv-modal .osv-select .el-input__inner";
    private static final String OSV_SAVE_BTN = ".markosv-modal .save-button";
    private static final String DROPDOWN_FIRST_OPTION = ".el-select-dropdown__item:visible >> nth=0";
    private static final String DROPDOWN_OPTION_OK = ".el-select-dropdown__item:has-text('OK'):visible";

    // ── Mark OSV Button (for already uploaded docs) ──────────────────────────
    private static final String MARK_OSV_BTN = ".action-btn:has-text('Mark OSV')";
    private static final String EDIT_OSV_BTN = ".edit-btn.edit-action";

    // ── Success Indicators ───────────────────────────────────────────────────
    private static final String UPLOAD_SUCCESS_TOAST = "text='Document Uploaded Successfully'";
    private static final String OSV_OK_TAG = ".osv-tag.el-tag--success";
    private static final String INDICATOR_SUCCESS = ".indicator-checklist.indicator-success";

    // ── Default test file for upload ─────────────────────────────────────────
    private static final String DEFAULT_PDF_PATH = "src/test/resources/testdata/bank_statement.pdf";

    public static Page getPage() {
        return BaseTest.getPage();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the Documents tab.
     */
    public void navigateToDocumentsTab() {
        log.info("Navigating to Documents tab...");
        getPage().locator(DOCUMENTS_TAB_LINK).click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        getPage().waitForTimeout(2000); // Allow checklist to render
        log.info("Documents tab loaded.");
    }

    /**
     * Reads the KYC Checklist sidebar and returns list of pending (non-passed) document names.
     */
    public List<String> getPendingMandatoryDocuments() {
        List<String> pendingDocs = new ArrayList<>();
        Locator pendingItems = getPage().locator(CHECKLIST_PENDING_ITEM);
        int count = pendingItems.count();

        for (int i = 0; i < count; i++) {
            String docName = pendingItems.nth(i).locator("p").textContent().trim();
            if (!docName.isEmpty()) {
                pendingDocs.add(docName);
            }
        }
        return pendingDocs;
    }

    /**
     * Main method: Dynamically uploads mandatory documents and marks OSV.
     * Reads KYC Checklist to determine what's pending.
     * If no mandatory docs are pending, logs and skips gracefully.
     */
    public void uploadMandatoryDocumentsAndMarkOsv() {
        navigateToDocumentsTab();

        List<String> pendingDocs = getPendingMandatoryDocuments();

        if (pendingDocs.isEmpty()) {
            log.info("No mandatory documents pending in KYC Checklist. Skipping document upload.");
            return;
        }

        log.info("Found {} pending mandatory documents: {}", pendingDocs.size(), pendingDocs);

        for (String docName : pendingDocs) {
            processDocument(docName);
        }

        log.info("All pending mandatory documents processed successfully.");
    }

    /**
     * Clicks the Approve button in the KYC Checklist sidebar.
     * Should be called when the approve button is enabled (after QC Review).
     */
    public void approveKycChecklist() {
        navigateToDocumentsTab();
        log.info("Attempting to click Approve button in KYC Checklist...");

        Locator approveBtn = getPage().locator(APPROVE_BUTTON);
        approveBtn.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Check if button is enabled
        if (approveBtn.isDisabled()) {
            log.warn("Approve button is disabled. Cannot approve at this stage.");
            throw new AssertionError("KYC Checklist Approve button is disabled. Ensure all mandatory docs are uploaded and QC review is complete.");
        }

        approveBtn.click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("KYC Checklist approved successfully.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Processes a single document from the checklist.
     * Determines if it needs upload or just OSV marking.
     */
    private void processDocument(String checklistDocName) {
        log.info("Processing document: '{}'", checklistDocName);

        // Parse entity name and doc section from checklist text
        // Format: "EntityName's DocSection" (e.g., "Myntra Corp's KYC Documents")
        // Or just "Loan Agreement Signed" for Others section
        String entityName = extractEntityName(checklistDocName);
        String sectionHint = extractSectionName(checklistDocName);

        log.info("  Entity: '{}', Section hint: '{}'", entityName, sectionHint);

        // Find the matching section in the main content area
        Locator section = findDocumentSection(entityName, sectionHint);

        if (section == null) {
            log.warn("  Could not locate section for '{}'. Skipping.", checklistDocName);
            return;
        }

        // Check if section already has a document uploaded
        boolean hasDocument = !section.locator(".doc-container .doc-wrapper .doc").isHidden()
                && section.locator(".doc-container .doc-wrapper .doc").count() > 0;

        if (hasDocument) {
            // Document already uploaded — just need to mark OSV if not already marked
            boolean hasOsvOk = section.locator(OSV_OK_TAG).count() > 0;
            if (hasOsvOk) {
                log.info("  Document already uploaded and OSV marked OK. Skipping.");
            } else {
                log.info("  Document already uploaded but OSV not marked. Marking OSV...");
                markOsvForExistingDocument(section);
            }
        } else {
            // Section is empty — need to upload
            log.info("  Section is empty. Uploading document...");
            uploadDocumentToSection(section);
        }
    }

    /**
     * Uploads a document to an empty section using setInputFiles on the hidden input.
     * After upload, handles the OSV modal.
     */
    private void uploadDocumentToSection(Locator section) {
        // Click the upload button within this section to trigger the file input
        Locator uploadBtn = section.locator(UPLOAD_BTN_IN_SECTION).first();
        uploadBtn.scrollIntoViewIfNeeded();

        // Use setInputFiles on the hidden file input
        Path filePath = Paths.get(DEFAULT_PDF_PATH);
        getPage().setInputFiles(HIDDEN_FILE_INPUT, filePath);

        log.info("  File set via setInputFiles. Waiting for OSV modal...");

        // Wait for OSV modal to appear
        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

        // Handle OSV modal — select TYPE (first option) and OSV STATUS (OK)
        handleOsvModal(true);
    }

    /**
     * Marks OSV for a document that's already uploaded.
     * Clicks the edit/Mark OSV button and handles the modal.
     */
    private void markOsvForExistingDocument(Locator section) {
        // Try clicking the edit button (pencil icon) to open OSV modal
        Locator editBtn = section.locator(EDIT_OSV_BTN).first();

        if (editBtn.isVisible()) {
            editBtn.click();
        } else {
            // Try Mark OSV button
            Locator markOsvBtn = section.locator(MARK_OSV_BTN).first();
            markOsvBtn.scrollIntoViewIfNeeded();
            markOsvBtn.click();
        }

        // Wait for OSV modal
        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        // For existing docs, TYPE may already be selected — just select OSV STATUS
        handleOsvModal(false);
    }

    /**
     * Handles the OSV modal dialog.
     * @param selectType if true, selects the first option in TYPE dropdown
     */
    private void handleOsvModal(boolean selectType) {
        getPage().waitForTimeout(500); // Allow modal to fully render

        if (selectType) {
            // Click TYPE dropdown and select first option
            log.info("  Selecting TYPE (first available option)...");
            Locator typeDropdown = getPage().locator(OSV_TYPE_DROPDOWN);
            typeDropdown.click();
            getPage().waitForTimeout(300);

            // Select first visible option in the dropdown
            Locator firstOption = getPage().locator(".el-select-dropdown__item:visible").first();
            firstOption.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            firstOption.click();
            getPage().waitForTimeout(300);
        }

        // Click OSV STATUS dropdown and select "OK"
        log.info("  Selecting OSV STATUS = 'OK'...");
        Locator osvStatusDropdown = getPage().locator(OSV_STATUS_DROPDOWN);
        osvStatusDropdown.click();
        getPage().waitForTimeout(300);

        Locator okOption = getPage().locator(".el-select-dropdown__item:has-text('OK'):visible").first();
        okOption.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        okOption.click();
        getPage().waitForTimeout(300);

        // Click Save button (should be enabled now)
        log.info("  Clicking Save...");
        Locator saveBtn = getPage().locator(OSV_SAVE_BTN);
        saveBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        saveBtn.click();

        // Wait for modal to close
        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10000));

        getPage().waitForTimeout(1000); // Allow page to update
        log.info("  OSV modal saved and closed.");
    }

    /**
     * Finds the document section in the main content area matching the entity and section hint.
     */
    private Locator findDocumentSection(String entityName, String sectionHint) {
        // Strategy: Find the entity-wrapper containing the entity name,
        // then within it find the section-wrapper whose section-name contains the hint

        Locator allEntityWrappers = getPage().locator(ENTITY_WRAPPER);
        int entityCount = allEntityWrappers.count();

        for (int i = 0; i < entityCount; i++) {
            Locator entityWrapper = allEntityWrappers.nth(i);

            // Check if this entity wrapper matches (by name or "Others" heading)
            String entityText = "";
            Locator nameLocator = entityWrapper.locator(".mainNameText");
            Locator subHeading = entityWrapper.locator(".sub-heading");

            if (nameLocator.count() > 0) {
                entityText = nameLocator.first().textContent().trim();
            } else if (subHeading.count() > 0) {
                entityText = subHeading.first().textContent().trim();
            }

            boolean entityMatches = entityName.isEmpty()
                    || entityText.toLowerCase().contains(entityName.toLowerCase())
                    || (entityName.equalsIgnoreCase("Others") && entityText.contains("Others"));

            if (!entityMatches) continue;

            // Find section within this entity that matches the section hint
            Locator sections = entityWrapper.locator(SECTION_WRAPPER);
            int sectionCount = sections.count();

            for (int j = 0; j < sectionCount; j++) {
                Locator sec = sections.nth(j);
                String secName = sec.locator(SECTION_NAME).textContent().trim();

                if (secName.toLowerCase().contains(sectionHint.toLowerCase())) {
                    log.info("  Found matching section: '{}' under entity '{}'", secName, entityText);
                    return sec;
                }
            }
        }

        return null;
    }

    /**
     * Extracts entity name from checklist text.
     * E.g., "Myntra Corp's KYC Documents" → "Myntra Corp"
     * E.g., "Loan Agreement Signed" → "Others"
     */
    private String extractEntityName(String checklistText) {
        if (checklistText.contains("'s ")) {
            return checklistText.substring(0, checklistText.indexOf("'s ")).trim();
        }
        // If no "'s" pattern, it's likely in "Others" section
        return "Others";
    }

    /**
     * Extracts section/document name from checklist text.
     * E.g., "Myntra Corp's KYC Documents" → "KYC Documents"
     * E.g., "Loan Agreement Signed" → "Loan Agreement Signed"
     */
    private String extractSectionName(String checklistText) {
        if (checklistText.contains("'s ")) {
            return checklistText.substring(checklistText.indexOf("'s ") + 3).trim();
        }
        return checklistText.trim();
    }
}
