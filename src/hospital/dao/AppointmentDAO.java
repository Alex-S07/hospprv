package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.Appointment;
import hospital.models.DoctorSchedule;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

     public int getNextTokenNumber(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(MAX(token_number), 0) + 1 FROM appointments " +
                "WHERE doctor_id = ? AND DATE(appointment_datetime) = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);  // Use user_id here
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

       public Appointment createAppointment(Appointment appointment) throws SQLException {
        // Check if doctor is available on that date
        DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
        LocalDate appointmentDate = appointment.getAppointmentDateTime().toLocalDate();
        
        // Note: You'll need to update DoctorScheduleDAO to also work with user_id
        if (!scheduleDAO.isDoctorAvailable(appointment.getDoctorId(), appointmentDate)) {
            DoctorSchedule schedule = scheduleDAO.getScheduleForDate(appointment.getDoctorId(), appointmentDate);
            String reason = schedule != null && schedule.getReason() != null ? 
                " (" + schedule.getReason() + ")" : "";
            throw new SQLException("Doctor is on LEAVE on this date" + reason);
        }
        
        // Get consultation fee using user_id
        DoctorDAO doctorDAO = new DoctorDAO();
        double consultationFee = doctorDAO.getConsultationFeeByUserId(appointment.getDoctorId());
        appointment.setConsultationFee(consultationFee);
        
        // Get next token number using user_id
        int tokenNumber = getNextTokenNumber(appointment.getDoctorId(), appointmentDate);
        appointment.setTokenNumber(tokenNumber);
        
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, " +
                "token_number, consultation_fee, status, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());  // This should be user_id now
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            stmt.setInt(4, appointment.getTokenNumber());
            stmt.setDouble(5, appointment.getConsultationFee());
            stmt.setString(6, appointment.getStatus());
            stmt.setInt(7, appointment.getCreatedBy());

            // System.out.println("DEBUG: Inserting appointment with doctor_id (user_id): " + appointment.getDoctorId());
            // System.out.println("DEBUG: Consultation fee: " + appointment.getConsultationFee());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int appointmentId = rs.getInt(1);
                    return getAppointmentById(appointmentId);
                }
            }
        }
        return null;
    }

    // Update other methods to join with users table instead of doctors table
    public List<Appointment> getTodaysAppointments(int userId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, COALESCE(a.consultation_fee,0) AS consultation_fee, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " +  // From users table
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " +  // Join with users
                "WHERE a.doctor_id = ? AND DATE(a.appointment_datetime) = CURDATE() " +
                "ORDER BY a.token_number";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    /**
     * Get all appointments for today (no doctor filter)
     */
    public List<Appointment> getTodaysAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, COALESCE(a.consultation_fee,0) AS consultation_fee, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " +
                "WHERE DATE(a.appointment_datetime) = CURDATE() " +
                "ORDER BY a.token_number";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    // Get appointments by date range
    public List<Appointment> getAppointmentsByDateRange(int doctorId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, COALESCE(a.consultation_fee,0) AS consultation_fee, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " +
                "WHERE a.doctor_id = ? AND DATE(a.appointment_datetime) BETWEEN ? AND ? " +
                "ORDER BY a.appointment_datetime, a.token_number";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    // Update appointment status
    public boolean updateAppointmentStatus(int appointmentId, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Get appointment by ID
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String sql = "SELECT a.*, COALESCE(a.consultation_fee,0) AS consultation_fee, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " +
                "WHERE a.appointment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAppointment(rs);
            }
        }
        return null;
    }

    // Add this method to AppointmentDAO.java

    /**
     * Check if patient already has an appointment on the given date
     * 
     * @return true if patient already has appointment, false otherwise
     */
    public boolean hasAppointmentOnDate(int patientId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE patient_id = ? AND DATE(appointment_datetime) = ? " +
                "AND status NOT IN ('CANCELLED', 'COMPLETED')";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Check if patient already has appointment with specific doctor on given date
     * 
     * @return true if appointment exists, false otherwise
     */
    public boolean hasAppointmentWithDoctor(int patientId, int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE patient_id = ? AND doctor_id = ? AND DATE(appointment_datetime) = ? " +
                "AND status NOT IN ('CANCELLED', 'COMPLETED')";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setDate(3, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Get patient's existing appointment for the date
     */
    /**
 * Get patient's existing appointment for the date
 */
public Appointment getPatientAppointmentOnDate(int patientId, LocalDate date) throws SQLException {
    String sql = "SELECT a.*, " +
            "p.first_name AS patient_name, " +
            "u.username AS doctor_name " +  // Changed from d.full_name to u.username
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN users u ON a.doctor_id = u.user_id " +  // Changed from doctors d to users u
            "WHERE a.patient_id = ? " +
            "AND DATE(a.appointment_datetime) = ? " +
            "AND a.status NOT IN ('CANCELLED', 'COMPLETED')";

    try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, patientId);
        stmt.setDate(2, Date.valueOf(date));

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return mapResultSetToAppointment(rs);
        }
    }
    return null;
}

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
    try {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));

        Timestamp ts = rs.getTimestamp("appointment_datetime");
        if (ts != null) {
            appointment.setAppointmentDateTime(ts.toLocalDateTime());
        }
        appointment.setTokenNumber(rs.getInt("token_number"));

        // safe read for consultation_fee
        double fee = 0.0;
        try {
            fee = rs.getDouble("consultation_fee");
            if (rs.wasNull()) fee = 0.0;
        } catch (SQLException ex) {
            fee = 0.0;
        }
        appointment.setConsultationFee(fee);

        appointment.setStatus(rs.getString("status"));
        
        // Debug the names
        String patientName = rs.getString("patient_name");
        String doctorName = rs.getString("doctor_name");
        
        // System.out.println("DEBUG - Patient Name: " + patientName + ", Doctor Name: " + doctorName);
        
        appointment.setPatientName(patientName);
        appointment.setDoctorName(doctorName);

        return appointment;
    } catch (Exception e) {
        System.err.println("Error in mapResultSetToAppointment: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
}