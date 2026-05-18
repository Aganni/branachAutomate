package ui.pages.jarvis;

import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import hooks.BaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final String APPROVE_BUTTON = ".checklist-main .approve-button";

    // ── Document Sections (Main Content) ─────────────────────────────────────
    private static final String ENTITY_WRAPPER = ".entity-wrapper";
    private static final String SECTION_WRAPPER = ".section-wrapper";
    private static final String SECTION_NAME = ".section-name";

    // ── Upload Mechanism ─────────────────────────────────────────────────────
    private static final String UPLOAD_BTN_IN_SECTION = ".drop .action-btn:has-text('upload')";

    // ── OSV Modal ────────────────────────────────────────────────────────────
    private static final String OSV_MODAL = ".markosv-modal";
    private static final String OSV_TYPE_DROPDOWN = ".markosv-modal .doctype-select .el-input__inner";
    private static final String OSV_STATUS_DROPDOWN = ".markosv-modal .osv-select .el-input__inner";
    private static final String OSV_SAVE_BTN = ".markosv-modal .save-button";

    // ── Mark OSV Button (for already uploaded docs) ──────────────────────────
    private static final String MARK_OSV_BTN = ".action-btn:has-text('Mark OSV')";
    private static final String EDIT_OSV_BTN = ".edit-btn.edit-action";

    // ── Success/Error Indicators ─────────────────────────────────────────────
    private static final String UPLOAD_ERROR_TOAST = ".el-message--error";
    private static final String OSV_OK_TAG = ".osv-tag.el-tag--success";

    // ── Test files for upload ────────────────────────────────────────────────
    private static final String DEFAULT_PDF_PATH = "src/test/resources/testdata/bank_statement.pdf";
    private static final String PHOTO_FILE_PATH = "src/test/resources/testdata/photo.png";

    // Track which sections have been processed (for duplicate POA handling)
    private final Set<String> processedSectionIds = new HashSet<>();
    // Track how many times a section hint has been processed (for TYPE dropdown selection)
    private final java.util.Map<String, Integer> sectionHintCount = new java.util.HashMap<>();

    public static Page getPage() {
        return BaseTest.getPage();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    public void navigateToDocumentsTab() {
        log.info("Navigating to Documents tab...");
        getPage().locator(DOCUMENTS_TAB_LINK).click();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);

        log.info("Waiting for KYC Checklist to render...");
        getPage().locator(".checklist-title").waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        getPage().waitForTimeout(2000);
        log.info("Documents tab loaded.");
    }

    public List<String> getPendingMandatoryDocuments() {
        List<String> pendingDocs = new ArrayList<>();
        Locator allItems = getPage().locator(CHECKLIST_ITEMS);
        int count = allItems.count();

        if (count == 0) {
            Locator checklistTitle = getPage().locator("p.checklist-title");
            if (checklistTitle.count() > 0) {
                allItems = checklistTitle.locator("xpath=..").locator(".status-div");
                count = allItems.count();
            }
        }

        log.info("Total KYC Checklist items found: {}", count);

        for (int i = 0; i < count; i++) {
            Locator item = allItems.nth(i);
            Locator pTag = item.locator("p");
            if (pTag.count() == 0) continue;

            String docName = pTag.first().textContent().trim();
            String pClass = pTag.first().getAttribute("class");

            if (pClass != null && pClass.contains("passed")) {
                log.info("  [PASSED] {}", docName);
                continue;
            }

            if (!docName.isEmpty()) {
                pendingDocs.add(docName);
                log.info("  [PENDING] {}", docName);
            }
        }
        return pendingDocs;
    }

    public void uploadMandatoryDocumentsAndMarkOsv() {
        navigateToDocumentsTab();
        processedSectionIds.clear();
        sectionHintCount.clear();

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
        // Wait for backend to sync after all uploads
        getPage().waitForTimeout(5000);
        log.info("Waited for backend sync after document uploads.");
    }

    public void approveKycChecklist() {
        navigateToDocumentsTab();
        log.info("Attempting to click Approve button in KYC Checklist...");

        Locator approveBtn = getPage().locator(APPROVE_BUTTON);
        approveBtn.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        if (approveBtn.isDisabled()) {
            log.warn("Approve button is disabled. Cannot approve at this stage.");
            throw new AssertionError("KYC Checklist Approve button is disabled.");
        }

        approveBtn.click();
        log.info("Sidebar Approve button clicked. Waiting for confirmation modal...");

        // Wait for the confirmation modal to appear
        Locator confirmationModal = getPage().locator(".checklist-complete-dialog .el-dialog");
        confirmationModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        log.info("Confirmation modal appeared: 'Approve App ID... Documents KYC Check'");

        // Click the Approve button inside the confirmation modal (unique class selector)
        Locator modalApproveBtn = getPage().locator(".checklist-complete-dialog .approve-checklist-cta");
        modalApproveBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        modalApproveBtn.click();
        log.info("Clicked Approve in confirmation modal.");

        // Wait for the notification toast (el-notification with Success title)
        getPage().waitForTimeout(1000);
        Locator successNotification = getPage().locator(".el-notification .el-notification__title")
                .filter(new Locator.FilterOptions().setHasText("Success"));
        try {
            successNotification.first().waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            log.info("KYC Checklist approved successfully. Success notification received.");
        } catch (Exception e) {
            // Toast may have disappeared quickly — check if page state changed
            log.warn("Success notification not caught (may have disappeared). Proceeding with page refresh.");
        }

        // Wait and refresh page for backend sync
        getPage().waitForTimeout(1000);
        getPage().reload();
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
        log.info("Page refreshed after DMS approval.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void processDocument(String checklistDocName) {
        log.info("Processing document: '{}'", checklistDocName);

        String entityName = extractEntityName(checklistDocName);
        String sectionHint = extractSectionName(checklistDocName);
        log.info("  Entity: '{}', Section hint: '{}'", entityName, sectionHint);

        // Find matching section — skips already-processed sections (handles duplicate POA)
        Locator section = findDocumentSection(entityName, sectionHint);

        if (section == null) {
            log.warn("  Could not locate section for '{}'. Skipping.", checklistDocName);
            return;
        }

        // Mark this section's drop-zone ID as processed
        Locator dropZone = section.locator(".drop-zone");
        if (dropZone.count() > 0) {
            String zoneId = dropZone.first().getAttribute("id");
            if (zoneId != null) {
                processedSectionIds.add(zoneId);
            }
        }

        // Check if section already has a document uploaded
        Locator docElements = section.locator(".doc-container .doc-wrapper .doc");
        boolean hasDocument = docElements.count() > 0 && docElements.first().isVisible();

        if (hasDocument) {
            boolean hasOsvOk = section.locator(OSV_OK_TAG).count() > 0;
            if (hasOsvOk) {
                log.info("  Document already uploaded and OSV marked OK. Skipping.");
            } else {
                log.info("  Document already uploaded but OSV not marked. Marking OSV...");
                markOsvForExistingDocument(section);
            }
        } else {
            log.info("  Section is empty. Uploading document...");
            // Track how many times this hint has been used (for TYPE dropdown index)
            int typeIndex = sectionHintCount.getOrDefault(sectionHint, 0);
            sectionHintCount.put(sectionHint, typeIndex + 1);
            uploadDocumentToSection(section, sectionHint, typeIndex);
        }
    }

    /**
     * Uploads a document to an empty section.
     * Selects file type based on section hint (Photo → png, others → pdf).
     * Verifies upload success and fails on error toast.
     * @param typeIndex which option to select from TYPE dropdown (0=first, 1=second, etc.)
     */
    private void uploadDocumentToSection(Locator section, String sectionHint, int typeIndex) {
        Locator uploadBtn = section.locator(UPLOAD_BTN_IN_SECTION).first();
        uploadBtn.scrollIntoViewIfNeeded();
        getPage().waitForTimeout(500);

        // Choose file based on section type
        Path filePath = resolveFileForSection(sectionHint);
        log.info("  Using file: {} (TYPE dropdown index: {})", filePath.getFileName(), typeIndex);

        // Click upload and set file via the file chooser event
        FileChooser fileChooser = getPage().waitForFileChooser(() -> {
            uploadBtn.click();
        });
        fileChooser.setFiles(filePath);

        log.info("  File uploaded via FileChooser. Waiting for OSV modal...");

        // Check for error toast first (file format not allowed)
        getPage().waitForTimeout(2000);
        Locator errorToast = getPage().locator(UPLOAD_ERROR_TOAST);
        if (errorToast.isVisible()) {
            String errorMsg = errorToast.textContent().trim();
            log.error("  UPLOAD FAILED with error: {}", errorMsg);
            BaseTest.getPage().screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("target/screenshots/upload_error_" + System.currentTimeMillis() + ".png")));
            throw new AssertionError("Document upload failed: " + errorMsg);
        }

        // Wait for OSV modal to appear
        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

        handleOsvModal(true, typeIndex);
    }

    /**
     * Resolves the correct file to upload based on section hint.
     * Photo sections need PNG/JPG, others use PDF.
     */
    private Path resolveFileForSection(String sectionHint) {
        String hintLower = sectionHint.toLowerCase();

        // Photo sections require image files (png/jpg)
        if (hintLower.contains("photo") || hintLower.contains("selfie")) {
            Path photoPath = Paths.get(PHOTO_FILE_PATH);
            if (photoPath.toFile().exists()) {
                return photoPath;
            }
            log.warn("  Photo file not found at '{}'. Falling back to PDF.", PHOTO_FILE_PATH);
        }

        return Paths.get(DEFAULT_PDF_PATH);
    }

    private void markOsvForExistingDocument(Locator section) {
        Locator editBtn = section.locator(EDIT_OSV_BTN).first();

        if (editBtn.isVisible()) {
            editBtn.click();
        } else {
            Locator markOsvBtn = section.locator(MARK_OSV_BTN).first();
            markOsvBtn.scrollIntoViewIfNeeded();
            markOsvBtn.click();
        }

        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        handleOsvModal(false, 0);
    }

    private void handleOsvModal(boolean selectType, int typeIndex) {
        getPage().waitForTimeout(500);

        if (selectType) {
            log.info("  Selecting TYPE (option index: {})...", typeIndex);
            Locator typeDropdown = getPage().locator(OSV_TYPE_DROPDOWN);
            typeDropdown.click();
            getPage().waitForTimeout(300);

            // Select the option at the given index (0=first, 1=second, etc.)
            Locator options = getPage().locator(".el-select-dropdown__item:visible");
            options.nth(typeIndex).waitFor(new Locator.WaitForOptions().setTimeout(5000));
            String selectedType = options.nth(typeIndex).textContent().trim();
            options.nth(typeIndex).click();
            log.info("  Selected TYPE: '{}'", selectedType);
            getPage().waitForTimeout(300);
        }

        log.info("  Selecting OSV STATUS = 'OK'...");
        Locator osvStatusDropdown = getPage().locator(OSV_STATUS_DROPDOWN);
        osvStatusDropdown.click();
        getPage().waitForTimeout(300);

        Locator okOption = getPage().locator(".el-select-dropdown__item:has-text('OK'):visible").first();
        okOption.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        okOption.click();
        getPage().waitForTimeout(300);

        log.info("  Clicking Save...");
        Locator saveBtn = getPage().locator(OSV_SAVE_BTN);
        saveBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        saveBtn.click();

        getPage().locator(OSV_MODAL).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10000));

        getPage().waitForTimeout(1000);
        log.info("  OSV modal saved and closed.");
    }

    /**
     * Finds the document section matching entity and section hint.
     * Skips sections that already have OSV OK marked (they're done).
     * Skips sections that have already been processed in this run.
     * Prefers empty sections that need upload.
     */
    private Locator findDocumentSection(String entityName, String sectionHint) {
        Locator allEntityWrappers = getPage().locator(ENTITY_WRAPPER);
        int entityCount = allEntityWrappers.count();

        Locator bestMatch = null;
        String bestMatchName = "";
        int bestMatchScore = -1; // Higher = better

        for (int i = 0; i < entityCount; i++) {
            Locator entityWrapper = allEntityWrappers.nth(i);

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

            Locator sections = entityWrapper.locator(SECTION_WRAPPER);
            int sectionCount = sections.count();

            for (int j = 0; j < sectionCount; j++) {
                Locator sec = sections.nth(j);
                String secName = sec.locator(SECTION_NAME).textContent().trim();
                String secNameLower = secName.toLowerCase().replaceAll("\\*", "").trim();
                String hintLower = sectionHint.toLowerCase().trim();

                // Check if this section was already processed in this run
                Locator dropZone = sec.locator(".drop-zone");
                if (dropZone.count() > 0) {
                    String zoneId = dropZone.first().getAttribute("id");
                    if (zoneId != null && processedSectionIds.contains(zoneId)) {
                        continue;
                    }
                }

                // Check name match
                boolean matches = false;
                int nameScore = 0;

                if (secNameLower.equals(hintLower)) {
                    matches = true;
                    nameScore = 3; // Exact match
                } else if (secNameLower.startsWith(hintLower)) {
                    matches = true;
                    nameScore = 2; // Starts-with
                } else if (hintLower.length() >= 3 && secNameLower.contains(hintLower)) {
                    matches = true;
                    nameScore = 1; // Contains
                }

                if (!matches) continue;

                // Check section state — skip sections that already have OSV OK
                boolean hasOsvOk = sec.locator(OSV_OK_TAG).count() > 0;
                if (hasOsvOk) {
                    log.info("  Skipping section '{}' — already has OSV OK", secName);
                    continue;
                }

                // Score: empty sections get bonus points
                boolean isEmpty = sec.getAttribute("class") != null
                        && sec.getAttribute("class").contains("is-empty");
                int score = nameScore * 10 + (isEmpty ? 5 : 0);

                if (score > bestMatchScore) {
                    bestMatch = sec;
                    bestMatchName = secName;
                    bestMatchScore = score;
                }
            }
        }

        if (bestMatch != null) {
            log.info("  Found matching section: '{}' (score={}) for hint '{}'", bestMatchName, bestMatchScore, sectionHint);
        }
        return bestMatch;
    }

    private String extractEntityName(String checklistText) {
        if (checklistText.contains("'s ")) {
            return checklistText.substring(0, checklistText.indexOf("'s ")).trim();
        }
        return "Others";
    }

    private String extractSectionName(String checklistText) {
        if (checklistText.contains("'s ")) {
            return checklistText.substring(checklistText.indexOf("'s ") + 3).trim();
        }
        return checklistText.trim();
    }
}
