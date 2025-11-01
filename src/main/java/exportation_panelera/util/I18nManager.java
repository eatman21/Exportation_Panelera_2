package exportation_panelera.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Manages internationalization resources for the application.
 * Provides centralized access to localized strings and language switching.
 */
public class I18nManager {

    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static final String LANGUAGE_PREF_KEY = "app.language";
    private static final Preferences prefs = Preferences.userNodeForPackage(I18nManager.class);

    private static Locale currentLocale;
    private static ResourceBundle bundle;

    // Available locales
    public static final Locale ENGLISH = new Locale("en");
    public static final Locale SPANISH = new Locale("es");

    static {
        // Initialize with saved preference or default to English
        String savedLanguage = prefs.get(LANGUAGE_PREF_KEY, "en");
        currentLocale = "es".equals(savedLanguage) ? SPANISH : ENGLISH;
        loadBundle();
    }

    /**
     * Get a localized string by key
     *
     * @param key The resource key
     * @return The localized string, or the key itself if not found
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // Return the key itself if not found
            return key;
        }
    }

    /**
     * Get a localized string with parameter substitution
     *
     * @param key The resource key
     * @param params Parameters to substitute in the string
     * @return The localized string with parameters
     */
    public static String getString(String key, Object... params) {
        try {
            String format = bundle.getString(key);
            return String.format(format, params);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Get the current locale
     *
     * @return The current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Set the current locale and reload resources
     *
     * @param locale The new locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();

        // Save preference
        String langCode = locale.getLanguage();
        prefs.put(LANGUAGE_PREF_KEY, langCode);
    }

    /**
     * Switch to English
     */
    public static void setEnglish() {
        setLocale(ENGLISH);
    }

    /**
     * Switch to Spanish
     */
    public static void setSpanish() {
        setLocale(SPANISH);
    }

    /**
     * Check if current language is English
     *
     * @return true if current language is English
     */
    public static boolean isEnglish() {
        return ENGLISH.equals(currentLocale);
    }

    /**
     * Check if current language is Spanish
     *
     * @return true if current language is Spanish
     */
    public static boolean isSpanish() {
        return SPANISH.equals(currentLocale);
    }

    /**
     * Get the current language code
     *
     * @return Language code (e.g., "en", "es")
     */
    public static String getLanguageCode() {
        return currentLocale.getLanguage();
    }

    /**
     * Load the resource bundle for the current locale
     */
    private static void loadBundle() {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
        } catch (Exception e) {
            // Fallback to English if bundle cannot be loaded
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, ENGLISH);
        }
    }

    /**
     * Get all available locales
     *
     * @return Array of available locales
     */
    public static Locale[] getAvailableLocales() {
        return new Locale[]{ENGLISH, SPANISH};
    }

    /**
     * Get display name for a locale
     *
     * @param locale The locale
     * @return Display name (e.g., "English", "Español")
     */
    public static String getLocaleDisplayName(Locale locale) {
        if (ENGLISH.equals(locale)) {
            return "English";
        } else if (SPANISH.equals(locale)) {
            return "Español";
        }
        return locale.getDisplayName();
    }
}
