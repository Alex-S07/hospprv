package hospital.views.doctor;

import hospital.dao.*;
import hospital.models.*;
import hospital.models.Appointment.AppointmentStatus;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
    private DefaultListModel<String> prescriptionListModel;
    private JList<String> prescriptionList;
    private JComboBox<String> medicineCombo;
    private JTextField dosageField;
    private JTextField frequencyField;
    private JTextField durationField;
    private JTextArea instructionsArea;
    private JTextArea diagnosisArea;
    private JTextArea symptomsArea;

    private Appointment selectedAppointment;
    private Patient selectedPatient;

    public PrescriptionAndHistoryPanel(User user) {
        this.currentUser = user;
        this.patientDAO = new PatientDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.prescriptionDAO = new PrescriptionDAO();
        this.inventoryDAO = new InventoryDAO();

        initializeComponents();
        loadTodaysAppointments();
        loadMedicines();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Constants.BACKGROUND_COLOR);

        // Create split pane: Left (appointments), Right (patient details +
        // prescription)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(350);

        // LEFT PANEL - Today's Appointments
        JPanel leftPanel = createAppointmentsPanel();

        // RIGHT PANEL - Split into top (patient info) and bottom (prescription)
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setDividerLocation(300);

        JPanel patientInfoPanel = createPatientInfoPanel();
        JPanel prescriptionPanel = createPrescriptionPanel();

        rightSplitPane.setTopComponent(patientInfoPanel);
        rightSplitPane.setBottomComponent(prescriptionPanel);

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightSplitPane);

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Today's Appointments"));
        panel.setBackground(Color.WHITE);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        JComboBox<String> statusFilter = new JComboBox<>(new String[] {
                "All", "Scheduled", "In Progress", "Completed"
        });
        statusFilter.addActionListener(e -> filterAppointments((String) statusFilter.getSelectedItem()));
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTodaysAppointments());
        filterPanel.add(refreshBtn);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Appointments table
        String[] columns = { "Token", "Patient", "Time", "Status" };
        appointmentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(30);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPatient();
            }
        });

        // Color-code status
        appointmentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 3);
                    if ("Completed".equals(status)) {
                        c.setBackground(new Color(212, 237, 218));
                    } else if ("In Progress".equals(status)) {
                        c.setBackground(new Color(255, 243, 205));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton startBtn = new JButton("Start Consultation");
        startBtn.setBackground(new Color(52, 152, 219));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> startConsultation());

        buttonPanel.add(startBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Patient Information"));
        panel.setBackground(Color.WHITE);

        // Current patient details
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new TitledBorder("Current Details"));

        patientDetailsArea = new JTextArea(5, 30);
        patientDetailsArea.setEditable(false);
        patientDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        patientDetailsArea.setText("Select a patient to view details...");
        detailsPanel.add(new JScrollPane(patientDetailsArea), BorderLayout.CENTER);

        // Previous history
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(new TitledBorder("Previous Medical History"));

        previousHistoryArea = new JTextArea(8, 30);
        previousHistoryArea.setEditable(false);
        previousHistoryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        previousHistoryArea.setText("No previous history available...");
        historyPanel.add(new JScrollPane(previousHistoryArea), BorderLayout.CENTER);

        panel.add(detailsPanel, BorderLayout.NORTH);
        panel.add(historyPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPrescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Create Prescription"));
        panel.setBackground(Color.WHITE);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Symptoms
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("Symptoms:"), gbc);
        gbc.gridx = 1;
        symptomsArea = new JTextArea(2, 25);
        symptomsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(symptomsArea), gbc);
        row++;

        // Diagnosis
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1;
        diagnosisArea = new JTextArea(2, 25);
        diagnosisArea.setLineWrap(true);
        formPanel.add(new JScrollPane(diagnosisArea), gbc);
        row++;

        // Separator
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        row++;

        // Medicine selection
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Medicine:"), gbc);
        gbc.gridx = 1;
        medicineCombo = new JComboBox<>();
        formPanel.add(medicineCombo, gbc);
        row++;

        // Dosage
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Dosage:"), gbc);
        gbc.gridx = 1;
        dosageField = new JTextField(25);
        dosageField.setToolTipText("e.g., 500mg, 10ml");
        formPanel.add(dosageField, gbc);
        row++;

        // Frequency
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Frequency:"), gbc);
        gbc.gridx = 1;
        frequencyField = new JTextField(25);
        frequencyField.setToolTipText("e.g., 3 times daily, twice daily");
        formPanel.add(frequencyField, gbc);
        row++;

        // Duration
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Duration:"), gbc);
        gbc.gridx = 1;
        durationField = new JTextField(25);
        durationField.setToolTipText("e.g., 7 days, 2 weeks");
        formPanel.add(durationField, gbc);
        row++;

        // Instructions
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("Instructions:"), gbc);
        gbc.gridx = 1;
        instructionsArea = new JTextArea(2, 25);
        instructionsArea.setLineWrap(true);
        instructionsArea.setToolTipText("e.g., Take after meals");
        formPanel.add(new JScrollPane(instructionsArea), gbc);
        row++;

        // Add medicine button
        gbc.gridx = 1;
        gbc.gridy = row;
        JButton addMedicineBtn = new JButton("Add Medicine");
        addMedicineBtn.setBackground(new Color(46, 204, 113));
        addMedicineBtn.setForeground(Color.WHITE);
        addMedicineBtn.addActionListener(e -> addMedicineToPrescription());
        formPanel.add(addMedicineBtn, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Prescribed medicines list
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new TitledBorder("Prescribed Medicines"));
        listPanel.setBackground(Color.WHITE);

        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        prescriptionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);

        JPanel listButtonPanel = new JPanel(new FlowLayout());
        listButtonPanel.setBackground(Color.WHITE);

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> removeSelectedMedicine());

        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> prescriptionListModel.clear());

        listButtonPanel.add(removeBtn);
        listButtonPanel.add(clearBtn);
        listPanel.add(listButtonPanel, BorderLayout.SOUTH);

        panel.add(listPanel, BorderLayout.CENTER);

        // Save prescription button
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        savePanel.setBackground(Color.WHITE);

        JButton savePrescriptionBtn = new JButton("Save Prescription & Complete");
        savePrescriptionBtn.setBackground(Constants.SUCCESS_COLOR);
        savePrescriptionBtn.setForeground(Color.WHITE);
        savePrescriptionBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        savePrescriptionBtn.addActionListener(e -> savePrescription());

        savePanel.add(savePrescriptionBtn);
        panel.add(savePanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============ Data Loading Methods ============

    private void loadTodaysAppointments() {
        try {
            // Get doctor's user_id
            List<Appointment> appointments = appointmentDAO.getTodaysAppointments(currentUser.getUserId());
            displayAppointments(appointments);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage());
        }
    }

    private void displayAppointments(List<Appointment> appointments) {
        appointmentTableModel.setRowCount(0);
        for (Appointment apt : appointments) {
            Object[] row = {
                    apt.getTokenNumber(),
                    apt.getPatientName(),
                    apt.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    apt.getStatus()
            };
            appointmentTableModel.addRow(row);
        }
    }

    private void filterAppointments(String status) {
        // Implement filter logic based on status
        loadTodaysAppointments();
    }

    private void loadSelectedPatient() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        try {
            // Get appointment details
            int token = (Integer) appointmentTableModel.getValueAt(selectedRow, 0);
            List<Appointment> appointments = appointmentDAO.getTodaysAppointments(currentUser.getUserId());

            selectedAppointment = appointments.stream()
                    .filter(a -> a.getTokenNumber() == token)
                    .findFirst()
                    .orElse(null);

            if (selectedAppointment != null) {
                selectedPatient = patientDAO.getPatientById(selectedAppointment.getPatientId());
                displayPatientDetails();
                loadPatientHistory();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patient: " + e.getMessage());
        }
    }

    private void displayPatientDetails() {
        if (selectedPatient == null)
            return;

        StringBuilder details = new StringBuilder();
        details.append("Patient ID: ").append(selectedPatient.getPatientId()).append("\n");
        details.append("Name: ").append(selectedPatient.getFirstName()).append(" ")
                .append(selectedPatient.getLastName()).append("\n");
        details.append("Age: ").append(calculateAge(selectedPatient.getDateOfBirth())).append(" years\n");
        details.append("Gender: ").append(selectedPatient.getGender()).append("\n");
        details.append("DOB: ").append(selectedPatient.getDateOfBirth()).append("\n");

        if (selectedPatient.getMedicalHistory() != null && !selectedPatient.getMedicalHistory().isEmpty()) {
            details.append("\nKnown Conditions:\n").append(selectedPatient.getMedicalHistory());
        }

        patientDetailsArea.setText(details.toString());
    }

    private void loadPatientHistory() {
        if (selectedPatient == null)
            return;

        try {
            List<MedicalRecord> records = medicalRecordDAO.getPatientMedicalHistory(
                    selectedPatient.getPatientId());

            if (records.isEmpty()) {
                previousHistoryArea.setText("No previous medical records found.");
                return;
            }

            StringBuilder history = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            for (MedicalRecord record : records) {
                history.append("=====================================\n");
                history.append("Date: ").append(record.getVisitDate().format(formatter)).append("\n");
                history.append("Doctor: ").append(record.getDoctorName()).append("\n");
                history.append("Diagnosis: ").append(record.getDiagnosis()).append("\n");
                history.append("Treatment: ").append(record.getTreatment() != null ? record.getTreatment() : "N/A")
                        .append("\n");

                if (record.getVitalSigns() != null && !record.getVitalSigns().isEmpty()) {
                    history.append("Vital Signs: ").append(record.getVitalSigns()).append("\n");
                }

                if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                    history.append("Notes: ").append(record.getNotes()).append("\n");
                }

                // Load prescriptions for this appointment
                List<Prescription> prescriptions = prescriptionDAO.getTodaysPrescriptions();

                if (!prescriptions.isEmpty()) {
                    history.append("\nPrescriptions:\n");
                    for (Prescription p : prescriptions) {
                        history.append("  • ").append(p.getMedicineName())
                                .append(" - ").append(p.getDosage())
                                .append(", ").append(p.getFrequency())
                                .append(" for ").append(p.getDuration());

                        if (p.getInstructions() != null && !p.getInstructions().isEmpty()) {
                            history.append("\n    Instructions: ").append(p.getInstructions());
                        }
                        history.append("\n");
                    }
                }
                history.append("\n");
            }

            previousHistoryArea.setText(history.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading patient history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ============ Action Methods ============

    // In PrescriptionAndHistoryPanel.java
    private void startConsultation() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        try {
            appointmentDAO.updateAppointmentStatus(
                    selectedAppointment.getAppointmentId(),
                    AppointmentStatus.IN_PROGRESS // Use constant instead of string
            );
            JOptionPane.showMessageDialog(this, "Consultation started!");
            loadTodaysAppointments();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addMedicineToPrescription() {
        String medicine = (String) medicineCombo.getSelectedItem();
        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String duration = durationField.getText().trim();
        String instructions = instructionsArea.getText().trim();

        if (medicine == null || medicine.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select a medicine.");
            return;
        }

        if (dosage.isEmpty() || frequency.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in dosage, frequency, and duration.");
            return;
        }

        // ✅ Calculate quantity
        int calculatedQuantity = calculateMedicineQuantity(frequency, duration);

        // ✅ Check if sufficient stock is available
        Medicine selectedMedicine = getSelectedMedicine();
        if (selectedMedicine != null && selectedMedicine.getStockQuantity() < calculatedQuantity) {
            int result = JOptionPane.showConfirmDialog(this,
                    String.format(
                            "Warning: Required quantity is %d but only %d units available in stock.\nDo you want to continue?",
                            calculatedQuantity, selectedMedicine.getStockQuantity()),
                    "Low Stock Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // ✅ Format entry with calculated quantity displayed
        String prescriptionEntry = String.format("%s | %s | %s | %s | %s (Qty: %d)",
                medicine.split(" \\(")[0],
                dosage,
                frequency,
                duration,
                instructions.isEmpty() ? "No special instructions" : instructions,
                calculatedQuantity);

        prescriptionListModel.addElement(prescriptionEntry);

        // Show confirmation with quantity
        JOptionPane.showMessageDialog(this,
                String.format("Medicine added!\nCalculated quantity: %d units for %s over %s",
                        calculatedQuantity, frequency, duration),
                "Medicine Added",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        dosageField.setText("");
        frequencyField.setText("");
        durationField.setText("");
        instructionsArea.setText("");
        medicineCombo.setSelectedIndex(0);
    }

    private void removeSelectedMedicine() {
        int selectedIndex = prescriptionList.getSelectedIndex();
        if (selectedIndex >= 0) {
            prescriptionListModel.remove(selectedIndex);
        }
    }

    private void savePrescription() {
        if (selectedAppointment == null || selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.");
            return;
        }

        if (prescriptionListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one medicine.");
            return;
        }

        String symptoms = symptomsArea.getText().trim();
        String diagnosis = diagnosisArea.getText().trim();

        if (symptoms.isEmpty() || diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter symptoms and diagnosis.");
            return;
        }

        try {
            // Create medical record
            MedicalRecord record = new MedicalRecord();
            record.setPatientId(selectedPatient.getPatientId());
            record.setDoctorId(currentUser.getUserId());
            record.setAppointmentId(selectedAppointment.getAppointmentId());
            record.setVisitDate(LocalDateTime.now());
            record.setDiagnosis(diagnosis);

            MedicalRecord savedRecord = medicalRecordDAO.createMedicalRecord(record);

            if (savedRecord != null) {
                // Save prescriptions with calculated quantities
                for (int i = 0; i < prescriptionListModel.size(); i++) {
                    String entry = prescriptionListModel.getElementAt(i);
                    String[] parts = entry.split(" \\| ");

                    String medicineName = parts[0];
                    String dosage = parts[1];
                    String frequency = parts[2];
                    String duration = parts[3];
                    String instructions = parts.length > 4 ? parts[4] : "";

                    // ✅ Calculate quantity based on frequency and duration
                    int calculatedQuantity = calculateMedicineQuantity(frequency, duration);

                    Prescription prescription = new Prescription();
                    prescription.setRecordId(savedRecord.getRecordId());
                    prescription.setMedicineName(medicineName);
                    prescription.setDosage(dosage);
                    prescription.setFrequency(frequency);
                    prescription.setDuration(duration);
                    prescription.setInstructions(instructions);
                    prescription.setQuantity(calculatedQuantity); // ✅ Use calculated quantity
                    prescription.setCreatedAt(LocalDateTime.now());

                    prescriptionDAO.createPrescription(prescription);

                    // Optional: Log the calculation for debugging
                    System.out
                            .println(String.format("Medicine: %s, Frequency: %s, Duration: %s, Calculated Quantity: %d",
                                    medicineName, frequency, duration, calculatedQuantity));
                }

                // Update appointment status to completed
                appointmentDAO.updateAppointmentStatus(
                        selectedAppointment.getAppointmentId(),
                        AppointmentStatus.COMPLETED);

                JOptionPane.showMessageDialog(this,
                        "Prescription saved successfully!\nQuantities calculated automatically based on frequency and duration.");

                // Clear form
                clearPrescriptionForm();
                loadTodaysAppointments();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving prescription: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private Medicine getSelectedMedicine(String medicineName) {
        if (medicinesList == null)
            return null;

        for (Medicine medicine : medicinesList) {
            if (medicine.getName().equals(medicineName)) {
                return medicine;
            }
        }
        return null;
    }

    // Add this helper method to your PrescriptionAndHistoryPanel class

    /**
     * Calculates the quantity of medicine needed based on frequency and duration
     * 
     * @param frequency - e.g., "3 times daily", "twice daily", "once daily"
     * @param duration  - e.g., "7 days", "2 weeks", "1 month"
     * @return calculated quantity (number of units needed)
     */
    private int calculateMedicineQuantity(String frequency, String duration) {
        try {
            // Parse frequency to get doses per day
            int dosesPerDay = parseFrequency(frequency);

            // Parse duration to get total days
            int totalDays = parseDuration(duration);

            // Calculate total quantity needed
            int quantity = dosesPerDay * totalDays;

            // Add 10% buffer for safety
            quantity = (int) Math.ceil(quantity);

            return Math.max(quantity, 1); // Minimum 1

        } catch (Exception e) {
            // If parsing fails, return default quantity of 10
            System.err.println("Error calculating quantity: " + e.getMessage());
            return 10;
        }
    }

    /**
     * Parses frequency string to extract doses per day
     */
    private int parseFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            return 1;
        }

        frequency = frequency.toLowerCase().trim();

        // Common patterns
        if (frequency.contains("once") || frequency.contains("1 time")) {
            return 1;
        } else if (frequency.contains("twice") || frequency.contains("2 times") ||
                frequency.contains("two times")) {
            return 2;
        } else if (frequency.contains("thrice") || frequency.contains("3 times") ||
                frequency.contains("three times")) {
            return 3;
        } else if (frequency.contains("4 times") || frequency.contains("four times")) {
            return 4;
        }

        // Extract number from patterns like "3 times daily", "2x daily"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(frequency);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // Default to 1 if can't parse
        return 1;
    }

    /**
     * Parses duration string to extract total days
     */
    private int parseDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 7; // Default 7 days
        }

        duration = duration.toLowerCase().trim();

        // Extract number and unit
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("(\\d+)\\s*(day|days|week|weeks|month|months)");
        java.util.regex.Matcher matcher = pattern.matcher(duration);

        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            // Convert to days
            if (unit.startsWith("week")) {
                return number * 7;
            } else if (unit.startsWith("month")) {
                return number * 30;
            } else { // days
                return number;
            }
        }

        // Try to extract just a number (assume days)
        pattern = java.util.regex.Pattern.compile("(\\d+)");
        matcher = pattern.matcher(duration);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // Default to 7 days if can't parse
        return 7;
    }

    /**
     * Display calculated quantity to doctor for confirmation
     */
    private String formatQuantityInfo(int quantity, String frequency, String duration) {
        return String.format("Calculated Quantity: %d tablets/units (%s for %s)",
                quantity, frequency, duration);
    }

    private List<Medicine> medicinesList; // Store loaded medicines

    private void loadMedicines() {
        try {
            medicinesList = inventoryDAO.getAllMedicines();
            medicineCombo.removeAllItems();
            medicineCombo.addItem("-- Select Medicine --");

            for (Medicine medicine : medicinesList) {
                String display = String.format("%s (Stock: %d, ₹%.2f)",
                        medicine.getName(),
                        medicine.getStockQuantity(),
                        medicine.getUnitPrice());
                medicineCombo.addItem(display);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
        }
    }

    private Medicine getSelectedMedicine() {
        int selectedIndex = medicineCombo.getSelectedIndex();
        if (selectedIndex <= 0 || medicinesList == null) {
            return null;
        }
        return medicinesList.get(selectedIndex - 1);
    }

    // Add prescription button handler
    private void addPrescriptionToList() {
        Medicine selectedMedicine = getSelectedMedicine();
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine!");
            return;
        }

        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String duration = durationField.getText().trim();
        String instructions = instructionsArea.getText().trim();

        if (dosage.isEmpty() || frequency.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!");
            return;
        }

        // Check stock availability
        if (selectedMedicine.getStockQuantity() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Medicine out of stock!",
                    "Stock Alert", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Format: "MedicineName | Dosage | Frequency | Duration | Instructions"
        String prescriptionEntry = String.format("%s | %s | %s | %s | %s",
                selectedMedicine.getName(),
                dosage,
                frequency,
                duration,
                instructions);

        prescriptionListModel.addElement(prescriptionEntry);

        // Clear fields
        medicineCombo.setSelectedIndex(0);
        dosageField.setText("");
        frequencyField.setText("");
        durationField.setText("");
        instructionsArea.setText("");
    }

    private void clearPrescriptionForm() {
        symptomsArea.setText("");
        diagnosisArea.setText("");
        prescriptionListModel.clear();
        dosageField.setText("");
        frequencyField.setText("");
        durationField.setText("");
        instructionsArea.setText("");
        medicineCombo.setSelectedIndex(0);
    }

    private int calculateAge(java.time.LocalDate dob) {
        if (dob == null)
            return 0;
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }
}