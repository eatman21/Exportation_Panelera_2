package exportation_panelera.dao;

import exportation_panelera.Model.LoginDTO;
import exportation_panelera.db.DatabaseManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDAO
 * Tests authentication, user creation, password updates, and user deactivation
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDAOTest {

    private UserDAO userDAO;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseManager> mockedDbManager;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DatabaseManager static method
        mockedDbManager = mockStatic(DatabaseManager.class);
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);
    }

    @After
    public void tearDown() {
        if (mockedDbManager != null) {
            mockedDbManager.close();
        }
    }

    @Test
    public void testAuthenticateUser_Success() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "password123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn("password123_hashed");
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        // Act
        boolean result = userDAO.authenticateUser(username, password);

        // Assert
        assertTrue("User should be authenticated successfully", result);
        verify(mockStatement).setString(1, username);
    }

    @Test
    public void testAuthenticateUser_InvalidPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn("password123_hashed");
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        // Act
        boolean result = userDAO.authenticateUser(username, password);

        // Assert
        assertFalse("Authentication should fail with wrong password", result);
    }

    @Test
    public void testAuthenticateUser_UserNotFound() throws SQLException {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        boolean result = userDAO.authenticateUser(username, password);

        // Assert
        assertFalse("Authentication should fail for non-existent user", result);
    }

    @Test
    public void testAuthenticateUser_InactiveUser() throws SQLException {
        // Arrange
        String username = "inactiveuser";
        String password = "password123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn("password123_hashed");
        when(mockResultSet.getBoolean("is_active")).thenReturn(false);

        // Act
        boolean result = userDAO.authenticateUser(username, password);

        // Assert
        assertFalse("Authentication should fail for inactive user", result);
    }

    @Test
    public void testAuthenticateUser_EmptyUsername() {
        // Act
        boolean result = userDAO.authenticateUser("", "password123");

        // Assert
        assertFalse("Authentication should fail with empty username", result);
    }

    @Test
    public void testAuthenticateUser_NullUsername() {
        // Act
        boolean result = userDAO.authenticateUser(null, "password123");

        // Assert
        assertFalse("Authentication should fail with null username", result);
    }

    @Test
    public void testAuthenticateUser_EmptyPassword() {
        // Act
        boolean result = userDAO.authenticateUser("testuser", "");

        // Assert
        assertFalse("Authentication should fail with empty password", result);
    }

    @Test
    public void testAuthenticateUser_OfflineMode_DefaultAdmin() throws SQLException {
        // Arrange - simulate database unavailable
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(null);

        // Act
        boolean result = userDAO.authenticateUser("admin", "admin123");

        // Assert
        assertTrue("Default admin should authenticate in offline mode", result);
    }

    @Test
    public void testAuthenticateUser_OfflineMode_InvalidUser() throws SQLException {
        // Arrange - simulate database unavailable
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(null);

        // Act
        boolean result = userDAO.authenticateUser("testuser", "password");

        // Assert
        assertFalse("Non-admin user should fail authentication in offline mode", result);
    }

    @Test
    public void testCreateUser_Success() throws SQLException {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("newuser");
        loginDTO.setPassword("password123");

        PreparedStatement mockCountStatement = mock(PreparedStatement.class);
        ResultSet mockCountResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockCountStatement)
            .thenReturn(mockStatement);
        when(mockCountStatement.executeQuery()).thenReturn(mockCountResultSet);
        when(mockCountResultSet.next()).thenReturn(true);
        when(mockCountResultSet.getInt(1)).thenReturn(0); // User doesn't exist
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.createUser(loginDTO);

        // Assert
        assertTrue("User should be created successfully", result);
        verify(mockStatement).setString(1, "newuser");
        verify(mockStatement).setString(2, "password123_hashed");
    }

    @Test
    public void testCreateUser_NullDTO() {
        // Act
        boolean result = userDAO.createUser(null);

        // Assert
        assertFalse("Should fail when DTO is null", result);
    }

    @Test
    public void testCreateUser_UsernameAlreadyExists() throws SQLException {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("existinguser");
        loginDTO.setPassword("password123");

        PreparedStatement mockCountStatement = mock(PreparedStatement.class);
        ResultSet mockCountResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockCountStatement);
        when(mockCountStatement.executeQuery()).thenReturn(mockCountResultSet);
        when(mockCountResultSet.next()).thenReturn(true);
        when(mockCountResultSet.getInt(1)).thenReturn(1); // User exists

        // Act
        boolean result = userDAO.createUser(loginDTO);

        // Assert
        assertFalse("Should fail when username already exists", result);
    }

    @Test
    public void testUpdatePassword_Success() throws SQLException {
        // Arrange
        String username = "testuser";
        String newPassword = "newpassword123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.updatePassword(username, newPassword);

        // Assert
        assertTrue("Password should be updated successfully", result);
        verify(mockStatement).setString(1, "newpassword123_hashed");
        verify(mockStatement).setString(3, username);
    }

    @Test
    public void testUpdatePassword_EmptyUsername() {
        // Act
        boolean result = userDAO.updatePassword("", "newpassword123");

        // Assert
        assertFalse("Should fail with empty username", result);
    }

    @Test
    public void testUpdatePassword_ShortPassword() {
        // Act
        boolean result = userDAO.updatePassword("testuser", "short");

        // Assert
        assertFalse("Should fail with password shorter than 6 characters", result);
    }

    @Test
    public void testUpdatePassword_UserNotFound() throws SQLException {
        // Arrange
        String username = "nonexistent";
        String newPassword = "newpassword123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = userDAO.updatePassword(username, newPassword);

        // Assert
        assertFalse("Should fail when user is not found", result);
    }

    @Test
    public void testDeactivateUser_Success() throws SQLException {
        // Arrange
        String username = "testuser";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.deactivateUser(username);

        // Assert
        assertTrue("User should be deactivated successfully", result);
        verify(mockStatement).setString(2, username);
    }

    @Test
    public void testDeactivateUser_EmptyUsername() {
        // Act
        boolean result = userDAO.deactivateUser("");

        // Assert
        assertFalse("Should fail with empty username", result);
    }

    @Test
    public void testDeactivateUser_UserNotFound() throws SQLException {
        // Arrange
        String username = "nonexistent";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = userDAO.deactivateUser(username);

        // Assert
        assertFalse("Should fail when user is not found", result);
    }

    @Test
    public void testAuthenticateUser_DatabaseException_FallbackToOffline() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Connection error"));

        // Act
        boolean result = userDAO.authenticateUser("admin", "admin123");

        // Assert
        assertTrue("Should fallback to offline authentication for admin", result);
    }
}
