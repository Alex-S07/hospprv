package hospital.main;

import hospital.view.LoginFrame;
import hospital.util.DatabaseUtil;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize database connection
        DatabaseUtil.initializeDatabase();
        
        // Launch login window
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}