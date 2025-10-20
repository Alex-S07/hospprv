package hospital.views.laboratory;

import hospital.models.User;
import hospital.utils.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel showing pending laboratory test requests. Left: JTable list. Right: details and result entry.
 */
public class PendingRequestsPanel extends JPanel {
    private User currentUser;

    // Left table showing pending requests
    private JTable requestsTable;
    private DefaultTableModel tableModel;

    // Right side: details and result entry
    private JPanel detailsPanel;
    private JTextArea requestDetailsArea;
    private JTextField resultField;
    private JButton submitResultsBtn;

    public PendingRequestsPanel(User user) {
        this.currentUser = user;
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

        // Details panel
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BorderLayout(8, 8));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        requestDetailsArea = new JTextArea();
        requestDetailsArea.setEditable(false);
        requestDetailsArea.setLineWrap(true);
        requestDetailsArea.setWrapStyleWord(true);

        JPanel resultEntryPanel = new JPanel(new BorderLayout(5,5));
        resultField = new JTextField();
        submitResultsBtn = new JButton("Submit Results");

        resultEntryPanel.add(new JLabel("Result:"), BorderLayout.NORTH);
        resultEntryPanel.add(resultField, BorderLayout.CENTER);
        resultEntryPanel.add(submitResultsBtn, BorderLayout.SOUTH);

        detailsPanel.add(new JScrollPane(requestDetailsArea), BorderLayout.CENTER);
        detailsPanel.add(resultEntryPanel, BorderLayout.SOUTH);
    }

    private void setupLayout() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Pending Requests"));

        detailsPanel.setBorder(BorderFactory.createTitledBorder("Request Details"));

        split.setLeftComponent(leftPanel);
        split.setRightComponent(detailsPanel);

        add(split, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // When selecting a row, load details into the right panel
        requestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = requestsTable.getSelectedRow();
                if (row >= 0) {
                    String requestId = tableModel.getValueAt(row, 0).toString();
                    // TODO: load details from DB using requestId
                    requestDetailsArea.setText("Details for request: " + requestId + "\n\n(Load from DB here)");
                }
            }
        });

        submitResultsBtn.addActionListener(e -> {
            int row = requestsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a request first.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String requestId = tableModel.getValueAt(row, 0).toString();
            String resultText = resultField.getText().trim();
            if (resultText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter result details before submitting.", "Empty result", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // TODO: Submit result to DB for requestId and update status
            JOptionPane.showMessageDialog(this, "Results submitted for request " + requestId, "Submitted", JOptionPane.INFORMATION_MESSAGE);

            // TODO: refresh table after submission
        });
    }

    /**
     * Refresh data in the table. This should query the DB for pending lab requests.
     */
    public void refreshData() {
        // TODO: Load pending requests from DB and populate tableModel
        // For now, populate with placeholder rows
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"REQ-001", "John Doe", "Dr. Smith", "CBC", "2025-10-18"});
        tableModel.addRow(new Object[]{"REQ-002", "Jane Roe", "Dr. Lee", "Lipid Panel", "2025-10-19"});
    }
}
