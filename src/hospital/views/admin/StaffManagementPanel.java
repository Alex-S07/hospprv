package hospital.views.admin;

import hospital.dao.UserDAO;
import hospital.dao.DoctorDAO;
import hospital.models.User;
import hospital.models.Doctor;
import hospital.utils.Constants;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

class StaffManagementPanel extends JPanel {
    private User currentUser;
    private UserDAO userDAO;
    private DoctorDAO doctorDAO;
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> staffTypeCombo;
    private JButton addStaffBtn;
    private JButton editStaffBtn;
    private JButton deleteStaffBtn;
    private JButton refreshBtn;
    private JButton changePasswordBtn;
    
    public StaffManagementPanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.doctorDAO = new DoctorDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadStaff();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Staff type filter
        String[] staffTypes = {"Users Only"};
        staffTypeCombo = new JComboBox<>(staffTypes);
        staffTypeCombo.setPreferredSize(new Dimension(150, 35));
        
        // Table setup
        String[] columns = {"ID", "Type", "Name", "Email", "Phone", "Role/Specialization", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staffTable.setRowHeight(28);
        staffTable.getTableHeader().setReorderingAllowed(false);
        staffTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        
        // Buttons
        addStaffBtn = new JButton("Add Staff");
        editStaffBtn = new JButton("Edit Staff");
        deleteStaffBtn = new JButton("Delete Staff");
        changePasswordBtn = new JButton("Change Password");
        refreshBtn = new JButton("Refresh");
        
        styleButton(addStaffBtn, Constants.SUCCESS_COLOR);
        styleButton(editStaffBtn, Constants.PRIMARY_COLOR);
        styleButton(deleteStaffBtn, Constants.DANGER_COLOR);
        styleButton(changePasswordBtn, Constants.WARNING_COLOR);
        styleButton(refreshBtn, Constants.SECONDARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Constants.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Constants.BACKGROUND_COLOR);
        filterPanel.add(new JLabel("View:"));
        filterPanel.add(staffTypeCombo);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(filterPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
        buttonPanel.add(addStaffBtn);
        buttonPanel.add(editStaffBtn);
        buttonPanel.add(deleteStaffBtn);
        buttonPanel.add(changePasswordBtn);
        buttonPanel.add(refreshBtn);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addStaffBtn.addActionListener(e -> showAddStaffDialog());
        editStaffBtn.addActionListener(e -> showEditStaffDialog());
        deleteStaffBtn.addActionListener(e -> deleteSelectedStaff());
        changePasswordBtn.addActionListener(e -> changePassword());
        refreshBtn.addActionListener(e -> loadStaff());
        staffTypeCombo.addActionListener(e -> loadStaff());
    }
    
    private void loadStaff() {
        String staffType = (String) staffTypeCombo.getSelectedItem();
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tableModel.setRowCount(0);
                
                if ("All Staff".equals(staffType) || "Users Only".equals(staffType)) {
                    loadUsers();
                }
                
                // if ("All Staff".equals(staffType) || "Doctors".equals(staffType)) {
                //     loadDoctors();
                // }
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StaffManagementPanel.this,
                        "Error loading staff: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void loadUsers() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        for (User user : users) {
            Object[] row = {
                user.getUserId(),
                "User",
                user.getUsername(),
                user.getEmail(),
                "-",
                user.getRole(),
                user.isActive() ? "Active" : "Inactive"
            };
            tableModel.addRow(row);
        }
    }
    
    // private void loadDoctors() throws SQLException {
    //     List<Doctor> doctors = doctorDAO.getAllDoctors();
    //     for (Doctor doctor : doctors) {
    //         Object[] row = {
    //             doctor.getDoctorId(),
    //             "Doctor",
    //             doctor.getUsername(),
    //             doctor.getEmail(),
    //             doctor.getPhone(),
    //             doctor.getSpecialization(),
    //             doctor.isAvailable() ? "Available" : "Unavailable"
    //         };
    //         tableModel.addRow(row);
    //     }
    // }
    
    private void showAddStaffDialog() {
        String[] options = {"Add User", "Add Doctor"};
        int choice = JOptionPane.showOptionDialog(this,
            "What type of staff do you want to add?",
            "Add Staff",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        
        if (choice == 0) {
            showAddUserDialog();
        } else if (choice == 1) {
            showAddDoctorDialog();
        }
    }
    
    private void showAddUserDialog() {
        AddUserDialog dialog = new AddUserDialog((Frame) SwingUtilities.getWindowAncestor(this), userDAO);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadStaff();
        }
    }
    
    private void showAddDoctorDialog() {
        AddDoctorDialog dialog = new AddDoctorDialog((Frame) SwingUtilities.getWindowAncestor(this), doctorDAO, userDAO);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadStaff();
        }
    }
    
    private void showEditStaffDialog() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to edit!");
            return;
        }
        
        String type = (String) tableModel.getValueAt(selectedRow, 1);
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        
        if ("User".equals(type)) {
            EditUserDialog dialog = new EditUserDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), userDAO, id);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadStaff();
            }
        } else if ("Doctor".equals(type)) {
            EditDoctorDialog dialog = new EditDoctorDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), doctorDAO, id);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadStaff();
            }
        }
    }
    
    private void deleteSelectedStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to delete!");
            return;
        }
        
        String type = (String) tableModel.getValueAt(selectedRow, 1);
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);
        
        if ("User".equals(type) && id == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + type + ": " + name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success;
                if ("User".equals(type)) {
                    success = userDAO.deleteUser(id);
                } else {
                    success = doctorDAO.deleteDoctor(id);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Staff member deleted successfully!");
                    loadStaff();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete staff member!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting staff: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void changePassword() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to change password!");
            return;
        }
        
        String type = (String) tableModel.getValueAt(selectedRow, 1);
        if (!"User".equals(type)) {
            JOptionPane.showMessageDialog(this, "Password can only be changed for Users!");
            return;
        }
        
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 2);
        
        ChangePasswordDialog dialog = new ChangePasswordDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), userDAO, userId, username);
        dialog.setVisible(true);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 38));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    }
    
    public void refreshData() {
        loadStaff();
    }
   
}