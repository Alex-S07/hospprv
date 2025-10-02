package hospital.views.doctor;

import hospital.models.User;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import java.awt.*;

public class DoctorDashboard extends BaseFrame {
    private User currentUser;
    private JPanel contentPanel;
    private JPanel sidePanel;
    
    // Menu buttons
    private JButton consultationsBtn;
    private JButton scheduleBtn;
    private JButton logoutBtn;
    
    // Content panels
    private PrescriptionAndHistoryPanel prescriptionPanel;
    private SchedulePanel schedulePanel;
    
    public DoctorDashboard(User user) {
        super("Doctor Dashboard - " + Constants.APP_NAME);
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        showConsultations();
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
        
        // Create menu buttons
        consultationsBtn = createMenuButton("Consultations", "consultations");
        scheduleBtn = createMenuButton("Duty/Leave Schedule", "schedule");
        logoutBtn = createMenuButton("Logout", "logout");
        
        // Create content panels
        prescriptionPanel = new PrescriptionAndHistoryPanel(currentUser);
        schedulePanel = new SchedulePanel(currentUser);
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setupSidePanel();
        
        // Add both panels to card layout
        contentPanel.add(prescriptionPanel, "consultations");
        contentPanel.add(schedulePanel, "schedule");
        
        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupSidePanel() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Constants.PRIMARY_COLOR);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("DOCTOR PANEL");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel("Dr. " + currentUser.getUsername());
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
        sidePanel.add(consultationsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(scheduleBtn);
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
        consultationsBtn.addActionListener(e -> showConsultations());
        scheduleBtn.addActionListener(e -> showSchedule());
        logoutBtn.addActionListener(e -> handleLogout());
    }
    
    private void showConsultations() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "consultations");
    }
    
    private void showSchedule() {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, "schedule");
        schedulePanel.refreshData();
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new hospital.views.login.LoginFrame().setVisible(true);
            });
        }
    }
}