package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * A persistent record of a QUALITY_AUDIT activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class QualityAuditActivity extends AbstractActivity {

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
    public QualityAuditActivity(String applicationComponent) {
        super(ActivityCategory.QUALITY_AUDIT, applicationComponent);
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
    public void setBugScore(ActivityScore bugScore) {
        this.bugScore = bugScore;
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
    public void setVulnerabilityScore(ActivityScore vulnerabilityScore) {
        this.vulnerabilityScore = vulnerabilityScore;
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
    public void setHotspotScore(ActivityScore hotspotScore) {
        this.hotspotScore = hotspotScore;
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

}
