package hospital.models;

import java.util.Date;

/**
 * Represents a doctor's order for a laboratory test.
 * Used to track lab test requests from initiation through completion.
 */
public class LabRequest {
    private int requestId;
    private int patientId;
    private int doctorId;
    private String testName;
    private Date requestDate;
    private String status; // "PENDING", "COMPLETED", "CANCELLED"

    // No-args constructor
    public LabRequest() {
    }

    // Constructor with all fields
    public LabRequest(int requestId, int patientId, int doctorId, String testName, Date requestDate, String status) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testName = testName;
        this.requestDate = requestDate;
        this.status = status;
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LabRequest{" +
                "requestId=" + requestId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", testName='" + testName + '\'' +
                ", requestDate=" + requestDate +
                ", status='" + status + '\'' +
                '}';
    }
}
