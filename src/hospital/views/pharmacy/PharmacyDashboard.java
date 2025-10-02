package hospital.views.pharmacy;

import hospital.dao.*;
import hospital.models.*;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PharmacyDashboard extends BaseFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JPanel sidePanel;

    // Menu buttons
    private JButton dashboardBtn;
    private JButton prescriptionsBtn;
    private JButton inventoryBtn;
    private JButton dispensedBtn;
    private JButton logoutBtn;

    // Content panels
    private PrescriptionDispensePanel prescriptionPanel;
    private InventoryManagementPanel inventoryPanel;
    private DispensedHistoryPanel dispensedPanel;

    public PharmacyDashboard(User user) {
        super("Pharmacy Dashboard - " + Constants.APP_NAME);
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        showPrescriptions();
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

        // Create menu buttons
        dashboardBtn = createMenuButton("Dashboard", "dashboard");
        prescriptionsBtn = createMenuButton("Prescriptions", "prescriptions");
        inventoryBtn = createMenuButton("Inventory", "inventory");
        dispensedBtn = createMenuButton("Dispensed History", "dispensed");
        logoutBtn = createMenuButton("Logout", "logout");

        // Create panels
        prescriptionPanel = new PrescriptionDispensePanel(currentUser);
        inventoryPanel = new InventoryManagementPanel(currentUser);
        dispensedPanel = new DispensedHistoryPanel(currentUser);
    }

    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setupSidePanel();

        // Add panels to card layout
        contentPanel.add(prescriptionPanel, "prescriptions");
        contentPanel.add(inventoryPanel, "inventory");
        contentPanel.add(dispensedPanel, "dispensed");

        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupSidePanel() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("PHARMACY");
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
        sidePanel.add(prescriptionsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(inventoryBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(dispensedBtn);
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
        prescriptionsBtn.addActionListener(e -> showPrescriptions());
        inventoryBtn.addActionListener(e -> showInventory());
        dispensedBtn.addActionListener(e -> showDispensed());
        logoutBtn.addActionListener(e -> handleLogout());
    }

    private void showPrescriptions() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "prescriptions");
        prescriptionPanel.refreshData();
    }

    private void showInventory() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "inventory");
        inventoryPanel.refreshData();
    }

    private void showDispensed() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "dispensed");
        dispensedPanel.refreshData();
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
                this,
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
