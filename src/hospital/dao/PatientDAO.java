package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    
    public Patient createPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, " +
                    "medical_history, registered_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getMedicalHistory());
            stmt.setTimestamp(6, Timestamp.valueOf(patient.getRegisteredAt()));
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    patient.setPatientId(rs.getInt(1));
                }
                return patient;
            }
        }
        return null;
    }
    
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY registered_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        }
        return patients;
    }
    
    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPatient(rs);
            }
        }
        return null;
    }
    
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE " +
                    "first_name LIKE ? OR last_name LIKE ? OR " +
                    "CONCAT(first_name, ' ', last_name) LIKE ? OR " +
                    "patient_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try {
                stmt.setInt(4, Integer.parseInt(searchTerm));
            } catch (NumberFormatException e) {
                stmt.setInt(4, -1);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        }
        return patients;
    }
    
    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                    "gender = ?, medical_history = ? WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getMedicalHistory());
            stmt.setInt(6, patient.getPatientId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public int getTotalPatientsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public int getTodayRegistrationsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE DATE(registered_at) = CURDATE()";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }
        
        patient.setGender(rs.getString("gender"));
        patient.setMedicalHistory(rs.getString("medical_history"));
        
        Timestamp regAt = rs.getTimestamp("registered_at");
        if (regAt != null) {
            patient.setRegisteredAt(regAt.toLocalDateTime());
        }
        
        return patient;
    }
}