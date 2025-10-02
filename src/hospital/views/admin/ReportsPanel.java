package hospital.views.admin;

import hospital.dao.ReportDAO;
import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
// import com.toedter.calendar.JDateChooser;

class ReportsPanel extends JPanel {
    private User currentUser;
    private ReportDAO reportDAO;
    private JComboBox<String> reportTypeCombo;
    private JButton generateBtn;
    private JTextArea reportArea;
    // private com.toedter.calendar.JDateChooser startDateChooser;
    // private com.toedter.calendar.JDateChooser endDateChooser;

    public ReportsPanel(User user) {
        this.currentUser = user;
        this.reportDAO = new ReportDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] reportTypes = {
            "Daily Revenue Report",
            "Monthly Revenue Report",
            "Patient Demographics",
            "Doctor Workload Report",
            "Appointment Summary"
        };
        reportTypeCombo = new JComboBox<>(reportTypes);
        reportTypeCombo.setPreferredSize(new Dimension(250, 35));
        
        generateBtn = new JButton("Generate Report");
        generateBtn.setBackground(Constants.PRIMARY_COLOR);
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFocusPainted(false);
        generateBtn.setPreferredSize(new Dimension(150, 38));
        
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Date choosers would require external library
        // For now, we'll use text fields as placeholders
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(Constants.BACKGROUND_COLOR);
        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeCombo);
        controlPanel.add(generateBtn);
        
        headerPanel.add(controlPanel, BorderLayout.SOUTH);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        generateBtn.addActionListener(e -> generateReport());
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        reportArea.setText("Generating " + reportType + "...\n\n");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return reportDAO.generateReport(reportType);
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    reportArea.setText(report);
                } catch (Exception e) {
                    reportArea.setText("Error generating report: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    public void refreshData() {
        reportArea.setText("");
    }
}