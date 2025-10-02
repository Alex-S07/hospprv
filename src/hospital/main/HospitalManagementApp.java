package hospital.main;

import hospital.config.DatabaseConfig;
import hospital.views.login.LoginFrame;
import hospital.utils.Constants;
import javax.swing.*;
import java.awt.*;

public class HospitalManagementApp {
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set custom UI properties
        setupUIDefaults();
        
        // Test database connection
        DatabaseConfig.testConnection();
        
        // Start the application
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void setupUIDefaults() {
        // Set default font
        Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        
        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Set colors
        UIManager.put("Button.background", Constants.PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", Constants.BACKGROUND_COLOR);
    }
}