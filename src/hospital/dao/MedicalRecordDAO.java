package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.MedicalRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {

    public MedicalRecord createMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, " +
                "visit_date, symptoms, diagnosis) " + // 6 columns
                "VALUES (?, ?, ?, ?, ?, ?)"; // 6 parameters

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, record.getPatientId());
            stmt.setInt(2, record.getDoctorId());
            stmt.setInt(3, record.getAppointmentId());
            stmt.setTimestamp(4, Timestamp.valueOf(record.getVisitDate())); // Changed from getVisitDate
            stmt.setString(5, record.getSymptoms()); // Added this - was missing
            stmt.setString(6, record.getDiagnosis());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    record.setRecordId(rs.getInt(1));
                }
                return record;
            }
        }
        return null;
    }

    public List<MedicalRecord> getPatientMedicalHistory(int patientId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT mr.*, " +
                "p.first_name AS patient_name," +
                "d.full_name AS doctor_name" +
                " FROM medical_records mr" +
                " JOIN patients p ON mr.patient_id = p.patient_id" +
                " JOIN doctors d ON mr.doctor_id = d.doctor_id" +
                " JOIN users u ON d.user_id = u.user_id" +
                " WHERE mr.patient_id = ?" +
                " ORDER BY mr.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToMedicalRecord(rs));
            }
        }
        return records;
    }

    public MedicalRecord getMedicalRecordByAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT mr.*, " +
                "p.first_name AS patient_name," +
                "d.full_name AS doctor_name" +
                " FROM medical_records mr" +
                " JOIN patients p ON mr.patient_id = p.patient_id" +
                " JOIN doctors d ON mr.doctor_id = d.doctor_id" +
                " JOIN users u ON d.user_id = u.user_id" +
                " WHERE mr.appointment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMedicalRecord(rs);
            }
        }
        return null;
    }

    public boolean updateMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = "UPDATE medical_records SET diagnosis = ?, treatment = ?, " +
                "notes = ?, vital_signs = ? WHERE record_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, record.getDiagnosis());
            stmt.setString(2, record.getTreatment());
            stmt.setString(3, record.getNotes());
            stmt.setString(4, record.getVitalSigns());
            stmt.setInt(5, record.getRecordId());

            return stmt.executeUpdate() > 0;
        }
    }

    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setAppointmentId(rs.getInt("appointment_id"));
        record.setPatientId(rs.getInt("patient_id"));
        record.setDoctorId(rs.getInt("doctor_id"));

        Timestamp visitDate = rs.getTimestamp("visit_date");
        if (visitDate != null) {
            record.setVisitDate(visitDate.toLocalDateTime());
        }

        record.setSymptoms(rs.getString("symptoms"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setTreatment(rs.getString("treatment"));
        record.setNotes(rs.getString("notes"));
        record.setVitalSigns(rs.getString("vital_signs"));

        try {
            record.setPatientName(rs.getString("patient_name"));
            record.setDoctorName(rs.getString("doctor_name"));
        } catch (SQLException e) {
            // These fields may not exist in all queries
        }

        return record;
    }
}