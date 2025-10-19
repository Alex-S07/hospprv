package hospital.models;

import java.time.LocalDateTime;

public class Prescription {
    private int prescriptionId;
    private int recordId;
    private int medicineId;
    private String medicineName;
    private String frequency;
    private String duration;
    private String instructions;
    private int quantity = 1;  // Default quantity
    private String status = "Pending";  // Pending, Dispensed
    private LocalDateTime createdAt;
    private LocalDateTime dispensedAt;
    private int dispensedBy;
    private double totalCost;
    
    // Additional fields for display
    private String patientName;
    private String doctorName;
    
    // Constructors
    public Prescription() {
    }
    
    public Prescription(int recordId, int medicineId, String dosage, 
                       String frequency, String duration) {
        this.recordId = recordId;
        this.medicineId = medicineId;
        this.frequency = frequency;
        this.duration = duration;
    }
    
    // Getters and Setters
    public int getPrescriptionId() {
        return prescriptionId;
    }
    
    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    
    public int getRecordId() {
        return recordId;
    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }
    
    public int getMedicineId() {
        return medicineId;
    }
    
    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }
    
    public String getMedicineName() {
        return medicineName;
    }
    
    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public double getTotalCost() {
        return totalCost; 
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost; 
    }

    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }
    
    public void setDispensedAt(LocalDateTime dispensedAt) {
        this.dispensedAt = dispensedAt;
    }
    
    public int getDispensedBy() {
        return dispensedBy;
    }
    
    public void setDispensedBy(int dispensedBy) {
        this.dispensedBy = dispensedBy;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId=" + prescriptionId +
                ", recordId=" + recordId +
                ", medicineName='" + medicineName + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                '}';
    }
}