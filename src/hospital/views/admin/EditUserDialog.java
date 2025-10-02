package hospital.views.admin;


import hospital.dao.UserDAO;
import hospital.models.User;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class EditUserDialog extends JDialog {
    private UserDAO userDAO;
    private int userId;
    private JTextField usernameField;
    private JTextField emailField;
    private JComboBox<String> roleCombo;
    private JCheckBox activeCheckbox;
    private boolean success = false;
    
    public EditUserDialog(Frame parent, UserDAO userDAO, int userId) {
        super(parent, "Edit User", true);
        this.userDAO = userDAO;
        this.userId = userId;
        initComponents();
        setupLayout();
        loadUserData();
        setSize(450, 350);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        
        String[] roles = {"ADMIN", "DOCTOR", "RECEPTIONIST", "PHARMACY"};
        roleCombo = new JComboBox<>(roles);
        
        activeCheckbox = new JCheckBox("Active");
        activeCheckbox.setSelected(true);
    }
    
    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        addField(panel, gbc, "Username:", usernameField, row++);
        addField(panel, gbc, "Email:", emailField, row++);
        addField(panel, gbc, "Role:", roleCombo, row++);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(activeCheckbox, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(new Color(41, 128, 185));
        saveBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        
        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridy = row;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
    
    private void loadUserData() {
        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                roleCombo.setSelectedItem(user.getRole());
                activeCheckbox.setSelected(user.isActive());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage());
        }
    }
    
    private void handleSave() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        boolean isActive = activeCheckbox.isSelected();
        
        if (!ValidationUtil.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters!");
            return;
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address!");
            return;
        }
        
        try {
            User user = new User();
            user.setUserId(userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            user.setActive(isActive);
            
            if (userDAO.updateUser(user)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}
