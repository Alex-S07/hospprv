package hospital.views.admin;

import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminDashboard extends BaseFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JPanel sidePanel;
    
    // Menu buttons
    private JButton dashboardBtn;
    private JButton staffManagementBtn;
    private JButton reportsBtn;
    private JButton systemConfigBtn;
    private JButton logoutBtn;
    
    // Content panels
    private DashboardPanel dashboardPanel;
    private StaffManagementPanel staffManagementPanel;
    private ReportsPanel reportsPanel;
    private SystemConfigPanel systemConfigPanel;
    
    public AdminDashboard(User user) {
        super("Admin Dashboard - " + Constants.APP_NAME);
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
        
        // Create side panel
        sidePanel = new JPanel();
        sidePanel.setBackground(Constants.PRIMARY_COLOR);
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        
        // Create content panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        // Create menu buttons
        dashboardBtn = createMenuButton("Dashboard", "dashboard");
        staffManagementBtn = createMenuButton("Staff Management", "staff");
        reportsBtn = createMenuButton("Reports", "reports");
        systemConfigBtn = createMenuButton("System Config", "config");
        logoutBtn = createMenuButton("Logout", "logout");
        
        // Create content panels
        dashboardPanel = new DashboardPanel(currentUser);
        staffManagementPanel = new StaffManagementPanel(currentUser);
        reportsPanel = new ReportsPanel(currentUser);
        systemConfigPanel = new SystemConfigPanel(currentUser);
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setupSidePanel();
        
        // Add content panels to card layout
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(staffManagementPanel, "staff");
        contentPanel.add(reportsPanel, "reports");
        contentPanel.add(systemConfigPanel, "config");
        
        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupSidePanel() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("ADMIN PANEL");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel("Welcome, " + currentUser.getUsername());
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
        
        // Menu buttons
        sidePanel.add(dashboardBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(staffManagementBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(reportsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(systemConfigBtn);
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(Constants.SECONDARY_COLOR);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Constants.PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    @Override
    protected void setupEventHandlers() {
        ActionListener menuActionListener = e -> {
            String command = e.getActionCommand();
            CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
            
            switch (command) {
                case "dashboard":
                    showDashboard();
                    break;
                case "staff":
                    cardLayout.show(contentPanel, "staff");
                    staffManagementPanel.refreshData();
                    break;
                case "reports":
                    cardLayout.show(contentPanel, "reports");
                    reportsPanel.refreshData();
                    break;
                case "config":
                    cardLayout.show(contentPanel, "config");
                    break;
                case "logout":
                    handleLogout();
                    break;
            }
        };
        
        dashboardBtn.addActionListener(menuActionListener);
        staffManagementBtn.addActionListener(menuActionListener);
        reportsBtn.addActionListener(menuActionListener);
        systemConfigBtn.addActionListener(menuActionListener);
        logoutBtn.addActionListener(menuActionListener);
    }
    
    private void showDashboard() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "dashboard");
        dashboardPanel.refreshData();
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new hospital.views.login.LoginFrame().setVisible(true);
            });
        }
    }
}