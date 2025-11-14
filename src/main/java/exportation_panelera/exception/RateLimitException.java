package exportation_panelera.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitException extends AuthenticationException {

    private final LocalDateTime retryAfter;
    private final int attemptCount;

    public RateLimitException(String username, int attemptCount, LocalDateTime retryAfter) {
        super(username, "Rate limit exceeded - too many login attempts");
        this.attemptCount = attemptCount;
        this.retryAfter = retryAfter;
    }

    public LocalDateTime getRetryAfter() {
        return retryAfter;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    @Override
    public String getMessage() {
        return String.format("Too many login attempts (%d). Please try again after %s",
            attemptCount, retryAfter);
    }
}
