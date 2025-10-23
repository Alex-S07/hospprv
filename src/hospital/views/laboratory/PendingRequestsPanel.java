package hospital.views.laboratory;

import hospital.dao.LabDAO;
import hospital.models.TestRequest;
import hospital.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel showing pending laboratory test requests.
 * Left: JTable with pending tests. Right: details panel with result submission.
 */
public class PendingRequestsPanel extends JPanel {
    private User currentUser;
    private LabDAO labDAO;

    // Left table showing pending requests
    private JTable requestsTable;
    private DefaultTableModel tableModel;

    // Right side: details and result entry
    private JTextArea requestDetailsArea;
    private JTextArea resultTextArea;
    private JButton submitResultBtn;
    
    private TestRequest selectedRequest;

    public PendingRequestsPanel(User user) {
        this.currentUser = user;
        this.labDAO = new LabDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Table columns: Request ID, Patient, Doctor, Test Name, Date
        String[] cols = {"Request ID", "Patient", "Doctor", "Test Name", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setRowHeight(25);
        requestsTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        // Details area (read-only)
        requestDetailsArea = new JTextArea();
        requestDetailsArea.setEditable(false);
        requestDetailsArea.setLineWrap(true);
        requestDetailsArea.setWrapStyleWord(true);
        requestDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Result input area
        resultTextArea = new JTextArea(6, 30);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        resultTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Submit button
        submitResultBtn = new JButton("Submit Result");
        submitResultBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        submitResultBtn.setBackground(new Color(34, 139, 34));
        submitResultBtn.setForeground(Color.WHITE);
        submitResultBtn.setEnabled(false);
    }

    private void setupLayout() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setDividerLocation(700);

        // Left panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Pending Test Requests"));

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Test Details & Result Entry"));

        // Details section
        JScrollPane detailsScroll = new JScrollPane(requestDetailsArea);
        detailsScroll.setPreferredSize(new Dimension(400, 150));

        // Result entry section
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Enter Test Result"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel instructionLabel = new JLabel("Enter the test result below:");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JScrollPane resultScroll = new JScrollPane(resultTextArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitResultBtn);

        resultPanel.add(instructionLabel, BorderLayout.NORTH);
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Combine in right panel
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setDividerLocation(200);
        rightSplit.setTopComponent(detailsScroll);
        rightSplit.setBottomComponent(resultPanel);

        rightPanel.add(rightSplit, BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);

        add(split, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // When selecting a row, load details
        requestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = requestsTable.getSelectedRow();
                if (row >= 0) {
                    int requestId = (Integer) tableModel.getValueAt(row, 0);
                    loadTestDetails(requestId);
                }
            }
        });

        // Submit result button
        submitResultBtn.addActionListener(e -> handleSubmitResult());
    }

    /**
     * Refresh data in the table from database.
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        selectedRequest = null;
        requestDetailsArea.setText("");
        resultTextArea.setText("");
        submitResultBtn.setEnabled(false);
        
        try {
            List<TestRequest> requests = labDAO.getLabRequests("Pending");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            for (TestRequest request : requests) {
                tableModel.addRow(new Object[]{
                    request.getRequestId(),
                    request.getPatientName(),
                    request.getDoctorName(),
                    request.getTestName(),
                    request.getRequestDate() != null ? dateFormat.format(request.getRequestDate()) : "N/A"
                });
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading pending requests: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTestDetails(int requestId) {
        try {
            selectedRequest = labDAO.getRequestDetails(requestId);
            
            if (selectedRequest != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                
                StringBuilder details = new StringBuilder();
                details.append("=== TEST REQUEST DETAILS ===\n\n");
                details.append("Request ID: ").append(selectedRequest.getRequestId()).append("\n");
                details.append("Patient: ").append(selectedRequest.getPatientName()).append("\n");
                details.append("Doctor: ").append(selectedRequest.getDoctorName()).append("\n");
                details.append("Test: ").append(selectedRequest.getTestName()).append("\n");
                details.append("Requested On: ").append(
                    selectedRequest.getRequestDate() != null ? 
                    dateFormat.format(selectedRequest.getRequestDate()) : "N/A"
                ).append("\n");
                details.append("Status: ").append(selectedRequest.getStatus()).append("\n");
                
                if (selectedRequest.getRemarks() != null && !selectedRequest.getRemarks().isEmpty()) {
                    details.append("\nDoctor's Remarks:\n").append(selectedRequest.getRemarks()).append("\n");
                }
                
                requestDetailsArea.setText(details.toString());
                submitResultBtn.setEnabled(true);
                resultTextArea.setText("");
                resultTextArea.requestFocus();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading test details: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmitResult() {
        if (selectedRequest == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a test request first.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String resultText = resultTextArea.getText().trim();
        if (resultText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the test result before submitting.", 
                "Empty Result", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to submit this result?\n\n" +
            "This will mark the test as completed.",
            "Confirm Submission", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            boolean success = labDAO.submitTestResult(
                selectedRequest.getRequestId(), 
                resultText
            );
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Result submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear selection and refresh
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to submit result. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error submitting result: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
