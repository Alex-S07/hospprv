package hospital.views.admin;

import hospital.dao.UserDAO;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class ChangePasswordDialog extends JDialog {
    private UserDAO userDAO;
    private int userId;
    private String username;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    
    public ChangePasswordDialog(Frame parent, UserDAO userDAO, int userId, String username) {
        super(parent, "Change Password", true);
        this.userDAO = userDAO;
        this.userId = userId;
        this.username = username;
        initComponents();
        setupLayout();
        setSize(400, 250);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
    }
    
    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel userLabel = new JLabel("User: " + username);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(userLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        
        gbc.gridx = 1;
        panel.add(newPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(new Color(230, 126, 34));
        saveBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        
        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void handleSave() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (!ValidationUtil.isValidPassword(newPassword)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        try {
            if (userDAO.changePassword(userId, newPassword)) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
