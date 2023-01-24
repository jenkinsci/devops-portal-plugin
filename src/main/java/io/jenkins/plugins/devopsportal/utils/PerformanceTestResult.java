package io.jenkins.plugins.devopsportal.utils;

import java.io.Serializable;

public class PerformanceTestResult implements Serializable {

    private long testCount;
    private long sampleCount;
    private long errorCount;

    public long getTestCount() {
        return testCount;
    }

    public void setTestCount(long testCount) {
        this.testCount = testCount;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public void addSampleCount(int samples) {
        this.sampleCount += samples;
    }

    public void addErrorCount(float errors) {
        this.errorCount += Math.round(errors);
    }

}
