package hospital.views.doctor;

import hospital.dao.AppointmentDAO;
import hospital.models.Appointment;
import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AppointmentPanel extends JPanel {
    private User currentUser;
    private AppointmentDAO appointmentDAO;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private JLabel totalAppointmentsLabel;
    private JLabel completedLabel;
    private JLabel pendingLabel;
    
    public AppointmentPanel(User user) {
        this.currentUser = user;
        this.appointmentDAO = new AppointmentDAO();
        initializeComponents();
        setupLayout();
        loadTodaysAppointments();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Token", "Time", "Patient Name", 
                           "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Actions is column index 4
            }
        };
        
        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setRowHeight(30);
        
        totalAppointmentsLabel = new JLabel("0");
        completedLabel = new JLabel("0");
        pendingLabel = new JLabel("0");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Header with stats
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Today's Appointments");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(Constants.BACKGROUND_COLOR);
        statsPanel.add(createStatsCard("Total", totalAppointmentsLabel, Constants.PRIMARY_COLOR));
        statsPanel.add(createStatsCard("Completed", completedLabel, Constants.SUCCESS_COLOR));
        statsPanel.add(createStatsCard("Pending", pendingLabel, Constants.WARNING_COLOR));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Table
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createStatsCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadTodaysAppointments() {
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                // ✅ FIXED: Use the actual logged-in doctor's ID instead of hardcoded 1
                return appointmentDAO.getTodaysAppointments(currentUser.getUserId());
            }
            
            @Override
            protected void done() {
                try {
                    List<Appointment> appointments = get();
                    updateTable(appointments);
                    updateStats(appointments);
                } catch (Exception e) {
                    e.printStackTrace(); // ✅ ADDED: Print stack trace for debugging
                    JOptionPane.showMessageDialog(AppointmentPanel.this,
                        "Error loading appointments: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateTable(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment apt : appointments) {
            Object[] row = {
                apt.getTokenNumber(),
                apt.getAppointmentDateTime().toLocalTime().toString(),
                apt.getPatientName(),
                apt.getStatus(),
                "Actions"
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStats(List<Appointment> appointments) {
        totalAppointmentsLabel.setText(String.valueOf(appointments.size()));
        long completed = appointments.stream()
            .filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long pending = appointments.stream()
            .filter(a -> "SCHEDULED".equals(a.getStatus()) || 
                        "IN_PROGRESS".equals(a.getStatus())).count();
        
        completedLabel.setText(String.valueOf(completed));
        pendingLabel.setText(String.valueOf(pending));
    }
    
    public void refreshData() {
        loadTodaysAppointments();
    }
}