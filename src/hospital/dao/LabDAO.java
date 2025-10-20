package hospital.dao;

import hospital.models.TestRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for laboratory requests and results.
 * Handles all database operations for lab tests using test_requests and test_results tables.
 */
public class LabDAO extends BaseDAO {

    /**
     * Fetch pending or completed test requests for lab technician.
     * Joins with patients, doctors, and tests tables for complete info.
     * 
     * @param status "Pending" or "Completed"
     * @return List of TestRequest objects with joined data
     * @throws SQLException if database error occurs
     */
    public List<TestRequest> getLabRequests(String status) throws SQLException {
        List<TestRequest> requests = new ArrayList<>();
        
        String sql = "SELECT tr.request_id, tr.patient_id, tr.doctor_id, tr.test_id, " +
                    "tr.request_date, tr.status, tr.remarks, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "CONCAT(d.full_name) AS doctor_name, " +
                    "t.test_name, " +
                    "r.result_value, r.upload_date " +
                    "FROM test_requests tr " +
                    "JOIN patients p ON tr.patient_id = p.patient_id " +
                    "JOIN doctors d ON tr.doctor_id = d.doctor_id " +
                    "JOIN tests t ON tr.test_id = t.test_id " +
                    "LEFT JOIN test_results r ON tr.request_id = r.request_id " +
                    "WHERE tr.status = ? " +
                    "ORDER BY tr.request_date DESC";
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                TestRequest request = new TestRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setPatientId(rs.getInt("patient_id"));
                request.setDoctorId(rs.getInt("doctor_id"));
                request.setTestId(rs.getInt("test_id"));
                
                Timestamp requestDate = rs.getTimestamp("request_date");
                if (requestDate != null) {
                    request.setRequestDate(new java.util.Date(requestDate.getTime()));
                }
                
                request.setStatus(rs.getString("status"));
                request.setRemarks(rs.getString("remarks"));
                request.setPatientName(rs.getString("patient_name"));
                request.setDoctorName(rs.getString("doctor_name"));
                request.setTestName(rs.getString("test_name"));
                request.setResultValue(rs.getString("result_value"));
                
                Timestamp uploadDate = rs.getTimestamp("upload_date");
                if (uploadDate != null) {
                    request.setUploadDate(new java.util.Date(uploadDate.getTime()));
                }
                
                requests.add(request);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
        
        return requests;
    }

    /**
     * Fetch full details of a selected request including patient info and results.
     * 
     * @param requestId The request ID
     * @return TestRequest with complete details
     * @throws SQLException if database error occurs
     */
    public TestRequest getRequestDetails(int requestId) throws SQLException {
        TestRequest request = null;
        
        String sql = "SELECT tr.request_id, tr.patient_id, tr.doctor_id, tr.test_id, " +
                    "tr.request_date, tr.status, tr.remarks, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "p.gender, p.date_of_birth, " +
                    "d.full_name AS doctor_name, " +
                    "t.test_name, t.description, t.normal_range, " +
                    "r.result_value, r.comments, r.result_file, r.upload_date " +
                    "FROM test_requests tr " +
                    "JOIN patients p ON tr.patient_id = p.patient_id " +
                    "JOIN doctors d ON tr.doctor_id = d.doctor_id " +
                    "JOIN tests t ON tr.test_id = t.test_id " +
                    "LEFT JOIN test_results r ON tr.request_id = r.request_id " +
                    "WHERE tr.request_id = ?";
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                request = new TestRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setPatientId(rs.getInt("patient_id"));
                request.setDoctorId(rs.getInt("doctor_id"));
                request.setTestId(rs.getInt("test_id"));
                
                Timestamp requestDate = rs.getTimestamp("request_date");
                if (requestDate != null) {
                    request.setRequestDate(new java.util.Date(requestDate.getTime()));
                }
                
                request.setStatus(rs.getString("status"));
                request.setRemarks(rs.getString("remarks"));
                request.setPatientName(rs.getString("patient_name"));
                request.setDoctorName(rs.getString("doctor_name"));
                request.setTestName(rs.getString("test_name"));
                request.setResultValue(rs.getString("result_value"));
                request.setResultComments(rs.getString("comments"));
                request.setResultFile(rs.getString("result_file"));
                
                Timestamp uploadDate = rs.getTimestamp("upload_date");
                if (uploadDate != null) {
                    request.setUploadDate(new java.util.Date(uploadDate.getTime()));
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
        
        return request;
    }

    /**
     * Insert test result and update request status to Completed.
     * This is a transactional operation - both must succeed or both fail.
     * 
     * @param requestId The test request ID
     * @param labAssistantId The lab assistant user ID
     * @param resultValue The test result value
     * @param comments Lab comments
     * @param filePath Path to uploaded result file
     * @return true if both operations successful
     * @throws SQLException if database error occurs
     */
    public boolean insertTestResult(int requestId, int labAssistantId, String resultValue, 
                                    String comments, String filePath) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert result
            String insertSql = "INSERT INTO test_results (request_id, lab_assistant_id, result_value, comments, result_file) " +
                              "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, requestId);
            insertStmt.setInt(2, labAssistantId);
            insertStmt.setString(3, resultValue);
            insertStmt.setString(4, comments);
            insertStmt.setString(5, filePath);
            
            int rowsInserted = insertStmt.executeUpdate();
            insertStmt.close();
            
            if (rowsInserted == 0) {
                conn.rollback();
                return false;
            }
            
            // Update status
            String updateSql = "UPDATE test_requests SET status = 'Completed' WHERE request_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, requestId);
            
            int rowsUpdated = updateStmt.executeUpdate();
            updateStmt.close();
            
            if (rowsUpdated == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeConnection(conn);
        }
    }

    /**
     * Updates the status of a test request.
     * 
     * @param requestId The ID of the request to update
     * @param newStatus The new status value (e.g., "Completed", "Cancelled")
     * @return true if update successful, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean updateRequestStatus(int requestId, String newStatus) throws SQLException {
        String sql = "UPDATE test_requests SET status = ? WHERE request_id = ?";
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
    }
}
