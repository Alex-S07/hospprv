package hospital.dao;


import hospital.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;

public class ReportDAO {
    
    public String generateReport(String reportType) throws SQLException {
        switch (reportType) {
            case "Daily Revenue Report":
                return generateDailyRevenueReport();
            case "Monthly Revenue Report":
                return generateMonthlyRevenueReport();
            case "Patient Demographics":
                return generatePatientDemographicsReport();
            case "Doctor Workload Report":
                return generateDoctorWorkloadReport();
            case "Appointment Summary":
                return generateAppointmentSummaryReport();
            default:
                return "Unknown report type";
        }
    }
    
    private String generateDailyRevenueReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("DAILY REVENUE REPORT\n");
        report.append("Date: ").append(LocalDate.now()).append("\n");
        report.append("=".repeat(60)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT " +
                        "COUNT(*) as total_bills, " +
                        "SUM(total_amount) as total_revenue, " +
                        "SUM(paid_amount) as paid_amount, " +
                        "SUM(total_amount - paid_amount) as pending_amount " +
                        "FROM bills WHERE DATE(created_at) = CURDATE()";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    report.append(String.format("Total Bills: %d\n", rs.getInt("total_bills")));
                    report.append(String.format("Total Revenue: ₹%.2f\n", rs.getDouble("total_revenue")));
                    report.append(String.format("Paid Amount: ₹%.2f\n", rs.getDouble("paid_amount")));
                    report.append(String.format("Pending Amount: ₹%.2f\n", rs.getDouble("pending_amount")));
                }
            }
        }
        
        return report.toString();
    }
    
    private String generateMonthlyRevenueReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("MONTHLY REVENUE REPORT\n");
        report.append("Month: ").append(LocalDate.now().getMonth()).append(" ")
              .append(LocalDate.now().getYear()).append("\n");
        report.append("=".repeat(60)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT " +
                        "COUNT(*) as total_bills, " +
                        "SUM(total_amount) as total_revenue, " +
                        "SUM(paid_amount) as paid_amount " +
                        "FROM bills WHERE MONTH(created_at) = MONTH(CURDATE()) " +
                        "AND YEAR(created_at) = YEAR(CURDATE())";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    report.append(String.format("Total Bills: %d\n", rs.getInt("total_bills")));
                    report.append(String.format("Total Revenue: ₹%.2f\n", rs.getDouble("total_revenue")));
                    report.append(String.format("Collected: ₹%.2f\n", rs.getDouble("paid_amount")));
                }
            }
        }
        
        return report.toString();
    }
    
    private String generatePatientDemographicsReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("PATIENT DEMOGRAPHICS REPORT\n");
        report.append("=".repeat(60)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total patients
            String sql1 = "SELECT COUNT(*) as total FROM patients";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql1)) {
                if (rs.next()) {
                    report.append(String.format("Total Patients: %d\n\n", rs.getInt("total")));
                }
            }
            
            // Gender distribution
            report.append("Gender Distribution:\n");
            String sql2 = "SELECT gender, COUNT(*) as count FROM patients GROUP BY gender";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {
                    report.append(String.format("  %s: %d\n", rs.getString("gender"), rs.getInt("count")));
                }
            }
            
            report.append("\nAge Groups:\n");
            String sql3 = "SELECT " +
                         "CASE " +
                         "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) < 18 THEN 'Under 18' " +
                         "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) BETWEEN 18 AND 35 THEN '18-35' " +
                         "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) BETWEEN 36 AND 50 THEN '36-50' " +
                         "WHEN TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) BETWEEN 51 AND 65 THEN '51-65' " +
                         "ELSE 'Above 65' " +
                         "END as age_group, " +
                         "COUNT(*) as count " +
                         "FROM patients GROUP BY age_group";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql3)) {
                while (rs.next()) {
                    report.append(String.format("  %s: %d\n", rs.getString("age_group"), rs.getInt("count")));
                }
            }
        }
        
        return report.toString();
    }
    
    private String generateDoctorWorkloadReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("DOCTOR WORKLOAD REPORT\n");
        report.append("Period: Last 30 Days\n");
        report.append("=".repeat(60)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT " +
                        "d.full_name, d.specialization, " +
                        "COUNT(a.appointment_id) as total_appointments, " +
                        "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
                        "SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
                        "FROM doctors d " +
                        "LEFT JOIN appointments a ON d.doctor_id = a.doctor_id " +
                        "AND a.appointment_datetime >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                        "GROUP BY d.doctor_id " +
                        "ORDER BY total_appointments DESC";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                report.append(String.format("%-25s %-20s %10s %10s %10s\n",
                    "Doctor", "Specialization", "Total", "Completed", "Cancelled"));
                report.append("-".repeat(75)).append("\n");
                
                while (rs.next()) {
                    String name = rs.getString("full_name");
                    report.append(String.format("%-25s %-20s %10d %10d %10d\n",
                        name,
                        rs.getString("specialization"),
                        rs.getInt("total_appointments"),
                        rs.getInt("completed"),
                        rs.getInt("cancelled")));
                }
            }
        }
        
        return report.toString();
    }
    
    private String generateAppointmentSummaryReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("APPOINTMENT SUMMARY REPORT\n");
        report.append("Date: ").append(LocalDate.now()).append("\n");
        report.append("=".repeat(60)).append("\n\n");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Today's summary
            String sql1 = "SELECT status, COUNT(*) as count FROM appointments " +
                         "WHERE DATE(appointment_datetime) = CURDATE() GROUP BY status";
            
            report.append("Today's Appointments:\n");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql1)) {
                int total = 0;
                while (rs.next()) {
                    int count = rs.getInt("count");
                    total += count;
                    report.append(String.format("  %s: %d\n", rs.getString("status"), count));
                }
                report.append(String.format("  TOTAL: %d\n\n", total));
            }
            
            // This week's summary
            String sql2 = "SELECT COUNT(*) as count FROM appointments " +
                         "WHERE YEARWEEK(appointment_datetime) = YEARWEEK(CURDATE())";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql2)) {
                if (rs.next()) {
                    report.append(String.format("This Week's Total: %d\n", rs.getInt("count")));
                }
            }
            
            // This month's summary
            String sql3 = "SELECT COUNT(*) as count FROM appointments " +
                         "WHERE MONTH(appointment_datetime) = MONTH(CURDATE()) " +
                         "AND YEAR(appointment_datetime) = YEAR(CURDATE())";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql3)) {
                if (rs.next()) {
                    report.append(String.format("This Month's Total: %d\n", rs.getInt("count")));
                }
            }
        }
        
        return report.toString();
    }
}