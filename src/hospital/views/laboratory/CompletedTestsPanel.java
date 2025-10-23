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
 * Panel showing completed laboratory test requests with read-only results.
 */
public class CompletedTestsPanel extends JPanel {
    private User currentUser;
    private LabDAO labDAO;

    // Left table showing completed requests
    private JTable requestsTable;
    private DefaultTableModel tableModel;

    // Right side: read-only details
    private JTextArea requestDetailsArea;

    public CompletedTestsPanel(User user) {
        this.currentUser = user;
        this.labDAO = new LabDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Table columns: Request ID, Patient, Doctor, Test Name, Date Completed
        String[] cols = {"Request ID", "Patient", "Doctor", "Test Name", "Date Completed"};
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

        requestDetailsArea = new JTextArea();
        requestDetailsArea.setEditable(false);
        requestDetailsArea.setLineWrap(true);
        requestDetailsArea.setWrapStyleWord(true);
        requestDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private void setupLayout() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setDividerLocation(700);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Completed Tests"));

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Test Results (Read-Only)"));
        rightPanel.add(new JScrollPane(requestDetailsArea), BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);

        add(split, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // When selecting a row, load details into the right panel
        requestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = requestsTable.getSelectedRow();
                if (row >= 0) {
                    int requestId = (int) tableModel.getValueAt(row, 0);
                    loadTestDetails(requestId);
                }
            }
        });
    }

    /**
     * Refresh data in the table. Queries DB for completed lab requests.
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        
        try {
            List<TestRequest> requests = labDAO.getLabRequests("Completed");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            for (TestRequest request : requests) {
                Object[] row = {
                    request.getRequestId(),
                    request.getPatientName(),
                    request.getDoctorName(),
                    request.getTestName(),
                    request.getCompletedDate() != null ? dateFormat.format(request.getCompletedDate()) : "N/A"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading completed tests: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTestDetails(int requestId) {
        try {
            TestRequest request = labDAO.getRequestDetails(requestId);
            
            if (request == null) {
                requestDetailsArea.setText("Test request not found.");
                return;
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            StringBuilder details = new StringBuilder();
            
            details.append("═══════════════════════════════════════════════════════\n");
            details.append("                 TEST RESULTS - COMPLETED\n");
            details.append("═══════════════════════════════════════════════════════\n\n");
            
            details.append("REQUEST INFORMATION:\n");
            details.append("───────────────────────────────────────────────────────\n");
            details.append("Request ID: ").append(request.getRequestId()).append("\n");
            details.append("Patient: ").append(request.getPatientName()).append("\n");
            details.append("Test: ").append(request.getTestName()).append("\n");
            details.append("Requested By: Dr. ").append(getDoctorName(request)).append("\n");
            details.append("Request Date: ").append(dateFormat.format(request.getRequestDate())).append("\n");
            
            if (request.getCompletedDate() != null) {
                details.append("Completed Date: ").append(dateFormat.format(request.getCompletedDate())).append("\n");
            }
            details.append("\n");
            
            if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
                details.append("DOCTOR'S REMARKS:\n");
                details.append("───────────────────────────────────────────────────────\n");
                details.append(request.getRemarks()).append("\n\n");
            }
            
            details.append("═══════════════════════════════════════════════════════\n");
            details.append("                    LAB RESULTS\n");
            details.append("═══════════════════════════════════════════════════════\n\n");
            
            if (request.getResult() != null && !request.getResult().isEmpty()) {
                details.append(request.getResult()).append("\n");
            } else {
                details.append("No result recorded.\n");
            }
            
            requestDetailsArea.setText(details.toString());
            requestDetailsArea.setCaretPosition(0);
            
        } catch (SQLException e) {
            e.printStackTrace();
            requestDetailsArea.setText("Error loading test details: " + e.getMessage());
        }
    }

    private String getDoctorName(TestRequest request) {
        return request.getDoctorName() != null ? request.getDoctorName() : "Unknown";
    }
}
