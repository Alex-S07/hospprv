package hospital.views.receptionist;

import hospital.dao.AppointmentDAO;
import hospital.dao.PatientDAO;
import hospital.dao.DoctorDAO;
import hospital.dao.DoctorScheduleDAO;
import hospital.models.Appointment;
import hospital.models.Patient;
import hospital.models.Doctor;
import hospital.models.DoctorSchedule;
import hospital.models.User;
import hospital.utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;  // ADD THIS IMPORT
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class AppointmentBookingPanel extends JPanel {
    private User currentUser;
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;

    private JTextField patientSearchField;
    private JLabel selectedPatientLabel;
    private JComboBox<String> doctorCombo;
    private JTextField appointmentDateField;
    private JTextArea symptomsArea;
    private JLabel tokenNumberLabel;
    private JLabel consultationFeeLabel;

    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    private JButton searchPatientBtn;
    private JButton bookAppointmentBtn;
    private JButton clearBtn;

    private Patient selectedPatient;
    private List<Doctor> doctors;

    public AppointmentBookingPanel(User user) {
        this.currentUser = user;
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDoctors();
        loadTodaysAppointments();
    }

    private void initializeComponents() {
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("OptionPane.buttonForeground", Color.BLACK);
       UIManager.put("Button.disabledText", Color.DARK_GRAY);
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        patientSearchField = new JTextField(15);
        selectedPatientLabel = new JLabel("No patient selected");
        selectedPatientLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        selectedPatientLabel.setForeground(Constants.DANGER_COLOR);

        doctorCombo = new JComboBox<>();
        appointmentDateField = new JTextField(LocalDate.now().toString(), 15);
        symptomsArea = new JTextArea(3, 20);
        symptomsArea.setLineWrap(true);

        tokenNumberLabel = new JLabel("Token: -");
        tokenNumberLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        consultationFeeLabel = new JLabel("Fee: ₹0.00");
        consultationFeeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        searchPatientBtn = new JButton("Search Patient");
        bookAppointmentBtn = new JButton("Book Appointment");
        clearBtn = new JButton("Clear");

        styleButton(searchPatientBtn, Constants.PRIMARY_COLOR);
        styleButton(bookAppointmentBtn, Constants.SUCCESS_COLOR);
        styleButton(clearBtn, Constants.SECONDARY_COLOR);

        bookAppointmentBtn.setEnabled(false);

        String[] columns = { "Token", "Patient", "Doctor", "Date","Fee", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setRowHeight(28);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));

        JLabel titleLabel = new JLabel("Book Appointment");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Appointment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Patient Selection
        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Patient ID/Name:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientSearchField, gbc);
        gbc.gridx = 2;
        formPanel.add(searchPatientBtn, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Selected:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(selectedPatientLabel, gbc);
        gbc.gridwidth = 1;
        row++;

        // Doctor Selection
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Doctor:*"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(doctorCombo, gbc);
        gbc.gridwidth = 1;
        row++;

        // Date
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Date:* (YYYY-MM-DD)"), gbc);
        gbc.gridx = 1;
        formPanel.add(appointmentDateField, gbc);
        row++;

        // Token and Fee Display
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(tokenNumberLabel);
        infoPanel.add(consultationFeeLabel);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        formPanel.add(infoPanel, gbc);
        row++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(bookAppointmentBtn);
        buttonPanel.add(clearBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        formPanel.add(buttonPanel, gbc);

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Constants.BACKGROUND_COLOR);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        searchPatientBtn.addActionListener(e -> searchPatient());
        bookAppointmentBtn.addActionListener(e -> bookAppointment());
        clearBtn.addActionListener(e -> clearFields());

        doctorCombo.addActionListener(e -> updateConsultationFee());
    }

    private void loadDoctors() {
        try {
            doctors = doctorDAO.getAllDoctors();
            doctorCombo.removeAllItems();
            for (Doctor doctor : doctors) {
                // Show user_id in the combo box since that's what appointments table uses
                doctorCombo.addItem(doctor.getUserId() + " - Dr. " + doctor.getUsername());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + e.getMessage());
        }
    }

    private void searchPatient() {
        String search = patientSearchField.getText().trim();
        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Patient ID or Name!");
            return;
        }

        try {
            // Try by ID first
            try {
                int patientId = Integer.parseInt(search);
                selectedPatient = patientDAO.getPatientById(patientId);
            } catch (NumberFormatException e) {
                // Search by name
                List<Patient> patients = patientDAO.searchPatients(search);
                if (!patients.isEmpty()) {
                    if (patients.size() == 1) {
                        selectedPatient = patients.get(0);
                    } else {
                        // Show selection dialog
                        selectedPatient = showPatientSelectionDialog(patients);
                    }
                }
            }

            if (selectedPatient != null) {
                selectedPatientLabel.setText("ID: " + selectedPatient.getPatientId() +
                        " - " + selectedPatient.getFirstName());
                selectedPatientLabel.setForeground(Constants.SUCCESS_COLOR);
                bookAppointmentBtn.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Patient not found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private Patient showPatientSelectionDialog(List<Patient> patients) {
        String[] options = patients.stream()
                .map(p -> "ID: " + p.getPatientId() + " - " + p.getFirstName())
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Multiple patients found. Select one:",
                "Patient Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            int index = java.util.Arrays.asList(options).indexOf(selected);
            return patients.get(index);
        }
        return null;
    }

    private void updateConsultationFee() {
        int selectedIndex = doctorCombo.getSelectedIndex();
        if (selectedIndex >= 0 && doctors != null && selectedIndex < doctors.size()) {
            Doctor doctor = doctors.get(selectedIndex);
            consultationFeeLabel.setText("Fee: ₹" + String.format("%.2f", doctor.getConsultationFee()));
            
            // Debug info
            // System.out.println("Selected Doctor - User ID: " + doctor.getUserId() + 
            //                   ", Doctor ID: " + doctor.getDoctorId() + 
            //                   ", Fee: " + doctor.getConsultationFee());
        }
    }

    private void bookAppointment() {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient!");
            return;
        }

        if (doctorCombo.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor!");
            return;
        }

        try {
            int doctorIndex = doctorCombo.getSelectedIndex();
            Doctor doctor = doctors.get(doctorIndex);
            LocalDate appointmentDate = LocalDate.parse(appointmentDateField.getText().trim());
            double consultationFee = doctor.getConsultationFee();

            // System.out.println("DEBUG: Doctor: " + doctor.getUsername() + 
            //                   ", User ID: " + doctor.getUserId() +
            //                   ", Doctor ID: " + doctor.getDoctorId() +
            //                   ", Consultation Fee: " + doctor.getConsultationFee());
            
            //  FIX: Use user_id for all doctor-related operations
            int doctorUserId = doctor.getUserId();
            
            //  CHECK 1: Check if doctor is available (not on leave)
            DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
            if (!scheduleDAO.isDoctorAvailable(doctorUserId, appointmentDate)) {
                DoctorSchedule schedule = scheduleDAO.getScheduleForDate(doctorUserId, appointmentDate);
                String reason = schedule != null && schedule.getReason() != null ? "\nReason: " + schedule.getReason()
                        : "";

                JOptionPane.showMessageDialog(this,
                        "Dr. " + doctor.getUsername() + " is on LEAVE on " + appointmentDate + reason +
                                "\n\nPlease select a different date or doctor.",
                        "Doctor Not Available",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            //  CHECK 2: Prevent same patient-doctor duplicate
            if (appointmentDAO.hasAppointmentWithDoctor(
                    selectedPatient.getPatientId(),
                    doctorUserId, // Use user_id here
                    appointmentDate)) {
                JOptionPane.showMessageDialog(this,
                        "This patient already has an appointment with Dr. " + doctor.getUsername() +
                                " on " + appointmentDate + "!",
                        "Duplicate Appointment",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ✅ CHECK 3: Warn if patient has appointment with different doctor
            if (appointmentDAO.hasAppointmentOnDate(
                    selectedPatient.getPatientId(),
                    appointmentDate)) {

                Appointment existingAppt = appointmentDAO.getPatientAppointmentOnDate(
                        selectedPatient.getPatientId(),
                        appointmentDate);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "WARNING: This patient already has an appointment on " + appointmentDate + "\n" +
                                "Existing: Dr. " + existingAppt.getDoctorName() +
                                " (Token #" + existingAppt.getTokenNumber() + ")\n\n" +
                                "Do you want to book another appointment with a different doctor?",
                        "Multiple Appointments Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Get next token for this doctor on this date
            int tokenNumber = appointmentDAO.getNextTokenNumber(doctorUserId, appointmentDate); // Use user_id

            Appointment appointment = new Appointment();
            appointment.setPatientId(selectedPatient.getPatientId());
            appointment.setDoctorId(doctorUserId); // Use user_id here
            appointment.setAppointmentDateTime(appointmentDate.atStartOfDay());
            appointment.setTokenNumber(tokenNumber);
            appointment.setConsultationFee(consultationFee);
            appointment.setStatus("SCHEDULED");
            appointment.setCreatedBy(currentUser.getUserId());

            Appointment created = appointmentDAO.createAppointment(appointment);

            if (created != null) {
                JOptionPane.showMessageDialog(this,
                        "Appointment booked successfully!\n" +
                                "Token Number: " + tokenNumber + "\n" +
                                "Patient: " + selectedPatient.getFirstName() + "\n" +
                                "Doctor: Dr. " + doctor.getUsername() + "\n" +
                                "Date: " + appointmentDate + "\n" +
                                "Consultation Fee: ₹" + String.format("%.2f", consultationFee),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                loadTodaysAppointments();
                clearFields();
            }
        } catch (SQLException e) {
            // Handle SQL errors specifically
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error booking appointment: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadTodaysAppointments() {
        try {
            // show all today's appointments (not filtered by current user/doctor)
            List<Appointment> appointments = appointmentDAO.getTodaysAppointments();
            displayAppointments(appointments);  // FIXED: removed extra 'i'
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage());
        }
    }

    private void displayAppointments(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment apt : appointments) {
            // Skip null appointments
            if (apt == null) {
                System.err.println("DEBUG: Found null appointment, skipping");
                continue;
            }
            
            Object[] row = {
                apt.getTokenNumber(),
                apt.getPatientName() != null ? apt.getPatientName() : "Unknown Patient",
                apt.getDoctorName() != null ? apt.getDoctorName() : "Unknown Doctor",
                apt.getAppointmentDateTime() != null ? apt.getAppointmentDateTime().toLocalDate() : LocalDate.now(),
                "₹" + String.format("%.2f", apt.getConsultationFee()),
                apt.getStatus() != null ? apt.getStatus() : "UNKNOWN"
            };
            tableModel.addRow(row);
        }
    }

    private void clearFields() {
        patientSearchField.setText("");
        selectedPatientLabel.setText("No patient selected");
        selectedPatientLabel.setForeground(Constants.DANGER_COLOR);
        symptomsArea.setText("");
        tokenNumberLabel.setText("Token: -");

        selectedPatient = null;
        bookAppointmentBtn.setEnabled(false);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 38));
    }

    public void refreshData() {
        loadTodaysAppointments();
        loadDoctors();
    }
}