package hospital.views.receptionist;

import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ReceptionistDashboard extends BaseFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JPanel sidePanel;
    
    // Menu buttons
    private JButton dashboardBtn;
    private JButton patientRegBtn;
    private JButton appointmentBtn;
    private JButton billingBtn;
    private JButton logoutBtn;
    
    // Content panels
    private ReceptionDashboardPanel dashboardPanel;
    private PatientRegistrationPanel patientPanel;
    private AppointmentBookingPanel appointmentPanel;
    private BillingPanel billingPanel;
    
    public ReceptionistDashboard(User user) {
        super("Receptionist Dashboard - " + Constants.APP_NAME);
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        showDashboard();
    }
    
    @Override
    protected void initializeComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        
        // Side panel
        sidePanel = new JPanel();
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Light gray border
        
        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Menu buttons
        dashboardBtn = createMenuButton("📊 Dashboard", "dashboard");
        patientRegBtn = createMenuButton("👥 Patient Registration", "patients");
        appointmentBtn = createMenuButton("📅 Book Appointment", "appointments");
        billingBtn = createMenuButton("💰 Billing", "billing");
        logoutBtn = createMenuButton("🚪 Logout", "logout");
        
        // Create panels
        dashboardPanel = new ReceptionDashboardPanel(currentUser);
        patientPanel = new PatientRegistrationPanel(currentUser);
        appointmentPanel = new AppointmentBookingPanel(currentUser);
        billingPanel = new BillingPanel(currentUser);
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setupSidePanel();
        
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(patientPanel, "patients");
        contentPanel.add(appointmentPanel, "appointments");
        contentPanel.add(billingPanel, "billing");
        
        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupSidePanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200))); // Light gray border
        
        JLabel titleLabel = new JLabel("RECEPTIONIST");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK); // Keep black for better readability
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(Color.DARK_GRAY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(userLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        sidePanel.add(headerPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        sidePanel.add(dashboardBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(patientRegBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(appointmentBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(billingBtn);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(logoutBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
    private JButton createMenuButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK); // Black text for better readability
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Light gray border
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 45));
        button.setPreferredSize(new Dimension(200, 45));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 240, 240)); // Light gray on hover
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 180, 180), 1), // Slightly darker gray
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Light gray border
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        return button;
    }
    
    @Override
    protected void setupEventHandlers() {
        ActionListener menuListener = e -> {
            String cmd = e.getActionCommand();
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            
            // Reset all buttons
            resetMenuButtons();
            
            // Highlight active button
            JButton activeButton = getButtonByCommand(cmd);
            if (activeButton != null) {
                activeButton.setBackground(new Color(0, 100, 200)); // Blue background for active
                activeButton.setForeground(Color.BLACK); // White text for active button
                activeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            
            switch (cmd) {
                case "dashboard":
                    showDashboard();
                    break;
                case "patients":
                    cl.show(contentPanel, "patients");
                    patientPanel.refreshData();
                    break;
                case "appointments":
                    cl.show(contentPanel, "appointments");
                    appointmentPanel.refreshData();
                    break;
                case "billing":
                    cl.show(contentPanel, "billing");
                    billingPanel.refreshData();
                    break;
                case "logout":
                    handleLogout();
                    break;
            }
        };
        
        dashboardBtn.addActionListener(menuListener);
        patientRegBtn.addActionListener(menuListener);
        appointmentBtn.addActionListener(menuListener);
        billingBtn.addActionListener(menuListener);
        logoutBtn.addActionListener(menuListener);
    }
    
    private void resetMenuButtons() {
        for (JButton button : new JButton[]{dashboardBtn, patientRegBtn, appointmentBtn, billingBtn, logoutBtn}) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK); // Black text for inactive buttons
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Light gray border
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
        }
    }
    
    private JButton getButtonByCommand(String command) {
        switch (command) {
            case "dashboard": return dashboardBtn;
            case "patients": return patientRegBtn;
            case "appointments": return appointmentBtn;
            case "billing": return billingBtn;
            case "logout": return logoutBtn;
            default: return null;
        }
    }
    
    private void showDashboard() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "dashboard");
        dashboardPanel.refreshData();
        
        // Highlight dashboard button
        resetMenuButtons();
        dashboardBtn.setBackground(new Color(0, 100, 200)); // Blue background
        dashboardBtn.setForeground(Color.WHITE); // White text for active button
        dashboardBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new hospital.views.login.LoginFrame().setVisible(true);
            });
        }
    }
}