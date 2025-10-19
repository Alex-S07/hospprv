package hospital.views.pharmacy;

import hospital.dao.*;
import hospital.models.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

class PrescriptionDispensePanel extends JPanel {
    private User currentUser;
    private PrescriptionDAO prescriptionDAO;
    private InventoryDAO inventoryDAO;

    private JTable prescriptionTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JTextField searchField;
    private JLabel totalBillLabel;

    private List<Prescription> currentPrescriptions;
    private double totalBill = 0.0;

    public PrescriptionDispensePanel(User user) {
        this.currentUser = user;
        this.prescriptionDAO = new PrescriptionDAO();
        this.inventoryDAO = new InventoryDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Pending Prescriptions");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search Patient:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchPrescriptions());
        searchPanel.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        searchPanel.add(refreshBtn);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);

        // Left - Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Today's Prescriptions"));

        String[] columns = { "Record ID", "Patient", "Doctor", "Medicines", "Date", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        prescriptionTable = new JTable(tableModel);
        prescriptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prescriptionTable.setRowHeight(30);
        prescriptionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPrescriptionDetails();
            }
        });

        tablePanel.add(new JScrollPane(prescriptionTable), BorderLayout.CENTER);
        splitPane.setLeftComponent(tablePanel);

        // Right - Details
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(new TitledBorder("Prescription Details"));

        detailsArea = new JTextArea(15, 30);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setText("Select a prescription to view details...");
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        // Bill panel
        JPanel billPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        billPanel.setBackground(new Color(255, 243, 205));
        billPanel.setBorder(new LineBorder(new Color(255, 193, 7), 2));
        JLabel billTitleLabel = new JLabel("Total Bill: ");
        billTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalBillLabel = new JLabel("₹0.00");
        totalBillLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        totalBillLabel.setForeground(new Color(230, 126, 34));
        billPanel.add(billTitleLabel);
        billPanel.add(totalBillLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(billPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton dispenseBtn = new JButton("Dispense & Update Stock");
        dispenseBtn.setBackground(new Color(46, 204, 113));
        dispenseBtn.setForeground(Color.BLUE);
        dispenseBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        dispenseBtn.addActionListener(e -> dispensePrescription());
        actionPanel.add(dispenseBtn);

        JButton printBtn = new JButton("Print Bill");
        printBtn.setForeground(Color.BLACK);
        printBtn.addActionListener(e -> printPrescription());
        actionPanel.add(printBtn);

        buttonPanel.add(actionPanel, BorderLayout.CENTER);
        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(detailsPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            List<Prescription> prescriptions = prescriptionDAO.getTodaysPrescriptions();
            displayPrescriptions(prescriptions);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading prescriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Populates the TABLE with prescription records
     */
    private void displayPrescriptions(List<Prescription> prescriptions) {
        tableModel.setRowCount(0);

        // Group by record_id and filter only Pending
        java.util.Map<Integer, java.util.List<Prescription>> groupedPrescriptions = new java.util.HashMap<>();
        for (Prescription p : prescriptions) {
            // Only show pending prescriptions
            if ("Pending".equalsIgnoreCase(p.getStatus())) {
                groupedPrescriptions.computeIfAbsent(p.getRecordId(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        for (java.util.Map.Entry<Integer, java.util.List<Prescription>> entry : groupedPrescriptions.entrySet()) {
            List<Prescription> recordPrescriptions = entry.getValue();
            if (!recordPrescriptions.isEmpty()) {
                Prescription first = recordPrescriptions.get(0);

                StringBuilder medicines = new StringBuilder();
                for (int i = 0; i < recordPrescriptions.size(); i++) {
                    String medName = recordPrescriptions.get(i).getMedicineName();
                    medicines.append(medName != null ? medName : "Unknown");
                    if (i < recordPrescriptions.size() - 1)
                        medicines.append(", ");
                }

                Object[] row = {
                    first.getRecordId(),
                    first.getPatientName() != null ? first.getPatientName() : "N/A",
                    first.getDoctorName() != null ? first.getDoctorName() : "N/A",
                    medicines.toString(),
                    first.getCreatedAt() != null ? 
                        first.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A",
                    first.getStatus()
                };
                tableModel.addRow(row);
            }
        }
    }

    private void loadPrescriptionDetails() {
        int selectedRow = prescriptionTable.getSelectedRow();
        if (selectedRow < 0) {
            detailsArea.setText("Select a prescription to view details...");
            totalBillLabel.setText("₹0.00");
            return;
        }

        try {
            int recordId = (Integer) tableModel.getValueAt(selectedRow, 0);
            List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByRecordId(recordId);

            if (!prescriptions.isEmpty()) {
                currentPrescriptions = prescriptions;
                displayPrescriptionDetails(prescriptions);
            } else {
                detailsArea.setText("No prescription details found.");
                totalBillLabel.setText("₹0.00");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading details: " + e.getMessage());
        }
    }

    /**
     * Displays DETAILED billing information in the text area
     */
    private void displayPrescriptionDetails(List<Prescription> prescriptions) {
        StringBuilder details = new StringBuilder();
        details.append("══════════════════════════════════════════════════════════════════════════════════════════════\n");
        details.append("                          PRESCRIPTION & BILLING DETAILS\n");
        details.append("══════════════════════════════════════════════════════════════════════════════════════════════\n\n");

        totalBill = 0.0;

        if (!prescriptions.isEmpty()) {
            Prescription first = prescriptions.get(0);
            details.append( "       Patient: ").append(first.getPatientName()).append("\n");
            details.append("       Doctor: ").append(first.getDoctorName()).append("\n");
            details.append("       Date: ").append(first.getCreatedAt().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n");
            details.append("       Status: ").append(first.getStatus()).append("\n\n");
        }

        details.append("                            MEDICINES & BILLING:\n");
        details.append("──────────────────────────────────────────────────────────────────────────────────────────────\n\n");

        for (int i = 0; i < prescriptions.size(); i++) {
            Prescription p = prescriptions.get(i);
            String medName = p.getMedicineName();

            details.append((i + 1)).append(". ").append(medName).append("\n");

            try {
                Medicine medicine = inventoryDAO.getMedicineByName(medName);
                if (medicine != null) {
                    int quantity = p.getQuantity();
                    double price = medicine.getUnitPrice();
                    double itemTotal = price * quantity;
                    totalBill += itemTotal;

                    details.append("   Quantity Required: ").append(quantity).append(" units\n");
                    details.append("   Unit Price: ₹").append(String.format("%.2f", price)).append("\n");
                    details.append("   Subtotal: ₹").append(String.format("%.2f", itemTotal)).append("\n");

                    if (medicine.getStockQuantity() < quantity) {
                        details.append("   ⚠ WARNING: Insufficient stock! ");
                        details.append("(Need: ").append(quantity);
                        details.append(", Available: ").append(medicine.getStockQuantity()).append(")\n");
                    } else {
                        details.append("   ✓ Stock Available: ").append(medicine.getStockQuantity()).append(" units\n");
                    }
                } else {
                    details.append("   ⚠ Medicine not found in inventory\n");
                }
            } catch (SQLException e) {
                details.append("   ⚠ Error fetching price\n");
            }

            details.append("   Frequency: ").append(p.getFrequency()).append("\n");
            details.append("   Duration: ").append(p.getDuration()).append("\n");

            if (p.getInstructions() != null && !p.getInstructions().isEmpty()) {
                details.append("   Instructions: ").append(p.getInstructions()).append("\n");
            }
            details.append("\n");
        }

        details.append("══════════════════════════════════════════════════════════════════════════════════════════════\n");
        details.append(String.format("%-30s ₹%.2f\n", "             TOTAL BILL:", totalBill));
        details.append("══════════════════════════════════════════════════════════════════════════════════════════════\n");

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
        totalBillLabel.setText("₹" + String.format("%.2f", totalBill));
    }

    private void dispensePrescription() {
        if (currentPrescriptions == null || currentPrescriptions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        // Check stock
        StringBuilder stockWarnings = new StringBuilder();
        boolean hasStockIssues = false;

        for (Prescription p : currentPrescriptions) {
            try {
                Medicine medicine = inventoryDAO.getMedicineByName(p.getMedicineName());
                if (medicine != null) {
                    int quantity = p.getQuantity();
                    if (medicine.getStockQuantity() < quantity) {
                        hasStockIssues = true;
                        stockWarnings.append("- ").append(p.getMedicineName())
                                .append(" (Need: ").append(quantity)
                                .append(", Available: ").append(medicine.getStockQuantity())
                                .append(")\n");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (hasStockIssues) {
            int proceed = JOptionPane.showConfirmDialog(this,
                    "Stock Warning:\n" + stockWarnings.toString() + "\nProceed anyway?",
                    "Low Stock Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (proceed != JOptionPane.YES_OPTION)
                return;
        }

        String message = String.format(
                "Dispense prescription and update stock?\n\nTotal Bill: ₹%.2f", totalBill);

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Dispense",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Update stock
                for (Prescription p : currentPrescriptions) {
                    Medicine medicine = inventoryDAO.getMedicineByName(p.getMedicineName());
                    if (medicine != null) {
                        int quantity = p.getQuantity();
                        int newStock = Math.max(0, medicine.getStockQuantity() - quantity);
                        inventoryDAO.updateMedicineStock(medicine.getMedicineId(), newStock);
                    }
                }

                // Mark as dispensed
                int recordId = currentPrescriptions.get(0).getRecordId();
                prescriptionDAO.markAsDispensed(recordId, currentUser.getUserId());

                // Generate bill
                String bill = generateBillText();

                JOptionPane.showMessageDialog(this,
                        String.format("✓ Prescription dispensed successfully!\n\nTotal: ₹%.2f\nStock updated.",
                                totalBill),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Show print dialog
                int print = JOptionPane.showConfirmDialog(this,
                        "Would you like to print the bill?",
                        "Print Bill",
                        JOptionPane.YES_NO_OPTION);

                if (print == JOptionPane.YES_OPTION) {
                    showPrintDialog(bill);
                }

                refreshData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating stock: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private String generateBillText() {
        StringBuilder bill = new StringBuilder();
        bill.append("\n");
        bill.append("══════════════════════════════════════════════════════════════════════════════════════════════\n");
        bill.append("                   PHARMACY BILLING RECEIPT\n");
        bill.append("                   Hospital Management System\n");
        bill.append("══════════════════════════════════════════════════════════════════════════════════════════════\n\n");

        if (!currentPrescriptions.isEmpty()) {
            Prescription first = currentPrescriptions.get(0);
            bill.append("         Patient: ").append(first.getPatientName()).append("\n");
            bill.append("         Doctor: ").append(first.getDoctorName()).append("\n");
            bill.append("         Date: ").append(java.time.LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n");
            bill.append("         Pharmacist: ").append(currentUser.getUsername()).append("\n");
            bill.append("         Record ID: ").append(first.getRecordId()).append("\n\n");
        }

        bill.append("PRESCRIPTION ITEMS:\n");
        bill.append("──────────────────────────────────────────────────────────────────────────────────────────────\n");
        bill.append(String.format("%-25s %5s %10s %10s\n", "Medicine", "Qty", "Price", "Total"));
        bill.append("──────────────────────────────────────────────────────────────────────────────────────────────\n");

        double grandTotal = 0.0;

        for (Prescription p : currentPrescriptions) {
            try {
                Medicine medicine = inventoryDAO.getMedicineByName(p.getMedicineName());
                if (medicine != null) {
                    int quantity = p.getQuantity();
                    double price = medicine.getUnitPrice();
                    double itemTotal = price * quantity;
                    grandTotal += itemTotal;

                    String medName = p.getMedicineName();
                    if (medName.length() > 23)
                        medName = medName.substring(0, 23);

                    bill.append(String.format("%-25s %5d ₹%9.2f ₹%9.2f\n",
                            medName, quantity, price, itemTotal));

                    bill.append(String.format("  Frequency: %s | Duration: %s\n",
                            p.getFrequency(), p.getDuration()));

                    if (p.getInstructions() != null && !p.getInstructions().isEmpty()) {
                        bill.append("  Instructions: ").append(p.getInstructions()).append("\n");
                    }
                    bill.append("\n");
                }
            } catch (SQLException e) {
                bill.append(p.getMedicineName()).append(" - Error fetching price\n");
            }
        }

        bill.append("──────────────────────────────────────────────────────────────────────────────────────────────\n");
        bill.append(String.format("%-41s ₹%9.2f\n", "GRAND TOTAL:", grandTotal));
        bill.append("══════════════════════════════════════════════════════════════════════════════════════════════\n\n");

        bill.append("IMPORTANT INSTRUCTIONS:\n");
        bill.append("• Take medicines as prescribed by your doctor\n");
        bill.append("• Store in a cool, dry place away from sunlight\n");
        bill.append("• Keep out of reach of children\n");
        bill.append("• Complete the full course even if you feel better\n");
        bill.append("• Contact your doctor if you experience side effects\n\n");

        bill.append("Thank you for your visit!\n");
        bill.append("For queries, contact: pharmacy@hospital.com\n\n");

        return bill.toString();
    }

    private void showPrintDialog(String billText) {
        JTextArea printArea = new JTextArea(billText);
        printArea.setEditable(false);
        printArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(printArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton copyBtn = new JButton("Copy to Clipboard");
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(billText);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
            JOptionPane.showMessageDialog(null, "Bill copied to clipboard!");
        });
        buttonPanel.add(copyBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Print Bill", JOptionPane.PLAIN_MESSAGE);
    }

    private void printPrescription() {
        if (currentPrescriptions == null || currentPrescriptions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        String bill = generateBillText();
        showPrintDialog(bill);
    }

    private void searchPrescriptions() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            refreshData();
            return;
        }

        try {
            List<Prescription> allPrescriptions = prescriptionDAO.getTodaysPrescriptions();
            List<Prescription> filtered = new java.util.ArrayList<>();

            for (Prescription p : allPrescriptions) {
                if (p.getPatientName() != null &&
                        p.getPatientName().toLowerCase().contains(searchText.toLowerCase())) {
                    filtered.add(p);
                }
            }

            displayPrescriptions(filtered);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }
}