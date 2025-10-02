package hospital.views.admin;

import hospital.dao.DoctorDAO;
import hospital.dao.UserDAO;
import hospital.models.Doctor;
import hospital.models.User;
import hospital.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

class AddDoctorDialog extends JDialog {
    private DoctorDAO doctorDAO;
    private UserDAO userDAO;
    private JTextField fullNameField;
    private JTextField specializationField;
    private JTextField experienceField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField consultationFeeField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean success = false;

    public AddDoctorDialog(Frame parent, DoctorDAO doctorDAO, UserDAO userDAO) {
        super(parent, "Add New Doctor", true);
        this.doctorDAO = doctorDAO;
        this.userDAO = userDAO;
        initComponents();
        setupLayout();
        setSize(500, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        fullNameField = new JTextField(20);
        specializationField = new JTextField(20);
        experienceField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        consultationFeeField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
    }

    private void setupLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(panel, gbc, "Full Name:", fullNameField, row++);
        addField(panel, gbc, "Specialization:", specializationField, row++);
        addField(panel, gbc, "Experience (years):", experienceField, row++);
        addField(panel, gbc, "Phone:", phoneField, row++);
        addField(panel, gbc, "Email:", emailField, row++);
        addField(panel, gbc, "Consultation Fee:", consultationFeeField, row++);

        // Separator
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        panel.add(sep, gbc);
        gbc.gridwidth = 1;

        JLabel loginLabel = new JLabel("Login Credentials");
        loginLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(loginLabel, gbc);
        gbc.gridwidth = 1;

        addField(panel, gbc, "Username:", usernameField, row++);
        passwordField = new JPasswordField(20); // initialize it here
        addField(panel, gbc, "Password:", passwordField, row++);

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

        add(new JScrollPane(panel));
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void handleSave() {
        try {
            String fullName = fullNameField.getText().trim() ;
            String specialization = specializationField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (fullName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Doctor name is required!");
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

            if (!ValidationUtil.isValidUsername(username)) {
                JOptionPane.showMessageDialog(this, "Username must be at least 3 characters!");
                return;
            }

            if (!ValidationUtil.isValidPassword(password)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!");
                return;
            }

            int experience = Integer.parseInt(experienceField.getText().trim());
            double consultationFee = Double.parseDouble(consultationFeeField.getText().trim());

            // Create doctor object
            Doctor doctor = new Doctor();
            doctor.setUsername(username);
            doctor.setPassword(password);
            doctor.setFirstName(fullName); // Using full_name in the database
            doctor.setSpecialization(specialization);
            doctor.setExperienceYears(experience);
            doctor.setPhone(phone);
            doctor.setEmail(email);
            doctor.setConsultationFee(consultationFee);
            doctor.setAvailable(true);

            if (doctorDAO.createDoctor(doctor)) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully!");
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create doctor profile!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for experience and fee!");
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Email already exists")) {
                JOptionPane.showMessageDialog(this,
                        "This email is already registered!\nPlease use a different email address.",
                        "Duplicate Email", JOptionPane.ERROR_MESSAGE);
            } else if (errorMsg.contains("Username already exists")) {
                JOptionPane.showMessageDialog(this,
                        "This username is already taken!\nPlease choose a different username.",
                        "Duplicate Username", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error: " + errorMsg,
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSuccess() {
        return success;
    }
}