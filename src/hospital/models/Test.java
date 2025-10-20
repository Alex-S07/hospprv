package hospital.models;

import java.math.BigDecimal;

/**
 * Represents a laboratory test that can be ordered by doctors.
 */
public class Test {
    private int testId;
    private String testName;
    private String description;
    private String normalRange;
    private String unit;
    private BigDecimal cost;

    // No-args constructor
    public Test() {
    }

    // Constructor with all fields
    public Test(int testId, String testName, String description, String normalRange, String unit, BigDecimal cost) {
        this.testId = testId;
        this.testName = testName;
        this.description = description;
        this.normalRange = normalRange;
        this.unit = unit;
        this.cost = cost;
    }

    // Getters and Setters
    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return testName; // For display in dropdowns
    }
}
