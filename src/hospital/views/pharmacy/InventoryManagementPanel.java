package hospital.views.pharmacy;

import hospital.dao.*;
import hospital.models.*;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryManagementPanel extends JPanel {
    private User currentUser;
    private InventoryDAO inventoryDAO;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private Medicine selectedMedicine;

    public InventoryManagementPanel(User user) {
        this.currentUser = user;
        this.inventoryDAO = new InventoryDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Constants.BACKGROUND_COLOR);
        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Constants.BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Medicine Inventory Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Medicine Name", "Generic", "Category", "Stock", "Min Level", 
                           "Unit Price", "Expiry Date", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(28);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Color-code based on stock level
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 8);
                    if ("EXPIRED".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if ("LOW STOCK".equals(status)) {
                        c.setBackground(new Color(255, 243, 205));
                    } else if ("EXPIRING SOON".equals(status)) {
                        c.setBackground(new Color(255, 230, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectMedicine();
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Medicine List"));
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        JButton addBtn = new JButton("Add New Medicine");
        addBtn.setBackground(Constants.SUCCESS_COLOR);
        addBtn.setForeground(Color.BLACK);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(150, 38));
        addBtn.addActionListener(e -> showAddMedicineDialog());

        JButton updateStockBtn = new JButton("Update Stock");
        updateStockBtn.setBackground(Constants.PRIMARY_COLOR);
        updateStockBtn.setForeground(Color.BLACK);
        updateStockBtn.setFocusPainted(false);
        updateStockBtn.setPreferredSize(new Dimension(140, 38));
        updateStockBtn.addActionListener(e -> showUpdateStockDialog());
        
        JButton editBtn = new JButton("Edit Medicine");
        editBtn.setBackground(Constants.WARNING_COLOR);
        editBtn.setForeground(Color.BLACK);
        editBtn.setFocusPainted(false);
        editBtn.setPreferredSize(new Dimension(130, 38));
        editBtn.addActionListener(e -> showEditMedicineDialog());

        JButton lowStockBtn = new JButton("Check Low Stock");
        lowStockBtn.setBackground(Constants.DANGER_COLOR);
        lowStockBtn.setForeground(Color.BLACK);
        lowStockBtn.setFocusPainted(false);
        lowStockBtn.setPreferredSize(new Dimension(140, 38));
        lowStockBtn.addActionListener(e -> checkLowStock());
        
        JButton expiryBtn = new JButton("Check Expiry");
        expiryBtn.setBackground(new Color(230, 126, 34));
        expiryBtn.setForeground(Color.BLACK);
        expiryBtn.setFocusPainted(false);
        expiryBtn.setPreferredSize(new Dimension(130, 38));
        expiryBtn.addActionListener(e -> checkExpiry());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(Constants.SECONDARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setPreferredSize(new Dimension(100, 38));
        refreshBtn.addActionListener(e -> refreshData());

        buttonPanel.add(addBtn);
        buttonPanel.add(updateStockBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(lowStockBtn);
        buttonPanel.add(expiryBtn);
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        try {
            List<Medicine> medicines = inventoryDAO.getAllMedicines();
            displayInventory(medicines);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading inventory: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayInventory(List<Medicine> medicines) {
        tableModel.setRowCount(0);
        for (Medicine medicine : medicines) {
            String status = getStatus(medicine);
            Object[] row = {
                medicine.getMedicineId(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getCategory(),
                medicine.getStockQuantity(),
                medicine.getMinimumStockLevel(),
                String.format("₹%.2f", medicine.getUnitPrice()),
                medicine.getExpiryDate() != null ? medicine.getExpiryDate().toString() : "N/A",
                status
            };
            tableModel.addRow(row);
        }
    }
    
    private String getStatus(Medicine medicine) {
        if (medicine.isExpired()) {
            return "EXPIRED";
        } else if (medicine.isLowStock()) {
            return "LOW STOCK";
        } else if (medicine.isExpiringSoon()) {
            return "EXPIRING SOON";
        } else {
            return "OK";
        }
    }
    
    private void selectMedicine() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int medicineId = (Integer) tableModel.getValueAt(selectedRow, 0);
                selectedMedicine = inventoryDAO.getMedicineById(medicineId);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    // ==================== ADD NEW MEDICINE ====================
    private void showAddMedicineDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Add New Medicine", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JTextField genericField = new JTextField(20);
        JTextField manufacturerField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField stockField = new JTextField(20);
        JTextField minStockField = new JTextField(20);
        JTextField expiryField = new JTextField(20);
        expiryField.setToolTipText("Format: YYYY-MM-DD");
        JTextField batchField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        
        int row = 0;
        addFormField(panel, gbc, "Medicine Name:*", nameField, row++);
        addFormField(panel, gbc, "Generic Name:", genericField, row++);
        addFormField(panel, gbc, "Manufacturer:", manufacturerField, row++);
        addFormField(panel, gbc, "Category:", categoryField, row++);
        addFormField(panel, gbc, "Unit Price:*", priceField, row++);
        addFormField(panel, gbc, "Stock Quantity:*", stockField, row++);
        addFormField(panel, gbc, "Minimum Stock Level:", minStockField, row++);
        addFormField(panel, gbc, "Expiry Date (YYYY-MM-DD):", expiryField, row++);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(Constants.SUCCESS_COLOR);
        saveBtn.setForeground(Color.BLACK);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.BLACK);
        
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Medicine name is required!");
                    return;
                }
                
                Medicine medicine = new Medicine();
                medicine.setName(name);
                medicine.setGenericName(genericField.getText().trim());
                medicine.setManufacturer(manufacturerField.getText().trim());
                medicine.setCategory(categoryField.getText().trim());
                medicine.setUnitPrice(price);
                medicine.setStockQuantity(stock);
                medicine.setMinimumStockLevel(minStockField.getText().isEmpty() ? 10 : 
                    Integer.parseInt(minStockField.getText().trim()));
                
                if (!expiryField.getText().trim().isEmpty()) {
                    medicine.setExpiryDate(LocalDate.parse(expiryField.getText().trim()));
                }
                
                medicine.setBatchNumber(batchField.getText().trim());
                medicine.setStorageLocation(locationField.getText().trim());
                medicine.setCreatedAt(LocalDateTime.now());
                
                Medicine created = inventoryDAO.createMedicine(medicine);
                if (created != null) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Medicine added successfully!\nID: " + created.getMedicineId(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    // ==================== UPDATE STOCK ====================
    private void showUpdateStockDialog() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first!");
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Update Stock - " + selectedMedicine.getName(), true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel currentStockLabel = new JLabel("Current Stock: " + selectedMedicine.getStockQuantity());
        currentStockLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        JRadioButton addRadio = new JRadioButton("Add Stock", true);
        JRadioButton subtractRadio = new JRadioButton("Subtract Stock");
        ButtonGroup group = new ButtonGroup();
        group.add(addRadio);
        group.add(subtractRadio);
        
        JTextField quantityField = new JTextField(15);
        quantityField.setToolTipText("Enter quantity to add or subtract");
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(currentStockLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(addRadio, gbc);
        gbc.gridx = 1;
        panel.add(subtractRadio, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton updateBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");
        
        updateBtn.setBackground(Constants.PRIMARY_COLOR);
        updateBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        
        updateBtn.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be positive!");
                    return;
                }
                
                String operation = addRadio.isSelected() ? "ADD" : "SUBTRACT";
                
                boolean success = inventoryDAO.updateStock(
                    selectedMedicine.getMedicineId(), 
                    quantity, 
                    operation
                );
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Stock updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(updateBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ==================== EDIT MEDICINE ====================
    private void showEditMedicineDialog() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first!");
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Edit Medicine", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(selectedMedicine.getName(), 20);
        JTextField genericField = new JTextField(selectedMedicine.getGenericName(), 20);
        JTextField manufacturerField = new JTextField(selectedMedicine.getManufacturer(), 20);
        JTextField categoryField = new JTextField(selectedMedicine.getCategory(), 20);
        JTextField priceField = new JTextField(String.valueOf(selectedMedicine.getUnitPrice()), 20);
        JTextField minStockField = new JTextField(String.valueOf(selectedMedicine.getMinimumStockLevel()), 20);
        JTextField expiryField = new JTextField(
            selectedMedicine.getExpiryDate() != null ? selectedMedicine.getExpiryDate().toString() : "", 20);
        JTextField batchField = new JTextField(selectedMedicine.getBatchNumber(), 20);
        JTextField locationField = new JTextField(selectedMedicine.getStorageLocation(), 20);
        
        int row = 0;
        addFormField(panel, gbc, "Medicine Name:*", nameField, row++);
        addFormField(panel, gbc, "Generic Name:", genericField, row++);
        addFormField(panel, gbc, "Manufacturer:", manufacturerField, row++);
        addFormField(panel, gbc, "Category:", categoryField, row++);
        addFormField(panel, gbc, "Unit Price:*", priceField, row++);
        addFormField(panel, gbc, "Minimum Stock Level:", minStockField, row++);
        addFormField(panel, gbc, "Expiry Date (YYYY-MM-DD):", expiryField, row++);
        addFormField(panel, gbc, "Batch Number:", batchField, row++);
        addFormField(panel, gbc, "Storage Location:", locationField, row++);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(Constants.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        
        saveBtn.addActionListener(e -> {
            try {
                selectedMedicine.setName(nameField.getText().trim());
                selectedMedicine.setGenericName(genericField.getText().trim());
                selectedMedicine.setManufacturer(manufacturerField.getText().trim());
                selectedMedicine.setCategory(categoryField.getText().trim());
                selectedMedicine.setUnitPrice(Double.parseDouble(priceField.getText().trim()));
                selectedMedicine.setMinimumStockLevel(Integer.parseInt(minStockField.getText().trim()));
                
                if (!expiryField.getText().trim().isEmpty()) {
                    selectedMedicine.setExpiryDate(LocalDate.parse(expiryField.getText().trim()));
                }
                
                selectedMedicine.setBatchNumber(batchField.getText().trim());
                selectedMedicine.setStorageLocation(locationField.getText().trim());
                
                if (inventoryDAO.updateMedicine(selectedMedicine)) {
                    JOptionPane.showMessageDialog(dialog, "Medicine updated successfully!");
                    dialog.dispose();
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void checkLowStock() {
        try {
            List<Medicine> lowStock = inventoryDAO.getLowStockMedicines();
            if (lowStock.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "All medicines are adequately stocked!",
                    "Stock Check", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder msg = new StringBuilder("LOW STOCK ALERT:\n\n");
                for (Medicine m : lowStock) {
                    msg.append(String.format("%-30s: %d (Min: %d)\n", 
                        m.getName(), m.getStockQuantity(), m.getMinimumStockLevel()));
                }
                JOptionPane.showMessageDialog(this, msg.toString(), 
                    "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void checkExpiry() {
        try {
            List<Medicine> expiring = inventoryDAO.getExpiringMedicines();
            List<Medicine> expired = inventoryDAO.getExpiredMedicines();
            
            StringBuilder msg = new StringBuilder();
            
            if (!expired.isEmpty()) {
                msg.append("EXPIRED MEDICINES (Remove Immediately):\n");
                for (Medicine m : expired) {
                    msg.append(String.format("%-30s: %s\n", m.getName(), m.getExpiryDate()));
                }
                msg.append("\n");
            }
            
            if (!expiring.isEmpty()) {
                msg.append("EXPIRING SOON (Within 3 months):\n");
                for (Medicine m : expiring) {
                    msg.append(String.format("%-30s: %s\n", m.getName(), m.getExpiryDate()));
                }
            }
            
            if (expired.isEmpty() && expiring.isEmpty()) {
                msg.append("No medicines expiring soon!");
            }
            
            JOptionPane.showMessageDialog(this, msg.toString(), 
                "Expiry Check", 
                (!expired.isEmpty() ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}