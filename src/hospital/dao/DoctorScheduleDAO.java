package hospital.dao;


import hospital.config.DatabaseConfig;
import hospital.models.DoctorSchedule;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleDAO {
    
    public boolean isDoctorAvailable(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT status FROM doctor_schedules " +
                    "WHERE doctor_id = ? AND schedule_date = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return "DUTY".equalsIgnoreCase(status);
            }
        }
        
        // If no schedule entry exists, assume doctor is available (DUTY by default)
        return true;
    }
    
    public DoctorSchedule getScheduleForDate(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM doctor_schedules " +
                    "WHERE doctor_id = ? AND schedule_date = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setDoctorId(rs.getInt("doctor_id"));
                schedule.setScheduleDate(rs.getDate("schedule_date").toLocalDate());
                schedule.setStatus(rs.getString("status"));
                schedule.setReason(rs.getString("reason"));
                return schedule;
            }
        }
        return null;
    }
    
    public List<LocalDate> getDoctorLeaveDates(int doctorId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        List<LocalDate> leaveDates = new ArrayList<>();
        
        String sql = "SELECT schedule_date FROM doctor_schedules " +
                    "WHERE doctor_id = ? AND status = 'LEAVE' " +
                    "AND schedule_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                leaveDates.add(rs.getDate("schedule_date").toLocalDate());
            }
        }
        return leaveDates;
    }
}