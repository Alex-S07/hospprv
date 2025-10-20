package hospital.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "/properties/config.properties";
    private static Properties props;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        props = new Properties();
        try (InputStream is = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/hospital_db");
        String username = props.getProperty("db.username", "root");
        String password = props.getProperty("db.password", "");
        
        return DriverManager.getConnection(url, username, password);
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}