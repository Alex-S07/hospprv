package hospital.dao;


import hospital.config.DatabaseConfig;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardDAO {
    
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total Patients
            stats.put("totalPatients", getCount(conn, "SELECT COUNT(*) FROM patients"));
            
            // Total Doctors
            stats.put("totalDoctors", getCount(conn, "SELECT COUNT(*) FROM doctors WHERE is_available = true"));
            
            // Total Users
            stats.put("totalUsers", getCount(conn, "SELECT COUNT(*) FROM users WHERE is_active = true"));
            
            // Today's Appointments
            stats.put("todayAppointments", getCount(conn, 
                "SELECT COUNT(*) FROM appointments WHERE DATE(appointment_datetime) = CURDATE()"));
            
            // Total Revenue
            stats.put("totalRevenue", getDecimal(conn, 
                "SELECT COALESCE(SUM(paid_amount), 0) FROM bills"));
            
            // Pending Bills
            stats.put("pendingBills", getCount(conn, 
                "SELECT COUNT(*) FROM bills WHERE payment_status != 'PAID'"));
        }
        
        return stats;
    }
    
    private int getCount(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private double getDecimal(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
}