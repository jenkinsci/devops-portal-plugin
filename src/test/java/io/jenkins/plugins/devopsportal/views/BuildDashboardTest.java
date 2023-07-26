package io.jenkins.plugins.devopsportal.views;

import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.models.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.servlet.ServletException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
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

    public DeploymentOperation.DescriptorImpl getDeploymentDescriptor() {
        return jenkins.getInstance().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
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

            // Quality
            QualityAuditActivity qualityBackend = new QualityAuditActivity("backend");
            qualityBackend.setLinesCount(14500);
            qualityBackend.setBugCount(25);
            qualityBackend.setBugScore(ActivityScore.D);
            qualityBackend.setVulnerabilityCount(10);
            qualityBackend.setVulnerabilityScore(ActivityScore.C);
            qualityBackend.setHotspotCount(8);
            qualityBackend.setHotspotScore(ActivityScore.D);
            qualityBackend.setDuplicationRate(.14f);
            qualityBackend.setQualityGatePassed(false);
            qualityBackend.setComplete(false);
            status.setComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, "backend", qualityBackend);

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

            // Quality
            QualityAuditActivity qualityBackend = new QualityAuditActivity("backend");
            qualityBackend.setLinesCount(14500);
            qualityBackend.setBugCount(2);
            qualityBackend.setBugScore(ActivityScore.B);
            qualityBackend.setVulnerabilityCount(0);
            qualityBackend.setVulnerabilityScore(ActivityScore.A);
            qualityBackend.setHotspotCount(0);
            qualityBackend.setHotspotScore(ActivityScore.A);
            qualityBackend.setDuplicationRate(.14f);
            qualityBackend.setQualityGatePassed(true);
            qualityBackend.setComplete(true);
            status.setComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, "backend", qualityBackend);

            // Dependencies
            DependenciesAnalysisActivity dependenciesBackend = new DependenciesAnalysisActivity("backend");
            VulnerabilityAnalysisResult vulnerabilities = new VulnerabilityAnalysisResult();
            vulnerabilities.add("dependency1")
                    .add(new DependencyVulnerability("CVE-2020-11022", "MEDIUM", Arrays.asList("network", "confidentiality")));
            vulnerabilities.add("dependency2")
                    .add(new DependencyVulnerability("CVE-2022-40152", "CRITICAL", Arrays.asList("network", "availability")));
            vulnerabilities.add("dependency3")
                    .add(new DependencyVulnerability("CVE-2022-40152", "MEDIUM", Arrays.asList("network", "integrity")));
            dependenciesBackend.setVulnerabilities(vulnerabilities);
            status.setComponentActivityByCategory(ActivityCategory.DEPENDENCIES_ANALYSIS, "backend", dependenciesBackend);

            // Release
            ArtifactReleaseActivity releaseBackend = new ArtifactReleaseActivity("backend");
            releaseBackend.setArtifactName("Backend API");
            releaseBackend.setRepositoryName("Artifactory");
            releaseBackend.setArtifactURL("https://myartifactory.mysociety.com/artifact/a.b.c/latest");
            releaseBackend.setTags("tag1,tag2,tag3");
            status.setComponentActivityByCategory(ActivityCategory.ARTIFACT_RELEASE, "backend", releaseBackend);
        });

        // Deploy 2.5.8
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("2.5.8");
            deployment.setServiceId("Server 1");
            deployment.setTimestamp(Instant.parse("2022-11-30T18:35:24.00Z").getEpochSecond());
            deployment.setTags("A");
        });
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("2.5.8");
            deployment.setServiceId("Server 1");
            deployment.setTimestamp(Instant.parse("2023-01-05T14:30:06.00Z").getEpochSecond());
            deployment.setTags("B");
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
            qualityBackend.setComplete(true);
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
            qualityFrontend.setComplete(true);
            status.setComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, "frontend", qualityFrontend);

            // Dependencies
            DependenciesAnalysisActivity dependenciesBackend = new DependenciesAnalysisActivity("backend");
            VulnerabilityAnalysisResult vulnerabilities = new VulnerabilityAnalysisResult();
            vulnerabilities.add("dependency1")
                    .add(new DependencyVulnerability("CVE-2020-11022", "MEDIUM", Arrays.asList("network", "confidentiality")));
            vulnerabilities.add("dependency2")
                    .add(new DependencyVulnerability("CVE-2022-40152", "HIGH", Arrays.asList("network", "availability")));
            vulnerabilities.add("dependency3")
                    .add(new DependencyVulnerability("CVE-2022-40152", "MEDIUM", Arrays.asList("network", "integrity")));
            vulnerabilities.add("dependency4")
                    .add(new DependencyVulnerability("CVE-2022-22976", "LOW", Arrays.asList("network", "confidentiality")));
            vulnerabilities.add("dependency5")
                    .add(new DependencyVulnerability("CVE-2022-45047", "HIGH", Arrays.asList("network", "easycomplexity")));
            dependenciesBackend.setVulnerabilities(vulnerabilities);
            status.setComponentActivityByCategory(ActivityCategory.DEPENDENCIES_ANALYSIS, "backend", dependenciesBackend);

            // Release
            ArtifactReleaseActivity releaseBackend = new ArtifactReleaseActivity("backend");
            releaseBackend.setArtifactName("Backend API");
            releaseBackend.setRepositoryName("Artifactory");
            releaseBackend.setArtifactURL("https://myartifactory.mysociety.com/artifact/a.b.c/latest");
            releaseBackend.setTags("docker,latest,3.0.0,tomcat");
            status.setComponentActivityByCategory(ActivityCategory.ARTIFACT_RELEASE, "backend", releaseBackend);
            ArtifactReleaseActivity releaseFrontend = new ArtifactReleaseActivity("frontend");
            releaseFrontend.setArtifactName("Backend API");
            releaseFrontend.setRepositoryName("Artifactory");
            releaseFrontend.setArtifactURL("https://myartifactory.mysociety.com/artifact/a.b.c/latest");
            releaseFrontend.setTags("docker,latest,3.0.0,angular");
            status.setComponentActivityByCategory(ActivityCategory.ARTIFACT_RELEASE, "frontend", releaseFrontend);

        });

        // Deploy 3.0.0
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("3.0.0");
            deployment.setServiceId("Server 1");
            deployment.setTimestamp(Instant.parse("2023-02-14T08:11:13.00Z").getEpochSecond());
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
        assertEquals(3, versions.size());
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
        assertNull(getViewDescriptor().getLastDeploymentByApplication("Unknown application", "1.0.0"));
        assertNull(getViewDescriptor().getLastDeploymentByApplication("My Application", "Unknown version"));
        assertNull(getViewDescriptor().getLastDeploymentByApplication("My Application", "2.4.0"));
        DeploymentOperation deployment = getViewDescriptor().getLastDeploymentByApplication("My Application", "2.5.8");
        assertNotNull(deployment);
        assertEquals("My Application", deployment.getApplicationName());
        assertEquals("2.5.8", deployment.getApplicationVersion());
        assertEquals("Server 1", deployment.getServiceId());
        assertEquals(1672929006L, deployment.getTimestamp());
        org.hamcrest.MatcherAssert.assertThat(
                deployment.getTags(),
                is(Collections.singletonList("B"))
        );
        assertNotNull(getViewDescriptor().getLastDeploymentByApplication("My Application", "3.0.0"));
    }

    @Test
    public void testGetDeploymentTarget() {
        DeploymentOperation deployment = getViewDescriptor().getLastDeploymentByApplication("My Application", "3.0.0");
        assertNotNull(deployment);
        ServiceConfiguration service = getViewDescriptor().getDeploymentTarget(deployment);
        assertNotNull(service);
        assertEquals("Server 1", service.getLabel());
        assertEquals("foo.mydomain.com", service.getHostname());
        service = getViewDescriptor().getDeploymentTarget(null);
        assertNull(service);
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
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryQuality("Unknown application", "1.0.0").toString()
        );
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryQuality("My Application", "Unknown version").toString()
        );
        assertEquals(
                "{pending/sync-circle-outline/Updating}",
                getViewDescriptor().getSummaryQuality("My Application", "2.4.0").toString()
        );
        assertEquals(
                "{bad/skull-outline/Dependency Failure}",
                getViewDescriptor().getSummaryQuality("My Application", "2.5.8").toString()
        );
        assertEquals(
                "{bad/skull-outline/Quality Failure}",
                getViewDescriptor().getSummaryQuality("My Application", "3.0.0").toString()
        );
    }

    @Test
    public void testGetSummaryBugScore() {
        assertEquals("-", getViewDescriptor().getSummaryBugScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryBugScore("My Application", "Unknown version"));
        assertEquals("D", getViewDescriptor().getSummaryBugScore("My Application", "2.4.0"));
        assertEquals("B", getViewDescriptor().getSummaryBugScore("My Application", "2.5.8"));
        assertEquals("B", getViewDescriptor().getSummaryBugScore("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryVulnerabilityScore() {
        assertEquals("-", getViewDescriptor().getSummaryVulnerabilityScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "Unknown version"));
        assertEquals("C", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "2.4.0"));
        assertEquals("A", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "2.5.8"));
        assertEquals("C", getViewDescriptor().getSummaryVulnerabilityScore("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryHotspotScore() {
        assertEquals("-", getViewDescriptor().getSummaryHotspotScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryHotspotScore("My Application", "Unknown version"));
        assertEquals("D", getViewDescriptor().getSummaryHotspotScore("My Application", "2.4.0"));
        assertEquals("A", getViewDescriptor().getSummaryHotspotScore("My Application", "2.5.8"));
        assertEquals("D", getViewDescriptor().getSummaryHotspotScore("My Application", "3.0.0"));
    }

    @Test
    public void getSummaryDependencyVulnerabilityCount() {
        assertEquals("-", getViewDescriptor().getSummaryDependencyVulnerabilityCount("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryDependencyVulnerabilityCount("My Application", "Unknown version"));
        assertEquals("0", getViewDescriptor().getSummaryDependencyVulnerabilityCount("My Application", "2.4.0"));
        assertEquals("3", getViewDescriptor().getSummaryDependencyVulnerabilityCount("My Application", "2.5.8"));
        assertEquals("5", getViewDescriptor().getSummaryDependencyVulnerabilityCount("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryWorstScore() {
        assertEquals("-", getViewDescriptor().getSummaryWorstScore("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryWorstScore("My Application", "Unknown version"));
        assertEquals("-", getViewDescriptor().getSummaryWorstScore("My Application", "2.4.0"));
        assertEquals("CRITICAL", getViewDescriptor().getSummaryWorstScore("My Application", "2.5.8"));
        assertEquals("HIGH", getViewDescriptor().getSummaryWorstScore("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryRelease() {
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryRelease("Unknown application", "1.0.0").toString()
        );
        assertEquals(
                "{warn/help-circle-outline/Unknown}",
                getViewDescriptor().getSummaryRelease("My Application", "Unknown version").toString()
        );
        assertEquals(
                "{bad/skull-outline/Missing}",
                getViewDescriptor().getSummaryRelease("My Application", "2.4.0").toString()
        );
        assertEquals(
                "{good/heart-outline/Healthy}",
                getViewDescriptor().getSummaryRelease("My Application", "2.5.8").toString()
        );
        assertEquals(
                "{good/heart-outline/Healthy}",
                getViewDescriptor().getSummaryRelease("My Application", "3.0.0").toString()
        );
    }

    @Test
    public void testGetSummaryReleasesCount() {
        assertEquals("-", getViewDescriptor().getSummaryReleasesCount("Unknown application", "1.0.0"));
        assertEquals("-", getViewDescriptor().getSummaryReleasesCount("My Application", "Unknown version"));
        assertEquals("0", getViewDescriptor().getSummaryReleasesCount("My Application", "2.4.0"));
        assertEquals("1", getViewDescriptor().getSummaryReleasesCount("My Application", "2.5.8"));
        assertEquals("2", getViewDescriptor().getSummaryReleasesCount("My Application", "3.0.0"));
    }

    @Test
    public void testGetSummaryReleasesTags() {
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getSummaryReleasesTags("Unknown application", "1.0.0"),
                is(Collections.emptyList())
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getSummaryReleasesTags("My Application", "Unknown version"),
                is(Collections.emptyList())
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getSummaryReleasesTags("My Application", "2.4.0"),
                is(Collections.emptyList())
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getSummaryReleasesTags("My Application", "2.5.8"),
                is(Arrays.asList("tag1", "tag2", "tag3"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getSummaryReleasesTags("My Application", "3.0.0"),
                is(Arrays.asList("docker", "latest", "3.0.0", "tomcat", "angular"))
        );
    }

    @Test
    public void testDoCheckFilter() {
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter(null));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter(""));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("Valid"));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("V.?lid"));
        assertNotEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("In{v}4.lid"));
    }

    @Test
    public void testInstance() throws ServletException, IOException {
        BuildDashboard view = new BuildDashboard("Name");
        assertNotNull(view.getItems());
        assertEquals(0, view.getItems().size());
        assertFalse(view.contains(null));
        assertNull(view.doCreateItem(null, null));
        assertEquals("", view.getFilter());
        view.setFilter("filter");
        assertEquals("filter", view.getFilter());
        assertTrue(view.getRootURL().matches("http://localhost:(.*)/jenkins/"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("1970/01/01 00:00", view.formatDatetimeSeconds(0L));
        assertEquals("1970/01/02 10:17", view.formatDatetimeSeconds(123456L));
        assertEquals("1973/11/29 21:34", view.formatDatetimeSeconds(123456890L));
    }

}
