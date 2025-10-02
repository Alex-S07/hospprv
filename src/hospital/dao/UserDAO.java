package hospital.dao;

import hospital.models.User;
import hospital.utils.EncryptionUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends BaseDAO {

    public User authenticate(String username, String password) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = true";

    try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        stmt.setString(2, password); // ❌ Plain text comparison - NO HASHING

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            User user = mapResultSetToUser(rs);
            updateLastLogin(user.getUserId());
            return user;
        }
    }
    return null;
}

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
        return null;
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

      public boolean createUser(User user) throws SQLException {
    String sql = "INSERT INTO users (username, password, email, role, is_active, profile_completed) " +
                "VALUES (?, ?, ?, ?, TRUE, FALSE)";
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getPassword()); // ❌ Plain text - NO HASHING
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getRole());
        
        int result = stmt.executeUpdate();
        
        if (result > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setUserId(rs.getInt(1));
            }
            return true;
        }
    }
    return false;
}
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, is_active = ? WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setBoolean(4, user.isActive());
            stmt.setInt(5, user.getUserId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
    String sql = "UPDATE users SET password = ? WHERE user_id = ?";

    try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, newPassword); // ❌ Plain text - NO HASHING
        stmt.setInt(2, userId);

        return stmt.executeUpdate() > 0;
    }
}

    private void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        return user;
    }
}