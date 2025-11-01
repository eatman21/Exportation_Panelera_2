package exportation_panelera.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the Validacion utility class
 */
public class ValidacionTest {

    private Validacion validator;

    @Before
    public void setUp() {
        validator = new Validacion();
    }

    // Email Validation Tests
    @Test
    public void testValidEmails() {
        assertTrue(validator.ValidarEmail("user@example.com"));
        assertTrue(validator.ValidarEmail("test.user@domain.co.uk"));
        assertTrue(validator.ValidarEmail("user+tag@example.com"));
        assertTrue(validator.ValidarEmail("user_name@example-domain.com"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse(validator.ValidarEmail("invalid-email"));
        assertFalse(validator.ValidarEmail("@example.com"));
        assertFalse(validator.ValidarEmail("user@"));
        assertFalse(validator.ValidarEmail("user @example.com"));
        assertFalse(validator.ValidarEmail(""));
        assertFalse(validator.ValidarEmail(null));
    }

    // Empty String Validation Tests
    @Test
    public void testValidarNoVacio() {
        assertTrue(validator.validarNoVacio("Valid text"));
        assertTrue(validator.validarNoVacio("   text with spaces   "));

        assertFalse(validator.validarNoVacio(""));
        assertFalse(validator.validarNoVacio("   "));
        assertFalse(validator.validarNoVacio(null));
    }

    // Length Validation Tests
    @Test
    public void testValidarLongitudMinima() {
        assertTrue(validator.validarLongitudMinima("123456", 6));
        assertTrue(validator.validarLongitudMinima("1234567", 6));

        assertFalse(validator.validarLongitudMinima("12345", 6));
        assertFalse(validator.validarLongitudMinima("", 1));
        assertFalse(validator.validarLongitudMinima(null, 1));
    }

    @Test
    public void testValidarLongitudMaxima() {
        assertTrue(validator.validarLongitudMaxima("123456", 10));
        assertTrue(validator.validarLongitudMaxima("1234567890", 10));

        assertFalse(validator.validarLongitudMaxima("12345678901", 10));
        assertFalse(validator.validarLongitudMaxima(null, 10));
    }

    // Phone Number Validation Tests
    @Test
    public void testValidPhoneNumbers() {
        assertTrue(validator.validarTelefono("1234567"));
        assertTrue(validator.validarTelefono("123456789012345"));
        assertTrue(validator.validarTelefono("12345678"));
    }

    @Test
    public void testInvalidPhoneNumbers() {
        assertFalse(validator.validarTelefono("123456")); // Too short
        assertFalse(validator.validarTelefono("1234567890123456")); // Too long
        assertFalse(validator.validarTelefono("123-456-7890")); // Has dashes
        assertFalse(validator.validarTelefono("abc1234567")); // Has letters
        assertFalse(validator.validarTelefono(""));
        assertFalse(validator.validarTelefono(null));
    }

    // Username Validation Tests
    @Test
    public void testValidUsernames() {
        assertTrue(validator.validarUsername("user123"));
        assertTrue(validator.validarUsername("test_user"));
        assertTrue(validator.validarUsername("User_Name_123"));
        assertTrue(validator.validarUsername("abc"));
    }

    @Test
    public void testInvalidUsernames() {
        assertFalse(validator.validarUsername("ab")); // Too short
        assertFalse(validator.validarUsername("123456789012345678901")); // Too long
        assertFalse(validator.validarUsername("user name")); // Has space
        assertFalse(validator.validarUsername("user-name")); // Has dash
        assertFalse(validator.validarUsername("user@name")); // Has @
        assertFalse(validator.validarUsername(""));
        assertFalse(validator.validarUsername(null));
    }

    // Password Validation Tests
    @Test
    public void testValidPasswords() {
        assertTrue(validator.validarPassword("123456"));
        assertTrue(validator.validarPassword("password"));
        assertTrue(validator.validarPassword("SecurePass123!"));
    }

    @Test
    public void testInvalidPasswords() {
        assertFalse(validator.validarPassword("12345")); // Too short
        assertFalse(validator.validarPassword(""));
        assertFalse(validator.validarPassword(null));
    }

    // Integer Validation Tests
    @Test
    public void testValidIntegers() {
        assertTrue(validator.validarNumeroEntero("123"));
        assertTrue(validator.validarNumeroEntero("0"));
        assertTrue(validator.validarNumeroEntero("-456"));
        assertTrue(validator.validarNumeroEntero("  789  "));
    }

    @Test
    public void testInvalidIntegers() {
        assertFalse(validator.validarNumeroEntero("123.45"));
        assertFalse(validator.validarNumeroEntero("abc"));
        assertFalse(validator.validarNumeroEntero("12a34"));
        assertFalse(validator.validarNumeroEntero(""));
        assertFalse(validator.validarNumeroEntero(null));
    }

    // Decimal Validation Tests
    @Test
    public void testValidDecimals() {
        assertTrue(validator.validarNumeroDecimal("123.45"));
        assertTrue(validator.validarNumeroDecimal("0.0"));
        assertTrue(validator.validarNumeroDecimal("-456.78"));
        assertTrue(validator.validarNumeroDecimal("123"));
        assertTrue(validator.validarNumeroDecimal("  789.12  "));
    }

    @Test
    public void testInvalidDecimals() {
        assertFalse(validator.validarNumeroDecimal("abc"));
        assertFalse(validator.validarNumeroDecimal("12.34.56"));
        assertFalse(validator.validarNumeroDecimal(""));
        assertFalse(validator.validarNumeroDecimal(null));
    }

    // Positive Number Validation Tests
    @Test
    public void testValidarNumeroPositivo() {
        assertTrue(validator.validarNumeroPositivo(1.0));
        assertTrue(validator.validarNumeroPositivo(0.01));
        assertTrue(validator.validarNumeroPositivo(100.5));

        assertFalse(validator.validarNumeroPositivo(0));
        assertFalse(validator.validarNumeroPositivo(-1.0));
        assertFalse(validator.validarNumeroPositivo(-100.5));
    }

    // Letters Only Validation Tests
    @Test
    public void testValidarSoloLetras() {
        assertTrue(validator.validarSoloLetras("John"));
        assertTrue(validator.validarSoloLetras("John Doe"));
        assertTrue(validator.validarSoloLetras("María José"));

        assertFalse(validator.validarSoloLetras("John123"));
        assertFalse(validator.validarSoloLetras("John-Doe"));
        assertFalse(validator.validarSoloLetras("John@Doe"));
        assertFalse(validator.validarSoloLetras(""));
        assertFalse(validator.validarSoloLetras(null));
    }

    // XSS Sanitization Tests
    @Test
    public void testSanitizarInput() {
        assertEquals("&lt;script&gt;alert('XSS')&lt;&#x2F;script&gt;",
                    validator.sanitizarInput("<script>alert('XSS')</script>"));

        assertEquals("&lt;img src=x onerror=alert(1)&gt;",
                    validator.sanitizarInput("<img src=x onerror=alert(1)>"));

        assertEquals("Normal text", validator.sanitizarInput("Normal text"));

        assertEquals("Text with &quot;quotes&quot;",
                    validator.sanitizarInput("Text with \"quotes\""));

        assertEquals("", validator.sanitizarInput(null));

        assertEquals("", validator.sanitizarInput(""));
    }

    @Test
    public void testSanitizarInputPreservesSpaces() {
        String input = "   Text with spaces   ";
        String sanitized = validator.sanitizarInput(input);
        assertEquals("Text with spaces", sanitized);
    }

    @Test
    public void testSanitizarInputHandlesComplexHTML() {
        String dangerous = "<div onclick=\"malicious()\">Click</div>";
        String safe = validator.sanitizarInput(dangerous);
        assertFalse("Should not contain <div", safe.contains("<div"));
        assertFalse("Should not contain onclick", safe.contains("onclick="));
        assertTrue("Should contain escaped &lt;", safe.contains("&lt;"));
    }

    // Edge Cases
    @Test
    public void testWhitespaceHandling() {
        // Email should handle trimming
        assertTrue(validator.ValidarEmail("  user@example.com  "));

        // Username should handle trimming
        assertTrue(validator.validarUsername("  validUser  "));

        // Phone should handle trimming
        assertTrue(validator.validarTelefono("  1234567  "));
    }

    @Test
    public void testUnicodeCharacters() {
        // Letters validation should handle accented characters
        assertTrue(validator.validarSoloLetras("José María"));
        assertTrue(validator.validarSoloLetras("Müller"));
        assertTrue(validator.validarSoloLetras("François"));
    }
}
