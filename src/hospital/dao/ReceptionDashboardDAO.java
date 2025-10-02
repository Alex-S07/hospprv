package hospital.dao;


import hospital.config.DatabaseConfig;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReceptionDashboardDAO {
    
    public Map<String, Object> getReceptionStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            stats.put("totalPatients", getCount(conn, "SELECT COUNT(*) FROM patients"));
            stats.put("todayAppointments", getCount(conn, 
                "SELECT COUNT(*) FROM appointments WHERE DATE(appointment_datetime) = CURDATE()"));
            stats.put("todayRegistrations", getCount(conn, 
                "SELECT COUNT(*) FROM patients WHERE DATE(registered_at) = CURDATE()"));
            stats.put("pendingBills", getCount(conn, 
                "SELECT COUNT(*) FROM bills WHERE payment_status != 'PAID'"));
        }
        
        return stats;
    }
    
    private int getCount(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}