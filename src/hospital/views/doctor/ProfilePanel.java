package hospital.views.doctor;


import hospital.models.User;
import hospital.models.Doctor;
import hospital.dao.DoctorProfileDAO;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ProfilePanel extends JPanel {
    private User currentUser;
    private Doctor doctorProfile;
    private DoctorProfileDAO profileDAO;
    
    private JTextField fullNameField;
    private JComboBox<String> specializationCombo;
    private JTextField qualificationField;
    private JSpinner experienceSpinner;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField consultationFeeField;
    private JButton updateBtn;
    
    public ProfilePanel(User user, Doctor profile) {
        this.currentUser = user;
        this.doctorProfile = profile;
        this.profileDAO = new DoctorProfileDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadProfile();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        fullNameField = new JTextField(20);
        
        String[] specializations = {
            "Cardiology", "Orthopedics", "Pediatrics", "General Medicine",
            "Dermatology", "Neurology", "Gynecology", "ENT", "Ophthalmology"
        };
        specializationCombo = new JComboBox<>(specializations);
        
        qualificationField = new JTextField(20);
        
        SpinnerNumberModel expModel = new SpinnerNumberModel(0, 0, 50, 1);
        experienceSpinner = new JSpinner(expModel);
        
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        consultationFeeField = new JTextField(20);
        
        updateBtn = new JButton("Update Profile");
        updateBtn.setBackground(Constants.PRIMARY_COLOR);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFocusPainted(false);
        updateBtn.setPreferredSize(new Dimension(150, 40));
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 1;
        addField("Full Name:", fullNameField, gbc, row++);
        addField("Specialization:", specializationCombo, gbc, row++);
        addField("Qualification:", qualificationField, gbc, row++);
        addField("Experience (years):", experienceSpinner, gbc, row++);
        addField("Phone:", phoneField, gbc, row++);
        addField("Email:", emailField, gbc, row++);
        addField("Consultation Fee (₹):", consultationFeeField, gbc, row++);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Constants.BACKGROUND_COLOR);
        buttonPanel.add(updateBtn);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(buttonPanel, gbc);
    }
    
    private void addField(String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(field, gbc);
        gbc.weightx = 0;
    }
    
    private void setupEventHandlers() {
        updateBtn.addActionListener(e -> updateProfile());
    }
    
    private void loadProfile() {
        if (doctorProfile != null) {
            fullNameField.setText(doctorProfile.getUsername());
            specializationCombo.setSelectedItem(doctorProfile.getSpecialization());
            qualificationField.setText(doctorProfile.getQualification());
            experienceSpinner.setValue(doctorProfile.getExperienceYears());
            phoneField.setText(doctorProfile.getPhone());
            emailField.setText(doctorProfile.getEmail());
            consultationFeeField.setText(String.valueOf(doctorProfile.getConsultationFee()));
        }
    }
    
    private void updateProfile() {
        // Validation
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required");
            return;
        }
        if (qualificationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Qualification is required");
            return;
        }
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required");
            return;
        }
        if (emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required");
            return;
        }
        
        double fee;
        try {
            fee = Double.parseDouble(consultationFeeField.getText().trim());
            if (fee < 0) {
                JOptionPane.showMessageDialog(this, "Consultation fee must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid consultation fee");
            return;
        }
        
        doctorProfile.setUsername(fullNameField.getText().trim());
        doctorProfile.setSpecialization((String) specializationCombo.getSelectedItem());
        doctorProfile.setQualification(qualificationField.getText().trim());
        doctorProfile.setExperienceYears((Integer) experienceSpinner.getValue());
        doctorProfile.setPhone(phoneField.getText().trim());
        doctorProfile.setEmail(emailField.getText().trim());
        doctorProfile.setConsultationFee(fee);
        
        try {
            if (profileDAO.updateDoctorProfile(doctorProfile)) {
                JOptionPane.showMessageDialog(this, 
                    "Profile updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to update profile", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}