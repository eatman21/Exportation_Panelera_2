package exportation_panelera.security;

import exportation_panelera.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password policy validator
 * Enforces password complexity requirements
 */
public class PasswordPolicy {

    // Configuration
    private final int minLength;
    private final int maxLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigits;
    private final boolean requireSpecialChars;
    private final int minDifferentChars;

    // Patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    /**
     * Common weak passwords that should be rejected
     */
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "12345678", "qwerty", "abc123", "monkey",
        "1234567", "letmein", "trustno1", "dragon", "baseball", "111111",
        "iloveyou", "master", "sunshine", "ashley", "bailey", "passw0rd",
        "shadow", "123123", "654321", "superman", "qazwsx", "michael",
        "football", "admin", "admin123", "root", "toor", "pass", "test",
        "guest", "oracle", "password1", "welcome", "changeme"
    };

    /**
     * Create password policy with default settings:
     * - Min length: 8 characters
     * - Max length: 128 characters
     * - Requires: uppercase, lowercase, digits, special characters
     * - Min different characters: 6
     */
    public PasswordPolicy() {
        this(8, 128, true, true, true, true, 6);
    }

    /**
     * Create password policy with custom settings
     *
     * @param minLength Minimum password length
     * @param maxLength Maximum password length
     * @param requireUppercase Require at least one uppercase letter
     * @param requireLowercase Require at least one lowercase letter
     * @param requireDigits Require at least one digit
     * @param requireSpecialChars Require at least one special character
     * @param minDifferentChars Minimum number of different characters
     */
    public PasswordPolicy(int minLength, int maxLength, boolean requireUppercase,
                          boolean requireLowercase, boolean requireDigits,
                          boolean requireSpecialChars, int minDifferentChars) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigits = requireDigits;
        this.requireSpecialChars = requireSpecialChars;
        this.minDifferentChars = minDifferentChars;
    }

    /**
     * Validate a password against the policy
     *
     * @param password The password to validate
     * @throws ValidationException if password doesn't meet policy requirements
     */
    public void validate(String password) throws ValidationException {
        if (password == null) {
            throw new ValidationException("password", "Password cannot be null");
        }

        List<String> violations = new ArrayList<>();

        // Check length
        if (password.length() < minLength) {
            violations.add(String.format("Password must be at least %d characters long", minLength));
        }

        if (password.length() > maxLength) {
            violations.add(String.format("Password must not exceed %d characters", maxLength));
        }

        // Check character requirements
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one uppercase letter");
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one lowercase letter");
        }

        if (requireDigits && !DIGIT_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one digit");
        }

        if (requireSpecialChars && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one special character (!@#$%^&*...)");
        }

        // Check character diversity
        long uniqueChars = password.chars().distinct().count();
        if (uniqueChars < minDifferentChars) {
            violations.add(String.format("Password must contain at least %d different characters", minDifferentChars));
        }

        // Check against common passwords
        if (isCommonPassword(password)) {
            violations.add("Password is too common and easily guessed");
        }

        // Check for username in password (requires username parameter)
        // This check is done in validateWithUsername()

        // Throw exception if any violations found
        if (!violations.isEmpty()) {
            throw new ValidationException("password", String.join("; ", violations));
        }
    }

    /**
     * Validate a password against the policy, also checking that it doesn't contain the username
     *
     * @param password The password to validate
     * @param username The username to check against
     * @throws ValidationException if password doesn't meet policy requirements
     */
    public void validateWithUsername(String password, String username) throws ValidationException {
        // First run standard validation
        validate(password);

        // Check if password contains username
        if (username != null && !username.trim().isEmpty()) {
            String lowerPassword = password.toLowerCase();
            String lowerUsername = username.trim().toLowerCase();

            if (lowerPassword.contains(lowerUsername)) {
                throw new ValidationException("password", "Password must not contain the username");
            }

            // Check reverse
            if (lowerUsername.length() > 3 && lowerPassword.contains(new StringBuilder(lowerUsername).reverse())) {
                throw new ValidationException("password", "Password must not contain the username (reversed)");
            }
        }
    }

    /**
     * Check if a password is valid without throwing an exception
     *
     * @param password The password to check
     * @return true if valid, false otherwise
     */
    public boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * Get a description of the password policy requirements
     *
     * @return String describing the requirements
     */
    public String getPolicyDescription() {
        List<String> requirements = new ArrayList<>();

        requirements.add(String.format("Length: %d-%d characters", minLength, maxLength));

        if (requireUppercase) {
            requirements.add("At least one uppercase letter");
        }

        if (requireLowercase) {
            requirements.add("At least one lowercase letter");
        }

        if (requireDigits) {
            requirements.add("At least one digit");
        }

        if (requireSpecialChars) {
            requirements.add("At least one special character");
        }

        if (minDifferentChars > 0) {
            requirements.add(String.format("At least %d different characters", minDifferentChars));
        }

        requirements.add("Not a common/weak password");
        requirements.add("Must not contain username");

        return "Password Requirements:\n• " + String.join("\n• ", requirements);
    }

    /**
     * Calculate password strength score (0-100)
     *
     * @param password The password to evaluate
     * @return Strength score from 0 (very weak) to 100 (very strong)
     */
    public int calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (up to 30 points)
        int lengthScore = Math.min(30, (password.length() - minLength) * 3);
        score += Math.max(0, lengthScore);

        // Character variety score (up to 40 points)
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).find()) score += 10;
        if (DIGIT_PATTERN.matcher(password).find()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 10;

        // Unique characters score (up to 20 points)
        long uniqueChars = password.chars().distinct().count();
        int uniqueScore = (int) Math.min(20, (uniqueChars / (double) password.length()) * 30);
        score += uniqueScore;

        // Penalties
        if (isCommonPassword(password)) score -= 50;
        if (hasRepeatingPatterns(password)) score -= 10;

        // Bonus for very long passwords
        if (password.length() > 16) score += 10;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Get a textual description of password strength
     *
     * @param password The password to evaluate
     * @return String like "Very Strong", "Strong", "Moderate", "Weak", "Very Weak"
     */
    public String getStrengthDescription(String password) {
        int strength = calculateStrength(password);

        if (strength >= 80) return "Very Strong";
        if (strength >= 60) return "Strong";
        if (strength >= 40) return "Moderate";
        if (strength >= 20) return "Weak";
        return "Very Weak";
    }

    /**
     * Check if password is in the common passwords list
     */
    private boolean isCommonPassword(String password) {
        if (password == null) return false;

        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for simple repeating patterns
     */
    private boolean hasRepeatingPatterns(String password) {
        if (password == null || password.length() < 3) {
            return false;
        }

        // Check for character repetition (aaa, 111, etc.)
        Pattern repeatPattern = Pattern.compile("(.)\\1{2,}");
        if (repeatPattern.matcher(password).find()) {
            return true;
        }

        // Check for sequential characters (abc, 123, etc.)
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true; // Ascending sequence
            }
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true; // Descending sequence
            }
        }

        return false;
    }

    // Getters for configuration
    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public boolean isRequireDigits() {
        return requireDigits;
    }

    public boolean isRequireSpecialChars() {
        return requireSpecialChars;
    }

    public int getMinDifferentChars() {
        return minDifferentChars;
    }
}
