/*
 * Main entry point for Exportation Panelera Management System
 */
package exportation_panelera;

import exportation_panelera.View.SignInForm;
import exportation_panelera.db.DatabaseManager;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for Exportation Panelera
 * Initializes the application and displays the Sign In form
 *
 * @author Cris
 */
public class Exportation_Panelera {

    private static final Logger logger = Logger.getLogger(Exportation_Panelera.class.getName());

    /**
     * Application entry point
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Set System Look and Feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not set system look and feel", e);
        }

        // Initialize database connection pool
        logger.info("Initializing database connection pool...");
        boolean dbInitialized = DatabaseManager.initialize();

        if (dbInitialized) {
            logger.info("Database connection pool initialized successfully");
        } else {
            logger.warning("Database connection pool initialization failed - running in offline mode");
        }

        // Create and display the Sign In form on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("Starting Exportation Panelera application...");

                SignInForm signInForm = new SignInForm();
                signInForm.setVisible(true);

                logger.info("Sign In form displayed successfully");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error starting application", e);
                JOptionPane.showMessageDialog(
                    null,
                    "Error starting application: " + e.getMessage(),
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });

        // Add shutdown hook to close database connections gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application...");
            DatabaseManager.shutdown();
            logger.info("Application shutdown complete");
        }));
    }
}
