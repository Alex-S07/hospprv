package hospital.views.laboratory;

import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard for Laboratory Assistant.
 * Layout mirrors PharmacyDashboard: a left sidebar and a CardLayout content area.
 */
public class LaboratoryDashboard extends BaseFrame {
    private User currentUser;
    private JPanel sidePanel;
    private JPanel contentPanel;

    // Menu buttons
    private JButton pendingRequestsBtn;
    private JButton completedTestsBtn;
    private JButton logoutBtn;

    // Content panels
    private PendingRequestsPanel pendingRequestsPanel;
    private CompletedTestsPanel completedTestsPanel;

    public LaboratoryDashboard(User user) {
        super("Laboratory Dashboard - " + Constants.APP_NAME);
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        showPendingRequests();
    }

    @Override
    protected void initializeComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        sidePanel = new JPanel();
        sidePanel.setBackground(Constants.PRIMARY_COLOR);
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Constants.BACKGROUND_COLOR);

        pendingRequestsBtn = createMenuButton("Pending Requests", "pendingRequests");
        completedTestsBtn = createMenuButton("Completed Tests", "completedTests");
        logoutBtn = createMenuButton("Logout", "logout");

        pendingRequestsPanel = new PendingRequestsPanel(currentUser);
        completedTestsPanel = new CompletedTestsPanel(currentUser);
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
    protected void setupLayout() {
        setLayout(new BorderLayout());

        // Header area in side panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("LABORATORY");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Welcome, " + (currentUser != null ? currentUser.getUsername() : "User"));
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

        sidePanel.add(pendingRequestsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(completedTestsBtn);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(logoutBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add content panels
        contentPanel.add(pendingRequestsPanel, "pendingRequests");
        contentPanel.add(completedTestsPanel, "completedTests");

        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void setupEventHandlers() {
        pendingRequestsBtn.addActionListener(e -> showPendingRequests());
        completedTestsBtn.addActionListener(e -> showCompletedTests());
        logoutBtn.addActionListener(e -> handleLogout());
    }

    private void showPendingRequests() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "pendingRequests");
        pendingRequestsPanel.refreshData();
    }
    
    private void showCompletedTests() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "completedTests");
        completedTestsPanel.refreshData();
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new hospital.views.login.LoginFrame().setVisible(true));
        }
    }
}
