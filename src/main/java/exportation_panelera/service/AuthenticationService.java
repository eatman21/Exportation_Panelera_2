package exportation_panelera.service;

import exportation_panelera.Model.LoginDTO;
import exportation_panelera.dao.UserDAO;
import exportation_panelera.exception.AuthenticationException;
import exportation_panelera.exception.RateLimitException;
import exportation_panelera.exception.ValidationException;
import exportation_panelera.security.PasswordPolicy;
import exportation_panelera.security.RateLimiter;
import exportation_panelera.security.SessionManager;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for authentication operations
 * Handles business logic for user authentication, session management, and rate limiting
 */
public class AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());

    private final UserDAO userDAO;
    private final RateLimiter rateLimiter;
    private final SessionManager sessionManager;
    private final PasswordPolicy passwordPolicy;

    // Singleton instance
    private static AuthenticationService instance;

    /**
     * Private constructor for singleton pattern
     */
    private AuthenticationService() {
        this.userDAO = new UserDAO();
        this.rateLimiter = new RateLimiter(5, 15); // 5 attempts per 15 minutes
        this.sessionManager = new SessionManager(30, 3); // 30 minute timeout, 3 max sessions
        this.passwordPolicy = new PasswordPolicy(); // Default policy
    }

    /**
     * Get singleton instance
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Authenticate a user and create a session
     *
     * @param username The username
     * @param password The password
     * @param ipAddress The IP address of the client
     * @return Session token
     * @throws AuthenticationException if authentication fails
     * @throws RateLimitException if rate limit is exceeded
     */
    public String login(String username, String password, String ipAddress)
            throws AuthenticationException, RateLimitException {

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }

        String normalizedUsername = username.trim().toLowerCase();

        // Check rate limiting
        if (!rateLimiter.isAllowed(normalizedUsername)) {
            LocalDateTime resetTime = rateLimiter.getResetTime(normalizedUsername);
            int attemptCount = 5; // Max attempts exceeded
            throw new RateLimitException(normalizedUsername, attemptCount, resetTime);
        }

        // Attempt authentication
        boolean authenticated = userDAO.authenticateUser(username, password);

        if (!authenticated) {
            // Record failed attempt
            rateLimiter.recordAttempt(normalizedUsername);

            int remaining = rateLimiter.getRemainingAttempts(normalizedUsername);
            String message = String.format("Authentication failed. %d attempts remaining.", remaining);

            logger.warning(String.format("Failed login attempt for user %s from %s",
                normalizedUsername, ipAddress));

            throw new AuthenticationException(username, "Invalid username or password. " + message);
        }

        // Authentication successful - clear rate limiting
        rateLimiter.clearAttempts(normalizedUsername);

        // Create session
        String sessionToken = sessionManager.createSession(username, ipAddress);

        logger.info(String.format("User %s logged in successfully from %s",
            username, ipAddress));

        return sessionToken;
    }

    /**
     * Logout a user (destroy session)
     *
     * @param sessionToken The session token
     */
    public void logout(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return;
        }

        String username = sessionManager.getUsername(sessionToken);
        sessionManager.destroySession(sessionToken);

        if (username != null) {
            logger.info(String.format("User %s logged out", username));
        }
    }

    /**
     * Validate a session token
     *
     * @param sessionToken The session token
     * @return true if valid, false otherwise
     */
    public boolean validateSession(String sessionToken) {
        return sessionManager.validateSession(sessionToken);
    }

    /**
     * Get the username for a session token
     *
     * @param sessionToken The session token
     * @return Username, or null if session is invalid
     */
    public String getSessionUsername(String sessionToken) {
        return sessionManager.getUsername(sessionToken);
    }

    /**
     * Create a new user account
     *
     * @param username The username
     * @param password The password
     * @return true if user created successfully
     * @throws ValidationException if validation fails
     * @throws AuthenticationException if user creation fails
     */
    public boolean createUser(String username, String password)
            throws ValidationException, AuthenticationException {

        // Validate username
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("username", "Username cannot be empty");
        }

        if (username.length() < 3) {
            throw new ValidationException("username", "Username must be at least 3 characters");
        }

        if (username.length() > 50) {
            throw new ValidationException("username", "Username must not exceed 50 characters");
        }

        // Validate password against policy
        passwordPolicy.validateWithUsername(password, username);

        // Create user
        LoginDTO loginDTO = new LoginDTO(username, password);

        boolean created = userDAO.createUser(loginDTO);

        if (!created) {
            throw new AuthenticationException("Failed to create user account");
        }

        logger.info(String.format("Created new user account: %s", username));
        return true;
    }

    /**
     * Change a user's password
     *
     * @param username The username
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password changed successfully
     * @throws AuthenticationException if authentication fails
     * @throws ValidationException if validation fails
     */
    public boolean changePassword(String username, String oldPassword, String newPassword)
            throws AuthenticationException, ValidationException {

        // Authenticate with old password first
        boolean authenticated = userDAO.authenticateUser(username, oldPassword);
        if (!authenticated) {
            throw new AuthenticationException(username, "Current password is incorrect");
        }

        // Validate new password
        passwordPolicy.validateWithUsername(newPassword, username);

        // Check that new password is different from old
        if (oldPassword.equals(newPassword)) {
            throw new ValidationException("newPassword", "New password must be different from current password");
        }

        // Update password
        boolean updated = userDAO.updatePassword(username, newPassword);

        if (!updated) {
            throw new AuthenticationException("Failed to update password");
        }

        // Destroy all sessions for this user (force re-login)
        sessionManager.destroyUserSessions(username);

        logger.info(String.format("Password changed for user %s", username));
        return true;
    }

    /**
     * Get remaining login attempts for a username
     *
     * @param username The username
     * @return Number of remaining attempts
     */
    public int getRemainingAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        return rateLimiter.getRemainingAttempts(username.trim().toLowerCase());
    }

    /**
     * Check if an account is locked
     *
     * @param username The username
     * @return true if locked, false otherwise
     */
    public boolean isAccountLocked(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return rateLimiter.isAccountLocked(username.trim().toLowerCase());
    }

    /**
     * Get password policy description
     *
     * @return String describing password requirements
     */
    public String getPasswordPolicyDescription() {
        return passwordPolicy.getPolicyDescription();
    }

    /**
     * Calculate password strength
     *
     * @param password The password to evaluate
     * @return Strength score (0-100)
     */
    public int getPasswordStrength(String password) {
        return passwordPolicy.calculateStrength(password);
    }

    /**
     * Get password strength description
     *
     * @param password The password to evaluate
     * @return Description like "Very Strong", "Strong", etc.
     */
    public String getPasswordStrengthDescription(String password) {
        return passwordPolicy.getStrengthDescription(password);
    }

    /**
     * Get session manager statistics
     *
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }

    /**
     * Get rate limiter statistics
     *
     * @return Number of locked accounts
     */
    public int getLockedAccountCount() {
        return rateLimiter.getLockedAccountCount();
    }

    /**
     * Get session timeout in minutes
     *
     * @return Session timeout
     */
    public int getSessionTimeoutMinutes() {
        return sessionManager.getSessionTimeoutMinutes();
    }
}
