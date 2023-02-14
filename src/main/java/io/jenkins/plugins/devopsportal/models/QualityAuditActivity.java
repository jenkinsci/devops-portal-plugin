package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public String getDuplicationRateStr() {
        return String.format("%.2f", duplicationRate * 100) + "%";
    }

    @DataBoundSetter
    public void setDuplicationRate(float duplicationRate) {
        this.duplicationRate = duplicationRate;
    }

    public float getTestCoverage() {
        return testCoverage;
    }

    public String getTestCoverageStr() {
        return String.format("%.2f", testCoverage * 100) + "%";
    }

    @DataBoundSetter
    public void setTestCoverage(float testCoverage) {
        this.testCoverage = testCoverage;
    }

    public long getLinesCount() {
        return linesCount;
    }

    public String getLinesCountStr() {
        if (linesCount < 1000) {
            return "" + linesCount;
        }
        return Math.round(linesCount / 1000f) + "k";
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
        return (bugs != null && vulnerabilities != null && hotspots != null) &&
                (bugs.size() > 0 || vulnerabilities.size() > 0 || hotspots.size() > 0);
    }

    public void setMetrics(List<Map<String, Object>> metrics) {

        setQualityGatePassed(!"ERROR".equalsIgnoreCase(MiscUtils.getStringOrEmpty(
                metrics, "metric", "alert_status", "value")));

        setBugScore(ActivityScore.parseString(MiscUtils.getStringOrEmpty(
                metrics, "metric", "reliability_rating", "value")));

        setVulnerabilityScore(ActivityScore.parseString(MiscUtils.getStringOrEmpty(
                metrics, "metric", "security_rating", "value")));

        setHotspotScore(ActivityScore.parseString(MiscUtils.getStringOrEmpty(
                metrics, "metric", "security_review_rating", "value")));

        String value = MiscUtils.getStringOrEmpty(metrics, "metric", "coverage", "value");
        setTestCoverage(MiscUtils.getFloatOrZero(value) / 100f);

        value = MiscUtils.getStringOrEmpty(metrics, "metric", "duplicated_lines_density", "value");
        setDuplicationRate(MiscUtils.getFloatOrZero(value) / 100f);

        value = MiscUtils.getStringOrEmpty(metrics, "metric", "ncloc", "value");
        setLinesCount(MiscUtils.getIntOrZero(value));

    }

    public void setIssues(List<Map<String, Object>> issues) {
        setBugCount(0);
        setVulnerabilityCount(0);
        for (Map<String, Object> item : issues) {
            if ("java:S1135".equals(MiscUtils.getStringOrEmpty(item, "rule"))) {
                // Ignore TO DO issues
                continue;
            }
            String type = MiscUtils.getStringOrEmpty(item, "type").toUpperCase();
            if ("BUG".equals(type)) {
                this.bugs.add(new QualityIssue(item));
            }
            else if ("VULNERABILITY".equals(type)) {
                this.vulnerabilities.add(new QualityIssue(item));
            }
        }
        bugCount = this.bugs.size();
        vulnerabilityCount = this.vulnerabilities.size();
    }

    public void setHotSpots(@NonNull List<Map<String, Object>> hotspots) {
        this.hotspotCount = hotspots.size();
        this.hotspots = hotspots.stream().map(SecurityHotspot::new).collect(Collectors.toList());
    }

}
