package hospital.models;

import java.util.Date;

/**
 * Represents the recorded results for a laboratory test request.
 * Stores test result details and links to the original request.
 */
public class LabResult {
    private int resultId;
    private int requestId; // Foreign key to LabRequest
    private int assistantId; // ID of the lab assistant who recorded the results
    private String resultDetails; // Serialized JSON/String to store complex test data (e.g., CBC counts)
    private Date resultDate;

    // No-args constructor
    public LabResult() {
    }

    // Constructor with all fields
    public LabResult(int resultId, int requestId, int assistantId, String resultDetails, Date resultDate) {
        this.resultId = resultId;
        this.requestId = requestId;
        this.assistantId = assistantId;
        this.resultDetails = resultDetails;
        this.resultDate = resultDate;
    }

    // Getters and Setters
    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(int assistantId) {
        this.assistantId = assistantId;
    }

    public String getResultDetails() {
        return resultDetails;
    }

    public void setResultDetails(String resultDetails) {
        this.resultDetails = resultDetails;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }

    @Override
    public String toString() {
        return "LabResult{" +
                "resultId=" + resultId +
                ", requestId=" + requestId +
                ", assistantId=" + assistantId +
                ", resultDetails='" + resultDetails + '\'' +
                ", resultDate=" + resultDate +
                '}';
    }
}
