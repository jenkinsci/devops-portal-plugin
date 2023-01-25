package io.jenkins.plugins.devopsportal.views;

import io.jenkins.plugins.devopsportal.models.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BuildDashboardTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public ApplicationBuildStatus.DescriptorImpl getApplicationDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    public BuildDashboard.DescriptorImpl getViewDescriptor() {
        return jenkins.getInstance().getDescriptorByType(BuildDashboard.DescriptorImpl.class);
    }

    @Before
    public void initializeConfigurations() {

        // Configure services
        List<ServiceConfiguration> list = new ArrayList<>();
        list.add(new ServiceConfiguration(
                "Server 1",
                "production",
                "https://foo.mydomain.com/",
                true,
                5,
                true
        ));
        getServiceDescriptor().setServiceConfigurations(list);

        // Configure application
        getApplicationDescriptor().update("My Application", "2.4.0", status -> {

        });
        getApplicationDescriptor().update("My Application", "2.5.8", status -> {

            // Build
            BuildActivity buildBackend = new BuildActivity("backend");
            buildBackend.setArtifactFileName("backend-api.war");
            buildBackend.setArtifactFileSize(1250013);
            buildBackend.setScore(ActivityScore.A);
            status.setComponentActivityByCategory(ActivityCategory.BUILD, "backend", buildBackend);
            BuildActivity buildFrontend = new BuildActivity("frontend");
            buildFrontend.setArtifactFileName("frontend-angular.zip");
            buildFrontend.setArtifactFileSize(156942);
            buildFrontend.setScore(ActivityScore.A);
            status.setComponentActivityByCategory(ActivityCategory.BUILD, "frontend", buildFrontend);

            // Test
            UnitTestActivity testBackend = new UnitTestActivity("backend");
            testBackend.setTestsPassed(60);
            testBackend.setTestsIgnored(0);
            testBackend.setTestsFailed(4);
            testBackend.setTestCoverage(.50f);
            status.setComponentActivityByCategory(ActivityCategory.UNIT_TEST, "backend", testBackend);
            UnitTestActivity testFrontend = new UnitTestActivity("frontend");
            testFrontend.setTestsPassed(25);
            testFrontend.setTestsIgnored(0);
            testFrontend.setTestsFailed(0);
            testFrontend.setTestCoverage(.40f);
            status.setComponentActivityByCategory(ActivityCategory.UNIT_TEST, "frontend", testFrontend);

        });

        // Configure application
        getApplicationDescriptor().update("My Application", "3.0.0", status -> {

            // Build
            BuildActivity buildBackend = new BuildActivity("backend");
            buildBackend.setArtifactFileName("backend-api.war");
            buildBackend.setArtifactFileSize(1256264);
            buildBackend.setScore(ActivityScore.A);
            status.setComponentActivityByCategory(ActivityCategory.BUILD, "backend", buildBackend);
            BuildActivity buildFrontend = new BuildActivity("frontend");
            buildFrontend.setArtifactFileName("frontend-angular.zip");
            buildFrontend.setArtifactFileSize(0);
            buildFrontend.setScore(ActivityScore.E);
            status.setComponentActivityByCategory(ActivityCategory.BUILD, "frontend", buildFrontend);

            // Quality
            QualityAuditActivity qualityBackend = new QualityAuditActivity("backend");
            qualityBackend.setLinesCount(25000);
            qualityBackend.setBugCount(2);
            qualityBackend.setBugScore(ActivityScore.B);
            qualityBackend.setVulnerabilityCount(4);
            qualityBackend.setVulnerabilityScore(ActivityScore.C);
            qualityBackend.setHotspotCount(0);
            qualityBackend.setHotspotScore(ActivityScore.A);
            qualityBackend.setDuplicationRate(.15f);
            qualityBackend.setQualityGatePassed(false);
            qualityBackend.setTestCoverage(.15f);
            status.setComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, "backend", qualityBackend);
            QualityAuditActivity qualityFrontend = new QualityAuditActivity("frontend");
            qualityFrontend.setLinesCount(14460);
            qualityFrontend.setBugCount(0);
            qualityFrontend.setBugScore(ActivityScore.A);
            qualityFrontend.setVulnerabilityCount(1);
            qualityFrontend.setVulnerabilityScore(ActivityScore.B);
            qualityFrontend.setHotspotCount(8);
            qualityFrontend.setHotspotScore(ActivityScore.D);
            qualityFrontend.setDuplicationRate(.25f);
            qualityFrontend.setQualityGatePassed(true);
            qualityFrontend.setTestCoverage(.35f);
            status.setComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, "frontend", qualityFrontend);

        });

    }

    @Test
    public void testGetApplicationNames() {
        // Without filter
        List<String> names = getViewDescriptor().getApplicationNames(null);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("My Application", names.get(0));
        // With invalid filter
        names = getViewDescriptor().getApplicationNames(".+{7");
        assertNotNull(names);
        assertEquals(0, names.size());
        // With excluding filter
        names = getViewDescriptor().getApplicationNames("My Compo(.*)");
        assertNotNull(names);
        assertEquals(0, names.size());
        // With including filter
        names = getViewDescriptor().getApplicationNames("My App(.*)");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("My Application", names.get(0));
    }

    @Test
    public void testGetApplicationVersions() {
        // Unknown application
        List<String> versions = getViewDescriptor().getApplicationVersions("Unknown application");
        assertNotNull(versions);
        assertEquals(0, versions.size());
        // Valid application
        versions = getViewDescriptor().getApplicationVersions("My Application");
        assertNotNull(versions);
        assertEquals(2, versions.size());
    }

    @Test
    public void testGetApplicationBuild() {
        // Unknown application
        ApplicationBuildStatus status = getViewDescriptor().getApplicationBuild("Unknown application", "1.0.0");
        assertNull(status);
        // Unknown version
        status = getViewDescriptor().getApplicationBuild("My Application", "1.0.0");
        assertNull(status);
        // Valid version
        status = getViewDescriptor().getApplicationBuild("My Application", "3.0.0");
        assertNotNull(status);
    }

    @Test
    public void testGetBuildActivities() {
        ApplicationBuildStatus status = getViewDescriptor().getApplicationBuild("My Application", "3.0.0");
        assertNotNull(status);
        List<AbstractActivity> activities = getViewDescriptor().getBuildActivities(status, "BUILD");
        assertNotNull(activities);
        assertEquals(2, activities.size());
    }

    @Test
    public void testGetLastDeploymentByApplication() {
        // TODO
    }

    @Test
    public void testGetDeploymentTarget() {
        // TODO
    }

    @Test
    public void testGetSummaryBuild() {
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryBuild("Unknown application", "1.0.0").toString()
        );
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryBuild("My Application", "Unknown version").toString()
        );
        assertEquals(
                "{warn/help-circle-outline/NotBuilt}",
                getViewDescriptor().getSummaryBuild("My Application", "2.4.0").toString()
        );
        assertEquals(
                "{good/heart-outline/Healthy}",
                getViewDescriptor().getSummaryBuild("My Application", "2.5.8").toString()
        );
        assertEquals(
                "{bad/skull-outline/Failure}",
                getViewDescriptor().getSummaryBuild("My Application", "3.0.0").toString()
        );
    }

    @Test
    public void testGetSummaryArtifactsCount() {
        assertEquals("-", getViewDescriptor().getSummaryArtifactsCount("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryArtifactsCount("My Application", "Unknown version"));
        assertEquals("0/0", getViewDescriptor().getSummaryArtifactsCount("My Application", "2.4.0"));
        assertEquals("2/2", getViewDescriptor().getSummaryArtifactsCount("My Application", "2.5.8"));
        assertEquals("1/2", getViewDescriptor().getSummaryArtifactsCount("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryCoverageRate() {
        assertEquals("-", getViewDescriptor().getSummaryCoverageRate("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryCoverageRate("My Application", "Unknown version"));
        assertEquals("0%", getViewDescriptor().getSummaryCoverageRate("My Application", "2.4.0"));
        assertEquals("45%", getViewDescriptor().getSummaryCoverageRate("My Application", "2.5.8"));
        assertEquals("25%", getViewDescriptor().getSummaryCoverageRate("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryQuality() {
        // TODO
    }

    @Test
    public void testGetSummaryBugScore() {
        assertEquals("-", getViewDescriptor().getSummaryBugScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryBugScore("My Application", "Unknown version"));
        assertEquals("?", getViewDescriptor().getSummaryBugScore("My Application", "2.4.0"));
        assertEquals("?", getViewDescriptor().getSummaryBugScore("My Application", "2.5.8"));
        assertEquals("B", getViewDescriptor().getSummaryBugScore("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryVulnerabilityScore() {
        assertEquals("-", getViewDescriptor().getSummaryVulnerabilityScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "Unknown version"));
        assertEquals("?", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "2.4.0"));
        assertEquals("?", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "2.5.8"));
        assertEquals("C", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryHotspotScore() {
        assertEquals("-", getViewDescriptor().getSummaryHotspotScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryHotspotScore("My Application", "Unknown version"));
        assertEquals("?", getViewDescriptor().getSummaryHotspotScore("My Application", "2.4.0"));
        assertEquals("?", getViewDescriptor().getSummaryHotspotScore("My Application", "2.5.8"));
        assertEquals("D", getViewDescriptor().getSummaryHotspotScore("My Application", "3.0.0"));
    }


}
