package io.jenkins.plugins.devopsportal.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class QualityAuditActivityTest {

    @Test
    public void testDefaultValues() {
        QualityAuditActivity activity = new QualityAuditActivity("Application component");
        assertEquals(0, activity.getBugCount());
        assertEquals(0, activity.getVulnerabilityCount());
        assertEquals(0, activity.getHotspotCount());
        assertNull(activity.getBugScore());
        assertNull(activity.getVulnerabilityScore());
        assertNull(activity.getHotspotScore());
        assertFalse(activity.isQualityGatePassed());
        assertFalse(activity.isComplete());
        assertFalse(activity.hasIssues());
        assertNotNull(activity.getBugs());
        assertNotNull(activity.getVulnerabilities());
        assertNotNull(activity.getHotspots());
        assertEquals(0, activity.getBugs().size());
        assertEquals(0, activity.getVulnerabilities().size());
        assertEquals(0, activity.getHotspots().size());
        assertEquals(0, activity.getTestCoverage(), 0);
        assertEquals("0.00%", activity.getTestCoverageStr());
        assertEquals(0, activity.getDuplicationRate(), 0);
        assertEquals("0.00%", activity.getDuplicationRateStr());
        assertEquals(0, activity.getLinesCount());
        assertEquals("0", activity.getLinesCountStr());
    }

    @Test
    public void testWithValues() throws Exception {
        QualityAuditActivity activity = new QualityAuditActivity("Application component");
        ObjectMapper mapper = new ObjectMapper();
        List<?> metrics = mapper.readValue(new File("src/test/resources/sonar-metrics.json"), List.class);
        List<?> issues = mapper.readValue(new File("src/test/resources/sonar-issues.json"), List.class);
        List<?> hotspots = mapper.readValue(new File("src/test/resources/sonar-hotspots.json"), List.class);
        activity.setMetrics((List<Map<String, Object>>) metrics);
        activity.setIssues((List<Map<String, Object>>) issues);
        activity.setHotSpots((List<Map<String, Object>>) hotspots);
        assertEquals(3, activity.getBugCount());
        assertEquals(4, activity.getVulnerabilityCount());
        assertEquals(0, activity.getHotspotCount());
        assertEquals(ActivityScore.A, activity.getBugScore());
        assertEquals(ActivityScore.B, activity.getVulnerabilityScore());
        assertEquals(ActivityScore.C, activity.getHotspotScore());
        assertTrue(activity.isQualityGatePassed());
        assertFalse(activity.isComplete());
        assertTrue(activity.hasIssues());
        assertNotNull(activity.getBugs());
        assertNotNull(activity.getVulnerabilities());
        assertNotNull(activity.getHotspots());
        assertEquals(3, activity.getBugs().size());
        assertEquals(4, activity.getVulnerabilities().size());
        assertEquals(0, activity.getHotspots().size());
        assertEquals(0.56, activity.getTestCoverage(), 0.001);
        assertEquals("56.00%", activity.getTestCoverageStr());
        assertEquals(0.084, activity.getDuplicationRate(), 0.0001);
        assertEquals("8.40%", activity.getDuplicationRateStr());
        assertEquals(4774, activity.getLinesCount());
        assertEquals("5k", activity.getLinesCountStr());
    }

}
