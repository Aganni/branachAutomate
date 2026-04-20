package hooks;

import com.microsoft.playwright.*;
import org.testng.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class BaseTest {
    public static final String environment = "uat";

    // ThreadLocal for thread safety during parallel execution
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> userName = new ThreadLocal<>();
    private static final ThreadLocal<String> xApiKey = new ThreadLocal<>();
    private static final ThreadLocal<String> otp = new ThreadLocal<>();
    private static final ThreadLocal<String> email = new ThreadLocal<>();

    // Context Bridge: path to persisted storage state
    private static final ThreadLocal<String> storageStatePath = new ThreadLocal<>();

    public static final String STORAGE_STATE_DIR = System.getProperty("user.dir") + "/target/storage-states";
    public static Logger log = LogManager.getLogger(BaseTest.class);

    public BaseTest() {
    }

    // ───────────────────────────────────────────────
    //  Standard Playwright lifecycle
    // ───────────────────────────────────────────────

    public static Page getPage() {
        if (pageThreadLocal.get() == null) {
            startBrowserInstance();
        }
        return pageThreadLocal.get();
    }

    public static void startBrowserInstance() {
        try {
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));
            browserThreadLocal.set(browser);

            BrowserContext context = browser.newContext();
            contextThreadLocal.set(context);

            Page page = context.newPage();
            pageThreadLocal.set(page);

            log.info("Browser, context and page initialized successfully for thread");
        } catch (Exception exception) {
            log.error("Exception during browser set-up: " + exception.getMessage(), exception);
            throw new RuntimeException("Failed to initialize Playwright browser.", exception);
        }
    }

    public static void teardownBrowserInstance() {
        Page page = pageThreadLocal.get();
        BrowserContext context = contextThreadLocal.get();
        Browser browser = browserThreadLocal.get();
        Playwright playwright = playwrightThreadLocal.get();

        try {
            if (page != null) {
                try { page.close(); } catch (Exception ignore) { log.warn("Failed to close page: " + ignore.getMessage()); }
            }
            if (context != null) {
                try { context.close(); } catch (Exception ignore) { log.warn("Failed to close context: " + ignore.getMessage()); }
            }
            if (browser != null) {
                try { browser.close(); } catch (Exception ignore) { log.warn("Failed to close browser: " + ignore.getMessage()); }
            }
            if (playwright != null) {
                try { playwright.close(); } catch (Exception ignore) { log.warn("Failed to close playwright: " + ignore.getMessage()); }
            }
        } finally {
            pageThreadLocal.remove();
            contextThreadLocal.remove();
            browserThreadLocal.remove();
            playwrightThreadLocal.remove();
            userName.remove();
            xApiKey.remove();
            otp.remove();
            email.remove();
            storageStatePath.remove();
            log.info("Playwright resources cleaned up for thread");
        }
    }

    // ───────────────────────────────────────────────
    //  Context Bridge — Session Handoff Between Portals
    // ───────────────────────────────────────────────

    /**
     * Saves the current BrowserContext's storage state (cookies, localStorage)
     * to a JSON file so it can be injected into a new context later.
     *
     * Call this AFTER completing all actions on the first portal (DSA Portal).
     */
    public static String saveStorageState() {
        try {
            Path dir = Paths.get(STORAGE_STATE_DIR);
            Files.createDirectories(dir);

            String fileName = "dsa-session-" + Thread.currentThread().threadId() + ".json";
            String filePath = dir.resolve(fileName).toAbsolutePath().toString();

            contextThreadLocal.get().storageState(
                    new BrowserContext.StorageStateOptions().setPath(Paths.get(filePath))
            );
            storageStatePath.set(filePath);

            log.info("Storage state saved to: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Failed to save storage state: " + e.getMessage(), e);
            throw new RuntimeException("Context Bridge: failed to save storage state.", e);
        }
    }

    /**
     * Creates a brand-new BrowserContext pre-loaded with the storage state
     * from a previous portal session. Updates the ThreadLocal page reference.
     *
     * @param stateFilePath absolute path to the storage-state JSON
     */
    public static void createContextFromState(String stateFilePath) {
        try {
            Browser browser = browserThreadLocal.get();
            if (browser == null) {
                throw new IllegalStateException("Browser is not initialized. Call startBrowserInstance() first.");
            }

            // Close the old context and page gracefully
            Page oldPage = pageThreadLocal.get();
            BrowserContext oldContext = contextThreadLocal.get();
            if (oldPage != null) { try { oldPage.close(); } catch (Exception ignore) {} }
            if (oldContext != null) { try { oldContext.close(); } catch (Exception ignore) {} }

            // Create a new context seeded with the saved state
            BrowserContext newContext = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(Paths.get(stateFilePath))
            );
            contextThreadLocal.set(newContext);

            Page newPage = newContext.newPage();
            pageThreadLocal.set(newPage);

            log.info("New context created from storage state: {}", stateFilePath);
        } catch (Exception e) {
            log.error("Failed to create context from state: " + e.getMessage(), e);
            throw new RuntimeException("Context Bridge: failed to create context from saved state.", e);
        }
    }

    /**
     * High-level convenience method:
     *   1. Saves DSA Portal session
     *   2. Creates a new context with the saved session
     *   3. Navigates to the Jarvis Portal URL
     *
     * After this method returns, {@code getPage()} points at the Jarvis portal.
     */
    public static void switchToJarvisPortal() {
        try {
            String stateFile = saveStorageState();
            createContextFromState(stateFile);

            String jarvisUrl = initializeEnvironment("jarvisUrl");
            getPage().navigate(jarvisUrl);

            log.info("Switched to Jarvis Portal at: {}", jarvisUrl);
        } catch (Exception e) {
            log.error("Failed to switch to Jarvis portal: " + e.getMessage(), e);
            throw new RuntimeException("Context Bridge: portal switch failed.", e);
        }
    }

    // ───────────────────────────────────────────────
    //  Environment & Credentials (mirrors Apollo)
    // ───────────────────────────────────────────────

    public static String initializeEnvironment(String key) throws Exception {
        String propertyFile = System.getProperty("user.dir") + "/src/test/resources/properties/" + environment + ".properties";
        Properties properties = new Properties();

        if (!new File(propertyFile).exists()) {
            throw new FileNotFoundException("Properties file not found: " + propertyFile);
        }

        final int MAX_RETRIES = 3;
        final int RETRY_DELAY_MS = 2000;
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try (FileInputStream inputStream = new FileInputStream(propertyFile)) {
                properties.load(inputStream);
                String value = properties.getProperty(key);
                if (value != null) {
                    return value;
                }
                log.warn("Retrying attempt: " + (attempt + 1) + ". Property key '" + key + "' not found yet.");
                attempt++;
                Thread.sleep(RETRY_DELAY_MS);
            } catch (IOException e) {
                throw new Exception("Error reading properties file: " + propertyFile, e);
            }
        }
        throw new NullPointerException("Property key '" + key + "' not found in " + propertyFile);
    }

    public static synchronized void getCredentials(String portal) throws Exception {
        try {
            String propertyFile = System.getProperty("user.dir") + "/src/test/resources/properties/" + environment + "Credentials.properties";
            log.info("Loading credentials from: {}", propertyFile);
            
            if (!new File(propertyFile).exists()) {
                log.error("Credentials file not found at: {}", propertyFile);
                return;
            }

            try (InputStream inputStream = Files.newInputStream(Paths.get(propertyFile))) {
                Properties properties = new Properties();
                properties.load(inputStream);
                
                String user = properties.getProperty(portal.toLowerCase() + "UserEmail");
                String pass = properties.getProperty(portal.toLowerCase() + "UserPassword");
                String otpVal = properties.getProperty(portal.toLowerCase() +"Otp");

                if (user != null) setUserEmail(user);
                if (pass != null) setUserPassWord(pass);
                if (otpVal != null) setOtp(otpVal);
                
                log.info("Loaded credentials for {}: User={}, Email={}, OTP={}", 
                    portal, getUserEmail(), (getOtp() != null ? "****" : "null"), (getsetUserPassWord() != null ? "****" : "null"));
            }
        } catch (Exception e) {
            log.error("Exception in getCredentials: " + e.getMessage(), e);
        }
    }

    public static synchronized void setUserEmail(String user) { userName.set(user); }
    public static    String getUserEmail() { return userName.get(); }

    public static synchronized void setUserPassWord(String apiKey) { xApiKey.set(apiKey); }
    public static String getsetUserPassWord() { return xApiKey.get(); }

    public static synchronized void setOtp(String otpValue) { otp.set(otpValue); }
    public static String getOtp() { return otp.get(); }

    // ───────────────────────────────────────────────
    //  Storage state cleanup
    // ───────────────────────────────────────────────

    public static void cleanupStorageStates() {
        try {
            Path dir = Paths.get(STORAGE_STATE_DIR);
            if (Files.exists(dir)) {
                Files.list(dir)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (IOException ignore) {}
                        });
                log.info("Storage state files cleaned up.");
            }
        } catch (Exception e) {
            log.warn("Failed to clean up storage states: " + e.getMessage());
        }
    }

    public static void keepBrowserOpen(long milliseconds) {
        if (milliseconds <= 0) return;
        log.info("Browser will remain open for " + milliseconds / 1000 + " seconds...");
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Browser observation interrupted.", e);
        }
    }
}
