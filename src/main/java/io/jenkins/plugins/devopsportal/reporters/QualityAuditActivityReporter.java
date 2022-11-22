package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ActivityScore;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a QUALITY_AUDIT activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class QualityAuditActivityReporter extends AbstractActivityReporter<QualityAuditActivity> {

    private int bugCount;
    private ActivityScore bugScore;
    private int vulnerabilityCount;
    private ActivityScore vulnerabilityScore;
    private int hotspotCount;
    private ActivityScore hotspotScore;
    private float duplicationRate;
    private float testCoverage;
    private long linesCount;
    private boolean qualityGatePassed;

    @DataBoundConstructor
    public QualityAuditActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public int getBugCount() {
        return bugCount;
    }

    @DataBoundSetter
    public void setBugCount(int bugCount) {
        this.bugCount = bugCount;
    }

    public ActivityScore getBugScore() {
        return bugScore;
    }

    @DataBoundSetter
    public void setBugScore(String bugScore) {
        if (bugScore != null && !bugScore.isEmpty()) {
            this.bugScore = ActivityScore.valueOf(bugScore);
        }
    }

    public int getVulnerabilityCount() {
        return vulnerabilityCount;
    }

    @DataBoundSetter
    public void setVulnerabilityCount(int vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }

    public ActivityScore getVulnerabilityScore() {
        return vulnerabilityScore;
    }

    @DataBoundSetter
    public void setVulnerabilityScore(String vulnerabilityScore) {
        if (vulnerabilityScore != null && !vulnerabilityScore.isEmpty()) {
            this.vulnerabilityScore = ActivityScore.valueOf(vulnerabilityScore);
        }
    }

    public int getHotspotCount() {
        return hotspotCount;
    }

    @DataBoundSetter
    public void setHotspotCount(int hotspotCount) {
        this.hotspotCount = hotspotCount;
    }

    public ActivityScore getHotspotScore() {
        return hotspotScore;
    }

    @DataBoundSetter
    public void setHotspotScore(String hotspotScore) {
        if (hotspotScore != null && !hotspotScore.isEmpty()) {
            this.hotspotScore = ActivityScore.valueOf(hotspotScore);
        }
    }

    public float getDuplicationRate() {
        return duplicationRate;
    }

    @DataBoundSetter
    public void setDuplicationRate(float duplicationRate) {
        this.duplicationRate = duplicationRate;
    }

    public float getTestCoverage() {
        return testCoverage;
    }

    @DataBoundSetter
    public void setTestCoverage(float testCoverage) {
        this.testCoverage = testCoverage;
    }

    public long getLinesCount() {
        return linesCount;
    }

    @DataBoundSetter
    public void setLinesCount(long linesCount) {
        this.linesCount = linesCount;
    }

    public boolean isQualityGatePassed() {
        return qualityGatePassed;
    }

    @DataBoundSetter
    public void setQualityGatePassed(boolean qualityGatePassed) {
        this.qualityGatePassed = qualityGatePassed;
    }

    @Override
    public void updateActivity(@NonNull BuildStatus status, @NonNull QualityAuditActivity activity,
                               @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setBugCount(bugCount);
        activity.setBugScore(bugScore);
        activity.setVulnerabilityCount(vulnerabilityCount);
        activity.setVulnerabilityScore(vulnerabilityScore);
        activity.setHotspotCount(hotspotCount);
        activity.setHotspotScore(hotspotScore);
        activity.setDuplicationRate(duplicationRate);
        activity.setTestCoverage(testCoverage);
        activity.setLinesCount(linesCount);
        activity.setQualityGatePassed(qualityGatePassed);
        if (!qualityGatePassed) {
            activity.setScore(ActivityScore.D);
        }
        else {
            activity.setScore(ActivityScore.min(bugScore, vulnerabilityScore, hotspotScore));
        }
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.QUALITY_AUDIT;
    }

    @Symbol("reportQualityAudit")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.QualityAuditActivityReporter_DisplayName());
        }

        public ListBoxModel doFillBugScoreItems() {
            ListBoxModel list = new ListBoxModel();
            for (ActivityScore status : ActivityScore.values()) {
                list.add(status.name(), status.name());
            }
            return list;
        }

        public ListBoxModel doFillVulnerabilityScoreItems() {
            return doFillBugScoreItems();
        }

        public ListBoxModel doFillHotspotScoreItems() {
            return doFillBugScoreItems();
        }

    }

}
