package hospital.models;

import java.util.Date;

/**
 * Represents a doctor's request for a laboratory test for a patient.
 */
public class TestRequest {
    private int requestId;
    private int patientId;
    private int doctorId;
    private int testId;
    private Date requestDate;
    private String status; // "Pending" or "Completed"
    private String remarks;
    
    // Additional fields for joined data
    private String patientName;
    private String doctorName;
    private String testName;
    private String resultValue;
    private String resultComments;
    private String resultFile;
    private Date uploadDate;

    // No-args constructor
    public TestRequest() {
    }

    // Constructor with main fields
    public TestRequest(int requestId, int patientId, int doctorId, int testId, Date requestDate, String status, String remarks) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testId = testId;
        this.requestDate = requestDate;
        this.status = status;
        this.remarks = remarks;
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

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultComments() {
        return resultComments;
    }

    public void setResultComments(String resultComments) {
        this.resultComments = resultComments;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "TestRequest{" +
                "requestId=" + requestId +
                ", patientName='" + patientName + '\'' +
                ", testName='" + testName + '\'' +
                ", status='" + status + '\'' +
                ", requestDate=" + requestDate +
                '}';
    }
}
