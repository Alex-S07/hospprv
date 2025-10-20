package hospital.views.doctor;

import hospital.dao.DoctorDAO;
import hospital.dao.TestDAO;
import hospital.models.Patient;
import hospital.models.Test;
import hospital.models.TestRequest;
import hospital.models.User;
import hospital.utils.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel for doctors to manage lab test requests and view results.
 */
public class DoctorLabTestsPanel extends JPanel {
    private User currentUser;
    private TestDAO testDAO;
    private DoctorDAO doctorDAO;
    
    // Left side - test list
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JButton pendingBtn;
    private JButton completedBtn;
    private String currentFilter = "Pending";
    
    // Right side - details and new request
    private JPanel rightPanel;
    private JTextArea detailsArea;
    private JComboBox<PatientItem> patientCombo;
    private JComboBox<Test> testCombo;
    private JTextArea remarksArea;
    private JButton submitRequestBtn;
    
    private int doctorId;
    
    public DoctorLabTestsPanel(User user) {
        this.currentUser = user;
        this.testDAO = new TestDAO();
        this.doctorDAO = new DoctorDAO();
        
        // Get doctor ID from user
        try {
            this.doctorId = doctorDAO.getDoctorIdByUserId(user.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            this.doctorId = -1;
        }
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Table for test requests
        String[] columns = {"Request ID", "Patient Name", "Test Name", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        testsTable = new JTable(tableModel);
        testsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testsTable.setRowHeight(25);
        
        // Filter buttons
        pendingBtn = new JButton("Pending Tests");
        completedBtn = new JButton("Completed Tests");
        styleFilterButton(pendingBtn, true);
        styleFilterButton(completedBtn, false);
        
        // Right panel components
        detailsArea = new JTextArea(10, 30);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        patientCombo = new JComboBox<>();
        testCombo = new JComboBox<>();
        remarksArea = new JTextArea(4, 30);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        
        submitRequestBtn = new JButton("Submit Request");
        submitRequestBtn.setBackground(Constants.PRIMARY_COLOR);
        submitRequestBtn.setForeground(Color.WHITE);
        submitRequestBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        loadPatients();
        loadTests();
    }
    
    private void setupLayout() {
        // Left panel with table
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        // Filter buttons panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(pendingBtn);
        filterPanel.add(completedBtn);
        
        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(testsTable), BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Test Requests"));
        
        // Right panel with details and form
        rightPanel = new JPanel(new BorderLayout(10, 10));
        
        // Details section
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Test Details"));
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        
        // New request form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Request New Test"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Test:"), gbc);
        gbc.gridx = 1;
        formPanel.add(testCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(remarksArea), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        formPanel.add(submitRequestBtn, gbc);
        
        rightPanel.add(detailsPanel, BorderLayout.CENTER);
        rightPanel.add(formPanel, BorderLayout.SOUTH);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(600);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Filter buttons
        pendingBtn.addActionListener(e -> {
            currentFilter = "Pending";
            styleFilterButton(pendingBtn, true);
            styleFilterButton(completedBtn, false);
            refreshData();
        });
        
        completedBtn.addActionListener(e -> {
            currentFilter = "Completed";
            styleFilterButton(pendingBtn, false);
            styleFilterButton(completedBtn, true);
            refreshData();
        });
        
        // Table selection
        testsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = testsTable.getSelectedRow();
                if (row >= 0) {
                    int requestId = (int) tableModel.getValueAt(row, 0);
                    loadTestDetails(requestId);
                }
            }
        });
        
        // Submit new request
        submitRequestBtn.addActionListener(e -> handleSubmitRequest());
    }
    
    private void styleFilterButton(JButton button, boolean active) {
        if (active) {
            button.setBackground(Constants.PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        } else {
            button.setBackground(Color.LIGHT_GRAY);
            button.setForeground(Color.BLACK);
            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        }
        button.setFocusPainted(false);
    }
    
    public void refreshData() {
        tableModel.setRowCount(0);
        
        if (doctorId == -1) {
            detailsArea.setText("Error: Could not find doctor profile for this user.");
            return;
        }
        
        try {
            List<TestRequest> requests = testDAO.getDoctorTests(doctorId, currentFilter);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            for (TestRequest request : requests) {
                Object[] row = {
                    request.getRequestId(),
                    request.getPatientName(),
                    request.getTestName(),
                    request.getStatus(),
                    request.getRequestDate() != null ? dateFormat.format(request.getRequestDate()) : ""
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading test requests: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTestDetails(int requestId) {
        try {
            TestRequest request = testDAO.getTestRequestById(requestId);
            
            if (request == null) {
                detailsArea.setText("Test request not found.");
                return;
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            StringBuilder details = new StringBuilder();
            
            details.append("═══════════════════════════════════════\n");
            details.append("TEST REQUEST DETAILS\n");
            details.append("═══════════════════════════════════════\n\n");
            
            details.append("Request ID: ").append(request.getRequestId()).append("\n");
            details.append("Patient: ").append(request.getPatientName()).append("\n");
            details.append("Test: ").append(request.getTestName()).append("\n");
            details.append("Status: ").append(request.getStatus()).append("\n");
            details.append("Request Date: ").append(dateFormat.format(request.getRequestDate())).append("\n\n");
            
            details.append("Doctor Remarks:\n");
            details.append(request.getRemarks() != null ? request.getRemarks() : "No remarks provided").append("\n\n");
            
            if ("Completed".equals(request.getStatus())) {
                details.append("═══════════════════════════════════════\n");
                details.append("TEST RESULTS\n");
                details.append("═══════════════════════════════════════\n\n");
                
                details.append("Result Value: ").append(request.getResultValue() != null ? request.getResultValue() : "N/A").append("\n");
                details.append("Lab Comments: ").append(request.getResultComments() != null ? request.getResultComments() : "N/A").append("\n");
                details.append("Result File: ").append(request.getResultFile() != null ? request.getResultFile() : "No file uploaded").append("\n");
                
                if (request.getUploadDate() != null) {
                    details.append("Upload Date: ").append(dateFormat.format(request.getUploadDate())).append("\n");
                }
            }
            
            detailsArea.setText(details.toString());
            detailsArea.setCaretPosition(0);
            
        } catch (SQLException e) {
            e.printStackTrace();
            detailsArea.setText("Error loading test details: " + e.getMessage());
        }
    }
    
    private void loadPatients() {
        try {
            List<Patient> patients = testDAO.getAllPatients();
            patientCombo.removeAllItems();
            
            for (Patient patient : patients) {
                patientCombo.addItem(new PatientItem(patient));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading patients: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTests() {
        try {
            List<Test> tests = testDAO.getAllTests();
            testCombo.removeAllItems();
            
            for (Test test : tests) {
                testCombo.addItem(test);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading tests: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleSubmitRequest() {
        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        Test selectedTest = (Test) testCombo.getSelectedItem();
        String remarks = remarksArea.getText().trim();
        
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Please select a test.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (remarks.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this, 
                "No remarks provided. Continue without remarks?",
                "Confirm", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try {
            boolean success = testDAO.insertTestRequest(
                doctorId,
                selectedPatient.patient.getPatientId(),
                selectedTest.getTestId(),
                remarks
            );
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Test request submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                remarksArea.setText("");
                patientCombo.setSelectedIndex(0);
                testCombo.setSelectedIndex(0);
                
                // Refresh table if we're on pending view
                if ("Pending".equals(currentFilter)) {
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to submit test request.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper class for patient combo box items
    private static class PatientItem {
        Patient patient;
        
        PatientItem(Patient patient) {
            this.patient = patient;
        }
        
        @Override
        public String toString() {
            return patient.getFullName() + " (ID: " + patient.getPatientId() + ")";
        }
    }
}
