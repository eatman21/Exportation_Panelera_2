package exportation_panelera.View;

import exportation_panelera.Model.Delivery_InfDTO;
import exportation_panelera.controller.DeliveryController;
import exportation_panelera.db.DatabaseManager;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.SwingWorker;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Enhanced Delivery Management Form with modern features
 * Features: Export to CSV, Advanced Search, Async Loading, Enhanced UI
 */
public class DeliveryManagementForm extends javax.swing.JFrame {
    private static final Logger logger = Logger.getLogger(DeliveryManagementForm.class.getName());
    
    // Enhanced styling constants
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);      // Modern blue
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113);    // Modern green
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);        // Modern red
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);  // Light gray
    private static final Color HOVER_COLOR = new Color(41, 128, 185);        // Darker blue for hover
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
    
    // Connection status colors
    private static final Color COLOR_CONNECTED = new Color(39, 174, 96);     // Success green
    private static final Color COLOR_DISCONNECTED = new Color(231, 76, 60);  // Error red
    private static final Color COLOR_LOADING = new Color(52, 152, 219);      // Loading blue
    private static final String STATUS_CONNECTED = "Connected to Database";
    private static final String STATUS_OFFLINE = "Offline Mode - Database Unavailable";
    
    // Search and performance constants
    private static final int CONNECTION_CHECK_INTERVAL = 30; // seconds
    private static final int SEARCH_DELAY = 500; // milliseconds for search debouncing
    
    // UI Components - existing
    private JTable tblDeliveries;
    private JScrollPane scrollPane;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnTestConnection;
    private JTextField txtSearch;
    private JButton btnSearch;
    private JLabel statusLabel;
    
    // NEW UI Components for enhanced features
    private JButton btnExportCSV;
    private JComboBox<String> cmbSearchField;
    private JProgressBar progressBar;
    private JLabel lblRecordCount;
    private Timer searchTimer; // For debounced search
    
    // Controller and background services
    private DeliveryController controller;
    private ScheduledExecutorService connectionChecker;
    private boolean previousConnectionStatus = false;
    private TableRowSorter<DefaultTableModel> tableSorter; // For table sorting

    /**
     * Enhanced constructor with new features initialization
     */
    public DeliveryManagementForm() {
        controller = new DeliveryController();
        controller.debugPrintTableStructure();
        
        initComponents();
        setupEnhancedUI();
        setupTableSorting();
        initializeAsyncOperations();
        
        setLocationRelativeTo(null); // Center window
    }

    /**
     * Initialize all UI components with enhanced features
     */
    private void initComponents() {
        // Basic window setup
        setTitle("Delivery Management - Enhanced Version v2.0");
        setSize(1200, 800); // Larger window for new features
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create enhanced toolbar
        createEnhancedToolbar();
        
        // Create main table
        createEnhancedTable();
        
        // Create enhanced status panel
        createEnhancedStatusPanel();
        
        // Setup event handlers
        setupEventHandlers();
    }
    
    /**
     * Create enhanced toolbar with new features
     */
    private void createEnhancedToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BACKGROUND_COLOR);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        
        // Create buttons with enhanced styling
        btnAdd = createStyledButton("Add Delivery", PRIMARY_COLOR);
        btnEdit = createStyledButton("Edit Delivery", SECONDARY_COLOR);
        btnDelete = createStyledButton("Delete Delivery", ACCENT_COLOR);
        btnRefresh = createStyledButton("Refresh", new Color(155, 89, 182));
        btnTestConnection = createStyledButton("Test Connection", new Color(52, 73, 94));
        btnExportCSV = createStyledButton("Export to CSV", new Color(230, 126, 34)); // NEW
        
        // Enhanced search components
        cmbSearchField = new JComboBox<>(new String[]{
            "All Fields", "Delivery ID", "Export ID", "Tracking Number", 
            "Notes", "Status", "Delivery Date"
        });
        cmbSearchField.setFont(BUTTON_FONT);
        
        txtSearch = new JTextField(20);
        txtSearch.setFont(BUTTON_FONT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        btnSearch = createStyledButton("Search", new Color(52, 152, 219));
        
        // Add components to toolbar
        toolBar.add(btnAdd);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnEdit);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnDelete);
        toolBar.addSeparator(new Dimension(20, 0));
        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnTestConnection);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnExportCSV); // NEW
        toolBar.add(Box.createHorizontalGlue());
        
        // Search section
        toolBar.add(new JLabel("Search in: "));
        toolBar.add(cmbSearchField);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(txtSearch);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnSearch);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * Create styled button with hover effects
     */
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
        
        return button;
    }
    
    /**
     * Create enhanced table with sorting capabilities
     */
    private void createEnhancedTable() {
        tblDeliveries = new JTable();
        createTableModel();
        tblDeliveries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDeliveries.setRowHeight(28); // Slightly taller rows
        tblDeliveries.getTableHeader().setFont(LABEL_FONT);
        tblDeliveries.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Enhanced table styling
        tblDeliveries.getTableHeader().setBackground(PRIMARY_COLOR);
        tblDeliveries.getTableHeader().setForeground(Color.WHITE);
        tblDeliveries.setGridColor(new Color(200, 200, 200));
        tblDeliveries.setSelectionBackground(new Color(52, 152, 219, 50));
        
        // Double-click to edit
        tblDeliveries.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedDelivery();
                }
            }
        });
        
        scrollPane = new JScrollPane(tblDeliveries);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Setup table sorting functionality
     */
    private void setupTableSorting() {
        DefaultTableModel model = (DefaultTableModel) tblDeliveries.getModel();
        tableSorter = new TableRowSorter<>(model);
        tblDeliveries.setRowSorter(tableSorter);
    }
    
    /**
     * Create enhanced status panel with progress bar and record counter
     */
    private void createEnhancedStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, PRIMARY_COLOR));
        
        // Left side - status label
        statusLabel = new JLabel("Status: Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        // Center - progress bar (initially hidden)
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));
        
        // Right side - record counter
        lblRecordCount = new JLabel("Records: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRecordCount.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(lblRecordCount, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Setup enhanced event handlers
     */
    private void setupEventHandlers() {
        // Existing button actions
        btnAdd.addActionListener(e -> addDelivery());
        btnEdit.addActionListener(e -> editSelectedDelivery());
        btnDelete.addActionListener(e -> deleteSelectedDelivery());
        btnRefresh.addActionListener(e -> loadDeliveriesAsync());
        btnTestConnection.addActionListener(e -> testConnection());
        btnSearch.addActionListener(e -> performAdvancedSearch());
        
        // NEW: Export functionality
        btnExportCSV.addActionListener(e -> exportToCSV());
        
        // NEW: Real-time search with debouncing
        setupRealTimeSearch();
        
        // Enhanced table selection handling
        setupSmartButtonStates();
    }
    
    /**
     * Setup real-time search with debouncing
     */
    private void setupRealTimeSearch() {
        // Create timer for debounced search
        searchTimer = new Timer(SEARCH_DELAY, e -> performAdvancedSearch());
        searchTimer.setRepeats(false);
        
        // Add key listener for real-time search
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.restart(); // Restart timer on each keystroke
            }
        });
        
        // Also trigger search on field selection change
        cmbSearchField.addActionListener(e -> {
            if (!txtSearch.getText().trim().isEmpty()) {
                searchTimer.restart();
            }
        });
    }
    
    /**
     * Setup smart button state management
     */
    private void setupSmartButtonStates() {
        tblDeliveries.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = tblDeliveries.getSelectedRow() != -1;
                btnEdit.setEnabled(hasSelection);
                btnDelete.setEnabled(hasSelection && !DatabaseManager.isOfflineMode());
            }
        });
        
        // Initially disable edit/delete until selection
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    /**
     * Enhanced UI setup method
     */
    private void setupEnhancedUI() {
        // Apply modern styling to the frame
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Set window icon if available
        try {
            // You can add custom icon here if you have one
            // setIconImage(Toolkit.getDefaultToolkit().getImage("path/to/icon.png"));
        } catch (Exception e) {
            // Icon loading is optional
        }
    }
    
    /**
     * Initialize async operations
     */
    private void initializeAsyncOperations() {
        // Start initial data loading in background
        loadDeliveriesAsync();
        
        // Start connection monitoring
        startConnectionChecker();
    }

    /**
     * Create enhanced table model
     */
    private void createTableModel() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][] {}, 
            new String[] {"ID", "Export ID", "Tracking #", "Notes", "Delivery Date", "Status", "Created At", "Updated At"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Keep table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 6 || columnIndex == 7) {
                    return java.util.Date.class; // Date columns for proper sorting
                }
                return String.class;
            }
        };
        
        tblDeliveries.setModel(model);
    }
    
    /**
     * NEW FEATURE: Export current table data to CSV
     */
    private void exportToCSV() {
        try {
            DefaultTableModel model = (DefaultTableModel) tblDeliveries.getModel();
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No data to export. Please load deliveries first.",
                    "Export Error",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            // Choose file location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            fileChooser.setSelectedFile(new java.io.File("deliveries_export_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                
                // Ensure .csv extension
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                
                exportTableToCSV(model, file);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Data exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                logger.info("Successfully exported " + model.getRowCount() + " records to CSV");
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error exporting to CSV", ex);
            JOptionPane.showMessageDialog(
                this,
                "Error exporting data: " + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Helper method to write table data to CSV file
     */
    private void exportTableToCSV(DefaultTableModel model, java.io.File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.write(escapeCSVValue(model.getColumnName(col)));
                if (col < model.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");
            
            // Write data rows
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    String cellValue = "";
                    
                    if (value != null) {
                        if (value instanceof Date) {
                            cellValue = dateFormat.format((Date) value);
                        } else {
                            cellValue = value.toString();
                        }
                    }
                    
                    writer.write(escapeCSVValue(cellValue));
                    if (col < model.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        }
    }
    
    /**
     * Properly escape CSV values
     */
    private String escapeCSVValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * NEW FEATURE: Advanced search with field-specific filtering
     */
    private void performAdvancedSearch() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        String selectedField = (String) cmbSearchField.getSelectedItem();
        
        if (searchText.isEmpty()) {
            loadDeliveriesAsync(); // Load all if empty
            return;
        }
        
        try {
            showProgressBar("Searching...");
            
            // Get all deliveries
            List<Delivery_InfDTO> allDeliveries = controller.getAllDeliveries();
            
            if (allDeliveries == null || allDeliveries.isEmpty()) {
                hideProgressBar();
                showMessage("No deliveries available to search", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Filter based on selected field
            List<Delivery_InfDTO> filteredDeliveries = allDeliveries.stream()
                .filter(delivery -> delivery != null && matchesSearchCriteria(delivery, searchText, selectedField))
                .collect(Collectors.toList());
            
            // Update table with results
            SwingUtilities.invokeLater(() -> {
                displaySearchResults(filteredDeliveries, searchText, selectedField);
                hideProgressBar();
            });
            
        } catch (Exception ex) {
            hideProgressBar();
            logger.log(Level.SEVERE, "Error in advanced search", ex);
            showMessage("Search error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Check if delivery matches search criteria for specific field
     */
    private boolean matchesSearchCriteria(Delivery_InfDTO delivery, String searchText, String field) {
        switch (field) {
            case "Delivery ID":
                return containsIgnoreCase(delivery.getDeliveryId(), searchText);
            case "Export ID":
                return containsIgnoreCase(delivery.getExportId(), searchText);
            case "Tracking Number":
                return containsIgnoreCase(delivery.getTrackingNumber(), searchText);
            case "Notes":
                return containsIgnoreCase(delivery.getNotes(), searchText);
            case "Status":
                return containsIgnoreCase(delivery.getStatus(), searchText);
            case "Delivery Date":
                Date deliveryDate = delivery.getDeliveryDate() != null ? 
                    delivery.getDeliveryDate() : delivery.getDate();
                return deliveryDate != null && 
                    deliveryDate.toString().toLowerCase().contains(searchText);
            case "All Fields":
            default:
                return containsIgnoreCase(delivery.getDeliveryId(), searchText) ||
                       containsIgnoreCase(delivery.getExportId(), searchText) ||
                       containsIgnoreCase(delivery.getTrackingNumber(), searchText) ||
                       containsIgnoreCase(delivery.getNotes(), searchText) ||
                       containsIgnoreCase(delivery.getStatus(), searchText);
        }
    }
    
    /**
     * Display search results in table
     */
    private void displaySearchResults(List<Delivery_InfDTO> results, String searchText, String field) {
        DefaultTableModel model = (DefaultTableModel) tblDeliveries.getModel();
        model.setRowCount(0);
        
        if (results.isEmpty()) {
            updateStatus("No matches found for '" + searchText + "' in " + field, COLOR_DISCONNECTED);
            updateRecordCount(0);
            return;
        }
        
        // Add results to table
        for (Delivery_InfDTO delivery : results) {
            Object[] rowData = {
                delivery.getDeliveryId(),
                delivery.getExportId(),
                delivery.getTrackingNumber(),
                delivery.getNotes(),
                delivery.getDeliveryDate() != null ? delivery.getDeliveryDate() : delivery.getDate(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
            };
            model.addRow(rowData);
        }
        
        adjustColumnWidths();
        updateRecordCount(results.size());
        updateStatus("Found " + results.size() + " matches for '" + searchText + "' in " + field, COLOR_CONNECTED);
        
        logger.info("Advanced search completed: " + results.size() + " matches found");
    }
    
    /**
     * NEW FEATURE: Asynchronous data loading with progress indication
     */
    private void loadDeliveriesAsync() {
        // Create background worker for data loading
        SwingWorker<List<Delivery_InfDTO>, Void> worker = new SwingWorker<List<Delivery_InfDTO>, Void>() {
            @Override
            protected List<Delivery_InfDTO> doInBackground() throws Exception {
                // Show progress bar
                SwingUtilities.invokeLater(() -> showProgressBar("Loading deliveries..."));
                
                // Check connection
                boolean connected = controller.tryReconnect();
                SwingUtilities.invokeLater(() -> updateConnectionStatus(connected));
                
                // Get deliveries
                return controller.getAllDeliveries();
            }
            
            @Override
            protected void done() {
                try {
                    List<Delivery_InfDTO> deliveries = get();
                    displayDeliveries(deliveries);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error in async data loading", ex);
                    showMessage("Error loading deliveries: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                    loadSampleDataAsFallback();
                } finally {
                    hideProgressBar();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Display deliveries in table (extracted from loadDeliveries for reuse)
     */
    private void displayDeliveries(List<Delivery_InfDTO> deliveries) {
        DefaultTableModel model = (DefaultTableModel) tblDeliveries.getModel();
        model.setRowCount(0);
        
        if (deliveries == null || deliveries.isEmpty()) {
            updateStatus("No deliveries found", COLOR_DISCONNECTED);
            updateRecordCount(0);
            return;
        }
        
        // Add deliveries to table
        int successCount = 0;
        for (Delivery_InfDTO delivery : deliveries) {
            if (delivery == null) continue;
            
            try {
                Object[] rowData = {
                    delivery.getDeliveryId(),
                    delivery.getExportId(),
                    delivery.getTrackingNumber(),
                    delivery.getNotes(),
                    delivery.getDeliveryDate() != null ? delivery.getDeliveryDate() : delivery.getDate(),
                    delivery.getStatus(),
                    delivery.getCreatedAt(),
                    delivery.getUpdatedAt()
                };
                model.addRow(rowData);
                successCount++;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error adding delivery to table", e);
            }
        }
        
        adjustColumnWidths();
        updateRecordCount(successCount);
        updateStatus("Loaded " + successCount + " deliveries successfully", COLOR_CONNECTED);
        
        logger.info("Successfully displayed " + successCount + " deliveries");
    }
    
    /**
     * Show progress bar with message
     */
    private void showProgressBar(String message) {
        progressBar.setString(message);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
    }
    
    /**
     * Hide progress bar
     */
    private void hideProgressBar() {
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }
    
    /**
     * Update status label with color
     */
    private void updateStatus(String message, Color color) {
        statusLabel.setText("Status: " + message);
        statusLabel.setForeground(color);
    }
    
    /**
     * Update record counter
     */
    private void updateRecordCount(int count) {
        lblRecordCount.setText("Records: " + count);
    }
    
    /**
     * Show message dialog
     */
    private void showMessage(String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, "Information", messageType);
    }

    // Keep all existing methods (with minor updates for new features)
    
    /**
     * Enhanced connection status update
     */
    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            updateStatus(STATUS_CONNECTED, COLOR_CONNECTED);
            btnDelete.setEnabled(tblDeliveries.getSelectedRow() != -1);
        } else {
            updateStatus(STATUS_OFFLINE, COLOR_DISCONNECTED);
            btnDelete.setEnabled(false);
        }
        previousConnectionStatus = connected;
    }
    
    /**
     * Enhanced load sample data fallback
     */
    private void loadSampleDataAsFallback() {
        try {
            logger.info("Loading sample data as fallback");
            DefaultTableModel model = (DefaultTableModel) tblDeliveries.getModel();
            model.setRowCount(0);
            
            Object[][] sampleData = {
                {"DEL001", "EXP001", "TRK123456", "Sample delivery 1 - Express shipping", new Date(), "DELIVERED", new Date(), new Date()},
                {"DEL002", "EXP002", "TRK789012", "Sample delivery 2 - Standard shipping", new Date(), "IN_TRANSIT", new Date(), new Date()},
                {"DEL003", "EXP003", "TRK345678", "Sample delivery 3 - Priority mail", new Date(), "PENDING", new Date(), new Date()},
                {"DEL004", "EXP004", "TRK901234", "Sample delivery 4 - Overnight delivery", new Date(), "PROCESSING", new Date(), new Date()}
            };
            
            for (Object[] row : sampleData) {
                model.addRow(row);
            }
            
            adjustColumnWidths();
            updateRecordCount(sampleData.length);
            updateStatus("Showing sample data (database connection issue)", Color.ORANGE);
            logger.info("Sample data loaded successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Even sample data failed to load", e);
            updateStatus("Critical error - unable to display any data", COLOR_DISCONNECTED);
        }
    }
    
    // Keep existing helper methods with any necessary updates
    private boolean containsIgnoreCase(Object value, String searchText) {
        return value != null && value.toString().toLowerCase().contains(searchText);
    }
    
    private void adjustColumnWidths() {
        try {
            tblDeliveries.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
            tblDeliveries.getColumnModel().getColumn(1).setPreferredWidth(80);  // Export
            tblDeliveries.getColumnModel().getColumn(2).setPreferredWidth(120); // Tracking #
           tblDeliveries.getColumnModel().getColumn(3).setPreferredWidth(250); // Notes (wider for better readability)
           tblDeliveries.getColumnModel().getColumn(4).setPreferredWidth(120); // Delivery Date
           tblDeliveries.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
           tblDeliveries.getColumnModel().getColumn(6).setPreferredWidth(140); // Created At
           tblDeliveries.getColumnModel().getColumn(7).setPreferredWidth(140); // Updated At
           
           logger.fine("Column widths adjusted successfully");
       } catch (Exception e) {
           logger.log(Level.WARNING, "Error adjusting column widths", e);
       }
   }
   
   /**
    * Enhanced connection checker with better error handling
    */
   private void startConnectionChecker() {
       connectionChecker = Executors.newSingleThreadScheduledExecutor(r -> {
           Thread t = new Thread(r, "ConnectionChecker");
           t.setDaemon(true); // Make it daemon thread
           return t;
       });
       
       connectionChecker.scheduleAtFixedRate(() -> {
           try {
               final boolean connected = controller.tryReconnect();
               
               if (connected != previousConnectionStatus) {
                   SwingUtilities.invokeLater(() -> {
                       updateConnectionStatus(connected);
                       
                       String message = connected ?
                           "Database connection restored. Now working online." :
                           "Database connection lost. Working in offline mode.";
                       
                       // Only show notification if window is visible
                       if (isDisplayable() && isVisible()) {
                           JOptionPane.showMessageDialog(
                               this,
                               message,
                               "Connection Status Changed",
                               connected ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
                           );
                       }
                       
                       if (connected) {
                           loadDeliveriesAsync(); // Reload data when reconnected
                       }
                   });
                   
                   previousConnectionStatus = connected;
               }
           } catch (Exception ex) {
               logger.log(Level.WARNING, "Error in connection checker", ex);
           }
       }, 0, CONNECTION_CHECK_INTERVAL, TimeUnit.SECONDS);
   }
   
   /**
    * Enhanced connection test with better feedback
    */
   private void testConnection() {
       // Disable test button during test
       btnTestConnection.setEnabled(false);
       showProgressBar("Testing connection...");
       
       SwingWorker<Boolean, Void> testWorker = new SwingWorker<Boolean, Void>() {
           @Override
           protected Boolean doInBackground() throws Exception {
               return controller.tryReconnect();
           }
           
           @Override
           protected void done() {
               try {
                   boolean connected = get();
                   
                   if (connected) {
                       JOptionPane.showMessageDialog(
                           DeliveryManagementForm.this,
                           "✓ Successfully connected to the database!\n" +
                           "All features are now available.",
                           "Connection Test Successful",
                           JOptionPane.INFORMATION_MESSAGE
                       );
                       
                       updateConnectionStatus(true);
                       loadDeliveriesAsync(); // Refresh data
                   } else {
                       JOptionPane.showMessageDialog(
                           DeliveryManagementForm.this,
                           "✗ Could not connect to the database.\n\n" +
                           "Please check:\n" +
                           "• MySQL/XAMPP is running\n" +
                           "• Database 'exportation_panelera' exists\n" +
                           "• Network connection is stable\n" +
                           "• Database credentials are correct",
                           "Connection Test Failed",
                           JOptionPane.ERROR_MESSAGE
                       );
                       
                       updateConnectionStatus(false);
                   }
               } catch (Exception ex) {
                   logger.log(Level.SEVERE, "Error in connection test", ex);
                   JOptionPane.showMessageDialog(
                       DeliveryManagementForm.this,
                       "Connection test error: " + ex.getMessage(),
                       "Test Error",
                       JOptionPane.ERROR_MESSAGE
                   );
               } finally {
                   hideProgressBar();
                   btnTestConnection.setEnabled(true);
               }
           }
       };
       
       testWorker.execute();
   }
   
   /**
    * Enhanced add delivery with better offline handling
    */
   private void addDelivery() {
       try {
           if (DatabaseManager.isOfflineMode()) {
               int choice = JOptionPane.showConfirmDialog(
                   this,
                   "Currently in offline mode. You can view the form, but changes will not be saved.\n" +
                   "Do you want to continue anyway?",
                   "Offline Mode Warning",
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.WARNING_MESSAGE
               );
               
               if (choice != JOptionPane.YES_OPTION) {
                   return;
               }
           }
           
           ExportationDelivery deliveryForm = new ExportationDelivery();
           deliveryForm.setVisible(true);
           
           // Refresh data when form is closed
           deliveryForm.addWindowListener(new java.awt.event.WindowAdapter() {
               @Override
               public void windowClosed(java.awt.event.WindowEvent e) {
                   logger.info("Add delivery form closed, refreshing data");
                   loadDeliveriesAsync();
               }
           });
           
       } catch (Exception ex) {
           logger.log(Level.SEVERE, "Error opening add delivery form", ex);
           showMessage("Error opening delivery form: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
       }
   }
   
   /**
    * Enhanced edit delivery with better error handling
    */
   private void editSelectedDelivery() {
       int selectedRow = tblDeliveries.getSelectedRow();
       
       if (selectedRow == -1) {
           showMessage("Please select a delivery to edit", JOptionPane.INFORMATION_MESSAGE);
           return;
       }
       
       try {
           if (DatabaseManager.isOfflineMode()) {
               int choice = JOptionPane.showConfirmDialog(
                   this,
                   "Currently in offline mode. You can view the form, but changes will not be saved.\n" +
                   "Do you want to continue anyway?",
                   "Offline Mode Warning",
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.WARNING_MESSAGE
               );
               
               if (choice != JOptionPane.YES_OPTION) {
                   return;
               }
           }
           
           // Convert view row to model row (important for sorted tables)
           int modelRow = tblDeliveries.convertRowIndexToModel(selectedRow);
           String deliveryId = tblDeliveries.getModel().getValueAt(modelRow, 0).toString();
           
           logger.info("Editing delivery: " + deliveryId);
           
           showProgressBar("Loading delivery details...");
           
           // Load delivery data in background
           SwingWorker<Delivery_InfDTO, Void> loadWorker = new SwingWorker<Delivery_InfDTO, Void>() {
               @Override
               protected Delivery_InfDTO doInBackground() throws Exception {
                   return controller.getDeliveryById(deliveryId);
               }
               
               @Override
               protected void done() {
                   try {
                       Delivery_InfDTO deliveryToEdit = get();
                       
                       if (deliveryToEdit == null) {
                           showMessage("Cannot find the selected delivery in the database", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
                       
                       // Open edit form
                       ExportationDelivery deliveryForm = new ExportationDelivery(deliveryToEdit);
                       deliveryForm.setVisible(true);
                       
                       // Refresh when done
                       deliveryForm.addWindowListener(new java.awt.event.WindowAdapter() {
                           @Override
                           public void windowClosed(java.awt.event.WindowEvent e) {
                               logger.info("Edit delivery form closed, refreshing data");
                               loadDeliveriesAsync();
                           }
                       });
                       
                   } catch (Exception ex) {
                       logger.log(Level.SEVERE, "Error loading delivery for edit", ex);
                       showMessage("Error loading delivery: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                   } finally {
                       hideProgressBar();
                   }
               }
           };
           
           loadWorker.execute();
           
       } catch (Exception ex) {
           hideProgressBar();
           logger.log(Level.SEVERE, "Error in edit delivery", ex);
           showMessage("Error editing delivery: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
       }
   }
   
   /**
    * Enhanced delete delivery with better confirmation and feedback
    */
   private void deleteSelectedDelivery() {
       int selectedRow = tblDeliveries.getSelectedRow();
       
       if (selectedRow == -1) {
           showMessage("Please select a delivery to delete", JOptionPane.INFORMATION_MESSAGE);
           return;
       }
       
       if (DatabaseManager.isOfflineMode()) {
           showMessage(
               "Cannot delete deliveries while in offline mode.\n" +
               "Please reconnect to the database first.",
               JOptionPane.ERROR_MESSAGE
           );
           return;
       }
       
       try {
           // Convert view row to model row
           int modelRow = tblDeliveries.convertRowIndexToModel(selectedRow);
           String deliveryId = tblDeliveries.getModel().getValueAt(modelRow, 0).toString();
           String exportId = tblDeliveries.getModel().getValueAt(modelRow, 1).toString();
           String trackingNumber = tblDeliveries.getModel().getValueAt(modelRow, 2).toString();
           
           // Enhanced confirmation dialog
           int confirm = JOptionPane.showConfirmDialog(
               this,
               "⚠️ Are you sure you want to delete this delivery?\n\n" +
               "📦 Delivery ID: " + deliveryId + "\n" +
               "📋 Export ID: " + exportId + "\n" +
               "🔍 Tracking #: " + trackingNumber + "\n\n" +
               "⚠️ This action cannot be undone!",
               "Confirm Deletion",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE
           );
           
           if (confirm != JOptionPane.YES_OPTION) {
               return;
           }
           
           showProgressBar("Deleting delivery...");
           
           // Delete in background
           SwingWorker<Boolean, Void> deleteWorker = new SwingWorker<Boolean, Void>() {
               @Override
               protected Boolean doInBackground() throws Exception {
                   return controller.deleteDelivery(deliveryId);
               }
               
               @Override
               protected void done() {
                   try {
                       boolean success = get();
                       
                       if (success) {
                           JOptionPane.showMessageDialog(
                               DeliveryManagementForm.this,
                               "✓ Delivery deleted successfully!\n\n" +
                               "Deleted: " + deliveryId,
                               "Delete Successful",
                               JOptionPane.INFORMATION_MESSAGE
                           );
                           logger.info("Successfully deleted delivery: " + deliveryId);
                           loadDeliveriesAsync(); // Refresh table
                       } else {
                           showMessage(
                               "Failed to delete delivery.\n" +
                               "Please check the logs for details.",
                               JOptionPane.ERROR_MESSAGE
                           );
                       }
                       
                   } catch (Exception ex) {
                       logger.log(Level.SEVERE, "Error in delete operation", ex);
                       showMessage("Delete error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                   } finally {
                       hideProgressBar();
                   }
               }
           };
           
           deleteWorker.execute();
           
       } catch (Exception ex) {
           hideProgressBar();
           logger.log(Level.SEVERE, "Error in delete delivery", ex);
           showMessage("Error deleting delivery: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
       }
   }
   
   /**
    * Enhanced cleanup when form is closed
    */
   @Override
   public void dispose() {
       try {
           // Stop search timer
           if (searchTimer != null && searchTimer.isRunning()) {
               searchTimer.stop();
           }
           
           // Stop connection checker
           stopConnectionChecker();
           
           logger.info("DeliveryManagementForm disposed successfully");
       } catch (Exception e) {
           logger.log(Level.WARNING, "Error during form disposal", e);
       } finally {
           super.dispose();
       }
   }
   
   /**
    * Enhanced connection checker shutdown
    */
   private void stopConnectionChecker() {
       if (connectionChecker != null && !connectionChecker.isShutdown()) {
           connectionChecker.shutdown();
           try {
               if (!connectionChecker.awaitTermination(5, TimeUnit.SECONDS)) {
                   connectionChecker.shutdownNow();
                   logger.info("Connection checker shut down forcefully");
               }
           } catch (InterruptedException e) {
               connectionChecker.shutdownNow();
               Thread.currentThread().interrupt();
               logger.warning("Connection checker shutdown interrupted");
           }
       }
   }

   /**
    * Standalone main method for testing this form independently
    * NOTE: This is for development/testing only. The application should be started
    * from exportation_panelera.Exportation_Panelera.main() which shows SignIn first.
    */
   /*
   public static void main(String args[]) {
       // Set system look and feel
       try {
           javax.swing.UIManager.setLookAndFeel(
    javax.swing.UIManager.getSystemLookAndFeelClassName());
       } catch (Exception ex) {
           Logger.getLogger(DeliveryManagementForm.class.getName())
               .log(Level.WARNING, "Could not set system look and feel", ex);
       }

       // Create and display the enhanced form
       java.awt.EventQueue.invokeLater(() -> {
           try {
               DeliveryManagementForm form = new DeliveryManagementForm();
               form.setVisible(true);

               // Log successful startup
               Logger.getLogger(DeliveryManagementForm.class.getName())
                   .info("Enhanced Delivery Management Form started successfully");

           } catch (Exception ex) {
               Logger.getLogger(DeliveryManagementForm.class.getName())
                   .log(Level.SEVERE, "Error starting application", ex);

               JOptionPane.showMessageDialog(
                   null,
                   "Error starting application: " + ex.getMessage(),
                   "Startup Error",
                   JOptionPane.ERROR_MESSAGE
               );
           }
       });
   }
   */
}