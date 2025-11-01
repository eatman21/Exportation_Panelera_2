package exportation_panelera.controller;

import exportation_panelera.Model.Delivery_InfDTO;
import exportation_panelera.Model.Exportation_InfDTO;
import exportation_panelera.db.DatabaseManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.*;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeliveryController
 * Tests delivery and exportation management
 */
@RunWith(MockitoJUnitRunner.class)
public class DeliveryControllerTest {

    private DeliveryController controller;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseManager> mockedDbManager;

    @Before
    public void setUp() throws SQLException {
        controller = new DeliveryController();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDbManager = mockStatic(DatabaseManager.class);
        mockedDbManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);
        mockedDbManager.when(DatabaseManager::isOfflineMode).thenReturn(false);

        when(mockConnection.isClosed()).thenReturn(false);
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

        when(mockResultSet.next())
            .thenReturn(true)
            .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("exportation_id")).thenReturn("001");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001");
        when(mockResultSet.getString("status")).thenReturn("PENDING");
        when(mockResultSet.getString("notes")).thenReturn("Test note");

        // Act
        List<Delivery_InfDTO> deliveries = controller.getAllDeliveries();

        // Assert
        assertNotNull("Deliveries list should not be null", deliveries);
        assertTrue("Should have at least one delivery", deliveries.size() >= 1);
        verify(mockStatement).executeQuery();
    }

    @Test
    public void testGetAllDeliveries_OfflineMode() {
        // Arrange
        mockedDbManager.when(DatabaseManager::isOfflineMode).thenReturn(true);

        // Act
        List<Delivery_InfDTO> deliveries = controller.getAllDeliveries();

        // Assert
        assertNotNull("Should return sample deliveries in offline mode", deliveries);
        assertTrue("Should have sample deliveries", deliveries.size() > 0);
    }

    @Test
    public void testGetAllDeliveries_EmptyResult() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Delivery_InfDTO> deliveries = controller.getAllDeliveries();

        // Assert
        assertNotNull("Should return sample deliveries when DB is empty", deliveries);
    }

    @Test
    public void testCreateDelivery_Success() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("EXP001");
        delivery.setTrackingNumber("TRK001");
        delivery.setStatus("PENDING");
        delivery.setNotes("Test delivery");
        delivery.setDeliveryDate(new Date());

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        boolean result = controller.createDelivery(delivery);

        // Assert
        assertTrue("Delivery should be created successfully", result);
        assertEquals("Delivery ID should be set", 1, delivery.getId());
        verify(mockStatement).setString(eq(1), anyString()); // exportation_id
        verify(mockStatement).setString(3, "TRK001"); // tracking_number
    }

    @Test
    public void testCreateDelivery_NullDelivery() {
        // Act
        boolean result = controller.createDelivery(null);

        // Assert
        assertFalse("Should fail when delivery is null", result);
    }

    @Test
    public void testCreateDelivery_OfflineMode() {
        // Arrange
        mockedDbManager.when(DatabaseManager::isOfflineMode).thenReturn(true);

        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("EXP001");

        // Act
        boolean result = controller.createDelivery(delivery);

        // Assert
        assertTrue("Should simulate success in offline mode", result);
    }

    @Test
    public void testCreateDelivery_ExportIdHandling() throws SQLException {
        // Arrange - Test that EXP prefix is stripped
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setExportId("EXP123");
        delivery.setTrackingNumber("TRK001");

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        controller.createDelivery(delivery);

        // Assert - Verify the EXP prefix was stripped to just "123"
        verify(mockStatement).setString(1, "123");
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

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = controller.updateDelivery(delivery);

        // Assert
        assertTrue("Update should be successful", result);
        verify(mockStatement).setInt(6, 1); // WHERE id = ?
    }

    @Test
    public void testUpdateDelivery_NullDelivery() {
        // Act
        boolean result = controller.updateDelivery(null);

        // Assert
        assertFalse("Should fail when delivery is null", result);
    }

    @Test
    public void testUpdateDelivery_InvalidId() throws SQLException {
        // Arrange
        Delivery_InfDTO delivery = new Delivery_InfDTO();
        delivery.setId(0); // Invalid ID
        delivery.setExportId("EXP001");

        // Act
        boolean result = controller.updateDelivery(delivery);

        // Assert
        assertFalse("Should fail when delivery ID is invalid", result);
    }

    @Test
    public void testDeleteDelivery_Success() throws SQLException {
        // Arrange
        String deliveryId = "DEL001";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = controller.deleteDelivery(deliveryId);

        // Assert
        assertTrue("Delete should be successful", result);
        verify(mockStatement).setInt(1, 1); // Parsed numeric ID
    }

    @Test
    public void testDeleteDelivery_NotFound() throws SQLException {
        // Arrange
        String deliveryId = "DEL999";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = controller.deleteDelivery(deliveryId);

        // Assert
        assertFalse("Delete should fail when delivery not found", result);
    }

    @Test
    public void testDeleteDelivery_OfflineMode() {
        // Arrange
        mockedDbManager.when(DatabaseManager::isOfflineMode).thenReturn(true);

        // Act
        boolean result = controller.deleteDelivery("DEL001");

        // Assert
        assertFalse("Should not allow delete in offline mode", result);
    }

    @Test
    public void testGetDeliveryById_Success() throws SQLException {
        // Arrange
        String deliveryId = "DEL001";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("exportation_id")).thenReturn("001");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001");
        when(mockResultSet.getString("status")).thenReturn("PENDING");

        // Act
        Delivery_InfDTO delivery = controller.getDeliveryById(deliveryId);

        // Assert
        assertNotNull("Delivery should be found", delivery);
        assertEquals("Delivery ID should match", 1, delivery.getId());
    }

    @Test
    public void testGetDeliveryById_NotFound() throws SQLException {
        // Arrange
        String deliveryId = "DEL999";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Delivery_InfDTO delivery = controller.getDeliveryById(deliveryId);

        // Assert
        assertNull("Delivery should not be found", delivery);
    }

    @Test
    public void testCreateExportation_Success() throws SQLException {
        // Arrange
        Exportation_InfDTO exportation = new Exportation_InfDTO();
        exportation.setExportationId("2024001");
        exportation.setProductType("Panela");
        exportation.setAmount(100.0);
        exportation.setDestination("USA");

        // Mock for checking customer exists
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);

        // Mock for table columns metadata
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockColumnsRs = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumns(any(), any(), eq("exportations"), any())).thenReturn(mockColumnsRs);
        when(mockColumnsRs.next()).thenReturn(false);

        // Mock for ensureDefaultCustomerExists() - uses simple prepareStatement(String)
        when(mockConnection.prepareStatement(contains("SELECT id FROM customers")))
            .thenReturn(mockCheckStmt);
        // Mock for createExportation INSERT - uses prepareStatement(String, int)
        when(mockConnection.prepareStatement(contains("INSERT INTO exportations"), anyInt()))
            .thenReturn(mockStatement);

        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
        when(mockCheckRs.next()).thenReturn(true); // Customer exists

        when(mockStatement.executeUpdate()).thenReturn(1);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        boolean result = controller.createExportation(exportation);

        // Assert
        assertTrue("Exportation should be created successfully", result);
        assertEquals("Exportation database ID should be set", 1, exportation.getId());
    }

    @Test
    public void testCreateExportation_NullExportation() {
        // Act
        boolean result = controller.createExportation(null);

        // Assert
        assertFalse("Should fail when exportation is null", result);
    }

    @Test
    public void testCreateExportation_OfflineMode() {
        // Arrange
        mockedDbManager.when(DatabaseManager::isOfflineMode).thenReturn(true);

        Exportation_InfDTO exportation = new Exportation_InfDTO();
        exportation.setExportationId("2024001");

        // Act
        boolean result = controller.createExportation(exportation);

        // Assert
        assertTrue("Should simulate success in offline mode", result);
    }

    @Test
    public void testGetExportationById_Success() throws SQLException {
        // Arrange
        String exportationId = "2024001";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("exportation_id")).thenReturn(exportationId);
        when(mockResultSet.getString("product_type")).thenReturn("Panela");
        when(mockResultSet.getDouble("amount")).thenReturn(100.0);

        // Act
        Exportation_InfDTO exportation = controller.getExportationById(exportationId);

        // Assert
        assertNotNull("Exportation should be found", exportation);
        assertEquals("Exportation ID should match", exportationId, exportation.getExportationId());
    }

    @Test
    public void testGetExportationById_NotFound() throws SQLException {
        // Arrange
        String exportationId = "9999999";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Exportation_InfDTO exportation = controller.getExportationById(exportationId);

        // Assert
        assertNull("Exportation should not be found", exportation);
    }

    @Test
    public void testTryReconnect_Success() {
        // Arrange
        mockedDbManager.when(DatabaseManager::tryConnect).thenReturn(true);

        // Act
        boolean result = controller.tryReconnect();

        // Assert
        assertTrue("Reconnect should be successful", result);
    }

    @Test
    public void testTryReconnect_Failure() {
        // Arrange
        mockedDbManager.when(DatabaseManager::tryConnect).thenReturn(false);

        // Act
        boolean result = controller.tryReconnect();

        // Assert
        assertFalse("Reconnect should fail", result);
    }

    @Test
    public void testGetDeliveryByExportId_Success() throws SQLException {
        // Arrange
        String exportId = "EXP001";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("exportation_id")).thenReturn("001");
        when(mockResultSet.getString("tracking_number")).thenReturn("TRK001");
        when(mockResultSet.getString("status")).thenReturn("PENDING");

        // Act
        Delivery_InfDTO delivery = controller.getDeliveryByExportId(exportId);

        // Assert
        assertNotNull("Delivery should be found by export ID", delivery);
        verify(mockStatement).setString(1, "001"); // Verify prefix was stripped
    }

    @Test
    public void testGetDeliveryByExportId_NotFound() throws SQLException {
        // Arrange
        String exportId = "EXP999";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Delivery_InfDTO delivery = controller.getDeliveryByExportId(exportId);

        // Assert
        assertNull("Delivery should not be found", delivery);
    }
}
