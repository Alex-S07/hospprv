package hospital.views.doctor;

import hospital.dao.*;
import hospital.models.*;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrescriptionAndHistoryPanel extends JPanel {
    private User currentUser;
    private PatientDAO patientDAO;
    private AppointmentDAO appointmentDAO;
    private MedicalRecordDAO medicalRecordDAO;
    private PrescriptionDAO prescriptionDAO;
    private InventoryDAO inventoryDAO;

    // UI Components
    private JTable appointmentTable;
    private DefaultTableModel appointmentTableModel;
    private JTextArea patientDetailsArea;
    private JTextArea previousHistoryArea;
    private JTextArea prescriptionArea;
    private JComboBox<String> medicineCombo;
    private JTextField frequencyField, durationField;
    private JTextArea diagnosisArea, symptomsArea;
    private JLabel totalCostLabel;

    private Appointment selectedAppointment;
    private Patient selectedPatient;
    private List<Medicine> medicinesList;

    public PrescriptionAndHistoryPanel(User user) {
        this.currentUser = user;
        this.patientDAO = new PatientDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.prescriptionDAO = new PrescriptionDAO();
        this.inventoryDAO = new InventoryDAO();

        initializeUI();
        loadTodaysAppointments();
        loadMedicines();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Main split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(400);
        mainSplit.setLeftComponent(createAppointmentsPanel());
        mainSplit.setRightComponent(createPatientPanel());
        
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Today's Appointments"));
        panel.setBackground(Color.WHITE);

        // Table
        String[] columns = {"Token", "Patient", "Time", "Status"};
        appointmentTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setRowHeight(25);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedPatient();
        });

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTodaysAppointments());
        
        JButton startBtn = new JButton("Start Consultation");
        startBtn.setBackground(Color.BLUE);
        startBtn.setForeground(Color.BLACK);
        startBtn.addActionListener(e -> startConsultation());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(startBtn);

        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setDividerLocation(300);
        rightSplit.setTopComponent(createPatientInfoPanel());
        rightSplit.setBottomComponent(createPrescriptionPanel());
        
        panel.add(rightSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPatientInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Patient Information"));
        panel.setBackground(Color.WHITE);

        // Current details
        patientDetailsArea = new JTextArea(6, 30);
        patientDetailsArea.setEditable(false);
        patientDetailsArea.setText("Select a patient from appointments...");
        
        // Medical history
        previousHistoryArea = new JTextArea(8, 30);
        previousHistoryArea.setEditable(false);
        previousHistoryArea.setText("No previous history...");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Current Details", new JScrollPane(patientDetailsArea));
        tabbedPane.addTab("Medical History", new JScrollPane(previousHistoryArea));
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPrescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Prescription"));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBackground(Color.WHITE);

        // Symptoms and Diagnosis
        symptomsArea = new JTextArea(2, 20);
        diagnosisArea = new JTextArea(2, 20);
        
        formPanel.add(new JLabel("Symptoms:"));
        formPanel.add(new JScrollPane(symptomsArea));
        formPanel.add(new JLabel("Diagnosis:"));
        formPanel.add(new JScrollPane(diagnosisArea));

        // Medicine selection - Only name from DB
        medicineCombo = new JComboBox<>();
        frequencyField = new JTextField();
        durationField = new JTextField();
        
        formPanel.add(new JLabel("Medicine:"));
        formPanel.add(medicineCombo);
        formPanel.add(new JLabel("Frequency:"));
        formPanel.add(frequencyField);
        formPanel.add(new JLabel("Duration:"));
        formPanel.add(durationField);

        // Add medicine button
        JButton addMedicineBtn = new JButton("Add Medicine");
        addMedicineBtn.addActionListener(e -> addMedicineToPrescription());
        formPanel.add(new JLabel());
        formPanel.add(addMedicineBtn);

        // Total cost display
        totalCostLabel = new JLabel("Total Cost: ₹0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalCostLabel.setForeground(Color.RED);
        formPanel.add(new JLabel());
        formPanel.add(totalCostLabel);

        // Prescription display with calculated quantities
        prescriptionArea = new JTextArea(6, 30);
        prescriptionArea.setEditable(false);

        // Save button
        JButton saveBtn = new JButton("Save Prescription & Complete");
        saveBtn.setBackground(Color.GREEN);
        saveBtn.setForeground(Color.BLACK);
        saveBtn.addActionListener(e -> savePrescription());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JScrollPane(prescriptionArea), BorderLayout.CENTER);
        southPanel.add(saveBtn, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(southPanel, BorderLayout.CENTER);
        
        return panel;
    }

    // ============ DATA METHODS ============

    private void loadTodaysAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getTodaysAppointments(currentUser.getUserId());
            appointmentTableModel.setRowCount(0);
            
            for (Appointment apt : appointments) {
                appointmentTableModel.addRow(new Object[]{
                    apt.getTokenNumber(),
                    apt.getPatientName(),
                    apt.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    apt.getStatus()
                });
            }
        } catch (SQLException e) {
            showError("Error loading appointments: " + e.getMessage());
        }
    }

    private void loadSelectedPatient() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) return;

        try {
            int token = (Integer) appointmentTableModel.getValueAt(row, 0);
            List<Appointment> appointments = appointmentDAO.getTodaysAppointments(currentUser.getUserId());
            
            selectedAppointment = appointments.stream()
                .filter(a -> a.getTokenNumber() == token)
                .findFirst().orElse(null);
                
            if (selectedAppointment != null) {
                selectedPatient = patientDAO.getPatientById(selectedAppointment.getPatientId());
                displayPatientDetails();
                loadPatientHistory();
            }
        } catch (SQLException e) {
            showError("Error loading patient: " + e.getMessage());
        }
    }

    private void displayPatientDetails() {
        if (selectedPatient == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(selectedPatient.getPatientId()).append("\n");
        sb.append("Name: ").append(selectedPatient.getFirstName()).append(" ")
          .append(selectedPatient.getLastName()).append("\n");
        sb.append("Age: ").append(calculateAge(selectedPatient.getDateOfBirth())).append("\n");
        sb.append("Gender: ").append(selectedPatient.getGender()).append("\n");
        
        patientDetailsArea.setText(sb.toString());
    }

    private void loadPatientHistory() {
        if (selectedPatient == null) return;
        
        try {
            List<MedicalRecord> records = medicalRecordDAO.getPatientMedicalHistory(
                selectedPatient.getPatientId());
                
            if (records.isEmpty()) {
                previousHistoryArea.setText("No previous records found.");
                return;
            }
            
            StringBuilder history = new StringBuilder();
            for (MedicalRecord record : records) {
                history.append("Date: ").append(record.getVisitDate().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n");
                history.append("Diagnosis: ").append(record.getDiagnosis()).append("\n\n");
            }
            previousHistoryArea.setText(history.toString());
            
        } catch (SQLException e) {
            showError("Error loading history: " + e.getMessage());
        }
    }

    private void loadMedicines() {
        try {
            medicinesList = inventoryDAO.getAllMedicines();
            medicineCombo.removeAllItems();
            medicineCombo.addItem("-- Select Medicine --");
            
            for (Medicine medicine : medicinesList) {
                String display = String.format("%s (₹%.2f, Stock: %d)", 
                    medicine.getName(), medicine.getUnitPrice(), medicine.getStockQuantity());
                medicineCombo.addItem(display);
            }
        } catch (SQLException e) {
            showError("Error loading medicines: " + e.getMessage());
        }
    }

    // ============ ACTION METHODS ============

    private void startConsultation() {
        if (selectedAppointment == null) {
            showError("Please select an appointment first.");
            return;
        }
        
        try {
            appointmentDAO.updateAppointmentStatus(selectedAppointment.getAppointmentId(), "IN_PROGRESS");
            JOptionPane.showMessageDialog(this, "Consultation started!");
            loadTodaysAppointments();
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void addMedicineToPrescription() {
        if (medicineCombo.getSelectedIndex() <= 0) {
            showError("Please select a medicine.");
            return;
        }
        
        String frequency = frequencyField.getText().trim();
        String duration = durationField.getText().trim();
        
        if (frequency.isEmpty() || duration.isEmpty()) {
            showError("Please fill frequency and duration.");
            return;
        }
        
        Medicine selectedMedicine = getCurrentSelectedMedicine();
        if (selectedMedicine == null) {
            showError("Invalid medicine selection.");
            return;
        }
        
        // Auto-calculate quantity
        int quantity = calculateMedicineQuantity(frequency, duration);
        
        // Auto-calculate cost
        double cost = selectedMedicine.getUnitPrice() * quantity;
        
        // Check stock availability
        if (selectedMedicine.getStockQuantity() < quantity) {
            int result = JOptionPane.showConfirmDialog(this,
                String.format("Insufficient stock! Required: %d, Available: %d\nContinue anyway?", 
                    quantity, selectedMedicine.getStockQuantity()),
                "Low Stock Warning", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Add to prescription with auto-calculated values
        String entry = String.format("• %s | %s for %s | Qty: %d | Cost: ₹%.2f\n", 
            selectedMedicine.getName(),
            frequency, 
            duration, 
            quantity,
            cost);
        
        prescriptionArea.append(entry);
        
        // Update total cost
        updateTotalCost();
        
        // Clear fields
        frequencyField.setText("");
        durationField.setText("");
        medicineCombo.setSelectedIndex(0);
    }

    private void savePrescription() {
        if (selectedAppointment == null || selectedPatient == null) {
            showError("Please select a patient first.");
            return;
        }
        
        String symptoms = symptomsArea.getText().trim();
        String diagnosis = diagnosisArea.getText().trim();
        
        if (symptoms.isEmpty() || diagnosis.isEmpty()) {
            showError("Please enter symptoms and diagnosis.");
            return;
        }
        
        if (prescriptionArea.getText().trim().isEmpty()) {
            showError("Please add at least one medicine.");
            return;
        }

        try {
            // Save medical record
            MedicalRecord record = new MedicalRecord();
            record.setPatientId(selectedPatient.getPatientId());
            record.setDoctorId(currentUser.getUserId());
            record.setAppointmentId(selectedAppointment.getAppointmentId());
            record.setVisitDate(LocalDateTime.now());
            record.setDiagnosis(diagnosis);
            record.setNotes(symptoms);
            
            MedicalRecord savedRecord = medicalRecordDAO.createMedicalRecord(record);
            
            if (savedRecord != null) {
                // Parse and save prescriptions with calculated quantities
                String[] lines = prescriptionArea.getText().split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    
                    // Parse line: "• Medicine | Frequency for Duration | Qty: X | Cost: ₹Y"
                    String cleanLine = line.replace("• ", "");
                    String[] parts = cleanLine.split(" \\| ");
                    
                    if (parts.length >= 3) {
                        String medicineName = parts[0];
                        String frequencyDuration = parts[1];
                        String quantityStr = parts[2].replace("Qty: ", "");
                        String costStr = parts[3].replace("Cost: ₹", "");
                        
                        // Extract frequency and duration
                        String[] fdParts = frequencyDuration.split(" for ");
                        String frequency = fdParts[0];
                        String duration = fdParts[1];
                        
                        int quantity = Integer.parseInt(quantityStr);
                        double cost = Double.parseDouble(costStr);
                        
                        Prescription prescription = new Prescription();
                        prescription.setRecordId(savedRecord.getRecordId());
                        prescription.setMedicineName(medicineName);
                        prescription.setFrequency(frequency);
                        prescription.setDuration(duration);
                        prescription.setQuantity(quantity);
                        prescription.setTotalCost(cost);
                        prescription.setCreatedAt(LocalDateTime.now());
                        
                        prescriptionDAO.createPrescription(prescription);
                    }
                }
                
                // Complete appointment
                appointmentDAO.updateAppointmentStatus(selectedAppointment.getAppointmentId(), "COMPLETED");
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Prescription saved successfully!\nTotal Cost: %s", 
                    totalCostLabel.getText()));
                clearForm();
                loadTodaysAppointments();
            }
            
        } catch (SQLException e) {
            showError("Error saving: " + e.getMessage());
        } catch (Exception e) {
            showError("Error parsing prescription data: " + e.getMessage());
        }
    }

    private void clearForm() {
        symptomsArea.setText("");
        diagnosisArea.setText("");
        prescriptionArea.setText("");
        frequencyField.setText("");
        durationField.setText("");
        medicineCombo.setSelectedIndex(0);
        totalCostLabel.setText("Total Cost: ₹0.00");
    }

    // ============ CALCULATION METHODS ============

    /**
     * Auto-calculate medicine quantity based on frequency and duration
     */
    private int calculateMedicineQuantity(String frequency, String duration) {
        try {
            int dosesPerDay = parseFrequency(frequency);
            int totalDays = parseDuration(duration);
            return dosesPerDay * totalDays;
        } catch (Exception e) {
            showError("Error calculating quantity: " + e.getMessage());
            return 1;
        }
    }

    private int parseFrequency(String frequency) {
        frequency = frequency.toLowerCase().trim();
        
        if (frequency.contains("once") || frequency.contains("1 time")) return 1;
        if (frequency.contains("twice") || frequency.contains("2 times")) return 2;
        if (frequency.contains("thrice") || frequency.contains("3 times")) return 3;
        if (frequency.contains("4 times")) return 4;
        
        // Extract number from patterns
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(frequency);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return 1; // Default
    }

    private int parseDuration(String duration) {
        duration = duration.toLowerCase().trim();
        
        // Extract number and unit
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("(\\d+)\\s*(day|days|week|weeks|month|months)");
        java.util.regex.Matcher matcher = pattern.matcher(duration);

        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            if (unit.startsWith("week")) return number * 7;
            if (unit.startsWith("month")) return number * 30;
            return number; // days
        }

        // Try to extract just a number
        pattern = java.util.regex.Pattern.compile("(\\d+)");
        matcher = pattern.matcher(duration);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 7; // Default 7 days
    }

    /**
     * Update total cost by parsing all prescription entries
     */
    private void updateTotalCost() {
        double total = 0.0;
        String[] lines = prescriptionArea.getText().split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            // Extract cost from line: "... Cost: ₹123.45"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Cost: ₹([0-9.]+)");
            java.util.regex.Matcher matcher = pattern.matcher(line);
            
            if (matcher.find()) {
                total += Double.parseDouble(matcher.group(1));
            }
        }
        
        totalCostLabel.setText(String.format("Total Cost: ₹%.2f", total));
    }

    // ============ HELPER METHODS ============

    private int calculateAge(java.time.LocalDate dob) {
        if (dob == null) return 0;
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Method to get currently selected medicine with all details
    public Medicine getCurrentSelectedMedicine() {
        if (medicineCombo.getSelectedIndex() <= 0 || medicinesList == null) {
            return null;
        }
        
        String selected = (String) medicineCombo.getSelectedItem();
        String medicineName = selected.split(" \\(")[0];
        
        for (Medicine medicine : medicinesList) {
            if (medicine.getName().equals(medicineName)) {
                return medicine;
            }
        }
        return null;
    }
}