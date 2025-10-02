package hospital.views.receptionist;

import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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
        
        // Side panel
        sidePanel = new JPanel();
        sidePanel.setBackground(Constants.PRIMARY_COLOR);
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        
        // Content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        // Menu buttons
        dashboardBtn = createMenuButton("Dashboard", "dashboard");
        patientRegBtn = createMenuButton("Patient Registration", "patients");
        appointmentBtn = createMenuButton("Book Appointment", "appointments");
        billingBtn = createMenuButton("Billing", "billing");
        logoutBtn = createMenuButton("Logout", "logout");
        
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
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("RECEPTIONIST");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        userLabel.setForeground(new Color(230, 230, 230));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(userLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        sidePanel.add(headerPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        sidePanel.add(dashboardBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(patientRegBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(appointmentBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(billingBtn);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(logoutBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
    private JButton createMenuButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 45));
        button.setPreferredSize(new Dimension(200, 45));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(Constants.SECONDARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Constants.PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    @Override
    protected void setupEventHandlers() {
        ActionListener menuListener = e -> {
            String cmd = e.getActionCommand();
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            
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
    
    private void showDashboard() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "dashboard");
        dashboardPanel.refreshData();
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new hospital.views.login.LoginFrame().setVisible(true);
            });
        }
    }
}