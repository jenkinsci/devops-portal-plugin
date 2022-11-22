package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ActivityScore;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.UnitTestActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a UNIT_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class UnitTestActivityReporter extends AbstractActivityReporter<UnitTestActivity> {

    private float testCoverage;
    private int testsPassed;
    private int testsFailed;
    private int testsIgnored;

    @DataBoundConstructor
    public UnitTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public float getTestCoverage() {
        return testCoverage;
    }

    @DataBoundSetter
    public void setTestCoverage(float testCoverage) {
        this.testCoverage = testCoverage;
    }

    public int getTestsPassed() {
        return testsPassed;
    }

    @DataBoundSetter
    public void setTestsPassed(int testsPassed) {
        this.testsPassed = testsPassed;
    }

    public int getTestsFailed() {
        return testsFailed;
    }

    @DataBoundSetter
    public void setTestsFailed(int testsFailed) {
        this.testsFailed = testsFailed;
    }

    public int getTestsIgnored() {
        return testsIgnored;
    }

    @DataBoundSetter
    public void setTestsIgnored(int testsIgnored) {
        this.testsIgnored = testsIgnored;
    }

    @Override
    public void updateActivity(@NonNull ApplicationBuildStatus status, @NonNull UnitTestActivity activity,
                               @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setTestCoverage(testCoverage);
        activity.setTestsPassed(testsPassed);
        activity.setTestsFailed(testsFailed);
        activity.setTestsIgnored(testsIgnored);
        activity.setScore(testsFailed > 0 ? ActivityScore.D : ActivityScore.A);
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.UNIT_TEST;
    }

    @Symbol("reportUnitTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.UnitTestActivityReporter_DisplayName());
        }

    }

}
