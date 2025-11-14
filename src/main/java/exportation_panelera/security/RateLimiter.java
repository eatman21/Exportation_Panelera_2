package exportation_panelera.security;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Rate limiter for authentication attempts
 * Limits the number of login attempts per username within a time window
 */
public class RateLimiter {

    private static final Logger logger = Logger.getLogger(RateLimiter.class.getName());

    // Configuration
    private final int maxAttempts;
    private final int windowMinutes;

    // Track login attempts: username -> list of attempt timestamps
    private final Map<String, List<LocalDateTime>> loginAttempts = new ConcurrentHashMap<>();

    // Track locked accounts: username -> unlock time
    private final Map<String, LocalDateTime> lockedAccounts = new ConcurrentHashMap<>();

    /**
     * Create a rate limiter with default settings:
     * - Max 5 attempts per 15 minutes
     */
    public RateLimiter() {
        this(5, 15);
    }

    /**
     * Create a rate limiter with custom settings
     *
     * @param maxAttempts Maximum number of attempts allowed
     * @param windowMinutes Time window in minutes
     */
    public RateLimiter(int maxAttempts, int windowMinutes) {
        this.maxAttempts = maxAttempts;
        this.windowMinutes = windowMinutes;
        logger.info(String.format("RateLimiter initialized: %d attempts per %d minutes",
            maxAttempts, windowMinutes));
    }

    /**
     * Check if a login attempt is allowed for the given username
     *
     * @param username The username attempting to login
     * @return true if attempt is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim().toLowerCase();

        // Check if account is locked
        if (isAccountLocked(username)) {
            LocalDateTime unlockTime = lockedAccounts.get(username);
            logger.warning(String.format("Account locked for %s until %s", username, unlockTime));
            return false;
        }

        // Clean up old attempts outside the window
        cleanupOldAttempts(username);

        // Get recent attempts
        List<LocalDateTime> attempts = loginAttempts.getOrDefault(username, new ArrayList<>());

        // Check if limit exceeded
        if (attempts.size() >= maxAttempts) {
            logger.warning(String.format("Rate limit exceeded for %s: %d attempts in last %d minutes",
                username, attempts.size(), windowMinutes));

            // Lock the account for the window duration
            lockAccount(username);
            return false;
        }

        return true;
    }

    /**
     * Record a login attempt for the given username
     *
     * @param username The username that attempted to login
     */
    public void recordAttempt(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        username = username.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        loginAttempts.computeIfAbsent(username, k -> new ArrayList<>()).add(now);

        int attemptCount = loginAttempts.get(username).size();
        logger.fine(String.format("Recorded login attempt for %s (total: %d)", username, attemptCount));
    }

    /**
     * Clear all attempts for a username (call after successful login)
     *
     * @param username The username to clear attempts for
     */
    public void clearAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        username = username.trim().toLowerCase();
        loginAttempts.remove(username);
        lockedAccounts.remove(username);

        logger.fine(String.format("Cleared login attempts for %s", username));
    }

    /**
     * Get the number of remaining attempts for a username
     *
     * @param username The username to check
     * @return Number of remaining attempts before rate limit
     */
    public int getRemainingAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }

        username = username.trim().toLowerCase();

        if (isAccountLocked(username)) {
            return 0;
        }

        cleanupOldAttempts(username);
        int currentAttempts = loginAttempts.getOrDefault(username, new ArrayList<>()).size();
        return Math.max(0, maxAttempts - currentAttempts);
    }

    /**
     * Get the time when the rate limit will reset for a username
     *
     * @param username The username to check
     * @return LocalDateTime when limit resets, or null if not limited
     */
    public LocalDateTime getResetTime(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        username = username.trim().toLowerCase();

        // If account is locked, return unlock time
        if (isAccountLocked(username)) {
            return lockedAccounts.get(username);
        }

        // Get oldest attempt in the window
        List<LocalDateTime> attempts = loginAttempts.get(username);
        if (attempts == null || attempts.isEmpty()) {
            return null;
        }

        // Find the oldest attempt
        LocalDateTime oldest = attempts.stream()
            .min(LocalDateTime::compareTo)
            .orElse(null);

        if (oldest != null) {
            return oldest.plusMinutes(windowMinutes);
        }

        return null;
    }

    /**
     * Check if an account is currently locked
     *
     * @param username The username to check
     * @return true if locked, false otherwise
     */
    public boolean isAccountLocked(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim().toLowerCase();

        LocalDateTime unlockTime = lockedAccounts.get(username);
        if (unlockTime == null) {
            return false;
        }

        // Check if lock has expired
        if (LocalDateTime.now().isAfter(unlockTime)) {
            lockedAccounts.remove(username);
            return false;
        }

        return true;
    }

    /**
     * Lock an account for the window duration
     *
     * @param username The username to lock
     */
    private void lockAccount(String username) {
        LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(windowMinutes);
        lockedAccounts.put(username, unlockTime);
        logger.warning(String.format("Account locked for %s until %s", username, unlockTime));
    }

    /**
     * Clean up attempts that are outside the time window
     *
     * @param username The username to clean up attempts for
     */
    private void cleanupOldAttempts(String username) {
        List<LocalDateTime> attempts = loginAttempts.get(username);
        if (attempts == null || attempts.isEmpty()) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(windowMinutes);
        attempts.removeIf(attempt -> attempt.isBefore(cutoff));

        if (attempts.isEmpty()) {
            loginAttempts.remove(username);
        }
    }

    /**
     * Get statistics for monitoring
     *
     * @return Map of username -> attempt count
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();

        loginAttempts.forEach((username, attempts) -> {
            cleanupOldAttempts(username);
            int count = loginAttempts.getOrDefault(username, new ArrayList<>()).size();
            if (count > 0) {
                stats.put(username, count);
            }
        });

        return stats;
    }

    /**
     * Get the number of currently locked accounts
     *
     * @return Number of locked accounts
     */
    public int getLockedAccountCount() {
        // Clean up expired locks
        lockedAccounts.entrySet().removeIf(entry ->
            LocalDateTime.now().isAfter(entry.getValue())
        );

        return lockedAccounts.size();
    }

    /**
     * Clear all rate limiting data (for testing or administrative purposes)
     */
    public void clearAll() {
        loginAttempts.clear();
        lockedAccounts.clear();
        logger.info("Cleared all rate limiting data");
    }
}
