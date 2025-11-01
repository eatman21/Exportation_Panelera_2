package exportation_panelera.View;

import exportation_panelera.controller.DeliveryController;
import exportation_panelera.Model.Delivery_InfDTO;
import exportation_panelera.Model.Exportation_InfDTO;
import exportation_panelera.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.sql.*;

// Additional import for SwingUtilities
import javax.swing.SwingUtilities;

/**
 * Unified interface for managing exportation and delivery information
 * with integrated currency conversion and automatic tracking number generation
 */
public class ExportationDelivery extends JFrame {
    private static final Logger logger = Logger.getLogger(ExportationDelivery.class.getName());
    
    // UI Constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color ACCENT_COLOR = new Color(243, 156, 18);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    // Exchange rates (currency code -> rate to USD)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();
    
    // Country abbreviations for tracking numbers
    private static final Map<String, String> COUNTRY_ABBREVIATIONS = new HashMap<>();
    
    // Initialize with common exchange rates
    static {
        // Base currency is USD (1.0)
        EXCHANGE_RATES.put("USD", BigDecimal.ONE);
        EXCHANGE_RATES.put("EUR", new BigDecimal("0.91")); // 1 USD = 0.91 EUR
        EXCHANGE_RATES.put("GBP", new BigDecimal("0.78")); // 1 USD = 0.78 GBP
        EXCHANGE_RATES.put("CAD", new BigDecimal("1.35")); // 1 USD = 1.35 CAD
        EXCHANGE_RATES.put("COP", new BigDecimal("3900")); // 1 USD = 3900 COP (Colombian Peso)
        EXCHANGE_RATES.put("MXN", new BigDecimal("17.50")); // 1 USD = 17.50 MXN
        EXCHANGE_RATES.put("JPY", new BigDecimal("107.8")); // 1 USD = 107.8 JPY
        
        // Initialize country abbreviations for tracking numbers
        COUNTRY_ABBREVIATIONS.put("United States", "US");
        COUNTRY_ABBREVIATIONS.put("Canada", "CA");
        COUNTRY_ABBREVIATIONS.put("Mexico", "MX");
        COUNTRY_ABBREVIATIONS.put("Brazil", "BR");
        COUNTRY_ABBREVIATIONS.put("Colombia", "CO");
        COUNTRY_ABBREVIATIONS.put("United Kingdom", "UK");
        COUNTRY_ABBREVIATIONS.put("France", "FR");
        COUNTRY_ABBREVIATIONS.put("Germany", "DE");
        COUNTRY_ABBREVIATIONS.put("Spain", "ES");
        COUNTRY_ABBREVIATIONS.put("Italy", "IT");
        COUNTRY_ABBREVIATIONS.put("China", "CN");
        COUNTRY_ABBREVIATIONS.put("Japan", "JP");
        COUNTRY_ABBREVIATIONS.put("South Korea", "KR");
        COUNTRY_ABBREVIATIONS.put("Australia", "AU");
        COUNTRY_ABBREVIATIONS.put("Netherlands", "NL");
        COUNTRY_ABBREVIATIONS.put("Belgium", "BE");
        COUNTRY_ABBREVIATIONS.put("Switzerland", "CH");
        COUNTRY_ABBREVIATIONS.put("Sweden", "SE");
        COUNTRY_ABBREVIATIONS.put("Norway", "NO");
        COUNTRY_ABBREVIATIONS.put("Denmark", "DK");
        COUNTRY_ABBREVIATIONS.put("Finland", "FI");
        COUNTRY_ABBREVIATIONS.put("Poland", "PL");
        COUNTRY_ABBREVIATIONS.put("Czech Republic", "CZ");
        COUNTRY_ABBREVIATIONS.put("Hungary", "HU");
        COUNTRY_ABBREVIATIONS.put("Greece", "GR");
        COUNTRY_ABBREVIATIONS.put("Portugal", "PT");
        COUNTRY_ABBREVIATIONS.put("Turkey", "TR");
        COUNTRY_ABBREVIATIONS.put("Russia", "RU");
        COUNTRY_ABBREVIATIONS.put("India", "IN");
        COUNTRY_ABBREVIATIONS.put("Thailand", "TH");
        COUNTRY_ABBREVIATIONS.put("Singapore", "SG");
        COUNTRY_ABBREVIATIONS.put("Malaysia", "MY");
        COUNTRY_ABBREVIATIONS.put("Indonesia", "ID");
        COUNTRY_ABBREVIATIONS.put("Philippines", "PH");
        COUNTRY_ABBREVIATIONS.put("Vietnam", "VN");
        COUNTRY_ABBREVIATIONS.put("South Africa", "ZA");
        COUNTRY_ABBREVIATIONS.put("Egypt", "EG");
        COUNTRY_ABBREVIATIONS.put("Nigeria", "NG");
        COUNTRY_ABBREVIATIONS.put("Argentina", "AR");
        COUNTRY_ABBREVIATIONS.put("Chile", "CL");
        COUNTRY_ABBREVIATIONS.put("Peru", "PE");
        COUNTRY_ABBREVIATIONS.put("Ecuador", "EC");
        COUNTRY_ABBREVIATIONS.put("Uruguay", "UY");
        COUNTRY_ABBREVIATIONS.put("Venezuela", "VE");
        COUNTRY_ABBREVIATIONS.put("Costa Rica", "CR");
        COUNTRY_ABBREVIATIONS.put("Panama", "PA");
        COUNTRY_ABBREVIATIONS.put("Guatemala", "GT");
        COUNTRY_ABBREVIATIONS.put("Honduras", "HN");
        COUNTRY_ABBREVIATIONS.put("El Salvador", "SV");
        COUNTRY_ABBREVIATIONS.put("Nicaragua", "NI");
        COUNTRY_ABBREVIATIONS.put("Other", "XX"); // Default for "Other"
    }
    
    // Controllers
    private DeliveryController exportationController;
    private DeliveryController deliveryController;
    
    // Main components
    private JTabbedPane tabbedPane;
    private JPanel exportationPanel;
    private JPanel deliveryPanel;
    private JPanel shippingCostPanel;
    
    // Exportation components
    private JTextField txtExportationID;
    private JComboBox<String> cmbProductType;
    private JTextField txtQuantity;
    private JComboBox<String> cmbDestination;
    private com.toedter.calendar.JDateChooser dateExport;
    private JTextField txtUnitPrice;
    private JComboBox<String> cmbCurrency;
    private JTextField txtTotalPrice;
    
    // Currency conversion components (in Exportation panel)
    private JPanel pnlCurrencyConversion;
    private JLabel lblConvertedPrice;
    private JComboBox<String> cmbTargetCurrency;
    private JTextField txtConvertedPrice;
    
    // Delivery components
    private JTextField txtDeliveryID;
    private JTextField txtCarrierName;
    private JTextField txtTrackingNumber;
    private JTextArea txtAddress;
    private JTextField txtContactPerson;
    private JTextField txtContactPhone;
    private com.toedter.calendar.JDateChooser dateDelivery;
    private JComboBox<String> cmbStatus;
    private JTextArea txtNotes;
    
    // NEW: Auto-generate tracking number button
    private JButton btnGenerateTracking;
    
    // Shipping cost components
    private JComboBox<String> cmbShippingMethod;
    private JTextField txtWeight;
    private JTextField txtDistance;
    private JTextField txtBaseCost;
    private JTextField txtAdditionalCosts;
    private JTextField txtTotalShippingCost;
    
    // Shipping cost currency components
    private JComboBox<String> cmbShippingCurrency;
    private JLabel lblShippingConvertedCost;
    private JTextField txtShippingConvertedCost;
    
    // Action buttons
    private JButton btnSaveAll;
    private JButton btnClear;
    private JButton btnCalculateShipping;
    
    // Shared data
    private String currentExportationId;
    private boolean isEditMode = false;
    
    /**
     * Default constructor - Create a new form for adding a new exportation and delivery
     */
    public ExportationDelivery() {
        exportationController = new DeliveryController();
        deliveryController = new DeliveryController();
        
        initComponents();
        customizeUI();
        setupEventHandlers();
        
        setTitle("Exportation & Delivery Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Generate a new exportation ID
        generateExportationId();
    }
    
    /**
     * Constructor for editing an existing delivery
     * 
     * @param deliveryToEdit The delivery to edit
     */
    public ExportationDelivery(Delivery_InfDTO deliveryToEdit) {
        this(); // Call default constructor to initialize components
        
        if (deliveryToEdit != null) {
            isEditMode = true;
            
            // Load delivery data
            populateDeliveryFields(deliveryToEdit);
            
            // If there's an associated exportation, load that too
            if (deliveryToEdit.getExportId() != null) {
                currentExportationId = deliveryToEdit.getExportId();
                
                Exportation_InfDTO exportation = exportationController.getExportationById(currentExportationId);
                if (exportation != null) {
                    populateExportationFields(exportation);
                }
            }
            
            setTitle("Edit Exportation & Delivery");
        }
    }
    
    /**
     * Create a form for editing an existing exportation/delivery
     * @param exportationId The ID of the exportation to edit
     */
    public ExportationDelivery(String exportationId) {
        this(); // Call default constructor to initialize components
        
        isEditMode = true;
        currentExportationId = exportationId;
        
        // Load exportation data
        Exportation_InfDTO exportation = exportationController.getExportationById(exportationId);
        if (exportation != null) {
            populateExportationFields(exportation);
        }
        
        // Load delivery data if available
        Delivery_InfDTO delivery = deliveryController.getDeliveryByExportId(exportationId);
        if (delivery != null) {
            populateDeliveryFields(delivery);
        }
        
        setTitle("Edit Exportation & Delivery");
    }
    
    /**
     * NEW: Generate automatic tracking number based on selected destination
     * Format: TRK + 9 digits + Country Code (e.g., TRK123456789US, TRK345678901JP)
     */
    private void generateTrackingNumber() {
        String selectedCountry = (String) cmbDestination.getSelectedItem();
        
        if (selectedCountry == null || selectedCountry.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a destination country first.",
                    "No Destination Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get country abbreviation
        String countryCode = COUNTRY_ABBREVIATIONS.get(selectedCountry);
        if (countryCode == null) {
            countryCode = "XX"; // Default for unknown countries
        }
        
        // Generate tracking number format: TRK + 9 random digits + Country Code
        // Example: TRK123456789US, TRK345678901JP, etc.
        
        // Generate 9 random digits
        Random random = new Random();
        StringBuilder nineDigits = new StringBuilder();
        
        // First digit should not be 0 to ensure we always have 9 digits
        nineDigits.append(random.nextInt(9) + 1); // 1-9
        
        // Remaining 8 digits can be 0-9
        for (int i = 0; i < 8; i++) {
            nineDigits.append(random.nextInt(10)); // 0-9
        }
        
        // Create the tracking number: TRK + 9digits + CountryCode
        String trackingNumber = "TRK" + nineDigits.toString() + countryCode;
        
        // Set the tracking number in the field
        txtTrackingNumber.setText(trackingNumber);
        
        logger.info("Generated tracking number: " + trackingNumber + " for country: " + selectedCountry);
        
        // Show confirmation message
        JOptionPane.showMessageDialog(this,
                "Tracking number generated successfully!\n\n" +
                "Country: " + selectedCountry + " (" + countryCode + ")\n" +
                "Tracking Number: " + trackingNumber +
                "\n\nFormat: TRK + 9 digits + Country Code",
                "Tracking Number Generated",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * NEW: Auto-generate tracking number when destination changes
     */
    private void onDestinationChanged() {
        String selectedCountry = (String) cmbDestination.getSelectedItem();
        
        // Only auto-generate if tracking number is empty or user confirms
        if (txtTrackingNumber.getText().trim().isEmpty()) {
            generateTrackingNumber();
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "A tracking number already exists. Do you want to generate a new one for " + selectedCountry + "?",
                    "Generate New Tracking Number?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                generateTrackingNumber();
            }
        }
    }
    
    /**
     * Convert currency using our exchange rates
     * 
     * @param amount The amount to convert
     * @param fromCurrency The source currency code
     * @param toCurrency The target currency code
     * @return The converted amount
     */
    private BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        // If same currency, return the amount
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        // Validate currencies
        if (!EXCHANGE_RATES.containsKey(fromCurrency) || !EXCHANGE_RATES.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        
        // Convert to USD first (if not already in USD)
        BigDecimal amountInUSD;
        if (fromCurrency.equals("USD")) {
            amountInUSD = amount;
        } else {
            BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
            amountInUSD = amount.divide(fromRate, 10, RoundingMode.HALF_UP);
        }
        
        // Convert from USD to target currency
        if (toCurrency.equals("USD")) {
            return amountInUSD;
        } else {
            BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
            return amountInUSD.multiply(toRate);
        }
    }
    
    /**
     * Get the exchange rate between two currencies
     * 
     * @param fromCurrency The source currency code
     * @param toCurrency The target currency code
     * @return The exchange rate
     */
    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        // Validate currencies
        if (!EXCHANGE_RATES.containsKey(fromCurrency) || !EXCHANGE_RATES.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        
        // If same currency, rate is 1:1
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
        
        return toRate.divide(fromRate, 10, RoundingMode.HALF_UP);
    }
    
    /**
     * Initialize all components
     */
    private void initComponents() {
        // Set up the main layout
        tabbedPane = new JTabbedPane();
        
        // Create the panels
        exportationPanel = createExportationPanel();
        deliveryPanel = createDeliveryPanel();
        shippingCostPanel = createShippingCostPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Exportation Details", null, exportationPanel, "Enter exportation details");
        tabbedPane.addTab("Delivery Details", null, deliveryPanel, "Enter delivery details");
        tabbedPane.addTab("Shipping Cost", null, shippingCostPanel, "Calculate shipping costs");
        
        // Create action buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        btnSaveAll = new JButton("💾 Save All");
        btnClear = new JButton("🗑️ Clear All");

        buttonPanel.add(btnSaveAll);
        buttonPanel.add(btnClear);

        // Add components to frame
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create the exportation panel
     */
    private JPanel createExportationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create components
        JLabel lblExportationID = new JLabel("Exportation ID:");
        JLabel lblProductType = new JLabel("Product Type:");
        JLabel lblQuantity = new JLabel("Quantity:");
        JLabel lblDestination = new JLabel("Destination:");
        JLabel lblExportDate = new JLabel("Export Date:");
        JLabel lblUnitPrice = new JLabel("Unit Price:");
        JLabel lblCurrency = new JLabel("Currency:");
        JLabel lblTotalPrice = new JLabel("Total Price:");
        
        txtExportationID = new JTextField(20);
        cmbProductType = new JComboBox<>(new String[] {
            "Panela", "Syrup", "Sugar", "Wine", "Ethanol"
        });
        txtQuantity = new JTextField(10);
        cmbDestination = new JComboBox<>(new String[] {
            "United States", "Canada", "Mexico", "Brazil", "Colombia", 
            "United Kingdom", "France", "Germany", "Spain", "Italy", 
            "China", "Japan", "South Korea", "Australia", "Netherlands",
            "Belgium", "Switzerland", "Sweden", "Norway", "Denmark",
            "Finland", "Poland", "Czech Republic", "Hungary", "Greece",
            "Portugal", "Turkey", "Russia", "India", "Thailand",
            "Singapore", "Malaysia", "Indonesia", "Philippines", "Vietnam",
            "South Africa", "Egypt", "Nigeria", "Argentina", "Chile",
            "Peru", "Ecuador", "Uruguay", "Venezuela", "Costa Rica",
            "Panama", "Guatemala", "Honduras", "El Salvador", "Nicaragua",
            "Other"
        });
        dateExport = new com.toedter.calendar.JDateChooser();
        dateExport.setDateFormatString("yyyy-MM-dd");
        
        txtUnitPrice = new JTextField(10);
        cmbCurrency = new JComboBox<>(EXCHANGE_RATES.keySet().toArray(new String[0]));
        txtTotalPrice = new JTextField(15);
        txtTotalPrice.setEditable(false);
        
        // Create currency conversion panel
        pnlCurrencyConversion = new JPanel(new GridBagLayout());
        pnlCurrencyConversion.setBackground(BACKGROUND_COLOR);
        pnlCurrencyConversion.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR),
                "Currency Conversion",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                SECONDARY_COLOR));
        
        lblConvertedPrice = new JLabel("Converted Price:");
        cmbTargetCurrency = new JComboBox<>(EXCHANGE_RATES.keySet().toArray(new String[0]));
        cmbTargetCurrency.setSelectedItem("EUR"); // Default target currency
        txtConvertedPrice = new JTextField(15);
        txtConvertedPrice.setEditable(false);
        
        GridBagConstraints convGbc = new GridBagConstraints();
        convGbc.insets = new Insets(5, 5, 5, 5);
        convGbc.anchor = GridBagConstraints.WEST;
        
        convGbc.gridx = 0;
        convGbc.gridy = 0;
        pnlCurrencyConversion.add(new JLabel("Convert to:"), convGbc);
        
        convGbc.gridx = 1;
        convGbc.fill = GridBagConstraints.HORIZONTAL;
        convGbc.weightx = 1.0;
        pnlCurrencyConversion.add(cmbTargetCurrency, convGbc);
        
        convGbc.gridx = 0;
        convGbc.gridy = 1;
        convGbc.fill = GridBagConstraints.NONE;
        convGbc.weightx = 0;
        pnlCurrencyConversion.add(lblConvertedPrice, convGbc);
        
        convGbc.gridx = 1;
        convGbc.fill = GridBagConstraints.HORIZONTAL;
        convGbc.weightx = 1.0;
        pnlCurrencyConversion.add(txtConvertedPrice, convGbc);
        
        // Add components to panel - first column (labels)
        int row = 0;
        
        // Exportation ID
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(lblExportationID, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtExportationID, gbc);
        
        // Product Type
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblProductType, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(cmbProductType, gbc);
        
        // Quantity
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblQuantity, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtQuantity, gbc);
        
        // Destination
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblDestination, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(cmbDestination, gbc);
        
        // Export Date
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblExportDate, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(dateExport, gbc);
        
        // Unit Price
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblUnitPrice, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtUnitPrice, gbc);
        
        // Currency
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblCurrency, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(cmbCurrency, gbc);
        
        // Total Price
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblTotalPrice, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtTotalPrice, gbc);
        
        // Currency conversion panel
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pnlCurrencyConversion, gbc);
        
        // Add some space at the bottom
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    /**
     * Create the delivery panel - UPDATED with tracking number generation
     */
    private JPanel createDeliveryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create components
        JLabel lblDeliveryID = new JLabel("Delivery ID:");
        JLabel lblCarrierName = new JLabel("Carrier Name:");
        JLabel lblTrackingNumber = new JLabel("Tracking Number:");
        JLabel lblAddress = new JLabel("Delivery Address:");
        JLabel lblContactPerson = new JLabel("Contact Person:");
        JLabel lblContactPhone = new JLabel("Contact Phone:");
        JLabel lblDeliveryDate = new JLabel("Delivery Date:");
        JLabel lblStatus = new JLabel("Status:");
        JLabel lblNotes = new JLabel("Notes:");
        
        txtDeliveryID = new JTextField(20);
        txtCarrierName = new JTextField(20);
        txtTrackingNumber = new JTextField(20);
        txtAddress = new JTextArea(5, 20);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        txtContactPerson = new JTextField(20);
        txtContactPhone = new JTextField(20);
        
        dateDelivery = new com.toedter.calendar.JDateChooser();
        dateDelivery.setDateFormatString("yyyy-MM-dd");
        
        cmbStatus = new JComboBox<>(new String[] {
            "Pending", "In Transit", "Delivered", "Cancelled"
        });
        
        txtNotes = new JTextArea(3, 20);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        
        // NEW: Generate Tracking Number button
        btnGenerateTracking = new JButton("🔄 Generate Tracking Number");
        btnGenerateTracking.setToolTipText("Generate tracking number based on destination country");
        
        // Add components to panel
        int row = 0;
        
        // Delivery ID
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(lblDeliveryID, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtDeliveryID, gbc);
        
        // Carrier Name
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblCarrierName, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtCarrierName, gbc);
        
        // Tracking Number
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblTrackingNumber, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtTrackingNumber, gbc);
        
        // Generate Tracking Button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnGenerateTracking, gbc);
        
        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Address
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblAddress, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollAddress, gbc);
        
        // Contact Person
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        panel.add(lblContactPerson, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtContactPerson, gbc);
        
        // Contact Phone
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblContactPhone, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(txtContactPhone, gbc);
        
        // Delivery Date
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblDeliveryDate, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(dateDelivery, gbc);
        
        // Status
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblStatus, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(cmbStatus, gbc);
        
        // Notes
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(lblNotes, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(scrollNotes, gbc);
        
        return panel;
    }
    
    /**
     * Create the shipping cost panel
     */
    private JPanel createShippingCostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create components
        JLabel lblShippingMethod = new JLabel("Shipping Method:");
        JLabel lblWeight = new JLabel("Weight (kg):");
        JLabel lblDistance = new JLabel("Distance (km):");
        JLabel lblBaseCost = new JLabel("Base Cost:");
        JLabel lblAdditionalCosts = new JLabel("Additional Costs:");
        JLabel lblTotalShippingCost = new JLabel("Total Shipping Cost:");
        JLabel lblShippingCurrency = new JLabel("Currency:");
        
        cmbShippingMethod = new JComboBox<>(new String[] {
            "Air Freight", "Sea Freight", "Road Transport", "Rail Transport", "Express Courier"
        });
        txtWeight = new JTextField(10);
        txtDistance = new JTextField(10);
        txtBaseCost = new JTextField(10);
        txtBaseCost.setEditable(false);
        txtAdditionalCosts = new JTextField(10);
        txtTotalShippingCost = new JTextField(15);
        txtTotalShippingCost.setEditable(false);
        
        cmbShippingCurrency = new JComboBox<>(EXCHANGE_RATES.keySet().toArray(new String[0]));
        cmbShippingCurrency.setSelectedItem("USD"); // Default to USD
        
        lblShippingConvertedCost = new JLabel("Converted Cost:");
        txtShippingConvertedCost = new JTextField(15);
        txtShippingConvertedCost.setEditable(false);
        
        btnCalculateShipping = new JButton("Calculate Shipping Cost");
        
        // Shipping method section
        JPanel methodPanel = new JPanel(new GridBagLayout());
        methodPanel.setBackground(BACKGROUND_COLOR);
        methodPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Shipping Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                PRIMARY_COLOR));
        
        GridBagConstraints methodGbc = new GridBagConstraints();
        methodGbc.insets = new Insets(5, 5, 5, 5);
        methodGbc.anchor = GridBagConstraints.WEST;
        
        // Add components to method panel
        int methodRow = 0;
        
        // Shipping Method
        methodGbc.gridx = 0;
        methodGbc.gridy = methodRow;
        methodPanel.add(lblShippingMethod, methodGbc);
        
        methodGbc.gridx = 1;
        methodGbc.fill = GridBagConstraints.HORIZONTAL;
        methodGbc.weightx = 1.0;
        methodPanel.add(cmbShippingMethod, methodGbc);
        
        // Weight
        methodRow++;
        methodGbc.gridx = 0;
        methodGbc.gridy = methodRow;
        methodGbc.fill = GridBagConstraints.NONE;
        methodGbc.weightx = 0;
        methodPanel.add(lblWeight, methodGbc);
        
        methodGbc.gridx = 1;
        methodGbc.fill = GridBagConstraints.HORIZONTAL;
        methodGbc.weightx = 1.0;
        methodPanel.add(txtWeight, methodGbc);
        
        // Distance
        methodRow++;
        methodGbc.gridx = 0;
        methodGbc.gridy = methodRow;
        methodGbc.fill = GridBagConstraints.NONE;
        methodGbc.weightx = 0;
        methodPanel.add(lblDistance, methodGbc);
        
        methodGbc.gridx = 1;
        methodGbc.fill = GridBagConstraints.HORIZONTAL;
        methodGbc.weightx = 1.0;
        methodPanel.add(txtDistance, methodGbc);
        
        // Cost calculation section
        JPanel costPanel = new JPanel(new GridBagLayout());
        costPanel.setBackground(BACKGROUND_COLOR);
        costPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Cost Calculation",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                PRIMARY_COLOR));
        
        GridBagConstraints costGbc = new GridBagConstraints();
        costGbc.insets = new Insets(5, 5, 5, 5);
        costGbc.anchor = GridBagConstraints.WEST;
        
        // Add components to cost panel
        int costRow = 0;
        
        // Base Cost
        costGbc.gridx = 0;
        costGbc.gridy = costRow;
        costGbc.fill = GridBagConstraints.NONE;
        costGbc.weightx = 0;
        costPanel.add(lblBaseCost, costGbc);
        
        costGbc.gridx = 1;
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.weightx = 1.0;
        costPanel.add(txtBaseCost, costGbc);
        
        // Additional Costs
        costRow++;
        costGbc.gridx = 0;
        costGbc.gridy = costRow;
        costGbc.fill = GridBagConstraints.NONE;
        costGbc.weightx = 0;
        costPanel.add(lblAdditionalCosts, costGbc);
        
        costGbc.gridx = 1;
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.weightx = 1.0;
        costPanel.add(txtAdditionalCosts, costGbc);
        
        // Total Shipping Cost
        costRow++;
        costGbc.gridx = 0;
        costGbc.gridy = costRow;
        costGbc.fill = GridBagConstraints.NONE;
        costGbc.weightx = 0;
        costPanel.add(lblTotalShippingCost, costGbc);
        
        costGbc.gridx = 1;
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.weightx = 1.0;
        costPanel.add(txtTotalShippingCost, costGbc);
        
        // Currency
        costRow++;
        costGbc.gridx = 0;
        costGbc.gridy = costRow;
        costGbc.fill = GridBagConstraints.NONE;
        costGbc.weightx = 0;
        costPanel.add(lblShippingCurrency, costGbc);
        
        costGbc.gridx = 1;
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.weightx = 1.0;
        costPanel.add(cmbShippingCurrency, costGbc);
        
        // Converted Cost
        costRow++;
        costGbc.gridx = 0;
        costGbc.gridy = costRow;
        costGbc.fill = GridBagConstraints.NONE;
        costGbc.weightx = 0;
        costPanel.add(lblShippingConvertedCost, costGbc);
        
        costGbc.gridx = 1;
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.weightx = 1.0;
        costPanel.add(txtShippingConvertedCost, costGbc);
        
        // Calculate button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(btnCalculateShipping);
        
        // Add panels to main shipping panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(methodPanel, gbc);
        
        gbc.gridy = 1;
        panel.add(costPanel, gbc);
        
        gbc.gridy = 2;
        panel.add(buttonPanel, gbc);
        
        // Add empty space at the bottom
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    /**
     * Apply custom styling to all UI components
     */
    private void customizeUI() {
        // Set panel background colors
        exportationPanel.setBackground(BACKGROUND_COLOR);
        deliveryPanel.setBackground(BACKGROUND_COLOR);
        shippingCostPanel.setBackground(BACKGROUND_COLOR);
        
        // Style tabbed pane
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForegroundAt(0, PRIMARY_COLOR);
        
        // Style all text fields
        customizeTextField(txtExportationID);
        customizeTextField(txtQuantity);
        customizeTextField(txtUnitPrice);
        customizeTextField(txtTotalPrice);
        customizeTextField(txtConvertedPrice);
        customizeTextField(txtDeliveryID);
        customizeTextField(txtCarrierName);
        customizeTextField(txtTrackingNumber);
        customizeTextField(txtContactPerson);
        customizeTextField(txtContactPhone);
        customizeTextField(txtWeight);
        customizeTextField(txtDistance);
        customizeTextField(txtBaseCost);
        customizeTextField(txtAdditionalCosts);
        customizeTextField(txtTotalShippingCost);
        customizeTextField(txtShippingConvertedCost);
        
        // Style all combo boxes
        customizeComboBox(cmbProductType);
        customizeComboBox(cmbDestination);
        customizeComboBox(cmbCurrency);
        customizeComboBox(cmbTargetCurrency);
        customizeComboBox(cmbStatus);
        customizeComboBox(cmbShippingMethod);
        customizeComboBox(cmbShippingCurrency);
        
        // Style all text areas
        customizeTextArea(txtAddress);
        customizeTextArea(txtNotes);
        
        // Style date choosers
        customizeDateChooser(dateExport);
        customizeDateChooser(dateDelivery);
        
        // Style buttons
        customizeButton(btnSaveAll);
        customizeButton(btnClear);
        customizeButton(btnCalculateShipping);
        customizeButton(btnGenerateTracking);
        
        // Set default dates
        dateExport.setDate(new Date());
        dateDelivery.setDate(new Date());
        
        // Generate IDs
        generateExportationId();
        generateDeliveryId();
    }
    
    /**
     * Style a text field with custom look
     */
    private void customizeTextField(JTextField field) {
        if (field != null) {
            field.setFont(INPUT_FONT);
            field.setBorder(new LineBorder(SECONDARY_COLOR, 1));
            field.setBackground(Color.WHITE);
        }
    }
    
    /**
     * Style a text area with custom look
     */
    private void customizeTextArea(JTextArea area) {
        if (area != null) {
            area.setFont(INPUT_FONT);
            area.setBorder(new LineBorder(SECONDARY_COLOR, 1));
            area.setBackground(Color.WHITE);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
        }
    }
    
    /**
     * Style a combo box with custom look
     */
    private void customizeComboBox(JComboBox<?> comboBox) {
        if (comboBox != null) {
            comboBox.setFont(INPUT_FONT);
            comboBox.setBorder(new LineBorder(SECONDARY_COLOR, 1));
            comboBox.setBackground(Color.WHITE);
        }
    }
    
    /**
     * Style a date chooser with custom look
     */
    private void customizeDateChooser(com.toedter.calendar.JDateChooser dateChooser) {
        if (dateChooser != null) {
            dateChooser.setFont(INPUT_FONT);
            dateChooser.setBorder(new LineBorder(SECONDARY_COLOR, 1));
            dateChooser.setBackground(Color.WHITE);
            
            // Customize the text field inside the date chooser
            if (dateChooser.getDateEditor() != null && 
                dateChooser.getDateEditor().getUiComponent() instanceof JTextField) {
                JTextField textField = (JTextField) dateChooser.getDateEditor().getUiComponent();
                textField.setFont(INPUT_FONT);
                textField.setBorder(new EmptyBorder(2, 5, 2, 5));
            }
        }
    }
    
    /**
     * Style a button with custom look - user-friendly colors with black text
     */
    private void customizeButton(JButton button) {
        if (button != null) {
            button.setFont(LABEL_FONT);
            button.setForeground(Color.BLACK); // Black text for better visibility

            // Set user-friendly colors for different button types
            Color buttonColor;
            Color hoverColor;

            if (button == btnSaveAll) {
                // Light Green for Save button - positive action
                buttonColor = new Color(46, 204, 113); // Emerald Green
                hoverColor = new Color(39, 174, 96);   // Darker green
            } else if (button == btnClear) {
                // Light Red for Clear button - warning action
                buttonColor = new Color(231, 76, 60);  // Alizarin Red
                hoverColor = new Color(192, 57, 43);   // Darker red
            } else if (button == btnGenerateTracking) {
                // Light Blue for Generate button
                buttonColor = new Color(52, 152, 219); // Peter River Blue
                hoverColor = new Color(41, 128, 185);  // Darker blue
            } else if (button == btnCalculateShipping) {
                // Light Purple for Calculate button
                buttonColor = new Color(155, 89, 182); // Amethyst Purple
                hoverColor = new Color(142, 68, 173);  // Darker purple
            } else {
                buttonColor = PRIMARY_COLOR;
                hoverColor = SECONDARY_COLOR;
            }

            button.setBackground(buttonColor);
            button.setBorder(new EmptyBorder(5, 15, 5, 15));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Add hover effect
            final Color finalButtonColor = buttonColor;
            final Color finalHoverColor = hoverColor;

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(finalHoverColor);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(finalButtonColor);
                }
            });
        }
    }
    
    /**
     * Set up event handlers for all components - UPDATED with tracking number generation
     */
    private void setupEventHandlers() {
        // Exportation total price calculation when inputs change
        DocumentListener exportPriceListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateExportTotalPrice();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateExportTotalPrice();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateExportTotalPrice();
            }
        };
        
        txtUnitPrice.getDocument().addDocumentListener(exportPriceListener);
        txtQuantity.getDocument().addDocumentListener(exportPriceListener);
        
        // Currency change events
        cmbCurrency.addActionListener(e -> updateExportTotalPrice());
        cmbTargetCurrency.addActionListener(e -> updateExportTotalPrice());
        
        // NEW: Destination change event for automatic tracking number generation
        cmbDestination.addActionListener(e -> {
            // Only auto-generate tracking number if we're in the delivery tab and tracking field is empty
            if (tabbedPane.getSelectedIndex() == 1 && txtTrackingNumber.getText().trim().isEmpty()) {
                generateTrackingNumber();
            }
        });
        
        // NEW: Generate tracking number button event
        btnGenerateTracking.addActionListener(e -> generateTrackingNumber());
        
        // Shipping cost calculation events
        cmbShippingCurrency.addActionListener(e -> updateShippingConvertedCost());
        
        btnCalculateShipping.addActionListener(e -> calculateShippingCost());
        
        // Save button
        btnSaveAll.addActionListener(e -> saveAllData());
        
        // Clear button
        btnClear.addActionListener(e -> clearAllFields());
    }
    
    /**
     * Generate a unique exportation ID that fits in your database
     */
    private void generateExportationId() {
        // Generate a smaller, manageable ID instead of using full timestamp
        long timestamp = System.currentTimeMillis();
        
        // Use last 6 digits of timestamp
        int smallId = (int) (timestamp % 999999) + 1; // Ensures 1-999999
        String id = "EXP" + String.format("%06d", smallId);
        
        txtExportationID.setText(id);
        txtExportationID.setEditable(false);
        
        logger.info("Generated Exportation ID: " + id + " (numeric part: " + smallId + ")");
    }
    
    /**
     * Generate a unique delivery ID that fits in your database
     */
    private void generateDeliveryId() {
        // Generate a smaller, manageable ID instead of using full timestamp
        long timestamp = System.currentTimeMillis();
        
        // Use last 6 digits of timestamp + offset to avoid conflicts
        int smallId = (int) ((timestamp + 1000) % 999999) + 1; // Ensures 1-999999
        String id = "DEL" + String.format("%06d", smallId);
        
        txtDeliveryID.setText(id);
        txtDeliveryID.setEditable(false);
        
        logger.info("Generated Delivery ID: " + id + " (numeric part: " + smallId + ")");
    }
    
    /**
     * Update the total price in the exportation tab
     */
    private void updateExportTotalPrice() {
        try {
            if (!txtUnitPrice.getText().trim().isEmpty() && !txtQuantity.getText().trim().isEmpty()) {
                BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText().trim());
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                
                BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));
                String currencyCode = (String) cmbCurrency.getSelectedItem();
                
                DecimalFormat df = new DecimalFormat("#,##0.00");
                txtTotalPrice.setText(df.format(totalPrice) + " " + currencyCode);
                
                // Also update converted price
                updateConvertedPrice(totalPrice, currencyCode);
            }
        } catch (NumberFormatException e) {
            txtTotalPrice.setText("Invalid input");
        }
    }
    
    /**
     * Update the converted price
     */
    private void updateConvertedPrice(BigDecimal amount, String fromCurrency) {
        try {
            String toCurrency = (String) cmbTargetCurrency.getSelectedItem();
            
            // Convert currency
            BigDecimal convertedAmount = convertCurrency(amount, fromCurrency, toCurrency);
            
            DecimalFormat df = new DecimalFormat("#,##0.00");
            txtConvertedPrice.setText(df.format(convertedAmount) + " " + toCurrency);
        } catch (Exception e) {
            txtConvertedPrice.setText("Conversion error");
        }
    }
    
    /**
     * Calculate shipping cost based on inputs
     */
    private void calculateShippingCost() {
        try {
            // Validate inputs
            if (txtWeight.getText().trim().isEmpty() || txtDistance.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter weight and distance values",
                        "Missing Data",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double distance = Double.parseDouble(txtDistance.getText().trim());
            String method = (String) cmbShippingMethod.getSelectedItem();
            
            // Calculate base cost based on shipping method and weight
            double baseCost = 0;
            if ("Air Freight".equals(method)) {
                baseCost = 12.5 * weight + 0.75 * distance;
            } else if ("Sea Freight".equals(method)) {
                baseCost = 5.0 * weight + 0.2 * distance;
            } else if ("Road Transport".equals(method)) {
                baseCost = 2.5 * weight + 0.5 * distance;
            } else if ("Rail Transport".equals(method)) {
                baseCost = 3.0 * weight + 0.4 * distance;
            } else if ("Express Courier".equals(method)) {
                baseCost = 20.0 * weight + 1.0 * distance;
            }
            
            // Get additional costs
            double additionalCosts = 0;
            if (!txtAdditionalCosts.getText().trim().isEmpty()) {
                additionalCosts = Double.parseDouble(txtAdditionalCosts.getText().trim());
            }
            
            // Calculate total cost
            double totalCost = baseCost + additionalCosts;
            
            // Display results
            DecimalFormat df = new DecimalFormat("#,##0.00");
            txtBaseCost.setText(df.format(baseCost) + " USD");
            txtTotalShippingCost.setText(df.format(totalCost) + " USD");
            
            // Update converted cost
            updateShippingConvertedCost();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for weight, distance, and costs",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update the converted shipping cost
     */
    private void updateShippingConvertedCost() {
        try {
            if (!txtTotalShippingCost.getText().isEmpty() && !txtTotalShippingCost.getText().equals("Invalid input")) {
                // Extract the numeric part from the shipping cost
                String totalCostText = txtTotalShippingCost.getText();
                String amountStr = totalCostText.replace(",", "").replace(" USD", "");
                
                BigDecimal totalCost = new BigDecimal(amountStr);
                String toCurrency = (String) cmbShippingCurrency.getSelectedItem();
                
                // Convert from USD to selected currency
                BigDecimal convertedCost = convertCurrency(totalCost, "USD", toCurrency);
                
                DecimalFormat df = new DecimalFormat("#,##0.00");
                txtShippingConvertedCost.setText(df.format(convertedCost) + " " + toCurrency);
            }
        } catch (Exception e) {
            txtShippingConvertedCost.setText("Conversion error");
            logger.log(Level.WARNING, "Error updating shipping converted cost", e);
        }
    }
    
    /**
     * Enhanced save method that handles large IDs properly
     */
    private void saveAllData() {
        try {
            // Validate exportation data
            if (!validateExportationData()) {
                tabbedPane.setSelectedIndex(0); // Switch to exportation tab
                return;
            }
            
            // Validate delivery data
            if (!validateDeliveryData()) {
                tabbedPane.setSelectedIndex(1); // Switch to delivery tab
                return;
            }
            
            // Create exportation DTO
            Exportation_InfDTO exportation = new Exportation_InfDTO();
            
            // Extract just the numeric part for exportation ID
            String exportIdText = txtExportationID.getText().trim();
            String exportIdNumeric = exportIdText.replace("EXP", "");
            exportation.setExportationId(exportIdNumeric); // Store just the number
            
            exportation.setProductType((String) cmbProductType.getSelectedItem());
            exportation.setAmount(Double.parseDouble(txtQuantity.getText().trim()));
            exportation.setDestination((String) cmbDestination.getSelectedItem());
            exportation.setExportationDate(dateExport.getDate());
            
            if (!txtUnitPrice.getText().trim().isEmpty()) {
                BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText().trim());
                exportation.setUnitPrice(unitPrice);
            }
            
            exportation.setCurrency((String) cmbCurrency.getSelectedItem());
            exportation.setHasDelivery(true);
            
            // Create delivery DTO
            Delivery_InfDTO delivery = new Delivery_InfDTO();
            delivery.setDeliveryId(txtDeliveryID.getText().trim());
            
            // Use the numeric part of the export ID for the relationship
            delivery.setExportId(exportIdText); // Keep full EXP format for display
            delivery.setExportationId(exportIdNumeric); // Store just the number for database
            
            delivery.setCarrierName(txtCarrierName.getText().trim());
            delivery.setTrackingNumber(txtTrackingNumber.getText().trim());
            delivery.setDeliveryAddress(txtAddress.getText().trim());
            delivery.setContactPerson(txtContactPerson.getText().trim());
            delivery.setContactPhone(txtContactPhone.getText().trim());
            
            // Set both date fields for compatibility
            if (dateDelivery.getDate() != null) {
                delivery.setDeliveryDate(dateDelivery.getDate());
                delivery.setDate(dateDelivery.getDate());
            }
            
            // Set both status fields for compatibility
            String status = (String) cmbStatus.getSelectedItem();
            delivery.setDeliveryStatus(status);
            delivery.setStatus(status);
            
            delivery.setNotes(txtNotes.getText().trim());
            
            // Save shipping cost information if calculated
            if (!txtTotalShippingCost.getText().trim().isEmpty() && 
                !txtTotalShippingCost.getText().equals("Invalid input")) {
                
                // Extract shipping cost value
                String totalCostText = txtTotalShippingCost.getText();
                String amountStr = totalCostText.replace(",", "").replace(" USD", "");
                
                try {
                    double shippingCost = Double.parseDouble(amountStr);
                    delivery.setShippingCost(shippingCost);
                    delivery.setShippingMethod((String) cmbShippingMethod.getSelectedItem());
                    delivery.setShippingCurrency("USD"); // Default currency
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Error parsing shipping cost", e);
                }
            }
            
            logger.info("Attempting to save:");
            logger.info("Exportation ID: " + exportation.getExportationId());
            logger.info("Delivery Export ID: " + delivery.getExportationId());
            logger.info("Delivery Tracking: " + delivery.getTrackingNumber());
            
            // Save the data using controllers
            boolean exportSaved = exportationController.createExportation(exportation);
            boolean deliverySaved = deliveryController.createDelivery(delivery);
            
            if (exportSaved && deliverySaved) {
                JOptionPane.showMessageDialog(this,
                        "Exportation and delivery information saved successfully!\n" +
                        "Exportation ID: " + exportIdText + "\n" +
                        "Delivery ID: " + delivery.getDeliveryId() + "\n" +
                        "Tracking Number: " + delivery.getTrackingNumber(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                
                if (!isEditMode) {
                    // Clear the form for new entries
                    clearAllFields();
                    generateExportationId();
                    generateDeliveryId();
                } else {
                    // Close the form if in edit mode
                    dispose();
                }
            } else {
                String errorMsg = "Error saving data:\n";
                if (!exportSaved) errorMsg += "- Failed to save exportation\n";
                if (!deliverySaved) errorMsg += "- Failed to save delivery\n";
                errorMsg += "Please check the console for detailed error messages.";
                
                JOptionPane.showMessageDialog(this,
                        errorMsg,
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving data", e);
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + e.getMessage() + 
                    "\nPlease check the console for detailed error information.",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Validate the exportation data
     */
    private boolean validateExportationData() {
        StringBuilder errors = new StringBuilder();
        
        // Check required fields
        if (txtExportationID.getText().trim().isEmpty()) {
            errors.append("Exportation ID is required\n");
        }
        
        if (txtQuantity.getText().trim().isEmpty()) {
            errors.append("Quantity is required\n");
        } else {
            try {
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                if (quantity <= 0) {
                    errors.append("Quantity must be greater than zero\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Quantity must be a valid number\n");
            }
        }
        
        if (dateExport.getDate() == null) {
            errors.append("Export Date is required\n");
        }
        
        if (txtUnitPrice.getText().trim().isEmpty()) {
            errors.append("Unit Price is required\n");
        } else {
            try {
                BigDecimal price = new BigDecimal(txtUnitPrice.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("Unit Price must be greater than zero\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Unit Price must be a valid number\n");
            }
        }
        
        // Show errors if any
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(
                this,
                errors.toString(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    /**
     * Enhanced validation for delivery data with more flexible requirements
     */
    private boolean validateDeliveryData() {
        StringBuilder errors = new StringBuilder();
        
        // Check required fields - only validate what's actually required for your database
        if (txtDeliveryID.getText().trim().isEmpty()) {
            errors.append("Delivery ID is required\n");
        }
        
        if (txtTrackingNumber.getText().trim().isEmpty()) {
            errors.append("Tracking Number is required\n");
        }
        
        // Optional fields with warnings (not errors)
        StringBuilder warnings = new StringBuilder();
        
        if (txtCarrierName.getText().trim().isEmpty()) {
            warnings.append("Carrier Name is not specified\n");
        }
        
        if (txtAddress.getText().trim().isEmpty()) {
            warnings.append("Delivery Address is not specified\n");
        }
        
        if (txtContactPerson.getText().trim().isEmpty()) {
            warnings.append("Contact Person is not specified\n");
        }
        
        if (dateDelivery.getDate() == null) {
            warnings.append("Delivery Date is not specified (will use current date)\n");
        }
        
        // Show errors if any (these will prevent saving)
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(
                this,
                "The following errors must be fixed before saving:\n\n" + errors.toString(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        // Show warnings if any (these won't prevent saving)
        if (warnings.length() > 0) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "The following fields are not specified:\n\n" + warnings.toString() + 
                "\nDo you want to continue saving with default values?",
                "Missing Information",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            return choice == JOptionPane.YES_OPTION;
        }
        
        return true;
    }
    
    /**
     * Clear all fields in the form
     */
    private void clearAllFields() {
        // Clear exportation fields
        txtExportationID.setText("");
        cmbProductType.setSelectedIndex(0);
        txtQuantity.setText("");
        cmbDestination.setSelectedIndex(0);
        dateExport.setDate(new Date());
        txtUnitPrice.setText("");
        cmbCurrency.setSelectedIndex(0);
        txtTotalPrice.setText("");
        txtConvertedPrice.setText("");
        cmbTargetCurrency.setSelectedIndex(0);
        
        // Clear delivery fields
        txtDeliveryID.setText("");
        txtCarrierName.setText("");
        txtTrackingNumber.setText("");
        txtAddress.setText("");
        txtContactPerson.setText("");
        txtContactPhone.setText("");
        dateDelivery.setDate(new Date());
        cmbStatus.setSelectedIndex(0);
        txtNotes.setText("");
        
        // Clear shipping cost fields
        txtWeight.setText("");
        txtDistance.setText("");
        txtBaseCost.setText("");
        txtAdditionalCosts.setText("");
        txtTotalShippingCost.setText("");
        txtShippingConvertedCost.setText("");
        cmbShippingCurrency.setSelectedIndex(0);
        
        // Generate new IDs
        generateExportationId();
        generateDeliveryId();
    }
    
    /**
     * Populate exportation fields from a DTO
     */
    private void populateExportationFields(Exportation_InfDTO dto) {
        if (dto == null) return;
        
        txtExportationID.setText(dto.getExportationId());
        
        if (dto.getProductType() != null) {
            cmbProductType.setSelectedItem(dto.getProductType());
        }
        
        txtQuantity.setText(String.valueOf(dto.getAmount()));
        
        if (dto.getDestination() != null) {
            cmbDestination.setSelectedItem(dto.getDestination());
        }
        
        if (dto.getExportationDate() != null) {
            dateExport.setDate(dto.getExportationDate());
        }
        
        if (dto.getUnitPrice() != null) {
            txtUnitPrice.setText(dto.getUnitPrice().toString());
        }
        
        if (dto.getCurrency() != null) {
            cmbCurrency.setSelectedItem(dto.getCurrency());
        }
        
        // Update total price
        updateExportTotalPrice();
    }
    
    /**
     * Populate delivery fields from a DTO
     */
    private void populateDeliveryFields(Delivery_InfDTO dto) {
        if (dto == null) return;
        
        txtDeliveryID.setText(dto.getDeliveryId());
        txtCarrierName.setText(dto.getCarrierName());
        txtTrackingNumber.setText(dto.getTrackingNumber());
        txtAddress.setText(dto.getDeliveryAddress());
        txtContactPerson.setText(dto.getContactPerson());
        txtContactPhone.setText(dto.getContactPhone());
        
        if (dto.getDeliveryDate() != null) {
            dateDelivery.setDate(dto.getDeliveryDate());
        } else if (dto.getDate() != null) {
            dateDelivery.setDate(dto.getDate());
        }
        
        if (dto.getDeliveryStatus() != null) {
            cmbStatus.setSelectedItem(dto.getDeliveryStatus());
        } else if (dto.getStatus() != null) {
            cmbStatus.setSelectedItem(dto.getStatus());
        }
        
        if (dto.getNotes() != null) {
            txtNotes.setText(dto.getNotes());
        }
        
        // If shipping information is available, populate shipping tab
        if (dto.getShippingMethod() != null) {
            cmbShippingMethod.setSelectedItem(dto.getShippingMethod());
        }
        
        if (dto.getShippingCost() > 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            txtTotalShippingCost.setText(df.format(dto.getShippingCost()) + " USD");
            updateShippingConvertedCost();
        }
    }
    
    /**
     * Test method to validate tracking number generation
     */
    public void testTrackingGeneration() {
        logger.info("Testing tracking number generation...");
        logger.info("Format: TRK + 9 digits + Country Code");
        
        String[] testCountries = {"United States", "Canada", "Mexico", "United Kingdom", "Germany", "Japan", "Other"};
        
        for (String country : testCountries) {
            cmbDestination.setSelectedItem(country);
            generateTrackingNumber();
            String trackingNumber = txtTrackingNumber.getText();
            logger.info("Country: " + country + " -> Tracking: " + trackingNumber);
            
            // Validate format
            String countryCode = COUNTRY_ABBREVIATIONS.get(country);
            if (countryCode == null) countryCode = "XX";
            
            boolean validFormat = trackingNumber.startsWith("TRK") && 
                                trackingNumber.endsWith(countryCode) && 
                                trackingNumber.length() == (3 + 9 + countryCode.length());
            logger.info("Format validation: " + (validFormat ? "PASS" : "FAIL"));
        }
    }
    
    /**
     * Main method for testing - ULTRA SIMPLE VERSION (No UIManager issues)
     */
    public static void main(String args[]) {
        // No look and feel setting - just start the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ExportationDelivery form = new ExportationDelivery();
                    form.setVisible(true);
                    System.out.println("ExportationDelivery application started successfully!");
                    
                    // Test tracking number generation (optional - for debugging)
                    // form.testTrackingGeneration();
                } catch (Exception e) {
                    System.err.println("Error starting application: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error dialog to user
                    JOptionPane.showMessageDialog(null,
                            "Error starting application: " + e.getMessage(),
                            "Application Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}