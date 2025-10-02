package hospital.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Medicine {
    private int medicineId;
    private String name;
    private String genericName;
    private String manufacturer;
    private String category;
    private double unitPrice;
    private int stockQuantity;
    private int minimumStockLevel;
    private LocalDate expiryDate;
    private String batchNumber;
    private String storageLocation;
    private LocalDateTime createdAt;
    
    public Medicine() {}
    
    // Getters and Setters
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public int getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isLowStock() {
        return stockQuantity <= minimumStockLevel;
    }
    
    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now().plusMonths(3));
    }
    
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }
}
