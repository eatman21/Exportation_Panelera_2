package exportation_panelera.exception;

/**
 * Exception thrown when input validation fails
 */
public class ValidationException extends Exception {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super("Validation failed for field '" + fieldName + "': " + message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
