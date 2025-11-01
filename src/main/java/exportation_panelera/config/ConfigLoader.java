package exportation_panelera.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration loader for application settings
 * Loads encryption keys and other configuration from properties file
 *
 * @author Cris
 */
public class ConfigLoader {
    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private static final String CONFIG_FILE = "application.properties";

    // Default encryption key (should be overridden in properties file)
    private static final String DEFAULT_ENCRYPTION_KEY = "MyDefaultSecretKey2024";

    private static Properties properties;

    static {
        properties = new Properties();
        loadConfiguration();
    }

    /**
     * Load configuration from properties file
     */
    private static void loadConfiguration() {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warning("Application properties file not found, using default configuration");
                setDefaults();
            } else {
                properties.load(input);
                logger.info("Application configuration loaded successfully");

                // Validate required properties
                validateConfiguration();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error reading application configuration: " + ex.getMessage());
            logger.info("Using default application configuration");
            setDefaults();
        }
    }

    /**
     * Set default configuration values
     */
    private static void setDefaults() {
        properties.setProperty("encryption.key", DEFAULT_ENCRYPTION_KEY);
    }

    /**
     * Validate loaded properties and set defaults for missing values
     */
    private static void validateConfiguration() {
        if (!properties.containsKey("encryption.key")) {
            logger.warning("Encryption key not found in configuration, using default");
            properties.setProperty("encryption.key", DEFAULT_ENCRYPTION_KEY);
        }
    }

    /**
     * Get the encryption key from configuration
     * @return Encryption key
     */
    public static String getEncryptionKey() {
        return properties.getProperty("encryption.key", DEFAULT_ENCRYPTION_KEY);
    }

    /**
     * Get a property value by key
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get a property value by key
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Reload configuration from properties file
     */
    public static void reload() {
        properties = new Properties();
        loadConfiguration();
    }
}
