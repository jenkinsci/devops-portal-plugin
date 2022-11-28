package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
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

    private long requestCount;
    private float averageResponseTime;
    private boolean qualityGatePassed;

    @DataBoundConstructor
    public PerformanceTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
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

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull PerformanceTestActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setRequestCount(requestCount);
        activity.setAverageResponseTime(averageResponseTime);
        activity.setQualityGatePassed(qualityGatePassed);
        if (!qualityGatePassed) {
            activity.setScore(ActivityScore.E);
            return Result.UNSTABLE;
        }
        else {
            activity.setScore(ActivityScore.A);
            return null;
        }
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

        @SuppressWarnings("unused")
        public ListBoxModel doFillBugScoreItems() {
            ListBoxModel list = new ListBoxModel();
            for (ActivityScore status : ActivityScore.values()) {
                list.add(status.name(), status.name());
            }
            return list;
        }

    }

}
