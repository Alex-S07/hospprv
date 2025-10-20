package hospital.dao;

import hospital.models.Doctor;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO extends BaseDAO {

       public List<String> getDepartments() {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT DISTINCT specialization FROM doctors " +
                "WHERE is_available = true " +
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

     public List<String> getDoctorsByDepartment(String department, LocalDate date) {
        List<String> doctors = new ArrayList<>();
        String sql = "SELECT d.user_id, d.full_name, d.specialization, d.consultation_fee " +
                "FROM doctors d " +
                "LEFT JOIN doctor_schedules ds ON d.doctor_id = ds.doctor_id AND ds.schedule_date = ? " +
                "WHERE d.is_available = true " +
                "AND d.specialization = ? " +
                "AND (ds.status IS NULL OR ds.status = 'DUTY') " +
                "ORDER BY d.full_name";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setString(2, department);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");  // Use user_id instead of doctor_id
                String fullName = rs.getString("full_name");
                String spec = rs.getString("specialization");
                double fee = rs.getDouble("consultation_fee");
                doctors.add(userId + " - " + fullName + " (" + spec + ") - ₹" + fee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    // Get doctor by ID - FIXED
      public Doctor getDoctorByUserId(int userId) throws SQLException {
        String sql = "SELECT d.doctor_id, d.user_id, d.full_name, d.specialization, " +
                "d.experience_years, d.phone, d.email, d.consultation_fee, d.is_available, d.created_at " +
                "FROM doctors d WHERE d.user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setUserId(rs.getInt("user_id"));
                doctor.setUsername(rs.getString("full_name"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setExperienceYears(rs.getInt("experience_years"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setEmail(rs.getString("email"));
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setAvailable(rs.getBoolean("is_available"));
                doctor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return doctor;
            }
        }
        return null;
    }


    // Get all doctors - FIXED
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT doctor_id, user_id, full_name, specialization, email, phone, " +
                "consultation_fee, is_available, created_at FROM doctors";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setUserId(rs.getInt("user_id"));
                doctor.setUsername(rs.getString("full_name"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setEmail(rs.getString("email"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setAvailable(rs.getBoolean("is_available"));
                doctor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    // Get consultation fee for a doctor - NEW METHOD
    public double getConsultationFeeByUserId(int userId) throws SQLException {
        String sql = "SELECT consultation_fee FROM doctors WHERE user_id = ?";
        
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("consultation_fee");
            }
        }
        return 0.0;
    }

     public Doctor getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT doctor_id, user_id, full_name, specialization, " +
                "experience_years, phone, email, consultation_fee, is_available, created_at " +
                "FROM doctors WHERE doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setUserId(rs.getInt("user_id"));
                
                // Handle full_name - you might need to split into first/last name
                String fullName = rs.getString("full_name");
                doctor.setUsername(fullName); // or setFullName() if you have that method
                
                // If you need separate first/last names, split the full_name
                if (fullName != null && fullName.contains(" ")) {
                    String[] names = fullName.split(" ", 2);
                    doctor.setFirstName(names[0]);
                    if (names.length > 1) {
                        doctor.setLastName(names[1]);
                    } else {
                        doctor.setLastName("");
                    }
                } else {
                    doctor.setFirstName(fullName != null ? fullName : "");
                    doctor.setLastName("");
                }
                
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setExperienceYears(rs.getInt("experience_years"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setEmail(rs.getString("email"));
                doctor.setConsultationFee(rs.getDouble("consultation_fee"));
                doctor.setAvailable(rs.getBoolean("is_available"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    doctor.setCreatedAt(createdAt.toLocalDateTime());
                }
                
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
    
    /**
     * Get the doctor_id from the doctors table using the user_id.
     * 
     * @param userId The user ID from the users table
     * @return doctor_id if found, -1 otherwise
     * @throws SQLException if database error occurs
     */
    public int getDoctorIdByUserId(int userId) throws SQLException {
        String sql = "SELECT doctor_id FROM doctors WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
            
            return -1;
        }
    }
}