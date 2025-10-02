package hospital.views.admin;


import hospital.dao.UserDAO;
import hospital.models.User;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class AddUserDialog extends JDialog {
    private UserDAO userDAO;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JComboBox<String> roleCombo;
    private boolean success = false;
    
    public AddUserDialog(Frame parent, UserDAO userDAO) {
        super(parent, "Add New User", true);
        this.userDAO = userDAO;
        initComponents();
        setupLayout();
        setSize(450, 400);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        emailField = new JTextField(20);
        
        String[] roles = {"ADMIN", "RECEPTIONIST", "PHARMACY"};
        roleCombo = new JComboBox<>(roles);
    }
    
    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        addField(panel, gbc, "Username:", usernameField, row++);
        addField(panel, gbc, "Password:", passwordField, row++);
        addField(panel, gbc, "Confirm Password:", confirmPasswordField, row++);
        addField(panel, gbc, "Email:", emailField, row++);
        addField(panel, gbc, "Role:", roleCombo, row++);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(new Color(39, 174, 96));
        saveBtn.setForeground(Color.BLACK);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.BLACK);
        
        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
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
    
    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        
        if (!ValidationUtil.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters!");
            return;
        }
        
        if (!ValidationUtil.isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address!");
            return;
        }
        
        User user = new User(username, password, email, role);
        
        try {
            if (userDAO.createUser(user)) {
                JOptionPane.showMessageDialog(this, "User created successfully!");
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create user!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}