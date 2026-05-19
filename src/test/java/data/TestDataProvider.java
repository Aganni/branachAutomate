package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads test data from YAML files based on LPC (Loan Product Code) and loan type.
 * Data files are organized as: testdata/{LPC}/{loanType}.yaml
 *
 * Usage in feature file:
 *   Given Initialize data for "normal" loan of "UBL"
 *
 * This loads: src/test/resources/testdata/UBL/normal.yaml
 *
 * Access in code:
 *   TestDataProvider.get("dsa.business_details.entity_pan")
 *   TestDataProvider.getMap("dsa.partner_details")
 */
public class TestDataProvider {

    private static final Logger log = LogManager.getLogger(TestDataProvider.class);
    private static final String DATA_BASE_DIR = "src/test/resources/testdata/";

    private static final ThreadLocal<Map<String, Object>> dataStore = new ThreadLocal<>();
    private static final ThreadLocal<String> currentLpc = new ThreadLocal<>();
    private static final ThreadLocal<String> currentLoanType = new ThreadLocal<>();

    private TestDataProvider() {}

    /**
     * Initializes test data for a specific LPC and loan type.
     * Loads from: testdata/{lpc}/{loanType}.yaml
     *
     * @param loanType e.g., "normal", "topup", "bt"
     * @param lpc      e.g., "UBL", "AIR", "SBL"
     */
    @SuppressWarnings("unchecked")
    public static void initialize(String loanType, String lpc) {
        currentLpc.set(lpc);
        currentLoanType.set(loanType);

        String filePath = DATA_BASE_DIR + lpc + "/" + loanType + ".yaml";
        File file = new File(filePath);

        if (!file.exists()) {
            throw new RuntimeException("Test data file not found: " + file.getAbsolutePath()
                    + "\nExpected path: testdata/" + lpc + "/" + loanType + ".yaml");
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> data = mapper.readValue(file, LinkedHashMap.class);
            dataStore.set(data);
            log.info("Test data loaded for LPC [{}], loan type [{}] from: {}", lpc, loanType, filePath);
        } catch (Exception e) {
            log.error("Failed to load test data from: {}", filePath);
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    /**
     * Gets a value using dot notation path.
     * Example: get("dsa.business_details.entity_pan") → "AAECM9636S"
     */
    @SuppressWarnings("unchecked")
    public static String get(String path) {
        Map<String, Object> data = dataStore.get();
        if (data == null) {
            throw new RuntimeException("Test data not initialized. Call initialize(loanType, lpc) first.");
        }

        String[] keys = path.split("\\.");
        Object current = data;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                throw new RuntimeException("Invalid path: " + path + " (failed at key: " + key + ")");
            }
        }

        if (current == null) {
            throw new RuntimeException("No value found for path: " + path);
        }

        return current.toString();
    }

    /**
     * Gets a value, returns defaultValue if path doesn't exist.
     */
    public static String getOrDefault(String path, String defaultValue) {
        try {
            return get(path);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a section as a flat Map<String, String>.
     * Useful for passing to page objects that accept Map parameters.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getMap(String path) {
        Map<String, Object> data = dataStore.get();
        if (data == null) {
            throw new RuntimeException("Test data not initialized.");
        }

        String[] keys = path.split("\\.");
        Object current = data;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                throw new RuntimeException("Invalid path for getMap: " + path);
            }
        }

        if (!(current instanceof Map)) {
            throw new RuntimeException("Path does not point to a map: " + path);
        }

        Map<String, Object> rawMap = (Map<String, Object>) current;
        Map<String, String> result = new LinkedHashMap<>();
        flattenMap("", rawMap, result);
        return result;
    }

    public static String getLpc() {
        return currentLpc.get();
    }

    public static String getLoanType() {
        return currentLoanType.get();
    }

    @SuppressWarnings("unchecked")
    private static void flattenMap(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                flattenMap(entry.getKey(), (Map<String, Object>) entry.getValue(), result);
            } else if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    /**
     * Cleans up ThreadLocal data (call in @After hook).
     */
    public static void cleanup() {
        dataStore.remove();
        currentLpc.remove();
        currentLoanType.remove();
    }
}
