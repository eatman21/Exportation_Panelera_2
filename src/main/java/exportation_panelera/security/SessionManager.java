package exportation_panelera.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages user sessions with timeout and security features
 */
public class SessionManager {

    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    // Session data
    private static class Session {
        final String username;
        final LocalDateTime createdAt;
        LocalDateTime lastActivity;
        final String ipAddress;
        final Map<String, Object> attributes;

        Session(String username, String ipAddress) {
            this.username = username;
            this.createdAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.ipAddress = ipAddress;
            this.attributes = new ConcurrentHashMap<>();
        }

        void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        boolean isExpired(int timeoutMinutes) {
            return LocalDateTime.now().isAfter(lastActivity.plusMinutes(timeoutMinutes));
        }
    }

    // Configuration
    private final int sessionTimeoutMinutes;
    private final int maxSessionsPerUser;

    // Active sessions: sessionId -> Session
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // User to sessions mapping: username -> Set of session IDs
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    // Random generator for session IDs
    private final SecureRandom random = new SecureRandom();

    /**
     * Create session manager with default settings:
     * - 30 minute timeout
     * - 3 max concurrent sessions per user
     */
    public SessionManager() {
        this(30, 3);
    }

    /**
     * Create session manager with custom settings
     *
     * @param sessionTimeoutMinutes Session timeout in minutes
     * @param maxSessionsPerUser Maximum concurrent sessions per user
     */
    public SessionManager(int sessionTimeoutMinutes, int maxSessionsPerUser) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.maxSessionsPerUser = maxSessionsPerUser;
        logger.info(String.format("SessionManager initialized: %d minute timeout, %d max sessions per user",
            sessionTimeoutMinutes, maxSessionsPerUser));
    }

    /**
     * Create a new session for a user
     *
     * @param username The username
     * @param ipAddress The IP address of the user
     * @return Session ID token
     */
    public String createSession(String username, String ipAddress) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Clean up expired sessions first
        cleanupExpiredSessions();

        // Check if user already has an active session
        String existingSessionId = userSessions.get(username);
        if (existingSessionId != null) {
            Session existingSession = sessions.get(existingSessionId);
            if (existingSession != null && !existingSession.isExpired(sessionTimeoutMinutes)) {
                // Reuse existing session
                existingSession.updateActivity();
                logger.info(String.format("Reusing existing session for user %s", username));
                return existingSessionId;
            }
        }

        // Generate new session ID
        String sessionId = generateSessionId();

        // Create new session
        Session session = new Session(username, ipAddress);
        sessions.put(sessionId, session);
        userSessions.put(username, sessionId);

        logger.info(String.format("Created new session for user %s from %s", username, ipAddress));

        return sessionId;
    }

    /**
     * Validate a session and update its activity timestamp
     *
     * @param sessionId The session ID to validate
     * @return true if session is valid, false otherwise
     */
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        // Check if expired
        if (session.isExpired(sessionTimeoutMinutes)) {
            logger.info(String.format("Session %s expired for user %s", sessionId, session.username));
            destroySession(sessionId);
            return false;
        }

        // Update activity timestamp
        session.updateActivity();
        return true;
    }

    /**
     * Get the username associated with a session
     *
     * @param sessionId The session ID
     * @return Username, or null if session is invalid
     */
    public String getUsername(String sessionId) {
        if (!validateSession(sessionId)) {
            return null;
        }

        Session session = sessions.get(sessionId);
        return session != null ? session.username : null;
    }

    /**
     * Destroy a session
     *
     * @param sessionId The session ID to destroy
     */
    public void destroySession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Session session = sessions.remove(sessionId);
        if (session != null) {
            userSessions.remove(session.username);
            logger.info(String.format("Destroyed session for user %s", session.username));
        }
    }

    /**
     * Destroy all sessions for a user
     *
     * @param username The username
     */
    public void destroyUserSessions(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        String sessionId = userSessions.remove(username);
        if (sessionId != null) {
            sessions.remove(sessionId);
            logger.info(String.format("Destroyed all sessions for user %s", username));
        }
    }

    /**
     * Set a session attribute
     *
     * @param sessionId The session ID
     * @param key Attribute key
     * @param value Attribute value
     */
    public void setAttribute(String sessionId, String key, Object value) {
        if (!validateSession(sessionId)) {
            throw new IllegalStateException("Invalid or expired session");
        }

        Session session = sessions.get(sessionId);
        if (session != null) {
            session.attributes.put(key, value);
        }
    }

    /**
     * Get a session attribute
     *
     * @param sessionId The session ID
     * @param key Attribute key
     * @return Attribute value, or null if not found
     */
    public Object getAttribute(String sessionId, String key) {
        if (!validateSession(sessionId)) {
            return null;
        }

        Session session = sessions.get(sessionId);
        return session != null ? session.attributes.get(key) : null;
    }

    /**
     * Remove a session attribute
     *
     * @param sessionId The session ID
     * @param key Attribute key
     */
    public void removeAttribute(String sessionId, String key) {
        if (!validateSession(sessionId)) {
            return;
        }

        Session session = sessions.get(sessionId);
        if (session != null) {
            session.attributes.remove(key);
        }
    }

    /**
     * Get the number of active sessions
     *
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        cleanupExpiredSessions();
        return sessions.size();
    }

    /**
     * Get session information for monitoring
     *
     * @param sessionId The session ID
     * @return Session info string, or null if session doesn't exist
     */
    public String getSessionInfo(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        return String.format("User: %s, Created: %s, Last Activity: %s, IP: %s",
            session.username,
            session.createdAt,
            session.lastActivity,
            session.ipAddress);
    }

    /**
     * Clean up expired sessions
     */
    private void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(sessionTimeoutMinutes);
            if (expired) {
                userSessions.remove(entry.getValue().username);
                logger.fine(String.format("Cleaned up expired session for user %s", entry.getValue().username));
            }
            return expired;
        });
    }

    /**
     * Generate a secure random session ID
     *
     * @return Session ID string
     */
    private String generateSessionId() {
        byte[] randomBytes = new byte[32]; // 256 bits
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Clear all sessions (for testing or administrative purposes)
     */
    public void clearAll() {
        sessions.clear();
        userSessions.clear();
        logger.info("Cleared all sessions");
    }

    /**
     * Get session timeout in minutes
     */
    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }
}
