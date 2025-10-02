package hospital.models;

import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private LocalDateTime appointmentDateTime;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private int tokenNumber;
    private double consultationFee;
    private LocalDateTime createdAt;
    private int createdBy;

    // Additional fields for display purposes
    private String patientName;
    private String doctorName;
    private String appointmentType;

    // Constructors
    public Appointment() {
    }

    // Add these constants to your Appointment model or create a separate class
    public class AppointmentStatus {
        public static final String SCHEDULED = "SCHEDULED";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";
        public static final String NO_SHOW = "NO_SHOW";
    }

    public Appointment(int patientId, int doctorId, LocalDateTime appointmentDateTime,
            String symptoms, double consultationFee) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.consultationFee = consultationFee;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // Display fields
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Appointment(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    // getter
    public String getAppointmentType() {
        return appointmentType;
    }

    // setter
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
}
