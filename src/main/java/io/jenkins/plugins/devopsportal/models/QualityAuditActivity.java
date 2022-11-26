package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.sonarqube.ws.Hotspots;
import org.sonarqube.ws.Issues;

import java.util.ArrayList;
import java.util.List;

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
    private boolean qualityGatePassed = false;
    private boolean complete = false;

    private List<QualityIssue> bugs;
    private List<QualityIssue> vulnerabilities;
    private List<SecurityHotspot> hotspots;

    @DataBoundConstructor
    public QualityAuditActivity(String applicationComponent) {
        super(ActivityCategory.QUALITY_AUDIT, applicationComponent);
        this.bugs = new ArrayList<>();
        this.vulnerabilities = new ArrayList<>();
        this.hotspots = new ArrayList<>();
    }

    public int getBugCount() {
        return bugCount;
    }

    @DataBoundSetter
    public void setBugCount(int bugCount) {
        this.bugCount = bugCount;
        if (bugCount == 0) {
            this.bugs = new ArrayList<>();
        }
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
        if (vulnerabilityCount == 0) {
            this.vulnerabilities = new ArrayList<>();
        }
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
        if (hotspotCount == 0) {
            this.hotspots = new ArrayList<>();
        }
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

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public List<QualityIssue> getBugs() {
        return bugs;
    }

    public List<QualityIssue> getVulnerabilities() {
        return vulnerabilities;
    }

    public List<SecurityHotspot> getHotspots() {
        return hotspots;
    }

    public boolean hasIssues() {
        return bugs.size() > 0 || vulnerabilities.size() > 0 || hotspots.size() > 0;
    }

    public void addBug(Issues.Issue issue) {
        if (issue != null) {
            this.bugCount++;
            this.bugs.add(new QualityIssue(issue));
        }
    }

    public void addVulnerability(Issues.Issue issue) {
        if (issue != null) {
            this.vulnerabilityCount++;
            this.vulnerabilities.add(new QualityIssue(issue));
        }
    }

    public void addHotSpot(Hotspots.SearchWsResponse.Hotspot issue) {
        if (issue != null) {
            this.hotspotCount++;
            this.hotspots.add(new SecurityHotspot(issue));
        }
    }

}
