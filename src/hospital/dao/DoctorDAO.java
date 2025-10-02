package hospital.dao;

import hospital.models.Doctor;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO extends BaseDAO {

    // Get all distinct departments
    public List<String> getDepartments() {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT DISTINCT specialization FROM users " +
                "WHERE role = 'DOCTOR' AND is_active = '1' AND profile_completed = TRUE " +
                "ORDER BY specialization";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String spec = rs.getString("specialization");
                if (spec != null && !spec.trim().isEmpty()) {
                    departments.add(spec);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }

    // Get doctors by department (filtered by date availability)
    public List<String> getDoctorsByDepartment(String department, LocalDate date) {
        List<String> doctors = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.specialization, u.consultation_fee " +
                "FROM users u " +
                "LEFT JOIN doctor_schedules ds ON u.user_id = ds.doctor_id AND ds.schedule_date = ? " +
                "WHERE u.role = 'DOCTOR' AND u.is_active = '1' " +
                "AND u.specialization = ? AND u.profile_completed = TRUE " +
                "AND (ds.status IS NULL OR ds.status = 'DUTY') " +
                "ORDER BY u.username";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setString(2, department);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("username");
                String spec = rs.getString("specialization");
                double fee = rs.getDouble("consultation_fee");
                doctors.add(userId + " - " + fullName + " (" + spec + ") - ₹" + fee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctors;
    }

    public Doctor getDoctorByUserId(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, role, is_active, specialization, " +
                "experience_years, phone, consultation_fee, profile_completed " +
                "FROM users WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("user_id"));
                doctor.setUsername(rs.getString("username"));
                doctor.setEmail(rs.getString("email"));
                doctor.setRole(rs.getString("role"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setExperienceYears(rs.getInt("experience_years"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setProfileCompleted(rs.getBoolean("profile_completed"));
                doctor.setAvailable("1".equals(rs.getString("is_active")));
                return doctor;
            }
        }
        return null;
    }

    // Get all doctors from USERS table only
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT user_id, username, email, role, is_active FROM users WHERE role = 'DOCTOR'";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("user_id"));
                doctor.setUsername(rs.getString("username"));
                doctor.setEmail(rs.getString("email"));
                doctor.setRole(rs.getString("role"));
                doctor.setAvailable("1".equals(rs.getString("is_active")));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    // Get doctor by ID from USERS table
    public Doctor getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT user_id, username, email, role, is_active, specialization, " +
                "experience_years, phone, consultation_fee, profile_completed " +
                "FROM users WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("user_id"));
                doctor.setUsername(rs.getString("username"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setExperienceYears(rs.getInt("experience_years"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setEmail(rs.getString("email"));
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setProfileCompleted(rs.getBoolean("profile_completed"));
                doctor.setAvailable("1".equals(rs.getString("is_active")));
                return doctor;
            }
        }

        return null;
    }

    public boolean createDoctor(Doctor doctor) throws SQLException {
        // Check for duplicates first
        String checkEmailSql = "SELECT COUNT(*) FROM doctors WHERE email = ?";
        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, 'DOCTOR')";
        String insertDoctorSql = "INSERT INTO doctors (user_id, specialization, experience_years, phone, email, consultation_fee, is_available, full_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            // Check for duplicate email
            try (PreparedStatement stmt = conn.prepareStatement(checkEmailSql)) {
                stmt.setString(1, doctor.getEmail());
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Email already exists: " + doctor.getEmail());
                }
            }

            // Check for duplicate username
            try (PreparedStatement stmt = conn.prepareStatement(checkUsernameSql)) {
                stmt.setString(1, doctor.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Username already exists: " + doctor.getUsername());
                }
            }

            conn.setAutoCommit(false);

            // Insert user WITH EMAIL
            int userId;
            try (PreparedStatement stmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, doctor.getUsername());
                stmt.setString(2, doctor.getPassword());
                stmt.setString(3, doctor.getEmail()); // ADD EMAIL HERE
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert doctor
            try (PreparedStatement stmt = conn.prepareStatement(insertDoctorSql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, doctor.getSpecialization());
                stmt.setInt(3, doctor.getExperienceYears());
                stmt.setString(4, doctor.getPhone());
                stmt.setString(5, doctor.getEmail());
                stmt.setDouble(6, doctor.getConsultationFee());
                stmt.setBoolean(7, doctor.isAvailable());
                stmt.setString(8, doctor.getFirstName()); // This is full_name

                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating doctor: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update doctor in USERS table
    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE users SET username = ?, specialization = ?, " +
                "experience_years = ?, phone = ?, consultation_fee = ?, " +
                "profile_completed = ?, is_active = ? WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getUsername());
            stmt.setString(2, doctor.getSpecialization());
            stmt.setInt(3, doctor.getExperienceYears());
            stmt.setString(4, doctor.getPhone());
            stmt.setDouble(5, doctor.getConsultationFee());
            stmt.setBoolean(6, doctor.isProfileCompleted());
            stmt.setString(7, doctor.isAvailable() ? "Active" : "Inactive");
            stmt.setInt(8, doctor.getDoctorId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete doctor from USERS table
    public boolean deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            return stmt.executeUpdate() > 0;
        }
    }
}