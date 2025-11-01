package exportation_panelera.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Quick utility to verify if a hash matches a password
 */
public class VerifyHash {

    public static void main(String[] args) {
        String password = "admin123";
        String dbHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi";
        String newHash = "$2a$12$qDeuglk8K8cmXazOdrBhRuvB/CN7za6aOqwZtA5TGPVx0ZkIkxLxy";

        System.out.println("Testing password: " + password);
        System.out.println();

        System.out.println("=== CURRENT DATABASE HASH ===");
        System.out.println("Hash: " + dbHash);
        System.out.println("Hash length: " + dbHash.length());
        System.out.println("First 10 chars: " + dbHash.substring(0, 10));

        try {
            boolean dbHashValid = BCrypt.checkpw(password, dbHash);
            System.out.println("✓ BCrypt.checkpw() result: " + dbHashValid);
            if (dbHashValid) {
                System.out.println("✅ DATABASE HASH IS VALID FOR 'admin123'");
            } else {
                System.out.println("❌ DATABASE HASH DOES NOT MATCH 'admin123'");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR verifying database hash: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("=== NEWLY GENERATED HASH ===");
        System.out.println("Hash: " + newHash);
        System.out.println("Hash length: " + newHash.length());

        try {
            boolean newHashValid = BCrypt.checkpw(password, newHash);
            System.out.println("✓ BCrypt.checkpw() result: " + newHashValid);
            if (newHashValid) {
                System.out.println("✅ NEW HASH IS VALID FOR 'admin123'");
            } else {
                System.out.println("❌ NEW HASH DOES NOT MATCH 'admin123'");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR verifying new hash: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("=== RECOMMENDATION ===");
        if (!BCrypt.checkpw(password, dbHash)) {
            System.out.println("The database hash is invalid. Update it with this SQL:");
            System.out.println();
            System.out.println("UPDATE users SET password_hash = '" + newHash + "' WHERE username = 'admin';");
        }
    }
}
