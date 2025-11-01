package exportation_panelera.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections using HikariCP connection pooling.
 * Provides automatic connection management with offline mode support.
 *
 * @author YourName
 * @version 2.0
 */
public class DatabaseManager {

    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    // HikariCP DataSource
    private static volatile HikariDataSource dataSource = null;
    private static final AtomicBoolean offlineMode = new AtomicBoolean(false);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    // Configuration
    private static final String HIKARI_CONFIG_FILE = "hikari.properties";

    // Prevent instantiation of utility class
    private DatabaseManager() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    
    /**
     * Initializes the HikariCP connection pool with configuration from properties file
     *
     * @return true if initialization successful, false if in offline mode
     */
    public static synchronized boolean initialize() {
        if (initialized.get() && dataSource != null && !dataSource.isClosed()) {
            logger.info("Database pool already initialized and healthy");
            return true;
        }

        try {
            // Load HikariCP configuration
            HikariConfig config = loadHikariConfig();

            // Create the data source
            dataSource = new HikariDataSource(config);

            // Test the connection
            try (Connection testConn = dataSource.getConnection()) {
                if (testConn.isValid(5)) {
                    offlineMode.set(false);
                    initialized.set(true);

                    logger.info("HikariCP connection pool initialized successfully");
                    logger.info("Pool name: " + dataSource.getPoolName());
                    logger.info("Max pool size: " + dataSource.getMaximumPoolSize());
                    logger.info("Connected to: " + testConn.getMetaData().getDatabaseProductName());
                    return true;
                } else {
                    throw new SQLException("Connection validation failed");
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database pool: " + e.getMessage(), e);
            setOfflineModeWithReason("Database connection failed: " + e.getMessage());
            closeDataSource();
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during database initialization", e);
            setOfflineModeWithReason("Unexpected error: " + e.getMessage());
            closeDataSource();
            return false;
        }
    }

    /**
     * Load HikariCP configuration from properties file
     *
     * @return HikariConfig instance
     */
    private static HikariConfig loadHikariConfig() {
        HikariConfig config = new HikariConfig();

        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream(HIKARI_CONFIG_FILE)) {
            if (input == null) {
                logger.warning("hikari.properties not found, using default configuration");
                config.setJdbcUrl("jdbc:mysql://localhost:3308/exportation_panelera");
                config.setUsername("root");
                config.setPassword("");
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setPoolName("ExportationPaneleraPool");
            } else {
                Properties props = new Properties();
                props.load(input);
                config = new HikariConfig(props);
                logger.info("HikariCP configuration loaded from " + HIKARI_CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading hikari.properties, using defaults", e);
            config.setJdbcUrl("jdbc:mysql://localhost:3308/exportation_panelera");
            config.setUsername("root");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("ExportationPaneleraPool");
        }

        return config;
    }
    
    /**
     * Gets a connection from the HikariCP connection pool
     *
     * @return database connection or null if in offline mode
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        // If in offline mode, return null immediately
        if (offlineMode.get()) {
            logger.finest("In offline mode - returning null connection");
            return null;
        }

        // If not initialized, try to initialize
        if (!initialized.get() || dataSource == null || dataSource.isClosed()) {
            logger.info("Database pool not initialized, attempting to initialize");
            if (!initialize()) {
                throw new SQLException("Cannot establish database connection - system is in offline mode");
            }
        }

        try {
            // HikariCP manages connection health automatically
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get connection from pool", e);
            throw e;
        }
    }
    
    /**
     * Attempts to establish a new database connection pool
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean tryConnect() {
        logger.info("Attempting to establish database connection pool");

        // Reset state for clean retry
        closeDataSource();
        initialized.set(false);

        // Try to initialize
        boolean success = initialize();

        if (success) {
            logger.info("Database connection pool established successfully");
        } else {
            logger.warning("Failed to establish database connection pool");
        }

        return success;
    }

    /**
     * Close the current data source safely
     */
    private static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                logger.fine("Data source closed");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error closing data source", e);
            }
            dataSource = null;
        }
    }
    
    /**
     * Tests the database connection pool and returns detailed status
     *
     * @return true if connection works, false otherwise
     */
    public static boolean testConnection() {
        try {
            if (offlineMode.get()) {
                logger.info("Currently in offline mode, attempting to reconnect");
                return tryConnect();
            }

            if (!initialized.get() || dataSource == null || dataSource.isClosed()) {
                logger.info("Database pool not initialized, attempting initialization");
                return initialize();
            }

            // Test getting a connection from the pool
            try (Connection testConn = dataSource.getConnection()) {
                boolean healthy = testConn.isValid(2);
                if (!healthy) {
                    logger.warning("Connection validation failed, attempting reconnection");
                    return tryConnect();
                }
            }

            logger.fine("Database connection pool test passed");
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Connection test failed with exception", e);
            setOfflineModeWithReason("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if there is a valid connection pool to the database
     *
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        if (offlineMode.get() || !initialized.get()) {
            return false;
        }

        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }

        try (Connection testConn = dataSource.getConnection()) {
            return testConn.isValid(1);
        } catch (SQLException e) {
            logger.log(Level.FINE, "Connection check failed", e);
            return false;
        }
    }
    
    /**
     * Checks if the system is in offline mode
     * 
     * @return true if in offline mode, false if connected to database
     */
    public static boolean isOfflineMode() {
        return offlineMode.get();
    }
    
    /**
     * Set offline mode with a specific reason
     * 
     * @param reason The reason for entering offline mode
     */
    private static void setOfflineModeWithReason(String reason) {
        offlineMode.set(true);
        initialized.set(false);
        logger.warning("Entering offline mode: " + reason);
    }
    
    /**
     * Forces the system into or out of offline mode
     * 
     * @param mode true to enable offline mode, false to attempt reconnection
     */
    public static void setOfflineMode(boolean mode) {
        if (mode) {
            setOfflineModeWithReason("Manually set to offline mode");
        } else {
            logger.info("Attempting to exit offline mode");
            offlineMode.set(false);
            tryConnect();
        }
    }
    
    /**
     * Determines if a connection is managed by the HikariCP connection pool
     * All connections from getConnection() are pool-managed
     *
     * @param conn The connection to check
     * @return true if the connection is from our pool, false otherwise
     */
    public static boolean isConnectionManaged(Connection conn) {
        if (conn == null) {
            return false;
        }

        try {
            // All connections from HikariCP are managed
            // Check if connection is a HikariProxyConnection
            String className = conn.getClass().getName();
            return className.contains("HikariProxyConnection");
        } catch (Exception e) {
            logger.log(Level.FINE, "Error checking if connection is managed", e);
            return false;
        }
    }
    
    /**
     * Get database connection pool status information
     *
     * @return Status information as a string
     */
    public static String getConnectionStatus() {
        if (offlineMode.get()) {
            return "OFFLINE - Database unavailable";
        }

        if (!initialized.get()) {
            return "NOT_INITIALIZED - Database pool not initialized";
        }

        if (dataSource == null || dataSource.isClosed()) {
            return "CLOSED - Data source is closed";
        }

        try (Connection testConn = dataSource.getConnection()) {
            if (testConn.isValid(1)) {
                return String.format("ONLINE - Pool: %s (Active: %d, Idle: %d, Total: %d)",
                        dataSource.getPoolName(),
                        dataSource.getHikariPoolMXBean().getActiveConnections(),
                        dataSource.getHikariPoolMXBean().getIdleConnections(),
                        dataSource.getHikariPoolMXBean().getTotalConnections());
            } else {
                return "UNHEALTHY - Connection validation failed";
            }
        } catch (SQLException e) {
            return "ERROR - Cannot get connection from pool: " + e.getMessage();
        }
    }

    /**
     * Shutdown the database connection pool safely
     */
    public static synchronized void shutdown() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("Database connection pool has been shut down gracefully");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during database shutdown", e);
        } finally {
            dataSource = null;
            initialized.set(false);
            offlineMode.set(true);
        }
    }
    
    /**
     * Get configuration information (without sensitive data)
     *
     * @return Configuration summary
     */
    public static String getConfigurationSummary() {
        if (dataSource == null) {
            return "Database Configuration: Not initialized";
        }

        return String.format(
                "Database Configuration:\n" +
                        "  JDBC URL: %s\n" +
                        "  Pool Name: %s\n" +
                        "  Max Pool Size: %d\n" +
                        "  Min Idle: %d\n" +
                        "  Status: %s",
                dataSource.getJdbcUrl(),
                dataSource.getPoolName(),
                dataSource.getMaximumPoolSize(),
                dataSource.getMinimumIdle(),
                getConnectionStatus()
        );
    }

    /**
     * Close a database connection (for backward compatibility)
     * Note: With HikariCP, connections should be closed in try-with-resources
     * This method is kept for API compatibility but does nothing
     */
    @Deprecated
    public static void closeConnection() {
        logger.fine("closeConnection() called - with HikariCP, connections are auto-managed");
        // Do nothing - HikariCP manages connections automatically
    }
    
    /**
     * Create database tables if they don't exist
     * 
     * @return true if tables were created or already exist
     */
    public static boolean createTablesIfNotExist() {
        if (offlineMode.get()) {
            logger.info("In offline mode - skipping table creation");
            return true;
        }
        
        try {
            Connection conn = getConnection();
            if (conn == null) {
                logger.warning("Cannot create tables - no database connection");
                return false;
            }
            
            // Create deliveries table using traditional string concatenation
            String createDeliveriesTable = "CREATE TABLE IF NOT EXISTS deliveries (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "delivery_id VARCHAR(50) UNIQUE," +
                "exportation_id VARCHAR(50)," +
                "carrier_name VARCHAR(100)," +
                "tracking_number VARCHAR(100)," +
                "delivery_address TEXT," +
                "contact_person VARCHAR(100)," +
                "contact_phone VARCHAR(20)," +
                "delivery_date DATE," +
                "status VARCHAR(50)," +
                "notes TEXT," +
                "shipping_method VARCHAR(50)," +
                "shipping_cost DECIMAL(10,2)," +
                "shipping_currency VARCHAR(3)," +
                "reference_number VARCHAR(100)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "is_active BOOLEAN DEFAULT TRUE," +
                "last_login TIMESTAMP NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            
            // Create exportations table
            String createExportationsTable = "CREATE TABLE IF NOT EXISTS exportations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "exportation_id VARCHAR(50) UNIQUE," +
                "product_type VARCHAR(100)," +
                "amount DECIMAL(10,2)," +
                "destination VARCHAR(100)," +
                "exportation_date DATE," +
                "unit_price DECIMAL(10,2)," +
                "currency VARCHAR(3)," +
                "has_delivery BOOLEAN DEFAULT FALSE," +
                "status VARCHAR(50)," +
                "notes TEXT," +
                "customer_name VARCHAR(100)," +
                "customer_email VARCHAR(100)," +
                "customer_phone VARCHAR(20)," +
                "document_number VARCHAR(50)," +
                "export_license VARCHAR(50)," +
                "employee_id VARCHAR(50)," +
                "transport_method VARCHAR(50)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            
            // Execute table creation
            conn.createStatement().execute(createDeliveriesTable);
            conn.createStatement().execute(createUsersTable);
            conn.createStatement().execute(createExportationsTable);
            
            logger.info("Database tables created or verified successfully");
            
            // Create default admin user if users table is empty
            createDefaultAdminUser(conn);
            
            return true;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating database tables", e);
            return false;
        }
    }
    
    /**
     * Create a default admin user if the users table is empty
     */
    private static void createDefaultAdminUser(Connection conn) {
        try {
            // Check if users table is empty
            var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default admin user
                String insertAdmin = "INSERT INTO users (username, password_hash, is_active) VALUES (?, ?, ?)";
                var stmt = conn.prepareStatement(insertAdmin);
                stmt.setString(1, "admin");
                stmt.setString(2, "admin123_hashed"); // In production, use proper password hashing
                stmt.setBoolean(3, true);
                stmt.executeUpdate();
                
                logger.info("Default admin user created (username: admin, password: admin123)");
                stmt.close();
            }
            rs.close();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error creating default admin user", e);
        }
    }
}