package hospital.dao;

import hospital.config.DatabaseConfig;
import hospital.models.InventoryItem;
import hospital.models.Medicine;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    // ==================== INVENTORYITEM METHODS ====================
    
    /**
     * Create a new inventory item from InventoryItem model
     */
    public InventoryItem createInventoryItem(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO medicines (name, category, unit_price, stock_quantity, " +
                "expiry_date, manufacturer, minimum_stock_level, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getUnitPrice());
            stmt.setInt(4, item.getQuantity());
            stmt.setDate(5, item.getExpiryDate() != null ? Date.valueOf(item.getExpiryDate()) : null);
            stmt.setString(6, item.getSupplier());
            stmt.setInt(7, 10); // Default minimum stock level

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    item.setItemId(rs.getInt(1));
                }
                return item;
            }
        }
        return null;
    }

    /**
     * Get all inventory items as InventoryItem objects
     */
    public List<InventoryItem> getAllInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        return items;
    }

    /**
     * Get inventory item by ID
     */
    public InventoryItem getInventoryItemById(int itemId) throws SQLException {
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInventoryItem(rs);
            }
        }
        return null;
    }

    /**
     * Update inventory item
     */
    public boolean updateInventoryItem(InventoryItem item) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, category = ?, unit_price = ?, " +
                "stock_quantity = ?, expiry_date = ?, manufacturer = ? WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getUnitPrice());
            stmt.setInt(4, item.getQuantity());
            stmt.setDate(5, item.getExpiryDate() != null ? Date.valueOf(item.getExpiryDate()) : null);
            stmt.setString(6, item.getSupplier());
            stmt.setInt(7, item.getItemId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Update inventory item stock
     */
    public boolean updateInventoryItemStock(int itemId, int quantityChange, String operation) throws SQLException {
        String sql;
        if ("ADD".equals(operation)) {
            sql = "UPDATE medicines SET stock_quantity = stock_quantity + ? WHERE medicine_id = ?";
        } else if ("SUBTRACT".equals(operation)) {
            sql = "UPDATE medicines SET stock_quantity = stock_quantity - ? WHERE medicine_id = ? " +
                    "AND stock_quantity >= ?";
        } else {
            throw new SQLException("Invalid operation. Use 'ADD' or 'SUBTRACT'");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Math.abs(quantityChange));
            stmt.setInt(2, itemId);

            if ("SUBTRACT".equals(operation)) {
                stmt.setInt(3, Math.abs(quantityChange));
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0 && "SUBTRACT".equals(operation)) {
                throw new SQLException("Insufficient stock");
            }

            return rowsAffected > 0;
        }
    }

    /**
     * Get low stock inventory items
     */
    public List<InventoryItem> getLowStockInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines WHERE stock_quantity < 10 " +
                "ORDER BY stock_quantity";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        return items;
    }

    /**
     * Get expiring inventory items (within 30 days)
     */
    public List<InventoryItem> getExpiringInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines WHERE " +
                "expiry_date IS NOT NULL AND " +
                "expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                "ORDER BY expiry_date";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        return items;
    }

    /**
     * Get expired inventory items
     */
    public List<InventoryItem> getExpiredInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines WHERE " +
                "expiry_date IS NOT NULL AND expiry_date < CURDATE() " +
                "ORDER BY expiry_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        return items;
    }

    /**
     * Search inventory items
     */
    public List<InventoryItem> searchInventoryItems(String searchTerm) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT medicine_id, name, stock_quantity, unit_price, expiry_date, " +
                "manufacturer, category FROM medicines WHERE " +
                "name LIKE ? OR category LIKE ? OR manufacturer LIKE ? " +
                "ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        return items;
    }

    /**
     * Delete inventory item
     */
    public boolean deleteInventoryItem(int itemId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Map ResultSet to InventoryItem object
     */
    private InventoryItem mapResultSetToInventoryItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setItemId(rs.getInt("medicine_id"));
        item.setItemName(rs.getString("name"));
        item.setQuantity(rs.getInt("stock_quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            item.setExpiryDate(expiryDate.toLocalDate());
        }
        
        item.setSupplier(rs.getString("manufacturer"));
        item.setCategory(rs.getString("category"));
        
        return item;
    }

    // ==================== ORIGINAL MEDICINE METHODS (KEPT FOR COMPATIBILITY) ====================

    public Medicine createMedicine(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines (name, generic_name, manufacturer, category, " +
                "unit_price, stock_quantity, minimum_stock_level, expiry_date, " +
                "created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getGenericName());
            stmt.setString(3, medicine.getManufacturer());
            stmt.setString(4, medicine.getCategory());
            stmt.setDouble(5, medicine.getUnitPrice());
            stmt.setInt(6, medicine.getStockQuantity());
            stmt.setInt(7, medicine.getMinimumStockLevel());
            stmt.setDate(8, medicine.getExpiryDate() != null ? Date.valueOf(medicine.getExpiryDate()) : null);
            stmt.setTimestamp(9, Timestamp.valueOf(medicine.getCreatedAt()));

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    medicine.setMedicineId(rs.getInt(1));
                }
                return medicine;
            }
        }
        return null;
    }

    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    public Medicine getMedicineById(int medicineId) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicineId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMedicine(rs);
            }
        }
        return null;
    }

    public List<Medicine> searchMedicines(String searchTerm) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE " +
                "name LIKE ? OR generic_name LIKE ? OR category LIKE ? " +
                "ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE stock_quantity <= minimum_stock_level " +
                "ORDER BY stock_quantity";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    public List<Medicine> getExpiringMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE " +
                "expiry_date IS NOT NULL AND " +
                "expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 MONTH) " +
                "ORDER BY expiry_date";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    public List<Medicine> getExpiredMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE " +
                "expiry_date IS NOT NULL AND expiry_date < CURDATE() " +
                "ORDER BY expiry_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    public boolean updateMedicine(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, generic_name = ?, manufacturer = ?, " +
                "category = ?, unit_price = ?, stock_quantity = ?, " +
                "minimum_stock_level = ?, expiry_date = ? WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getGenericName());
            stmt.setString(3, medicine.getManufacturer());
            stmt.setString(4, medicine.getCategory());
            stmt.setDouble(5, medicine.getUnitPrice());
            stmt.setInt(6, medicine.getStockQuantity());
            stmt.setInt(7, medicine.getMinimumStockLevel());
            stmt.setDate(8, medicine.getExpiryDate() != null ? Date.valueOf(medicine.getExpiryDate()) : null);
            stmt.setInt(9, medicine.getMedicineId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStock(int medicineId, int quantityChange, String operation) throws SQLException {
        String sql;
        if ("ADD".equals(operation)) {
            sql = "UPDATE medicines SET stock_quantity = stock_quantity + ? WHERE medicine_id = ?";
        } else if ("SUBTRACT".equals(operation)) {
            sql = "UPDATE medicines SET stock_quantity = stock_quantity - ? WHERE medicine_id = ? " +
                    "AND stock_quantity >= ?";
        } else {
            throw new SQLException("Invalid operation. Use 'ADD' or 'SUBTRACT'");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Math.abs(quantityChange));
            stmt.setInt(2, medicineId);

            if ("SUBTRACT".equals(operation)) {
                stmt.setInt(3, Math.abs(quantityChange));
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0 && "SUBTRACT".equals(operation)) {
                throw new SQLException("Insufficient stock");
            }

            return rowsAffected > 0;
        }
    }

    public boolean deleteMedicine(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicineId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Medicine getMedicineByName(String name) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Medicine medicine = new Medicine();
                medicine.setMedicineId(rs.getInt("medicine_id"));
                medicine.setName(rs.getString("name"));
                medicine.setCategory(rs.getString("category"));
                medicine.setUnitPrice(rs.getDouble("unit_price"));
                medicine.setStockQuantity(rs.getInt("stock_quantity"));

                java.sql.Date expiryDate = rs.getDate("expiry_date");
                if (expiryDate != null) {
                    medicine.setExpiryDate(expiryDate.toLocalDate());
                }

                return medicine;
            }
        }

        return null;
    }

    public boolean updateMedicineStock(int medicineId, int newQuantity) throws SQLException {
        String sql = "UPDATE medicines SET stock_quantity = ? WHERE medicine_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, medicineId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean reduceMedicineStock(int medicineId, int quantity) throws SQLException {
        String sql = "UPDATE medicines SET stock_quantity = stock_quantity - ? " +
                "WHERE medicine_id = ? AND stock_quantity >= ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, medicineId);
            pstmt.setInt(3, quantity);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setName(rs.getString("name"));
        medicine.setGenericName(rs.getString("generic_name"));
        medicine.setManufacturer(rs.getString("manufacturer"));
        medicine.setCategory(rs.getString("category"));
        medicine.setUnitPrice(rs.getDouble("unit_price"));
        medicine.setStockQuantity(rs.getInt("stock_quantity"));
        medicine.setMinimumStockLevel(rs.getInt("minimum_stock_level"));

        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            medicine.setExpiryDate(expiryDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            medicine.setCreatedAt(createdAt.toLocalDateTime());
        }

        return medicine;
    }
}