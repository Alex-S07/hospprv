package hospital.views.admin;

import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;

class SystemConfigPanel extends JPanel {
    private User currentUser;
    private JTextField hospitalNameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField websiteField;
    private JButton saveBtn;
    
    public SystemConfigPanel(User user) {
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        hospitalNameField = new JTextField(30);
        addressField = new JTextField(30);
        phoneField = new JTextField(30);
        emailField = new JTextField(30);
        websiteField = new JTextField(30);
        
        saveBtn = new JButton("Save Configuration");
        saveBtn.setBackground(Constants.SUCCESS_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(180, 40));
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel titleLabel = new JLabel("System Configuration");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        addFormField("Hospital Name:", hospitalNameField, gbc, 1);
        addFormField("Address:", addressField, gbc, 2);
        addFormField("Phone:", phoneField, gbc, 3);
        addFormField("Email:", emailField, gbc, 4);
        addFormField("Website:", websiteField, gbc, 5);
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);
        add(saveBtn, gbc);
        
        // Load default values
        loadConfiguration();
    }
    
    private void addFormField(String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        add(lbl, gbc);
        
        gbc.gridx = 1; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        field.setPreferredSize(new Dimension(350, 35));
        add(field, gbc);
    }
    
    private void setupEventHandlers() {
        saveBtn.addActionListener(e -> saveConfiguration());
    }
    
    private void loadConfiguration() {
        // Load from database or config file
        hospitalNameField.setText("City General Hospital");
        addressField.setText("123 Medical Center Drive");
        phoneField.setText("1-800-HOSPITAL");
        emailField.setText("info@cityhospital.com");
        websiteField.setText("www.cityhospital.com");
    }
    
    private void saveConfiguration() {
        // Save to database or config file
        JOptionPane.showMessageDialog(this,
            "Configuration saved successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
}