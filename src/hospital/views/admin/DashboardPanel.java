package hospital.views.admin;

import hospital.dao.DashboardDAO;
import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

class DashboardPanel extends JPanel {
    private User currentUser;
    private DashboardDAO dashboardDAO;
    private JLabel totalPatientsLabel;
    private JLabel totalDoctorsLabel;
    private JLabel todayAppointmentsLabel;
    private JLabel totalRevenueLabel;
    private JLabel totalUsersLabel;
    private JLabel pendingBillsLabel;
    
    public DashboardPanel(User user) {
        this.currentUser = user;
        this.dashboardDAO = new DashboardDAO();
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        totalPatientsLabel = new JLabel("0");
        totalDoctorsLabel = new JLabel("0");
        todayAppointmentsLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("₹0.00");
        totalUsersLabel = new JLabel("0");
        pendingBillsLabel = new JLabel("0");
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(titleLabel, gbc);
        
        // Stats cards
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.33;
        gbc.weighty = 0.3;
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        add(createStatsCard("Total Patients", totalPatientsLabel, new Color(41, 128, 185)), gbc);
        
        gbc.gridx = 1;
        add(createStatsCard("Total Doctors", totalDoctorsLabel, new Color(39, 174, 96)), gbc);
        
        gbc.gridx = 2;
        add(createStatsCard("Total Users", totalUsersLabel, new Color(142, 68, 173)), gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        add(createStatsCard("Today's Appointments", todayAppointmentsLabel, new Color(230, 126, 34)), gbc);
        
        gbc.gridx = 1;
        add(createStatsCard("Total Revenue", totalRevenueLabel, new Color(52, 152, 219)), gbc);
        
        gbc.gridx = 2;
        add(createStatsCard("Pending Bills", pendingBillsLabel, new Color(231, 76, 60)), gbc);
    }
    
    private JPanel createStatsCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        titleLabel.setForeground(Color.WHITE);
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    public void refreshData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return dashboardDAO.getDashboardStats();
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    totalPatientsLabel.setText(String.valueOf(stats.get("totalPatients")));
                    totalDoctorsLabel.setText(String.valueOf(stats.get("totalDoctors")));
                    totalUsersLabel.setText(String.valueOf(stats.get("totalUsers")));
                    todayAppointmentsLabel.setText(String.valueOf(stats.get("todayAppointments")));
                    totalRevenueLabel.setText("₹" + String.format("%,.2f", stats.get("totalRevenue")));
                    pendingBillsLabel.setText(String.valueOf(stats.get("pendingBills")));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                        "Error loading dashboard data: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}

