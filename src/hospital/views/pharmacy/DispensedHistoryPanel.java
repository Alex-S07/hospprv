package hospital.views.pharmacy;

import hospital.dao.*;
import hospital.models.*;
import hospital.utils.Constants;
import hospital.views.common.BaseFrame;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DispensedHistoryPanel extends JPanel {
    private User currentUser;
    private PrescriptionDAO prescriptionDAO;
    private InventoryDAO inventoryDAO;

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;

    public DispensedHistoryPanel(User user) {
        this.currentUser = user;
        this.prescriptionDAO = new PrescriptionDAO();
        this.inventoryDAO = new InventoryDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initializeComponents();
    }

    private void initializeComponents() {
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Dispensed History");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);

        // Left - History table
        String[] columns = {"Record ID", "Date", "Time", "Patient", "Doctor", "Medicines", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(25);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDispensedDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        splitPane.setLeftComponent(scrollPane);

        // Right - Details
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Dispensed Details"));
        
        detailsArea = new JTextArea(20, 30);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setText("Select a record to view details...");
        
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        splitPane.setRightComponent(detailsPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            List<Prescription> dispensedList = prescriptionDAO.getDispensedHistory();
            displayHistory(dispensedList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayHistory(List<Prescription> prescriptions) {
        tableModel.setRowCount(0);

        // Group by record_id
        java.util.Map<Integer, java.util.List<Prescription>> groupedPrescriptions = new java.util.HashMap<>();
        for (Prescription p : prescriptions) {
            groupedPrescriptions.computeIfAbsent(p.getRecordId(), k -> new java.util.ArrayList<>()).add(p);
        }

        for (java.util.Map.Entry<Integer, java.util.List<Prescription>> entry : groupedPrescriptions.entrySet()) {
            List<Prescription> recordPrescriptions = entry.getValue();
            if (!recordPrescriptions.isEmpty()) {
                Prescription first = recordPrescriptions.get(0);

                // Build medicines list
                StringBuilder medicines = new StringBuilder();
                double total = 0.0;
                
                for (int i = 0; i < recordPrescriptions.size(); i++) {
                    Prescription p = recordPrescriptions.get(i);
                    medicines.append(p.getMedicineName());
                    if (i < recordPrescriptions.size() - 1) {
                        medicines.append(", ");
                    }
                    
                    // Calculate total
                    try {
                        Medicine med = inventoryDAO.getMedicineByName(p.getMedicineName());
                        if (med != null) {
                            total += med.getUnitPrice() * p.getQuantity();
                        }
                    } catch (SQLException e) {
                        // Ignore
                    }
                }

                Object[] row = {
                    first.getRecordId(),
                    first.getDispensedAt() != null ? 
                        first.getDispensedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "N/A",
                    first.getDispensedAt() != null ? 
                        first.getDispensedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A",
                    first.getPatientName(),
                    first.getDoctorName(),
                    medicines.toString(),
                    String.format("₹%.2f", total)
                };
                tableModel.addRow(row);
            }
        }
    }

    private void showDispensedDetails() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0) return;

        try {
            int recordId = (Integer) tableModel.getValueAt(selectedRow, 0);
            List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByRecordId(recordId);

            if (!prescriptions.isEmpty()) {
                displayDetails(prescriptions);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayDetails(List<Prescription> prescriptions) {
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════════════════\n");
        details.append("       DISPENSED PRESCRIPTION DETAILS\n");
        details.append("═══════════════════════════════════════════════\n\n");

        if (!prescriptions.isEmpty()) {
            Prescription first = prescriptions.get(0);
            details.append("Patient: ").append(first.getPatientName()).append("\n");
            details.append("Doctor: ").append(first.getDoctorName()).append("\n");
            details.append("Prescribed: ").append(first.getCreatedAt().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n");
            details.append("Dispensed: ").append(first.getDispensedAt() != null ? 
                first.getDispensedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "N/A").append("\n");
            details.append("Status: ").append(first.getStatus()).append("\n\n");
        }

        details.append("MEDICINES:\n");
        details.append("───────────────────────────────────────────────\n\n");

        double total = 0.0;
        for (int i = 0; i < prescriptions.size(); i++) {
            Prescription p = prescriptions.get(i);
            details.append((i + 1)).append(". ").append(p.getMedicineName()).append("\n");
            
            try {
                Medicine medicine = inventoryDAO.getMedicineByName(p.getMedicineName());
                if (medicine != null) {
                    int quantity = p.getQuantity();
                    double price = medicine.getUnitPrice();
                    double itemTotal = price * quantity;
                    total += itemTotal;
                    
                    details.append("   Quantity: ").append(quantity).append(" units\n");
                    details.append("   Unit Price: ₹").append(String.format("%.2f", price)).append("\n");
                    details.append("   Subtotal: ₹").append(String.format("%.2f", itemTotal)).append("\n");
                }
            } catch (SQLException e) {
                details.append("   Price: N/A\n");
            }
            
            details.append("   Frequency: ").append(p.getFrequency()).append(" times/day\n");
            details.append("   Duration: ").append(p.getDuration()).append(" days\n\n");
        }

        details.append("═══════════════════════════════════════════════\n");
        details.append(String.format("%-30s ₹%.2f\n", "TOTAL:", total));
        details.append("═══════════════════════════════════════════════\n");

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
    }
}