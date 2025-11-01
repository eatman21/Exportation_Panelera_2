package exportation_panelera.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the new secure Encoder class
 */
public class EncoderTest {

    private Encoder encoder;

    @Before
    public void setUp() {
        encoder = new Encoder();
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        String original = "Sensitive Data 123!";

        // Encrypt
        String encrypted = encoder.encrypt(original);
        assertNotNull("Encrypted text should not be null", encrypted);
        assertNotEquals("Encrypted text should differ from original", original, encrypted);

        // Decrypt
        String decrypted = encoder.decryptToString(encrypted);
        assertEquals("Decrypted text should match original", original, decrypted);
    }

    @Test
    public void testEncryptionProducesUniqueResults() throws Exception {
        String original = "Same Text";

        // Encrypt same text twice
        String encrypted1 = encoder.encrypt(original);
        String encrypted2 = encoder.encrypt(original);

        // Results should be different (due to random IV)
        assertNotEquals("Each encryption should produce unique result", encrypted1, encrypted2);

        // But both should decrypt to same original
        assertEquals(original, encoder.decryptToString(encrypted1));
        assertEquals(original, encoder.decryptToString(encrypted2));
    }

    @Test
    public void testEncryptEmptyString() {
        try {
            encoder.encrypt("");
            fail("Should throw exception for empty string");
        } catch (Exception e) {
            assertTrue("Should throw IllegalArgumentException",
                      e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testEncryptNull() {
        try {
            encoder.encrypt(null);
            fail("Should throw exception for null");
        } catch (Exception e) {
            assertTrue("Should throw IllegalArgumentException",
                      e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testDecryptInvalidData() {
        try {
            encoder.decryptToString("InvalidBase64Data!");
            fail("Should throw exception for invalid data");
        } catch (Exception e) {
            assertNotNull("Should throw exception", e);
        }
    }

    @Test
    public void testDecryptNull() {
        try {
            encoder.decryptToString(null);
            fail("Should throw exception for null");
        } catch (Exception e) {
            assertTrue("Should throw IllegalArgumentException",
                      e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testEncryptLongText() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a long text for testing encryption. ");
        }
        String longText = sb.toString();

        String encrypted = encoder.encrypt(longText);
        String decrypted = encoder.decryptToString(encrypted);

        assertEquals("Should handle long text correctly", longText, decrypted);
    }

    @Test
    public void testEncryptSpecialCharacters() throws Exception {
        String special = "Test!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";

        String encrypted = encoder.encrypt(special);
        String decrypted = encoder.decryptToString(encrypted);

        assertEquals("Should handle special characters", special, decrypted);
    }

    @Test
    public void testEncryptUnicode() throws Exception {
        String unicode = "Español: ñáéíóú 中文 日本語 한국어 🔒🔐";

        String encrypted = encoder.encrypt(unicode);
        String decrypted = encoder.decryptToString(encrypted);

        assertEquals("Should handle Unicode correctly", unicode, decrypted);
    }

    @Test
    public void testBackwardCompatibilityMethods() throws Exception {
        String original = "Test Data";

        // Test using old-style method names
        String encrypted = encoder.encrypt(original);
        encoder.decrypt(encrypted);

        String decryptedOldStyle = encoder.getClave_dencrypt();
        assertEquals("Backward compatibility methods should work", original, decryptedOldStyle);

        String encryptedOldStyle = encoder.getClave_encrypt();
        assertEquals("Should store last encrypted value", encrypted, encryptedOldStyle);
    }

    @Test
    public void testValidateKeyStrength() {
        // Assumes default key is strong enough (16+ characters)
        assertTrue("Encryption key should be strong enough", encoder.validateKeyStrength());
    }
}
