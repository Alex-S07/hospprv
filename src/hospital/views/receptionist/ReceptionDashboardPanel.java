package hospital.views.receptionist;

import hospital.dao.ReceptionDashboardDAO;
import hospital.models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class ReceptionDashboardPanel extends JPanel {
    private User currentUser;
    private ReceptionDashboardDAO dashboardDAO;
    private JLabel totalPatientsLabel;
    private JLabel todayAppointmentsLabel;
    private JLabel todayRegistrationsLabel;
    private JLabel pendingBillsLabel;
    
    public ReceptionDashboardPanel(User user) {
        this.currentUser = user;
        this.dashboardDAO = new ReceptionDashboardDAO();
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        totalPatientsLabel = createStatLabel("0");
        todayAppointmentsLabel = createStatLabel("0");
        todayRegistrationsLabel = createStatLabel("0");
        pendingBillsLabel = createStatLabel("0");
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("Reception Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE); // Changed to white
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.3;
        
        gbc.gridx = 0; gbc.gridy = 1;
        add(createStatsCard("Total Patients", totalPatientsLabel, new Color(52, 152, 219)), gbc);
        
        gbc.gridx = 1;
        add(createStatsCard("Today's Appointments", todayAppointmentsLabel, new Color(230, 126, 34)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        add(createStatsCard("Today's Registrations", todayRegistrationsLabel, new Color(39, 174, 96)), gbc);
        
        gbc.gridx = 1;
        add(createStatsCard("Pending Bills", pendingBillsLabel, new Color(231, 76, 60)), gbc);
    }
    
    private JPanel createStatsCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE); // Changed to white
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(Color.BLACK); // Changed to BLACK
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(Color.BLACK); // Changed to BLACK
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    public void refreshData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return dashboardDAO.getReceptionStats();
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    totalPatientsLabel.setText(String.valueOf(stats.get("totalPatients")));
                    todayAppointmentsLabel.setText(String.valueOf(stats.get("todayAppointments")));
                    todayRegistrationsLabel.setText(String.valueOf(stats.get("todayRegistrations")));
                    pendingBillsLabel.setText(String.valueOf(stats.get("pendingBills")));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReceptionDashboardPanel.this, 
                        "Error loading dashboard data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}