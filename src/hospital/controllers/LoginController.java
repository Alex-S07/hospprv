package hospital.controllers;


import hospital.dao.UserDAO;
import hospital.models.User;
import java.sql.SQLException;

public class LoginController {
    private UserDAO userDAO;
    
    public LoginController() {
        this.userDAO = new UserDAO();
    }
    
    public User authenticate(String username, String password) throws SQLException {
        return userDAO.authenticate(username, password);
    }
    
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            // Verify old password first (implementation needed)
            return userDAO.changePassword(userId, newPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
