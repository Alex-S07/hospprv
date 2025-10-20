package hospital.utils;


import java.awt.Color;

public class Constants {
    // Colors
    public static final Color PRIMARY_COLOR = new Color(041, 128, 185);
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    public static final Color WARNING_COLOR = new Color(241, 196, 15);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    public static final Color BACKGROUND_COLOR = new Color(255, 255,255);
    public static final Color CARD_COLOR = Color.BLACK;
    
    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_RECEPTIONIST = "RECEPTIONIST";
    public static final String ROLE_PHARMACY = "PHARMACY";
    public static final String ROLE_LAB_ASSISTANT = "LAB_ASSISTANT";
    
    // Appointment Status
    public static final String APPOINTMENT_SCHEDULED = "SCHEDULED";
    public static final String APPOINTMENT_COMPLETED = "COMPLETED";
    public static final String APPOINTMENT_CANCELLED = "CANCELLED";
    
    // Application Settings
    public static final String APP_NAME = "Hospital Management System";
    public static final String APP_VERSION = "1.0.0";
    
    // Database Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PATIENTS = "patients";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_PRESCRIPTIONS = "prescriptions";
    public static final String TABLE_MEDICINES = "medicines";
    public static final String TABLE_BILLS = "bills";
    public static final String TABLE_AUDIT_LOGS = "audit_logs";
    
    // Date/Time Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
}