package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.Prescription;
import hospital.models.Medicine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PrescriptionDAO {

    public List<Prescription> getTodaysPrescriptions() throws SQLException {
        String sql = "SELECT p.*, " +
                "COALESCE(m.name, p.medicine_name) AS medicine_name, " +
                "COALESCE(CONCAT(pat.first_name, ' ', pat.last_name), 'Unknown Patient') AS patient_name, " +
                "COALESCE(u.username, 'Unknown Doctor') AS doctor_name, " +  // Changed to users table
                "mr.diagnosis, " +
                "COALESCE(p.status, 'Pending') AS prescription_status " +
                "FROM prescriptions p " +
                "LEFT JOIN medical_records mr ON p.record_id = mr.record_id " +
                "LEFT JOIN patients pat ON mr.patient_id = pat.patient_id " +
                "LEFT JOIN users u ON mr.doctor_id = u.user_id " +  // Join with users table instead of doctors
                "LEFT JOIN medicines m ON p.medicine_id = m.medicine_id " +
                "WHERE DATE(p.created_at) = CURDATE() " +
                "ORDER BY p.created_at DESC, p.record_id";

        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(rs.getInt("prescription_id"));
                prescription.setRecordId(rs.getInt("record_id"));
                prescription.setMedicineId(rs.getInt("medicine_id"));
                prescription.setMedicineName(rs.getString("medicine_name"));
                prescription.setFrequency(rs.getString("frequency"));
                prescription.setDuration(rs.getString("duration"));
                prescription.setPatientName(rs.getString("patient_name"));
                prescription.setDoctorName(rs.getString("doctor_name"));
                prescription.setStatus(rs.getString("prescription_status"));

                int quantity = rs.getInt("quantity");
                prescription.setQuantity(quantity > 0 ? quantity : 1);

                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    prescription.setCreatedAt(timestamp.toLocalDateTime());
                }

                prescriptions.add(prescription);
            }
        }

        return prescriptions;
    }

    public List<Prescription> getPrescriptionsByRecordId(int recordId) throws SQLException {
        String sql = "SELECT p.*, " +
                "COALESCE(m.name, p.medicine_name) AS medicine_name, " +
                "COALESCE(CONCAT(pat.first_name, ' ', pat.last_name), 'Unknown Patient') AS patient_name, " +
                "COALESCE(u.username, 'Unknown Doctor') AS doctor_name, " +  // Changed to users table
                "mr.diagnosis, " +
                "COALESCE(p.status, 'Pending') AS prescription_status " +
                "FROM prescriptions p " +
                "LEFT JOIN medical_records mr ON p.record_id = mr.record_id " +
                "LEFT JOIN patients pat ON mr.patient_id = pat.patient_id " +
                "LEFT JOIN users u ON mr.doctor_id = u.user_id " +  // Join with users table instead of doctors
                "LEFT JOIN medicines m ON p.medicine_id = m.medicine_id " +
                "WHERE p.record_id = ?";

        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recordId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(rs.getInt("prescription_id"));
                prescription.setRecordId(rs.getInt("record_id"));
                prescription.setMedicineId(rs.getInt("medicine_id"));
                prescription.setMedicineName(rs.getString("medicine_name"));
                prescription.setFrequency(rs.getString("frequency"));
                prescription.setDuration(rs.getString("duration"));
                prescription.setPatientName(rs.getString("patient_name"));
                prescription.setDoctorName(rs.getString("doctor_name"));
                prescription.setStatus(rs.getString("prescription_status"));

                int quantity = rs.getInt("quantity");
                prescription.setQuantity(quantity > 0 ? quantity : 1);

                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    prescription.setCreatedAt(timestamp.toLocalDateTime());
                }

                prescriptions.add(prescription);
            }
        }

        return prescriptions;
    }

    public List<Prescription> getDispensedHistory() throws SQLException {
        String sql = "SELECT p.*, " +
                "COALESCE(m.name, p.medicine_name) AS medicine_name, " +
                "COALESCE(CONCAT(pat.first_name, ' ', pat.last_name), 'Unknown Patient') AS patient_name, " +
                "COALESCE(u.username, 'Unknown Doctor') AS doctor_name, " +  // Changed to users table
                "pharm.username AS pharmacist_name " +
                "FROM prescriptions p " +
                "LEFT JOIN medical_records mr ON p.record_id = mr.record_id " +
                "LEFT JOIN patients pat ON mr.patient_id = pat.patient_id " +
                "LEFT JOIN users u ON mr.doctor_id = u.user_id " +  // Join with users table instead of doctors
                "LEFT JOIN medicines m ON p.medicine_id = m.medicine_id " +
                "LEFT JOIN users pharm ON p.dispensed_by = pharm.user_id " +
                "WHERE p.status = 'Dispensed' " +
                "ORDER BY p.dispensed_at DESC " +
                "LIMIT 100";

        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(rs.getInt("prescription_id"));
                prescription.setRecordId(rs.getInt("record_id"));
                prescription.setMedicineId(rs.getInt("medicine_id"));
                prescription.setMedicineName(rs.getString("medicine_name"));
                prescription.setPatientName(rs.getString("patient_name"));
                prescription.setDoctorName(rs.getString("doctor_name"));
                prescription.setFrequency(rs.getString("frequency"));
                prescription.setDuration(rs.getString("duration"));
                prescription.setStatus("Dispensed");

                int quantity = rs.getInt("quantity");
                prescription.setQuantity(quantity > 0 ? quantity : 1);

                java.sql.Timestamp timestamp = rs.getTimestamp("dispensed_at");
                if (timestamp != null) {
                    prescription.setDispensedAt(timestamp.toLocalDateTime());
                }

                prescriptions.add(prescription);
            }
        }

        return prescriptions;
    }

    // Other methods remain the same...
    public Prescription createPrescription(Prescription p) throws SQLException {
        String sql = "INSERT INTO prescriptions " +
                     "(record_id, medicine_name, frequency, duration, quantity, total_amount, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getRecordId());
            stmt.setString(2, p.getMedicineName());
            stmt.setString(3, p.getFrequency());
            stmt.setString(4, p.getDuration());
            stmt.setInt(5, p.getQuantity());
            stmt.setDouble(6, p.getTotalCost());
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(p.getCreatedAt()));
            stmt.executeUpdate();
            return p;
        }
    }

    public boolean deletePrescription(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean markAsDispensed(int recordId, int pharmacistId) throws SQLException {
        String sql = "UPDATE prescriptions SET status = 'Dispensed', " +
                "dispensed_by = ?, dispensed_at = NOW() " +
                "WHERE record_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pharmacistId);
            pstmt.setInt(2, recordId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}