package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ActivityScore;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.PerformanceTestActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a PERFORMANCE_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class PerformanceTestActivityReporter extends AbstractActivityReporter<PerformanceTestActivity> {

    private long testCount;
    private long sampleCount;
    private long errorCount;

    @DataBoundConstructor
    public PerformanceTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
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

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull PerformanceTestActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env, @NonNull FilePath workspace) {
        activity.setTestCount(testCount);
        activity.setSampleCount(sampleCount);
        activity.setErrorCount(errorCount);
        return handleActivityResult(activity);
    }

    public static Result handleActivityResult(@NonNull PerformanceTestActivity activity) {
        // Errors presents
        if (activity.getErrorCount() > 0) {
            // Total failure
            if (activity.getErrorCount() >= activity.getSampleCount()) {
                activity.setScore(ActivityScore.E);
                return Result.FAILURE;
            }
            // Partial failure
            float ratio = (float) activity.getErrorCount() / (float) activity.getSampleCount() * 100;
            if (ratio <= 10) {
                activity.setScore(ActivityScore.B);
            }
            else if (ratio <= 30) {
                activity.setScore(ActivityScore.C);
            }
            else {
                activity.setScore(ActivityScore.D);
            }
            return Result.UNSTABLE;
        }
        activity.setScore(activity.getSampleCount() > 0 ? ActivityScore.A : null);
        return null;
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.PERFORMANCE_TEST;
    }

    @Symbol("reportPerformanceTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.PerformanceTestActivityReporter_DisplayName());
        }

    }

}
