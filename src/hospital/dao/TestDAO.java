package hospital.dao;

import hospital.models.Patient;
import hospital.models.Test;
import hospital.models.TestRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for laboratory test operations.
 * Handles test requests, test catalog, and test results.
 */
public class TestDAO extends BaseDAO {

    /**
     * Get all test requests for a specific doctor, optionally filtered by status.
     * 
     * @param doctorId The doctor's ID
     * @param status Filter by status ("Pending", "Completed", or null for all)
     * @return List of test requests with patient and test details
     * @throws SQLException if database error occurs
     */
    public List<TestRequest> getDoctorTests(int doctorId, String status) throws SQLException {
        List<TestRequest> requests = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT tr.request_id, tr.patient_id, tr.doctor_id, tr.test_id, ");
        sql.append("tr.request_date, tr.status, tr.remarks, ");
        sql.append("CONCAT(p.first_name, ' ', p.last_name) AS patient_name, ");
        sql.append("t.test_name, ");
        sql.append("r.result_value, r.comments AS result_comments, r.result_file, r.upload_date ");
        sql.append("FROM test_requests tr ");
        sql.append("JOIN patients p ON tr.patient_id = p.patient_id ");
        sql.append("JOIN tests t ON tr.test_id = t.test_id ");
        sql.append("LEFT JOIN test_results r ON tr.request_id = r.request_id ");
        sql.append("WHERE tr.doctor_id = ? ");
        
        if (status != null && !status.isEmpty()) {
            sql.append("AND tr.status = ? ");
        }
        
        sql.append("ORDER BY tr.request_date DESC");
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            stmt.setInt(1, doctorId);
            
            if (status != null && !status.isEmpty()) {
                stmt.setString(2, status);
            }
            
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
                request.setTestName(rs.getString("test_name"));
                
                // Result data (if completed)
                request.setResultValue(rs.getString("result_value"));
                request.setResultComments(rs.getString("result_comments"));
                request.setResultFile(rs.getString("result_file"));
                
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
     * Get detailed information about a specific test request.
     * 
     * @param requestId The request ID
     * @return TestRequest object with full details
     * @throws SQLException if database error occurs
     */
    public TestRequest getTestRequestById(int requestId) throws SQLException {
        TestRequest request = null;
        
        String sql = "SELECT tr.request_id, tr.patient_id, tr.doctor_id, tr.test_id, " +
                    "tr.request_date, tr.status, tr.remarks, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "p.gender, p.date_of_birth, " +
                    "t.test_name, t.description, t.normal_range, t.unit, " +
                    "r.result_value, r.comments AS result_comments, r.result_file, r.upload_date " +
                    "FROM test_requests tr " +
                    "JOIN patients p ON tr.patient_id = p.patient_id " +
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
                request.setTestName(rs.getString("test_name"));
                request.setResultValue(rs.getString("result_value"));
                request.setResultComments(rs.getString("result_comments"));
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
     * Insert a new test request.
     * 
     * @param doctorId Doctor ordering the test
     * @param patientId Patient receiving the test
     * @param testId Test to be performed
     * @param remarks Doctor's remarks/instructions
     * @return true if insert successful
     * @throws SQLException if database error occurs
     */
    public boolean insertTestRequest(int doctorId, int patientId, int testId, String remarks) throws SQLException {
        String sql = "INSERT INTO test_requests (patient_id, doctor_id, test_id, remarks, status) " +
                    "VALUES (?, ?, ?, ?, 'Pending')";
        
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setInt(3, testId);
            stmt.setString(4, remarks);
            
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
     * Get all available tests from the tests catalog.
     * 
     * @return List of all tests
     * @throws SQLException if database error occurs
     */
    public List<Test> getAllTests() throws SQLException {
        List<Test> tests = new ArrayList<>();
        String sql = "SELECT * FROM tests ORDER BY test_name ASC";
        
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Test test = new Test();
                test.setTestId(rs.getInt("test_id"));
                test.setTestName(rs.getString("test_name"));
                test.setDescription(rs.getString("description"));
                test.setNormalRange(rs.getString("normal_range"));
                test.setUnit(rs.getString("unit"));
                test.setCost(rs.getBigDecimal("cost"));
                
                tests.add(test);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
        
        return tests;
    }

    /**
     * Get all patients (for the dropdown when requesting a new test).
     * 
     * @return List of all patients
     * @throws SQLException if database error occurs
     */
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender " +
                    "FROM patients ORDER BY last_name, first_name ASC";
        
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                
                Date dob = rs.getDate("date_of_birth");
                if (dob != null) {
                    patient.setDateOfBirth(dob.toLocalDate());
                }
                
                patient.setGender(rs.getString("gender"));
                
                patients.add(patient);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
        
        return patients;
    }
}
