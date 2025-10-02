package hospital.utils;


import hospital.dao.InventoryDAO;
import hospital.models.Medicine;
import java.sql.SQLException;
import java.util.List;

public class InventoryAlerts {
    private InventoryDAO inventoryDAO;
    
    public InventoryAlerts() {
        this.inventoryDAO = new InventoryDAO();
    }
    
    public String generateAlertReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY ALERT REPORT\n");
        report.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("=".repeat(60)).append("\n\n");
        
        // Low Stock Alerts
        List<Medicine> lowStock = inventoryDAO.getLowStockMedicines();
        if (!lowStock.isEmpty()) {
            report.append("LOW STOCK ALERTS (").append(lowStock.size()).append(")\n");
            report.append("-".repeat(60)).append("\n");
            for (Medicine m : lowStock) {
                report.append(String.format("%-30s Stock: %3d (Min: %3d)\n", 
                    m.getName(), m.getStockQuantity(), m.getMinimumStockLevel()));
            }
            report.append("\n");
        }
        
        // Expiring Soon
        List<Medicine> expiring = inventoryDAO.getExpiringMedicines();
        if (!expiring.isEmpty()) {
            report.append("EXPIRING SOON (").append(expiring.size()).append(")\n");
            report.append("-".repeat(60)).append("\n");
            for (Medicine m : expiring) {
                report.append(String.format("%-30s Expiry: %s\n", 
                    m.getName(), m.getExpiryDate()));
            }
            report.append("\n");
        }
        
        // Already Expired
        List<Medicine> expired = inventoryDAO.getExpiredMedicines();
        if (!expired.isEmpty()) {
            report.append("EXPIRED MEDICINES - REMOVE IMMEDIATELY (").append(expired.size()).append(")\n");
            report.append("-".repeat(60)).append("\n");
            for (Medicine m : expired) {
                report.append(String.format("%-30s Expired: %s\n", 
                    m.getName(), m.getExpiryDate()));
            }
            report.append("\n");
        }
        
        if (lowStock.isEmpty() && expiring.isEmpty() && expired.isEmpty()) {
            report.append("No alerts at this time.\n");
        }
        
        return report.toString();
    }
    
    public int getTotalAlerts() throws SQLException {
        return inventoryDAO.getLowStockMedicines().size() + 
               inventoryDAO.getExpiringMedicines().size() +
               inventoryDAO.getExpiredMedicines().size();
    }
}