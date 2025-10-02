package hospital.views.login;

import hospital.controllers.LoginController;
import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.admin.AdminDashboard;
import hospital.views.doctor.DoctorDashboard;
import hospital.views.doctor.PatientHistoryPanel;
import hospital.views.receptionist.ReceptionistDashboard;
import hospital.views.pharmacy.PharmacyDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private LoginController loginController;

    public LoginFrame() {
        this.loginController = new LoginController();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setTitle("Hospital Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        statusLabel = new JLabel(" ");

        // Style components
        loginButton.setBackground(Constants.PRIMARY_COLOR);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        statusLabel.setForeground(Color.RED);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Hospital Management System");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Constants.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(loginButton, gbc);

        // Status label
        gbc.gridy = 3;
        formPanel.add(statusLabel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // Enter key support
        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        };

        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        // Show loading
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        statusLabel.setText(" ");

        // Perform login in background thread
        SwingWorker<User, Void> loginWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return loginController.authenticate(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                try {
                    User user = get();
                    if (user != null) {
                        openDashboard(user);
                        dispose();
                    } else {
                        statusLabel.setText("Invalid username or password");
                        passwordField.setText("");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Login failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        loginWorker.execute();
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            switch (user.getRole()) {
                case "ADMIN":
                    new AdminDashboard(user).setVisible(true);
                    break;
                case "DOCTOR":
                    new DoctorDashboard(user).setVisible(true);
                    break;
                case "RECEPTIONIST":
                    new ReceptionistDashboard(user).setVisible(true);
                    break;
                case "PHARMACY":
                    new PharmacyDashboard(user).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, 
                        "Unknown user role: " + user.getRole(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}