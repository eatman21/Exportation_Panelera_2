    package exportation_panelera.View;

    import exportation_panelera.Model.LoginDTO;
    import exportation_panelera.dao.UserDAO;
    import exportation_panelera.util.I18nManager;

    import javax.swing.*;
    import javax.swing.border.CompoundBorder;
    import javax.swing.border.EmptyBorder;
    import javax.swing.border.LineBorder;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.event.KeyEvent;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * Sign In Form with improved UI, security, and user experience.
     * Provides authentication interface for the exportation management system.
     * 
     * @author YourName
     * @version 1.0
     */
    public class SignInForm extends JFrame {

        private static final Logger logger = Logger.getLogger(SignInForm.class.getName());

        // UI Constants for consistent theming
        private static final Color PRIMARY_COLOR = new Color(24, 53, 103);
        private static final Color SECONDARY_COLOR = new Color(0, 119, 182);
        private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
        private static final Color PANEL_COLOR = new Color(237, 242, 247);
        private static final Color SUCCESS_COLOR = new Color(46, 139, 87);
        private static final Color ERROR_COLOR = new Color(203, 67, 53);
        private static final Color TEXT_COLOR = new Color(33, 33, 33);

        // Font constants
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
        private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
        private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);

        // UI Components
        private JLabel lblTitle;
        private JLabel lblUsername;
        private JLabel lblPassword;
        private JTextField txtUsername;
        private JPasswordField txtPassword;
        private JButton btnSignIn;
        private JButton btnCancel;
        private JButton btnCreateUser;
        private JLabel lblStatus;
        private JProgressBar progressBar;
        private JCheckBox chkShowPassword;

        // Data and controllers
        private UserDAO userDAO;
        private boolean isAuthenticating = false;

        /**
         * Creates new Sign In form
         */
        public SignInForm() {
            userDAO = new UserDAO();
            initComponents();
            customizeUI();
            setupEventHandlers();
            setLocationRelativeTo(null); // Center on screen
        }

        /**
         * Initialize all UI components
         */
        private void initComponents() {
            // Set basic frame properties
            setTitle(I18nManager.getString("signin.title"));
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);

            // Create main panel
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BACKGROUND_COLOR);
            mainPanel.setBorder(new EmptyBorder(70, 70, 70, 70));

            // Create and setup components
            createHeaderPanel(mainPanel);
            createFormPanel(mainPanel);
            createButtonPanel(mainPanel);
            createStatusPanel(mainPanel);

            // Add main panel to frame
            setContentPane(mainPanel);
            pack();

            // Set minimum size
            setMinimumSize(new Dimension(450, 350));
        }

        /**
         * Create the header panel with title
         */
        private void createHeaderPanel(JPanel parent) {
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BACKGROUND_COLOR);
            headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

            lblTitle = new JLabel(I18nManager.getString("signin.welcome"), JLabel.CENTER);
            lblTitle.setFont(HEADER_FONT);
            lblTitle.setForeground(PRIMARY_COLOR);
            lblTitle.setBackground(PANEL_COLOR);
            lblTitle.setOpaque(true);
            lblTitle.setBorder(new EmptyBorder(15, 20, 15, 20));

            headerPanel.add(lblTitle, BorderLayout.CENTER);
            parent.add(headerPanel, BorderLayout.NORTH);
        }

        /**
         * Create the form panel with input fields
         */
        private void createFormPanel(JPanel parent) {
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(BACKGROUND_COLOR);
            formPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);

            // Username row
            lblUsername = new JLabel(I18nManager.getString("signin.username") + ":");
            customizeLabel(lblUsername);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(lblUsername, gbc);

            txtUsername = new JTextField(20);
            customizeTextField(txtUsername);
            txtUsername.setToolTipText("Enter your username (3-50 characters)");
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            formPanel.add(txtUsername, gbc);

            // Password row
            lblPassword = new JLabel(I18nManager.getString("signin.password") + ":");
            customizeLabel(lblPassword);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(lblPassword, gbc);

            txtPassword = new JPasswordField(20);
            customizePasswordField(txtPassword);
            txtPassword.setToolTipText("Enter your password (minimum 6 characters)");
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            formPanel.add(txtPassword, gbc);

            // Show password checkbox
            chkShowPassword = new JCheckBox(I18nManager.getString("signin.showPassword"));
            chkShowPassword.setFont(INPUT_FONT);
            chkShowPassword.setBackground(BACKGROUND_COLOR);
            chkShowPassword.setForeground(TEXT_COLOR);
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(chkShowPassword, gbc);

            parent.add(formPanel, BorderLayout.CENTER);
        }

        /**
         * Create the button panel
         */
        private void createButtonPanel(JPanel parent) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);

            btnSignIn = new JButton(I18nManager.getString("signin.login"));
            customizeButton(btnSignIn, SUCCESS_COLOR);
            btnSignIn.setToolTipText("Sign in to the system");

            btnCancel = new JButton(I18nManager.getString("button.cancel"));
            customizeButton(btnCancel, ERROR_COLOR);
            btnCancel.setToolTipText("Cancel and exit application");

            btnCreateUser = new JButton("Create Account");
            customizeButton(btnCreateUser, SECONDARY_COLOR);
            btnCreateUser.setToolTipText("Create a new user account");

            buttonPanel.add(btnSignIn);
            buttonPanel.add(btnCancel);
            buttonPanel.add(btnCreateUser);

            parent.add(buttonPanel, BorderLayout.SOUTH);
        }

        /**
         * Create the status panel with progress bar
         */
        private void createStatusPanel(JPanel parent) {
            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBackground(BACKGROUND_COLOR);
            statusPanel.setBorder(new EmptyBorder(10, 30, 0, 30));

            lblStatus = new JLabel(" ");
            lblStatus.setFont(INPUT_FONT);
            lblStatus.setHorizontalAlignment(JLabel.CENTER);

            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setVisible(false);
            progressBar.setStringPainted(true);
            progressBar.setString(I18nManager.getString("signin.loggingIn"));

            statusPanel.add(lblStatus, BorderLayout.CENTER);
            statusPanel.add(progressBar, BorderLayout.SOUTH);

            // Add status panel between form and buttons
            JPanel centerPanel = (JPanel) parent.getComponent(1);
            parent.remove(centerPanel);

            JPanel combinedPanel = new JPanel(new BorderLayout());
            combinedPanel.setBackground(BACKGROUND_COLOR);
            combinedPanel.add(centerPanel, BorderLayout.CENTER);
            combinedPanel.add(statusPanel, BorderLayout.SOUTH);

            parent.add(combinedPanel, BorderLayout.CENTER);
        }

        /**
         * Apply custom styling to UI components
         */
        private void customizeUI() {
            getContentPane().setBackground(BACKGROUND_COLOR);

            // Set default button for Enter key
            getRootPane().setDefaultButton(btnSignIn);

            // Focus on username field initially
            SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
        }

        /**
         * Setup event handlers for all interactive components
         */
        private void setupEventHandlers() {
            // Sign in button action
            btnSignIn.addActionListener(this::handleSignIn);

            // Cancel button action
            btnCancel.addActionListener(e -> handleCancel());

            // Create user button action
            btnCreateUser.addActionListener(e -> handleCreateUser());

            // Show password checkbox
            chkShowPassword.addActionListener(e -> togglePasswordVisibility());

            // Enter key handling for text fields
            txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
            txtPassword.addActionListener(this::handleSignIn);

            // ESC key to cancel
            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(escapeKeyStroke, "ESCAPE");
            getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleCancel();
                }
            });
        }

        /**
         * Handle sign in action with validation and authentication
         */
        private void handleSignIn(ActionEvent event) {
            if (isAuthenticating) {
                return; // Prevent multiple simultaneous attempts
            }

            try {
                // Clear previous status
                clearStatus();

                // Get and validate input
                String username = txtUsername.getText().trim();
                char[] passwordChars = txtPassword.getPassword();
                String password = new String(passwordChars);

                // Clear password from memory
                java.util.Arrays.fill(passwordChars, ' ');

                // Validate input
                if (username.isEmpty()) {
                    showError("Please enter your username");
                    txtUsername.requestFocusInWindow();
                    return;
                }

                if (password.isEmpty()) {
                    showError("Please enter your password");
                    txtPassword.requestFocusInWindow();
                    return;
                }

                // Create DTO and validate
                LoginDTO loginDTO = new LoginDTO();
                try {
                    loginDTO.setUsername(username);
                    loginDTO.setPassword(password);
                    loginDTO.validate();
                } catch (IllegalArgumentException e) {
                    showError("Invalid input: " + e.getMessage());
                    return;
                }

                // Perform authentication in background
                performAuthentication(loginDTO);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error during sign in", e);
                showError("An unexpected error occurred. Please try again.");
            }
        }

        /**
         * Perform authentication in a background thread
         */
        private void performAuthentication(LoginDTO loginDTO) {
            isAuthenticating = true;
            setButtonsEnabled(false);
            showProgress("Authenticating...");

            // Use SwingWorker for background authentication
            SwingWorker<Boolean, Void> authWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Simulate some processing time for better UX
                    Thread.sleep(1000);

                    // Perform actual authentication
                    return userDAO.authenticateUser(loginDTO.getUsername(), loginDTO.getPassword());
                }

                @Override
                protected void done() {
                    try {
                        boolean authenticated = get();
                        handleAuthenticationResult(authenticated, loginDTO);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Authentication error", e);
                        showError("Authentication failed: " + e.getMessage());
                    } finally {
                        isAuthenticating = false;
                        setButtonsEnabled(true);
                        hideProgress();
                    }
                }
            };

            authWorker.execute();
        }

        /**
         * Handle the result of authentication attempt
         */
        private void handleAuthenticationResult(boolean authenticated, LoginDTO loginDTO) {
            if (authenticated) {
                showSuccess("Sign in successful!");

                // Clear sensitive data
                // Clear sensitive data
               loginDTO.clearSensitiveData();

               // Small delay to show success message
               Timer timer = new Timer(1500, e -> {
                   // Open main application window
                   openMainApplication();
               });
               timer.setRepeats(false);
               timer.start();

           } else {
               showError("Invalid username or password");
               txtPassword.setText("");
               txtUsername.requestFocusInWindow();
           }
       }

       /**
        * Open the main application window
        */
       private void openMainApplication() {
           try {
               MainView mainView = new MainView();
               mainView.setVisible(true);
               this.dispose();

               logger.info("User successfully signed in, main application opened");

           } catch (Exception e) {
               logger.log(Level.SEVERE, "Error opening main application", e);
               showError("Error opening main application: " + e.getMessage());
           }
       }

       /**
        * Handle cancel action
        */
       private void handleCancel() {
           int option = JOptionPane.showConfirmDialog(
               this,
               "Are you sure you want to exit the application?",
               "Confirm Exit",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE
           );

           if (option == JOptionPane.YES_OPTION) {
               logger.info("Application closed by user");
               System.exit(0);
           }
       }

       /**
        * Handle create user action
        */
       private void handleCreateUser() {
           JOptionPane.showMessageDialog(
               this,
               "User registration feature will be available in a future version.\n" +
               "Please contact your system administrator to create an account.",
               "Create User Account",
               JOptionPane.INFORMATION_MESSAGE
           );
       }

       /**
        * Toggle password visibility
        */
       private void togglePasswordVisibility() {
           if (chkShowPassword.isSelected()) {
               txtPassword.setEchoChar((char) 0);
           } else {
               txtPassword.setEchoChar('•');
           }
       }

       /**
        * Show success message
        */
       private void showSuccess(String message) {
           lblStatus.setText(message);
           lblStatus.setForeground(SUCCESS_COLOR);
           lblStatus.setIcon(createStatusIcon("✓", SUCCESS_COLOR));
       }

       /**
        * Show error message
        */
       private void showError(String message) {
           lblStatus.setText(message);
           lblStatus.setForeground(ERROR_COLOR);
           lblStatus.setIcon(createStatusIcon("✗", ERROR_COLOR));
       }

       /**
        * Show progress indicator
        */
       private void showProgress(String message) {
           lblStatus.setText(message);
           lblStatus.setForeground(SECONDARY_COLOR);
           progressBar.setVisible(true);
       }

       /**
        * Hide progress indicator
        */
       private void hideProgress() {
           progressBar.setVisible(false);
       }

       /**
        * Clear status message
        */
       private void clearStatus() {
           lblStatus.setText(" ");
           lblStatus.setIcon(null);
           hideProgress();
       }

       /**
        * Create a status icon
        */
       private Icon createStatusIcon(String text, Color color) {
           return new Icon() {
               @Override
               public void paintIcon(Component c, Graphics g, int x, int y) {
                   Graphics2D g2 = (Graphics2D) g.create();
                   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                   g2.setColor(color);
                   g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                   g2.drawString(text, x, y + 12);
                   g2.dispose();
               }

               @Override
               public int getIconWidth() { return 15; }

               @Override
               public int getIconHeight() { return 15; }
           };
       }

       /**
        * Enable or disable all buttons
        */
       private void setButtonsEnabled(boolean enabled) {
           btnSignIn.setEnabled(enabled);
           btnCancel.setEnabled(enabled);
           btnCreateUser.setEnabled(enabled);
       }

       /**
        * Customize label appearance
        */
       private void customizeLabel(JLabel label) {
           label.setFont(LABEL_FONT);
           label.setForeground(TEXT_COLOR);
           label.setBackground(PANEL_COLOR);
           label.setOpaque(true);
           label.setBorder(new EmptyBorder(5, 10, 5, 10));
       }

       /**
        * Customize text field appearance
        */
       private void customizeTextField(JTextField field) {
           field.setFont(INPUT_FONT);
           field.setBorder(new CompoundBorder(
               new LineBorder(SECONDARY_COLOR, 1),
               new EmptyBorder(8, 12, 8, 12)
           ));
           field.setBackground(Color.WHITE);
           field.setForeground(TEXT_COLOR);
           field.setPreferredSize(new Dimension(250, 35));

           // Add focus border
           field.addFocusListener(new java.awt.event.FocusAdapter() {
               @Override
               public void focusGained(java.awt.event.FocusEvent evt) {
                   field.setBorder(new CompoundBorder(
                       new LineBorder(PRIMARY_COLOR, 2),
                       new EmptyBorder(7, 11, 7, 11)
                   ));
               }

               @Override
               public void focusLost(java.awt.event.FocusEvent evt) {
                   field.setBorder(new CompoundBorder(
                       new LineBorder(SECONDARY_COLOR, 1),
                       new EmptyBorder(8, 12, 8, 12)
                   ));
               }
           });
       }

       /**
        * Customize password field appearance
        */
       private void customizePasswordField(JPasswordField field) {
           field.setFont(INPUT_FONT);
           field.setBorder(new CompoundBorder(
               new LineBorder(SECONDARY_COLOR, 1),
               new EmptyBorder(8, 12, 8, 12)
           ));
           field.setBackground(Color.WHITE);
           field.setForeground(TEXT_COLOR);
           field.setPreferredSize(new Dimension(250, 35));
           field.setEchoChar('•');

           // Add focus border
           field.addFocusListener(new java.awt.event.FocusAdapter() {
               @Override
               public void focusGained(java.awt.event.FocusEvent evt) {
                   field.setBorder(new CompoundBorder(
                       new LineBorder(PRIMARY_COLOR, 2),
                       new EmptyBorder(7, 11, 7, 11)
                   ));
               }

               @Override
               public void focusLost(java.awt.event.FocusEvent evt) {
                   field.setBorder(new CompoundBorder(
                       new LineBorder(SECONDARY_COLOR, 1),
                       new EmptyBorder(8, 12, 8, 12)
                   ));
               }
           });
       }

       /**
        * Customize button appearance
        */
       private void customizeButton(JButton button, Color bgColor) {
           button.setFont(BUTTON_FONT);
           button.setBackground(bgColor);
           button.setForeground(Color.DARK_GRAY);
           button.setBorder(new EmptyBorder(10, 20, 10, 20));
           button.setFocusPainted(false);
           button.setCursor(new Cursor(Cursor.HAND_CURSOR));

           // Add hover effect
           button.addMouseListener(new java.awt.event.MouseAdapter() {
               @Override
               public void mouseEntered(java.awt.event.MouseEvent evt) {
                   button.setBackground(bgColor.darker());
               }

               @Override
               public void mouseExited(java.awt.event.MouseEvent evt) {
                   button.setBackground(bgColor);
               }
           });
       }

       /**
        * Main method for testing the sign in form
        */
       public static void main(String[] args) {
           // Set system look and feel
           try {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
           } catch (Exception e) {
               Logger.getLogger(SignInForm.class.getName())
                     .log(Level.WARNING, "Could not set system look and feel", e);
           }

           // Create and show the form
           SwingUtilities.invokeLater(() -> {
               new SignInForm().setVisible(true);
           });
       }
    }