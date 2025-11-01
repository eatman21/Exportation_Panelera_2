package exportation_panelera.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility to generate BCrypt password hashes
 * Use this to create hashed passwords for database insertion
 *
 * Usage: Run this class and enter your desired password
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordHashGenerator <password>");
            System.out.println("Example: java PasswordHashGenerator mySecurePassword123");
            System.out.println();

            // Generate some example hashes
            System.out.println("Example password hashes:");
            System.out.println("========================");
            generateAndPrint("admin123");
            generateAndPrint("password123");
            generateAndPrint("securePass2024");

            return;
        }

        String password = args[0];
        generateAndPrint(password);
    }

    private static void generateAndPrint(String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println();
        System.out.println("SQL to insert admin user:");
        System.out.println("INSERT INTO users (username, password_hash, is_active, created_at) VALUES");
        System.out.println("  ('admin', '" + hash + "', 1, NOW());");
        System.out.println();
        System.out.println("---");
        System.out.println();
    }

    /**
     * Programmatically generate a hash for a password
     * @param password Plain text password
     * @return BCrypt hash
     */
    public static String generateHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verify a password against a hash
     * @param password Plain text password
     * @param hash BCrypt hash
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
