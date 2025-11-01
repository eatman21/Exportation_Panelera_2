package exportation_panelera.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validation utility class for input validation
 *
 * @author Cris
 */
public class Validacion {

    /**
     * Validates email format
     *
     * @param email Email to validate
     * @return true if email is valid, false otherwise
     */
    public boolean ValidarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Pattern to validate email
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher match = pattern.matcher(email.trim());
        return match.matches();
    }

    /**
     * Validates that a string is not null or empty
     *
     * @param value String to validate
     * @return true if valid, false if null or empty
     */
    public boolean validarNoVacio(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that a string has a minimum length
     *
     * @param value String to validate
     * @param minLength Minimum required length
     * @return true if meets minimum length, false otherwise
     */
    public boolean validarLongitudMinima(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    /**
     * Validates that a string has a maximum length
     *
     * @param value String to validate
     * @param maxLength Maximum allowed length
     * @return true if within max length, false otherwise
     */
    public boolean validarLongitudMaxima(String value, int maxLength) {
        return value != null && value.trim().length() <= maxLength;
    }

    /**
     * Validates phone number format (numeric only, 7-15 digits)
     *
     * @param phoneNumber Phone number to validate
     * @return true if valid phone number, false otherwise
     */
    public boolean validarTelefono(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9]{7,15}$");
        Matcher match = pattern.matcher(phoneNumber.trim());
        return match.matches();
    }

    /**
     * Validates username format (alphanumeric and underscore, 3-20 chars)
     *
     * @param username Username to validate
     * @return true if valid username, false otherwise
     */
    public boolean validarUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        Matcher match = pattern.matcher(username.trim());
        return match.matches();
    }

    /**
     * Validates password strength (minimum 6 chars)
     *
     * @param password Password to validate
     * @return true if password meets requirements, false otherwise
     */
    public boolean validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    /**
     * Validates numeric value (integer)
     *
     * @param value String to validate as integer
     * @return true if valid integer, false otherwise
     */
    public boolean validarNumeroEntero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates numeric value (decimal)
     *
     * @param value String to validate as decimal
     * @return true if valid decimal, false otherwise
     */
    public boolean validarNumeroDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates that a numeric value is positive
     *
     * @param value Numeric value to check
     * @return true if positive, false otherwise
     */
    public boolean validarNumeroPositivo(double value) {
        return value > 0;
    }

    /**
     * Validates that a string contains only letters and spaces
     *
     * @param value String to validate
     * @return true if only letters and spaces, false otherwise
     */
    public boolean validarSoloLetras(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[a-zA-Z\\s]+$");
        Matcher match = pattern.matcher(value.trim());
        return match.matches();
    }

    /**
     * Sanitizes input to prevent XSS attacks
     *
     * @param input String to sanitize
     * @return Sanitized string
     */
    public String sanitizarInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
}
