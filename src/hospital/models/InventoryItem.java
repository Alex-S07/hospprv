package hospital.models;


import java.time.LocalDate;

/**
 * Model class representing an inventory item (medicine) in the pharmacy system
 */
public class InventoryItem {
    private int itemId;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private LocalDate expiryDate;
    private String supplier;
    private String category;
    
    // Default constructor
    public InventoryItem() {
    }
    
    // Constructor with all fields
    public InventoryItem(int itemId, String itemName, int quantity, 
                        double unitPrice, LocalDate expiryDate) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
    }
    
    // Full constructor with all fields including supplier and category
    public InventoryItem(int itemId, String itemName, int quantity, 
                        double unitPrice, LocalDate expiryDate, 
                        String supplier, String category) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.category = category;
    }
    
    // Getters
    public int getItemId() {
        return itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public String getCategory() {
        return category;
    }
    
    // Setters
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    // Helper method to check if item is low stock
    public boolean isLowStock() {
        return quantity < 10;
    }
    
    // Helper method to check if item is expired
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
    
    // Helper method to check if item is expiring soon (within 30 days)
    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow) && !isExpired();
    }
    
    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", expiryDate=" + expiryDate +
                ", supplier='" + supplier + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}