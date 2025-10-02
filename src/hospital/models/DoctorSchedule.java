package hospital.models;


import java.time.LocalDate;

public class DoctorSchedule {
    private int scheduleId;
    private int doctorId;
    private LocalDate scheduleDate;
    private String status; // DUTY, LEAVE
    private String reason;
    
    public DoctorSchedule() {}
    
    public DoctorSchedule(int doctorId, LocalDate scheduleDate, String status) {
        this.doctorId = doctorId;
        this.scheduleDate = scheduleDate;
        this.status = status;
    }
    
    // Getters and Setters
    public int getScheduleId() { 
        return scheduleId; 
    }
    
    public void setScheduleId(int scheduleId) { 
        this.scheduleId = scheduleId; 
    }
    
    public int getDoctorId() { 
        return doctorId; 
    }
    
    public void setDoctorId(int doctorId) { 
        this.doctorId = doctorId; 
    }
    
    public LocalDate getScheduleDate() { 
        return scheduleDate; 
    }
    
    public void setScheduleDate(LocalDate scheduleDate) { 
        this.scheduleDate = scheduleDate; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public String getReason() { 
        return reason; 
    }
    
    public void setReason(String reason) { 
        this.reason = reason; 
    }
    
    @Override
    public String toString() {
        return scheduleDate + " - " + status + (reason != null ? " (" + reason + ")" : "");
    }
}
