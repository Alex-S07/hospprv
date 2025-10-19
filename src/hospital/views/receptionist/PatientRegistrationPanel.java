package hospital.views.receptionist;

import hospital.dao.PatientDAO;
import hospital.models.Patient;
import hospital.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PatientRegistrationPanel extends JPanel {
    private User currentUser;
    private PatientDAO patientDAO;
    private JTable patientsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, searchButton, clearButton;
    
    public PatientRegistrationPanel(User user) {
        this.currentUser = user;
        this.patientDAO = new PatientDAO();
        initializeComponents();
        setupLayout();
        loadPatients();
    }
    
    private void initializeComponents() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Table setup
        String[] columns = {"ID", "First Name", "Last Name", "Date of Birth", "Age", "Gender", "Medical History", "Registered Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        patientsTable = new JTable(tableModel);
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        patientsTable.setBackground(Color.WHITE);
        patientsTable.setForeground(Color.BLACK);
        patientsTable.setGridColor(new Color(200, 200, 200));
        patientsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        patientsTable.getTableHeader().setForeground(Color.BLACK);
        patientsTable.setSelectionBackground(new Color(0, 100, 200));
        patientsTable.setSelectionForeground(Color.WHITE);
        patientsTable.setRowHeight(25);
        
        // Buttons
        addButton = createStyledButton("➕ Add Patient");
        editButton = createStyledButton("✏️ Edit Patient");
        deleteButton = createStyledButton("🗑️ Delete Patient");
        searchButton = createStyledButton("🔍 Search");
        clearButton = createStyledButton("🔄 Clear");
        
        searchField = new JTextField(20);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.BLACK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.setCaretColor(Color.BLACK);
        
        setupEventHandlers();
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
        
        // Title
        JLabel titleLabel = new JLabel("Patient Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.BLACK);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Patients List",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            Color.BLACK
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddPatientDialog());
        editButton.addActionListener(e -> editSelectedPatient());
        deleteButton.addActionListener(e -> deleteSelectedPatient());
        searchButton.addActionListener(e -> searchPatients());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadPatients();
        });
        
        // Real-time search
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    loadPatients();
                } else {
                    searchPatients();
                }
            }
        });
    }
    
    private void loadPatients() {
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() throws Exception {
                return patientDAO.getAllPatients();
            }
            
            @Override
            protected void done() {
                try {
                    List<Patient> patients = get();
                    tableModel.setRowCount(0);
                    
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    
                    for (Patient patient : patients) {
                        tableModel.addRow(new Object[]{
                            patient.getPatientId(),
                            patient.getFirstName(),
                            patient.getLastName(),
                            patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(dateFormatter) : "",
                            patient.getAge(),
                            patient.getGender(),
                            patient.getMedicalHistory() != null ? 
                                (patient.getMedicalHistory().length() > 50 ? 
                                 patient.getMedicalHistory().substring(0, 47) + "..." : 
                                 patient.getMedicalHistory()) : "",
                            patient.getRegisteredAt() != null ? patient.getRegisteredAt().format(datetimeFormatter) : ""
                        });
                    }
                } catch (Exception e) {
                    showError("Error loading patients: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void showAddPatientDialog() {
        PatientFormDialog dialog = new PatientFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Patient", null);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadPatients();
        }
    }
    
    private void editSelectedPatient() {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a patient to edit");
            return;
        }
        
        int patientId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Patient, Void> worker = new SwingWorker<>() {
            @Override
            protected Patient doInBackground() throws Exception {
                return patientDAO.getPatientById(patientId);
            }
            
            @Override
            protected void done() {
                try {
                    Patient patient = get();
                    if (patient != null) {
                        PatientFormDialog dialog = new PatientFormDialog((JFrame) SwingUtilities.getWindowAncestor(PatientRegistrationPanel.this), 
                                "Edit Patient", patient);
                        dialog.setVisible(true);
                        
                        if (dialog.isSaved()) {
                            loadPatients();
                        }
                    }
                } catch (Exception e) {
                    showError("Error loading patient data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void deleteSelectedPatient() {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a patient to delete");
            return;
        }
        
        int patientId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String patientName = (String) tableModel.getValueAt(selectedRow, 1) + " " + (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete patient: " + patientName + "?\nThis action cannot be undone.", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deletePatient(patientId);
        }
    }
    
    private void deletePatient(int patientId) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return patientDAO.deletePatient(patientId);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccess("Patient deleted successfully!");
                        loadPatients();
                    } else {
                        showError("Failed to delete patient");
                    }
                } catch (Exception e) {
                    showError("Error deleting patient: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void searchPatients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }
        
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() throws Exception {
                return patientDAO.searchPatients(searchTerm);
            }
            
            @Override
            protected void done() {
                try {
                    List<Patient> patients = get();
                    tableModel.setRowCount(0);
                    
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    
                    for (Patient patient : patients) {
                        tableModel.addRow(new Object[]{
                            patient.getPatientId(),
                            patient.getFirstName(),
                            patient.getLastName(),
                            patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(dateFormatter) : "",
                            patient.getAge(),
                            patient.getGender(),
                            patient.getMedicalHistory() != null ? 
                                (patient.getMedicalHistory().length() > 50 ? 
                                 patient.getMedicalHistory().substring(0, 47) + "..." : 
                                 patient.getMedicalHistory()) : "",
                            patient.getRegisteredAt() != null ? patient.getRegisteredAt().format(datetimeFormatter) : ""
                        });
                    }
                    
                    if (patients.isEmpty()) {
                        showInfo("No patients found matching: " + searchTerm);
                    }
                } catch (Exception e) {
                    showError("Error searching patients: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    // Utility methods for showing messages
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void refreshData() {
        loadPatients();
    }
}