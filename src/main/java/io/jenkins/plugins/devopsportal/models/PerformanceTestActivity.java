package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * A persistent record of a PERFORMANCE_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class PerformanceTestActivity extends AbstractActivity {

    private long testCount;
    private long sampleCount;
    private long errorCount;

    @DataBoundConstructor
    public PerformanceTestActivity(String applicationComponent) {
        super(ActivityCategory.PERFORMANCE_TEST, applicationComponent);
    }

    public long getTestCount() {
        return testCount;
    }

    @DataBoundSetter
    public void setTestCount(long testCount) {
        this.testCount = testCount;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    @DataBoundSetter
    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    @DataBoundSetter
    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public boolean isQualityGatePassed() {
        return sampleCount > 0 && errorCount == 0;
    }

}
