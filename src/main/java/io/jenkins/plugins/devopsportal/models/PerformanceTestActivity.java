package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class PerformanceTestActivity extends AbstractActivity {

    private long requestCount;
    private float averageResponseTime;
    private boolean qualityGatePassed;

    @DataBoundConstructor
    public PerformanceTestActivity(String applicationComponent) {
        super(ActivityCategory.PERFORMANCE_TEST, applicationComponent);
    }

    public long getRequestCount() {
        return requestCount;
    }

    @DataBoundSetter
    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public float getAverageResponseTime() {
        return averageResponseTime;
    }

    @DataBoundSetter
    public void setAverageResponseTime(float averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public boolean isQualityGatePassed() {
        return qualityGatePassed;
    }

    @DataBoundSetter
    public void setQualityGatePassed(boolean qualityGatePassed) {
        this.qualityGatePassed = qualityGatePassed;
    }

}
