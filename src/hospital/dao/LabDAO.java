package hospital.dao;

import hospital.models.TestRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for laboratory requests and results.
 * Handles all database operations for lab tests using test_requests table only.
 * Results are stored directly in the test_requests.result field.
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
                    "tr.request_date, tr.status, tr.remarks, tr.result, tr.completed_date, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "d.full_name AS doctor_name, " +
                    "t.test_name " +
                    "FROM test_requests tr " +
                    "JOIN patients p ON tr.patient_id = p.patient_id " +
                    "JOIN doctors d ON tr.doctor_id = d.doctor_id " +
                    "JOIN tests t ON tr.test_id = t.test_id " +
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
                request.setResult(rs.getString("result"));
                
                Timestamp completedDate = rs.getTimestamp("completed_date");
                if (completedDate != null) {
                    request.setCompletedDate(new java.util.Date(completedDate.getTime()));
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
     * Fetch full details of a selected request including patient info.
     * 
     * @param requestId The request ID
     * @return TestRequest with complete details
     * @throws SQLException if database error occurs
     */
    public TestRequest getRequestDetails(int requestId) throws SQLException {
        TestRequest request = null;
        
        String sql = "SELECT tr.request_id, tr.patient_id, tr.doctor_id, tr.test_id, " +
                    "tr.request_date, tr.status, tr.remarks, tr.result, tr.completed_date, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "p.gender, p.date_of_birth, " +
                    "d.full_name AS doctor_name, " +
                    "t.test_name, t.description, t.normal_range " +
                    "FROM test_requests tr " +
                    "JOIN patients p ON tr.patient_id = p.patient_id " +
                    "JOIN doctors d ON tr.doctor_id = d.doctor_id " +
                    "JOIN tests t ON tr.test_id = t.test_id " +
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
                request.setResult(rs.getString("result"));
                
                Timestamp completedDate = rs.getTimestamp("completed_date");
                if (completedDate != null) {
                    request.setCompletedDate(new java.util.Date(completedDate.getTime()));
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
     * Submit test result and update request status to Completed.
     * Stores result directly in test_requests table.
     * 
     * @param requestId The test request ID
     * @param resultText The test result text
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    public boolean submitTestResult(int requestId, String resultText) throws SQLException {
        String sql = "UPDATE test_requests SET result = ?, status = 'Completed', completed_date = NOW() " +
                    "WHERE request_id = ?";
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, resultText);
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
