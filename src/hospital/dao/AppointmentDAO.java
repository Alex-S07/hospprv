package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.Appointment;
import hospital.models.DoctorSchedule;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public int getNextTokenNumber(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(MAX(token_number), 0) + 1 FROM appointments " +
                "WHERE doctor_id = ? AND DATE(appointment_datetime) = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
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
    
    if (!scheduleDAO.isDoctorAvailable(appointment.getDoctorId(), appointmentDate)) {
        DoctorSchedule schedule = scheduleDAO.getScheduleForDate(appointment.getDoctorId(), appointmentDate);
        String reason = schedule != null && schedule.getReason() != null ? 
            " (" + schedule.getReason() + ")" : "";
        throw new SQLException("Doctor is on LEAVE on this date" + reason);
    }
    
    String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, " +
            "token_number, consultation_fee, status, created_by, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

    try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setInt(1, appointment.getPatientId());
        stmt.setInt(2, appointment.getDoctorId());
        stmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDateTime()));
        stmt.setInt(4, appointment.getTokenNumber());
        stmt.setDouble(5, appointment.getConsultationFee());
        stmt.setString(6, appointment.getStatus());
        stmt.setInt(7, appointment.getCreatedBy());

        int result = stmt.executeUpdate();
        if (result > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                appointment.setAppointmentId(rs.getInt(1));
            }
            return appointment;
        }
    }
    return null;
}

    public List<Appointment> getTodaysAppointments(int doctorId) throws SQLException {
    // First check if doctor is on duty today
    DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
    if (!scheduleDAO.isDoctorAvailable(doctorId, LocalDate.now())) {
        // Return empty list if doctor is on leave
        return new ArrayList<>();
    }
    
    List<Appointment> appointments = new ArrayList<>();
    String sql = "SELECT a.*, " +
            "p.first_name AS patient_name, " +
            "u.username AS doctor_name " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN users u ON a.doctor_id = u.user_id " +
            "WHERE a.doctor_id = ? AND DATE(a.appointment_datetime) = CURDATE() " +
            "ORDER BY a.token_number";

    try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            appointments.add(mapResultSetToAppointment(rs));
        }
    }
    return appointments;
}

    // Overloaded method for specific doctor
    // public List<Appointment> getTodaysAppointments(int doctorId) throws SQLException {
    //     List<Appointment> appointments = new ArrayList<>();
    //     String sql = "SELECT a.*, " +
    //             "p.first_name AS patient_name, " +
    //             "u.username AS doctor_name " +
    //             "FROM appointments a " +
    //             "JOIN patients p ON a.patient_id = p.patient_id " +
    //             "JOIN users u ON a.doctor_id = u.user_id " +
    //             "WHERE a.doctor_id = ? AND DATE(a.appointment_datetime) = CURDATE() " +
    //             "ORDER BY a.token_number";

    //     try (Connection conn = DatabaseConfig.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setInt(1, doctorId);
    //         ResultSet rs = stmt.executeQuery();

    //         while (rs.next()) {
    //             appointments.add(mapResultSetToAppointment(rs));
    //         }
    //     }
    //     return appointments;
    // }

    // Get appointments by date range
    public List<Appointment> getAppointmentsByDateRange(int doctorId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " +
                "WHERE a.doctor_id = ? AND DATE(a.appointment_datetime) BETWEEN ? AND ? " + // ✅ FIXED: Proper date
                                                                                            // range
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
        String sql = "SELECT a.*, " +
                "p.first_name AS patient_name, " +
                "u.username AS doctor_name " + // ✅ FIXED: Changed to users table
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users u ON a.doctor_id = u.user_id " + // ✅ FIXED: Changed to users table
                "WHERE a.appointment_id = ?"; // ✅ FIXED: Correct WHERE clause

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
    public Appointment getPatientAppointmentOnDate(int patientId, LocalDate date) throws SQLException {
        String sql = "SELECT a.*, " +
                "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "d.full_name AS doctor_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
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
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));

        Timestamp ts = rs.getTimestamp("appointment_datetime");
        if (ts != null) {
            appointment.setAppointmentDateTime(ts.toLocalDateTime());
        }
        appointment.setTokenNumber(rs.getInt("token_number"));
        appointment.setConsultationFee(rs.getDouble("consultation_fee"));
        appointment.setStatus(rs.getString("status"));
        appointment.setPatientName(rs.getString("patient_name"));
        appointment.setDoctorName(rs.getString("doctor_name"));

        return appointment;
    }
}