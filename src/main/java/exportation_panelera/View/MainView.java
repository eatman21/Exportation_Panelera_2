package exportation_panelera.View;

import exportation_panelera.util.I18nManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

// Importaciones explícitas de las clases que estás usando
import exportation_panelera.View.ExportationDelivery;
import exportation_panelera.View.SignInForm;
import exportation_panelera.View.DeliveryManagementForm;

/**
 * Main View for the Exportation Panelera Management System
 * Provides navigation to different modules of the application
 */
public class MainView extends JFrame {
    private static final Logger logger = Logger.getLogger(MainView.class.getName());
    
    // Colors for UI consistency
    private static final Color PRIMARY_COLOR = new Color(24, 53, 103);
    private static final Color SECONDARY_COLOR = new Color(0, 119, 182);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color PANEL_COLOR = new Color(237, 242, 247);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    // UI Components
    private JPanel mainPanel;
    private JLabel lblTitle;
    private JButton btnDelivery;
    private JButton btnSignout;
    private BufferedImage backgroundImage;

    /**
     * Custom JPanel that paints a background image
     */
    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw the image scaled to fill the panel
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                // Add semi-transparent overlay for better contrast
                g2d.setColor(new Color(0, 0, 0, 80)); // Black with 31% opacity (lighter)
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    // Constructor
    public MainView() {
        setTitle(I18nManager.getString("main.title"));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        logger.log(Level.INFO, "Main View initialized");
    }
    
    private void initComponents() {
        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/sugar-cane.jpg"));
            logger.info("Background image loaded successfully");
        } catch (IOException | IllegalArgumentException e) {
            logger.log(Level.WARNING, "Could not load background image, using solid color", e);
            backgroundImage = null;
        }

        // Initialize main panel with background
        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Create header panel with title
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(800, 80));
        headerPanel.setLayout(new BorderLayout());
        
        lblTitle = new JLabel(I18nManager.getString("main.welcome"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        // Create navigation panel (transparent to show background)
        JPanel navigationPanel = new JPanel();
        navigationPanel.setOpaque(false); // Make transparent
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(35, 150, 35, 150)); // More side margins
        navigationPanel.setLayout(new GridLayout(2, 1, 0, 15)); // Reduced spacing between buttons

        // Create buttons
        btnDelivery = createNavigationButton(I18nManager.getString("main.delivery"), "Track and manage deliveries");
        btnSignout = createNavigationButton(I18nManager.getString("main.logout"), "Exit the application");

        navigationPanel.add(btnDelivery);
        navigationPanel.add(btnSignout);
        
        // Add to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(navigationPanel, BorderLayout.CENTER);
        
        // Add action listeners
        btnDelivery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDeliveryManagementForm();
            }
        });

        btnSignout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signOut();
            }
        });
        
        // Set content pane
        setContentPane(mainPanel);
    }
    
    private JButton createNavigationButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Moderate font size
        button.setBackground(new Color(255, 255, 255, 240)); // Almost opaque white
        button.setForeground(new Color(24, 53, 103)); // Dark blue text
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        // Add visible border with compact padding
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 119, 182), 2), // Thinner blue border
            BorderFactory.createEmptyBorder(12, 25, 12, 25) // Reduced padding
        ));
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for better UX
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 119, 182)); // Blue background on hover
                button.setForeground(Color.WHITE); // White text on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 240)); // Back to white
                button.setForeground(new Color(24, 53, 103)); // Back to dark blue text
            }
        });

        return button;
    }
    
    private void openExportationForm() {
        // Open the exportation information form
        try {
            logger.log(Level.INFO, "Opening Exportation Information form");
            // Use the unified ExportationDelivery form
            ExportationDelivery exportationForm = new ExportationDelivery();
            exportationForm.setVisible(true);
            this.setVisible(false); // Hide main view
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error opening Exportation Information", e);
            JOptionPane.showMessageDialog(this, 
                "Error opening Exportation Information: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openDeliveryManagementForm() {
        // Open the delivery management form
        try {
            logger.log(Level.INFO, "Opening Delivery Management form");
            DeliveryManagementForm deliveryForm = new DeliveryManagementForm();
            deliveryForm.setVisible(true);
            this.setVisible(false); // Hide main view
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error opening Delivery Management form", e);
            JOptionPane.showMessageDialog(this, 
                "Error opening Delivery Management form: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void signOut() {
        // Sign out and return to login screen
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to sign out?",
            "Sign Out", JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            logger.log(Level.INFO, "User signing out");
            SignInForm signingForm = new SignInForm();
            signingForm.setVisible(true);
            this.dispose(); // Close main view
        }
    }
    
    /**
     * Main method for running the application
     * @param args command line arguments
     */
    public static void main(String args[]) {
        try {
            // For better look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, "Error setting look and feel", e);
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainView().setVisible(true);
            }
        });
    }
}