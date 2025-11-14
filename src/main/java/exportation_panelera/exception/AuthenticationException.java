package exportation_panelera.exception;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends Exception {

    private final String username;
    private final String reason;

    public AuthenticationException(String message) {
        super(message);
        this.username = null;
        this.reason = message;
    }

    public AuthenticationException(String username, String reason) {
        super("Authentication failed for user: " + username + " - " + reason);
        this.username = username;
        this.reason = reason;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.username = null;
        this.reason = message;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }
}
