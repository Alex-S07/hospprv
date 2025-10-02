package hospital.views.doctor;


import hospital.models.User;
import hospital.models.DoctorSchedule;
import hospital.dao.DoctorProfileDAO;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class SchedulePanel extends JPanel {
    private User currentUser;
    private DoctorProfileDAO profileDAO;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton markDutyBtn;
    private JButton markLeaveBtn;
    private JButton refreshBtn;
    private JLabel statusLabel;
    
    public SchedulePanel(User user) {
        this.currentUser = user;
        this.profileDAO = new DoctorProfileDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSchedule();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Date", "Day", "Status", "Reason"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(30);
        scheduleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        markDutyBtn = new JButton("Mark as DUTY");
        markLeaveBtn = new JButton("Mark as LEAVE");
        refreshBtn = new JButton("Refresh");
        
        markDutyBtn.setBackground(new Color(46, 125, 50));
        markDutyBtn.setForeground(Color.WHITE);
        markDutyBtn.setFocusPainted(false);
        
        markLeaveBtn.setBackground(new Color(198, 40, 40));
        markLeaveBtn.setForeground(Color.WHITE);
        markLeaveBtn.setFocusPainted(false);
        
        refreshBtn.setBackground(Constants.PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        
        statusLabel = new JLabel("Select dates to mark as DUTY or LEAVE");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Duty/Leave Schedule Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
        buttonPanel.add(markDutyBtn);
        buttonPanel.add(markLeaveBtn);
        buttonPanel.add(refreshBtn);
        
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        markDutyBtn.addActionListener(e -> markSelectedDates("DUTY"));
        markLeaveBtn.addActionListener(e -> markSelectedDates("LEAVE"));
        refreshBtn.addActionListener(e -> refreshData());
    }
    
    private void markSelectedDates(String status) {
        int[] selectedRows = scheduleTable.getSelectedRows();
        
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one date", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String reason = null;
        if ("LEAVE".equals(status)) {
            reason = JOptionPane.showInputDialog(this, 
                "Enter reason for leave:", 
                "Leave Reason", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (reason == null || reason.trim().isEmpty()) {
                return;
            }
        }
        
        try {
            int successCount = 0;
            for (int row : selectedRows) {
                String dateStr = (String) tableModel.getValueAt(row, 0);
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setDoctorId(currentUser.getUserId());
                schedule.setScheduleDate(date);
                schedule.setStatus(status);
                schedule.setReason(reason);
                
                if (profileDAO.setDoctorSchedule(schedule)) {
                    successCount++;
                }
            }
            
            if (successCount > 0) {
                JOptionPane.showMessageDialog(this, 
                    successCount + " date(s) marked as " + status, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void refreshData() {
        loadSchedule();
    }
    
    private void loadSchedule() {
        tableModel.setRowCount(0);
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        
        try {
            List<DoctorSchedule> schedules = profileDAO.getDoctorSchedules(
                currentUser.getUserId(), startDate, endDate);
            
            Map<LocalDate, DoctorSchedule> scheduleMap = new HashMap<>();
            for (DoctorSchedule schedule : schedules) {
                scheduleMap.put(schedule.getScheduleDate(), schedule);
            }
            
            // Generate 30 days from today
            for (int i = 0; i < 30; i++) {
                LocalDate date = startDate.plusDays(i);
                String dayOfWeek = date.getDayOfWeek().toString();
                
                DoctorSchedule schedule = scheduleMap.get(date);
                String status = schedule != null ? schedule.getStatus() : "DUTY";
                String reason = schedule != null ? (schedule.getReason() != null ? schedule.getReason() : "") : "";
                
                Object[] row = {
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    dayOfWeek,
                    status,
                    reason
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading schedule: " + ex.getMessage());
        }
    }
}