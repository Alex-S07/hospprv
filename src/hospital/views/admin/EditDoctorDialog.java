package hospital.views.admin;


import hospital.dao.DoctorDAO;
import hospital.models.Doctor;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class EditDoctorDialog extends JDialog {
    private DoctorDAO doctorDAO;
    private int doctorId;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField specializationField;
    private JTextField experienceField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField consultationFeeField;
    private JCheckBox availableCheckbox;
    private boolean success = false;
    
    public EditDoctorDialog(Frame parent, DoctorDAO doctorDAO, int doctorId) {
        super(parent, "Edit Doctor", true);
        this.doctorDAO = doctorDAO;
        this.doctorId = doctorId;
        initComponents();
        setupLayout();
        loadDoctorData();
        setSize(500, 550);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        specializationField = new JTextField(20);
        experienceField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        consultationFeeField = new JTextField(20);
        availableCheckbox = new JCheckBox("Available");
        availableCheckbox.setSelected(true);
    }
    
    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        addField(panel, gbc, "First Name:", firstNameField, row++);
        addField(panel, gbc, "Last Name:", lastNameField, row++);
        addField(panel, gbc, "Specialization:", specializationField, row++);
        addField(panel, gbc, "Experience (years):", experienceField, row++);
        addField(panel, gbc, "Phone:", phoneField, row++);
        addField(panel, gbc, "Email:", emailField, row++);
        addField(panel, gbc, "Consultation Fee:", consultationFeeField, row++);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(availableCheckbox, gbc);
        
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
        
        add(new JScrollPane(panel));
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
    
    private void loadDoctorData() {
        try {
            Doctor doctor = doctorDAO.getDoctorById(doctorId);
            if (doctor != null) {
                firstNameField.setText(doctor.getFirstName());
                lastNameField.setText(doctor.getLastName());
                specializationField.setText(doctor.getSpecialization());
                experienceField.setText(String.valueOf(doctor.getExperienceYears()));
                phoneField.setText(doctor.getPhone());
                emailField.setText(doctor.getEmail());
                consultationFeeField.setText(String.valueOf(doctor.getConsultationFee()));
                availableCheckbox.setSelected(doctor.isAvailable());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading doctor data: " + e.getMessage());
        }
    }
    
    private void handleSave() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String specialization = specializationField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            
            if (firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name and last name are required!");
                return;
            }
            
            if (!ValidationUtil.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(this, "Invalid phone number!");
                return;
            }
            
            if (!ValidationUtil.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email!");
                return;
            }
            
            int experience = Integer.parseInt(experienceField.getText().trim());
            double consultationFee = Double.parseDouble(consultationFeeField.getText().trim());
            boolean isAvailable = availableCheckbox.isSelected();
            
            Doctor doctor = new Doctor();
            doctor.setDoctorId(doctorId);
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setSpecialization(specialization);
            doctor.setExperienceYears(experience);
            doctor.setPhone(phone);
            doctor.setEmail(email);
            doctor.setConsultationFee(consultationFee);
            doctor.setAvailable(isAvailable);
            
            if (doctorDAO.updateDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor updated successfully!");
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update doctor!");
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for experience and fee!");
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}
