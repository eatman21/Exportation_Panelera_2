package exportation_panelera.util;

import exportation_panelera.config.ConfigLoader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption and decryption utility using AES algorithm
 *
 * @deprecated This class uses weak encryption (MD5-based, no IV).
 *             Use {@link Encoder} instead which implements AES-256-GCM.
 *             This class is kept for backward compatibility only.
 *
 * @author Cris
 */
@Deprecated
public class Endcoder {

    private static final Logger logger = Logger.getLogger(Endcoder.class.getName());

    private String clave_encrypt;
    private String clave_dencrypt;

    // Load encryption key from configuration file instead of hardcoding
    private final String secretKey;

    public Endcoder() {
        this.secretKey = ConfigLoader.getEncryptionKey();
    }

    public String getClave_encrypt() {
        return clave_encrypt;
    }

    public String getClave_dencrypt() {
        return clave_dencrypt;
    }

    /**
     * Encrypts a plain text string using AES encryption
     *
     * @param plainText Text to encrypt
     * @return Encrypted text in Base64 format
     * @throws Exception if encryption fails
     * @deprecated Use {@link Encoder#encrypt(String)} instead
     */
    @Deprecated
    public String encrypt(String plainText) throws Exception {
        logger.warning("Using deprecated Endcoder class. Please migrate to Encoder class for better security.");

        Cipher cipher = Cipher.getInstance("AES");
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        byte[] keyPassword = md5.digest(secretKey.getBytes("utf-8"));
        byte[] bytesKey = Arrays.copyOf(keyPassword, 16);
        SecretKey secretKeySpec = new SecretKeySpec(bytesKey, "AES");

        byte[] plainTextByte = plainText.getBytes();

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedByte = cipher.doFinal(plainTextByte);

        Base64.Encoder encoder = Base64.getEncoder();

        String encryptedText = encoder.encodeToString(encryptedByte);

        clave_encrypt = encryptedText;
        return encryptedText;
    }

    /**
     * Decrypts an encrypted text string using AES decryption
     *
     * @param encryptedText Encrypted text in Base64 format
     * @throws Exception if decryption fails
     * @deprecated Use {@link Encoder#decrypt(String)} instead
     */
    @Deprecated
    public void decrypt(String encryptedText) throws Exception {
        logger.warning("Using deprecated Endcoder class. Please migrate to Encoder class for better security.");

        Cipher cipher = Cipher.getInstance("AES");
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        byte[] keyPassword = md5.digest(secretKey.getBytes("utf-8"));
        byte[] bytesKey = Arrays.copyOf(keyPassword, 16);
        SecretKey secretKeySpec = new SecretKeySpec(bytesKey, "AES");

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedByte = decoder.decode(encryptedText);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedByte = cipher.doFinal(encryptedByte);

        String decryptedText = new String(decryptedByte);
        logger.fine("Decryption completed successfully");

        clave_dencrypt = decryptedText;
    }

    /**
     * Decrypts an encrypted text and returns the result directly
     *
     * @param encryptedText Encrypted text in Base64 format
     * @return Decrypted text
     * @throws Exception if decryption fails
     * @deprecated Use {@link Encoder#decryptToString(String)} instead
     */
    @Deprecated
    public String decryptToString(String encryptedText) throws Exception {
        logger.warning("Using deprecated Endcoder class. Please migrate to Encoder class for better security.");

        Cipher cipher = Cipher.getInstance("AES");
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        byte[] keyPassword = md5.digest(secretKey.getBytes("utf-8"));
        byte[] bytesKey = Arrays.copyOf(keyPassword, 16);
        SecretKey secretKeySpec = new SecretKeySpec(bytesKey, "AES");

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedByte = decoder.decode(encryptedText);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedByte = cipher.doFinal(encryptedByte);

        String decryptedText = new String(decryptedByte);
        clave_dencrypt = decryptedText;

        return decryptedText;
    }
}
