package exportation_panelera.util;

import exportation_panelera.config.ConfigLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Secure encryption and decryption utility using AES-256-GCM
 *
 * This implementation uses:
 * - AES-256-GCM (Galois/Counter Mode) for authenticated encryption
 * - PBKDF2 for key derivation from password
 * - Random IV (Initialization Vector) for each encryption
 * - 128-bit authentication tag
 *
 * @author Cris
 */
public class Encoder {

    private static final Logger logger = Logger.getLogger(Encoder.class.getName());

    // AES-GCM parameters
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int AES_KEY_SIZE = 256; // 256 bits

    // PBKDF2 parameters for key derivation
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65536; // Industry standard
    private static final int SALT_LENGTH = 16; // 128 bits

    private String lastEncrypted;
    private String lastDecrypted;

    // Load encryption key from configuration file
    private final String secretKey;

    public Encoder() {
        this.secretKey = ConfigLoader.getEncryptionKey();
    }

    /**
     * Get the last encrypted value (for backward compatibility)
     */
    public String getClave_encrypt() {
        return lastEncrypted;
    }

    /**
     * Get the last decrypted value (for backward compatibility)
     */
    public String getClave_dencrypt() {
        return lastDecrypted;
    }

    /**
     * Encrypts a plain text string using AES-256-GCM
     *
     * @param plainText Text to encrypt
     * @return Encrypted text in Base64 format (includes IV and salt)
     * @throws Exception if encryption fails
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }

        try {
            // Generate random salt for key derivation
            byte[] salt = generateRandomBytes(SALT_LENGTH);

            // Derive encryption key from password using PBKDF2
            SecretKey key = deriveKey(secretKey, salt);

            // Generate random IV
            byte[] iv = generateRandomBytes(GCM_IV_LENGTH);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Encrypt
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine salt + IV + encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + encryptedBytes.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            // Encode to Base64
            String encryptedText = Base64.getEncoder().encodeToString(byteBuffer.array());
            lastEncrypted = encryptedText;

            logger.fine("Encryption successful");
            return encryptedText;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Encryption failed", e);
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts an encrypted text string using AES-256-GCM
     * Updates the lastDecrypted field (for backward compatibility)
     *
     * @param encryptedText Encrypted text in Base64 format
     * @throws Exception if decryption fails
     */
    public void decrypt(String encryptedText) throws Exception {
        lastDecrypted = decryptToString(encryptedText);
    }

    /**
     * Decrypts an encrypted text and returns the result directly
     *
     * @param encryptedText Encrypted text in Base64 format
     * @return Decrypted text
     * @throws Exception if decryption fails
     */
    public String decryptToString(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }

        try {
            // Decode from Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Extract salt, IV, and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);

            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // Derive decryption key from password using PBKDF2
            SecretKey key = deriveKey(secretKey, salt);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

            lastDecrypted = decryptedText;

            logger.fine("Decryption successful");
            return decryptedText;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Decryption failed", e);
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Derives a secret key from a password using PBKDF2
     *
     * @param password The password to derive key from
     * @param salt Salt for key derivation
     * @return Derived secret key
     * @throws Exception if key derivation fails
     */
    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            AES_KEY_SIZE
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword(); // Clear sensitive data

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generates cryptographically secure random bytes
     *
     * @param length Number of bytes to generate
     * @return Random bytes
     */
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * Validates the encryption key strength
     *
     * @return true if key is strong enough, false otherwise
     */
    public boolean validateKeyStrength() {
        if (secretKey == null || secretKey.length() < 16) {
            logger.warning("Encryption key is too weak (minimum 16 characters recommended)");
            return false;
        }
        return true;
    }
}
