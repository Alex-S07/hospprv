package hospital.views.receptionist;


import hospital.dao.PatientDAO;
import hospital.models.Patient;
import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

class PatientRegistrationPanel extends JPanel {
    private User currentUser;
    private PatientDAO patientDAO;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField dobField;
    private JComboBox<String> genderCombo;
    private JTextArea medicalHistoryArea;
    private JTextField searchField;
    
    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton clearBtn;
    
    private Patient selectedPatient;
    
    public PatientRegistrationPanel(User user) {
        this.currentUser = user;
        this.patientDAO = new PatientDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadPatients();
    }
    
    private void initializeComponents() {
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form fields
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        dobField = new JTextField(20);
        dobField.setToolTipText("Format: YYYY-MM-DD");
        
        String[] genders = {"MALE", "FEMALE", "OTHER"};
        genderCombo = new JComboBox<>(genders);
        
        medicalHistoryArea = new JTextArea(4, 20);
        medicalHistoryArea.setLineWrap(true);
        
        searchField = new JTextField(20);
        
        // Table
        String[] columns = {"ID", "Name", "Age", "Gender", "Registered Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(28);
        
        // Buttons
        addBtn = new JButton("Register Patient");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");
        clearBtn = new JButton("Clear");
        
        styleButton(addBtn, Constants.SUCCESS_COLOR);
        styleButton(updateBtn, Constants.PRIMARY_COLOR);
        styleButton(deleteBtn, Constants.DANGER_COLOR);
        styleButton(clearBtn, Constants.SECONDARY_COLOR);
        
        updateBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Constants.BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("Patient Registration");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        titlePanel.add(titleLabel);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        addFormField(formPanel, gbc, "First Name:*", firstNameField, row++);
        addFormField(formPanel, gbc, "Last Name:*", lastNameField, row++);
        addFormField(formPanel, gbc, "Date of Birth:* (YYYY-MM-DD)", dobField, row++);
        addFormField(formPanel, gbc, "Gender:*", genderCombo, row++);
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Medical History:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(medicalHistoryArea), gbc);
        row++;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Constants.BACKGROUND_COLOR);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchPatients());
        searchPanel.add(searchBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadPatients());
        searchPanel.add(refreshBtn);
        
        // Table Panel
        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Patient List"));
        
        // Main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Constants.BACKGROUND_COLOR);
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
    
    private void setupEventHandlers() {
        addBtn.addActionListener(e -> registerPatient());
        updateBtn.addActionListener(e -> updatePatient());
        deleteBtn.addActionListener(e -> deletePatient());
        clearBtn.addActionListener(e -> clearFields());
        
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectPatient();
            }
        });
    }
    
    private void registerPatient() {
        if (!validateForm()) return;
        
        try {
            Patient patient = new Patient();
            patient.setFirstName(firstNameField.getText().trim());
            patient.setLastName(lastNameField.getText().trim());
            patient.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
            patient.setGender((String) genderCombo.getSelectedItem());
            patient.setMedicalHistory(medicalHistoryArea.getText().trim());
            patient.setRegisteredAt(LocalDateTime.now());
            
            Patient created = patientDAO.createPatient(patient);
            if (created != null) {
                JOptionPane.showMessageDialog(this,
                    "Patient registered successfully!\nPatient ID: " + created.getPatientId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients();
                clearFields();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePatient() {
        if (selectedPatient == null || !validateForm()) return;
        
        try {
            selectedPatient.setFirstName(firstNameField.getText().trim());
            selectedPatient.setLastName(lastNameField.getText().trim());
            selectedPatient.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
            selectedPatient.setGender((String) genderCombo.getSelectedItem());
            selectedPatient.setMedicalHistory(medicalHistoryArea.getText().trim());
            
            if (patientDAO.updatePatient(selectedPatient)) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully!");
                loadPatients();
                clearFields();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void deletePatient() {
        if (selectedPatient == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete patient: " + selectedPatient.getFirstName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (patientDAO.deletePatient(selectedPatient.getPatientId())) {
                    JOptionPane.showMessageDialog(this, "Patient deleted successfully!");
                    loadPatients();
                    clearFields();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void searchPatients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }
        
        try {
            List<Patient> patients = patientDAO.searchPatients(searchTerm);
            displayPatients(patients);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            displayPatients(patients);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage());
        }
    }
    
    private void displayPatients(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient patient : patients) {
            Object[] row = {
                patient.getPatientId(),
                patient.getFirstName(),
                calculateAge(patient.getDateOfBirth()),
                patient.getGender(),
                patient.getRegisteredAt().toLocalDate()
            };
            tableModel.addRow(row);
        }
    }
    
    private void selectPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int patientId = (Integer) tableModel.getValueAt(selectedRow, 0);
                selectedPatient = patientDAO.getPatientById(patientId);
                
                firstNameField.setText(selectedPatient.getFirstName());
                lastNameField.setText(selectedPatient.getLastName());
                dobField.setText(selectedPatient.getDateOfBirth().toString());
                genderCombo.setSelectedItem(selectedPatient.getGender());
                medicalHistoryArea.setText(selectedPatient.getMedicalHistory());
                
                addBtn.setEnabled(false);
                updateBtn.setEnabled(true);
                deleteBtn.setEnabled(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        dobField.setText("");
        genderCombo.setSelectedIndex(0);
        medicalHistoryArea.setText("");
        
        selectedPatient = null;
        addBtn.setEnabled(true);
        updateBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        
        patientTable.clearSelection();
    }
    
    private boolean validateForm() {
        if (firstNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "First name is required!");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Last name is required!");
            return false;
        }
        try {
            LocalDate.parse(dobField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD");
            return false;
        }
        return true;
    }
    
    private int calculateAge(LocalDate dob) {
        return dob != null ? Period.between(dob, LocalDate.now()).getYears() : 0;
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 38));
    }
    
    public void refreshData() {
        loadPatients();
    }
}
