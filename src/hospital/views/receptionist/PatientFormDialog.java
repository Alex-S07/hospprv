package hospital.views.receptionist;

import hospital.dao.PatientDAO;
import hospital.models.Patient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PatientFormDialog extends JDialog {
    private Patient patient;
    private boolean saved = false;
    private PatientDAO patientDAO;
    
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField dobField;
    private JComboBox<String> genderCombo;
    private JTextArea medicalHistoryArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    public PatientFormDialog(JFrame parent, String title, Patient patient) {
        super(parent, title, true);
        this.patient = patient;
        this.patientDAO = new PatientDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Form fields
        firstNameField = createStyledTextField();
        lastNameField = createStyledTextField();
        dobField = createStyledTextField();
        dobField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        styleComboBox(genderCombo);
        
        medicalHistoryArea = new JTextArea(4, 30);
        medicalHistoryArea.setBackground(Color.WHITE);
        medicalHistoryArea.setForeground(Color.BLACK);
        medicalHistoryArea.setCaretColor(Color.BLACK);
        medicalHistoryArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        medicalHistoryArea.setLineWrap(true);
        medicalHistoryArea.setWrapStyleWord(true);
        
        // Buttons
        saveButton = createStyledButton("💾 Save");
        cancelButton = createStyledButton("❌ Cancel");
        
        // If editing, populate fields
        if (patient != null) {
            populateFields();
        }
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(new Color(0, 100, 200));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 240, 240));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Labels and fields
        addFormRow(formPanel, gbc, 0, "First Name *:", firstNameField);
        addFormRow(formPanel, gbc, 1, "Last Name *:", lastNameField);
        addFormRow(formPanel, gbc, 2, "Date of Birth * (YYYY-MM-DD):", dobField);
        addFormRow(formPanel, gbc, 3, "Gender *:", genderCombo);
        
        // Medical History (larger field)
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel medicalHistoryLabel = new JLabel("Medical History:");
        medicalHistoryLabel.setForeground(Color.BLACK);
        medicalHistoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(medicalHistoryLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(medicalHistoryArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        formPanel.add(scrollPane, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label, gbc);
        
        gbc.gridx = 1; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(field, gbc);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> savePatient());
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key to save
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void populateFields() {
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            dobField.setText(patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        
        genderCombo.setSelectedItem(patient.getGender());
        
        if (patient.getMedicalHistory() != null) {
            medicalHistoryArea.setText(patient.getMedicalHistory());
        }
    }
    
    private void savePatient() {
        if (!validateForm()) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Patient patientToSave;
                if (patient == null) {
                    // New patient
                    patientToSave = new Patient();
                } else {
                    // Existing patient
                    patientToSave = patient;
                }
                
                patientToSave.setFirstName(firstNameField.getText().trim());
                patientToSave.setLastName(lastNameField.getText().trim());
                patientToSave.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
                patientToSave.setGender((String) genderCombo.getSelectedItem());
                patientToSave.setMedicalHistory(medicalHistoryArea.getText().trim());
                
                if (patient == null) {
                    return patientDAO.createPatient(patientToSave) != null;
                } else {
                    return patientDAO.updatePatient(patientToSave);
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        saved = true;
                        JOptionPane.showMessageDialog(PatientFormDialog.this, 
                            patient == null ? "Patient created successfully!" : "Patient updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(PatientFormDialog.this, 
                            patient == null ? "Failed to create patient" : "Failed to update patient",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PatientFormDialog.this, 
                        "Error saving patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private boolean validateForm() {
        if (firstNameField.getText().trim().isEmpty()) {
            showValidationError("Please enter first name");
            firstNameField.requestFocus();
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            showValidationError("Please enter last name");
            lastNameField.requestFocus();
            return false;
        }
        
        if (dobField.getText().trim().isEmpty()) {
            showValidationError("Please enter date of birth");
            dobField.requestFocus();
            return false;
        }
        
        try {
            LocalDate.parse(dobField.getText().trim());
        } catch (DateTimeParseException e) {
            showValidationError("Please enter a valid date in YYYY-MM-DD format");
            dobField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
    
    public boolean isSaved() {
        return saved;
    }
}