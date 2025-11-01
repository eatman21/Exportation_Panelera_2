package exportation_panelera.dao;

import exportation_panelera.Model.Delivery_InfDTO;
import exportation_panelera.db.DatabaseManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeliveryDAO
 * Tests CRUD operations for delivery management
 */
@RunWith(MockitoJUnitRunner.class)
public class DeliveryDAOTest {

    private DeliveryDAO deliveryDAO;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseManager> mockedDbManager;
    private SimpleDateFormat dateFormat;

    @Before
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Mock DatabaseManager static method
        mockedDbManager = mockStatic(DatabaseManager.class);
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        when(mockConnection.isClosed()).thenReturn(false);

        deliveryDAO = new DeliveryDAO();
    }

    @After
    public void tearDown() {
        if (mockedDbManager != null) {
            mockedDbManager.close();
        }
    }

    @Test
    public void testGetAllDeliveries_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Simulate two deliveries
        when(mockResultSet.next())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);

        // Setup mock data for first delivery
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("exportation_id")).thenReturn("001", "002");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001", "TRK002");
        when(mockResultSet.getString("status")).thenReturn("PENDING", "DELIVERED");
        when(mockResultSet.getString("notes")).thenReturn("Note 1", "Note 2");
        when(mockResultSet.getString("delivery_date")).thenReturn("2024-01-15", "2024-01-16");

        // Act
        List<Delivery_InfDTO> deliveries = deliveryDAO.getAllDeliveries();

        // Assert
        assertNotNull("Deliveries list should not be null", deliveries);
        assertEquals("Should return 2 deliveries", 2, deliveries.size());
        assertEquals("First delivery tracking number should match", "TRK001", deliveries.get(0).getTrackingNumber());
        assertEquals("Second delivery status should match", "DELIVERED", deliveries.get(1).getStatus());
    }

    @Test
    public void testGetAllDeliveries_EmptyResult() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Delivery_InfDTO> deliveries = deliveryDAO.getAllDeliveries();

        // Assert
        assertNotNull("Deliveries list should not be null", deliveries);
        assertEquals("Should return empty list", 0, deliveries.size());
    }

    @Test
    public void testGetAllDeliveries_DatabaseUnavailable() throws SQLException {
        // Arrange
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(null);
        deliveryDAO = new DeliveryDAO(); // Recreate with null connection

        // Act
        List<Delivery_InfDTO> deliveries = deliveryDAO.getAllDeliveries();

        // Assert
        assertNotNull("Should return empty list when database unavailable", deliveries);
        assertEquals("Should return empty list", 0, deliveries.size());
    }

    @Test
    public void testGetDeliveryById_Success() throws SQLException {
        // Arrange
        int deliveryId = 1;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id")).thenReturn(deliveryId);
        when(mockResultSet.getString("exportation_id")).thenReturn("001");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001");
        when(mockResultSet.getString("status")).thenReturn("PENDING");
        when(mockResultSet.getString("notes")).thenReturn("Test Note");
        when(mockResultSet.getString("delivery_date")).thenReturn("2024-01-15");

        // Act
        Delivery_InfDTO delivery = deliveryDAO.getDeliveryById(deliveryId);

        // Assert
        assertNotNull("Delivery should not be null", delivery);
        assertEquals("Delivery ID should match", deliveryId, delivery.getId());
        assertEquals("Tracking number should match", "TRK001", delivery.getTrackingNumber());
        verify(mockStatement).setInt(1, deliveryId);
    }

    @Test
    public void testGetDeliveryById_NotFound() throws SQLException {
        // Arrange
        int deliveryId = 999;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Delivery_InfDTO delivery = deliveryDAO.getDeliveryById(deliveryId);

        // Assert
        assertNull("Delivery should be null when not found", delivery);
    }

    @Test
    public void testInsertDelivery_Success() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("EXP001");
        delivery.setTrackingNumber("TRK001");
        delivery.setStatus("PENDING");
        delivery.setNotes("Test delivery");
        delivery.setDeliveryDate(new Date());

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = deliveryDAO.insertDelivery(delivery);

        // Assert
        assertTrue("Insert should be successful", result);
        verify(mockStatement).setString(1, "001"); // Verify EXP prefix was removed
        verify(mockStatement).setString(3, "TRK001");
        verify(mockStatement).setString(4, "PENDING");
        verify(mockStatement).setString(5, "Test delivery");
    }

    @Test
    public void testInsertDelivery_WithNullDate() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("001");
        delivery.setTrackingNumber("TRK002");
        delivery.setStatus("PENDING");
        delivery.setNotes("No date delivery");
        delivery.setDeliveryDate(null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = deliveryDAO.insertDelivery(delivery);

        // Assert
        assertTrue("Insert should be successful", result);
        verify(mockStatement).setNull(2, java.sql.Types.DATE);
    }

    @Test
    public void testInsertDelivery_DatabaseError() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("001");
        delivery.setTrackingNumber("TRK001");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Insert failed"));

        // Act
        boolean result = deliveryDAO.insertDelivery(delivery);

        // Assert
        assertFalse("Insert should fail on database error", result);
    }

    @Test
    public void testUpdateDelivery_Success() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setId(1);
        delivery.setExportId("EXP002");
        delivery.setTrackingNumber("TRK002");
        delivery.setStatus("DELIVERED");
        delivery.setNotes("Updated delivery");
        delivery.setDeliveryDate(new Date());

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = deliveryDAO.updateDelivery(delivery);

        // Assert
        assertTrue("Update should be successful", result);
        verify(mockStatement).setString(1, "002"); // Verify EXP prefix was removed
        verify(mockStatement).setString(3, "TRK002");
        verify(mockStatement).setString(4, "DELIVERED");
        verify(mockStatement).setInt(6, 1);
    }

    @Test
    public void testUpdateDelivery_NotFound() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setId(999);
        delivery.setExportId("001");
        delivery.setTrackingNumber("TRK999");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = deliveryDAO.updateDelivery(delivery);

        // Assert
        assertFalse("Update should fail when delivery not found", result);
    }

    @Test
    public void testDeleteDelivery_Success() throws SQLException {
        // Arrange
        int deliveryId = 1;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = deliveryDAO.deleteDelivery(deliveryId);

        // Assert
        assertTrue("Delete should be successful", result);
        verify(mockStatement).setInt(1, deliveryId);
    }

    @Test
    public void testDeleteDelivery_NotFound() throws SQLException {
        // Arrange
        int deliveryId = 999;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = deliveryDAO.deleteDelivery(deliveryId);

        // Assert
        assertFalse("Delete should fail when delivery not found", result);
    }

    @Test
    public void testDeleteDelivery_DatabaseError() throws SQLException {
        // Arrange
        int deliveryId = 1;
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Delete failed"));

        // Act
        boolean result = deliveryDAO.deleteDelivery(deliveryId);

        // Assert
        assertFalse("Delete should fail on database error", result);
    }

    @Test
    public void testMapResultSetToDTO_WithTimestamps() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("exportation_id")).thenReturn("001");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001");
        when(mockResultSet.getString("status")).thenReturn("PENDING");
        when(mockResultSet.getString("notes")).thenReturn("Test");
        when(mockResultSet.getString("delivery_date")).thenReturn("2024-01-15");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(now);
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(now);

        // Act
        Delivery_InfDTO delivery = deliveryDAO.getDeliveryById(1);

        // Assert
        assertNotNull("Delivery should have created_at timestamp", delivery.getCreatedAt());
        assertNotNull("Delivery should have updated_at timestamp", delivery.getUpdatedAt());
    }

    @Test
    public void testExportIdPrefix_Handling() throws SQLException {
        // Arrange - Test that EXP prefix is properly removed before DB insert
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("EXP123");
        delivery.setTrackingNumber("TRK001");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        deliveryDAO.insertDelivery(delivery);

        // Assert - Verify the prefix was stripped
        verify(mockStatement).setString(1, "123");
    }

    @Test
    public void testExportIdPrefix_WithoutPrefix() throws SQLException {
        // Arrange - Test that IDs without EXP prefix work correctly
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("456");
        delivery.setTrackingNumber("TRK002");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        deliveryDAO.insertDelivery(delivery);

        // Assert - Verify the ID is used as-is
        verify(mockStatement).setString(1, "456");
    }
}
