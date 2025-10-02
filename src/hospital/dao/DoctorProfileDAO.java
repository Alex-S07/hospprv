package hospital.dao;


import hospital.models.Doctor;
import hospital.models.DoctorSchedule;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoctorProfileDAO extends BaseDAO {
    
    // Check if doctor profile is complete
    public boolean isDoctorProfileComplete(int doctorId) throws SQLException {
        String sql = "SELECT profile_completed FROM users WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("profile_completed");
            }
        }
        return false;
    }
    
    // Get doctor profile by ID
    public Doctor getDoctorProfile(int doctorId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ? AND role = 'DOCTOR'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Doctor profile = new Doctor();
                profile.setDoctorId(rs.getInt("user_id"));
                profile.setUsername(rs.getString("full_name"));
                profile.setSpecialization(rs.getString("specialization"));
                profile.setExperienceYears(rs.getInt("experience_years"));
                profile.setPhone(rs.getString("phone"));
                profile.setEmail(rs.getString("email"));
                profile.setConsultationFee(rs.getDouble("consultation_fee"));
                profile.setProfileCompleted(rs.getBoolean("profile_completed"));
                return profile;
            }
        }
        return null;
    }
    
    // Update doctor profile
    public boolean updateDoctorProfile(Doctor profile) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, specialization = ?, " +
                    "experience_years = ?, phone = ?, email = ?, consultation_fee = ?, " +
                    "profile_completed = TRUE WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profile.getUsername());
            stmt.setString(2, profile.getSpecialization());
            stmt.setInt(4, profile.getExperienceYears());
            stmt.setString(5, profile.getPhone());
            stmt.setString(6, profile.getEmail());
            stmt.setDouble(7, profile.getConsultationFee());
            stmt.setInt(8, profile.getDoctorId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Set doctor schedule (DUTY/LEAVE)
    public boolean setDoctorSchedule(DoctorSchedule schedule) throws SQLException {
        String sql = "INSERT INTO doctor_schedules (doctor_id, schedule_date, status, reason) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE status = ?, reason = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, schedule.getDoctorId());
            stmt.setDate(2, Date.valueOf(schedule.getScheduleDate()));
            stmt.setString(3, schedule.getStatus());
            stmt.setString(4, schedule.getReason());
            stmt.setString(5, schedule.getStatus());
            stmt.setString(6, schedule.getReason());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Get doctor schedules for a date range
    public List<DoctorSchedule> getDoctorSchedules(int doctorId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        String sql = "SELECT * FROM doctor_schedules " +
                    "WHERE doctor_id = ? AND schedule_date BETWEEN ? AND ? " +
                    "ORDER BY schedule_date";
        
        List<DoctorSchedule> schedules = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setDoctorId(rs.getInt("doctor_id"));
                schedule.setScheduleDate(rs.getDate("schedule_date").toLocalDate());
                schedule.setStatus(rs.getString("status"));
                schedule.setReason(rs.getString("reason"));
                schedules.add(schedule);
            }
        }
        return schedules;
    }
    
    // Check if doctor is on duty for a specific date
    public boolean isDoctorOnDuty(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT status FROM doctor_schedules " +
                    "WHERE doctor_id = ? AND schedule_date = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return "DUTY".equals(status);
            }
        }
        // If no schedule record, default to ON DUTY
        return true;
    }
    
    // Get available doctors by department for a specific date
    public List<Integer> getAvailableDoctorsByDepartment(String department, LocalDate date) 
            throws SQLException {
        String sql = "SELECT u.user_id FROM users u " +
                    "LEFT JOIN doctor_schedules ds ON u.user_id = ds.doctor_id AND ds.schedule_date = ? " +
                    "WHERE u.role = 'DOCTOR' AND u.status = 'Active' " +
                    "AND u.specialization = ? AND u.profile_completed = TRUE " +
                    "AND (ds.status IS NULL OR ds.status = 'DUTY')";
        
        List<Integer> doctorIds = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            stmt.setString(2, department);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                doctorIds.add(rs.getInt("user_id"));
            }
        }
        return doctorIds;
    }
}
