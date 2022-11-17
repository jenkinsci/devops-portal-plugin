package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * A persistent record of a UNIT_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class UnitTestActivity extends AbstractActivity {

    private float testCoverage;
    private int testsPassed;
    private int testsFailed;
    private int testsIgnored;

    @DataBoundConstructor
    public UnitTestActivity(String applicationComponent) {
        super(ActivityCategory.UNIT_TEST, applicationComponent);
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

}
