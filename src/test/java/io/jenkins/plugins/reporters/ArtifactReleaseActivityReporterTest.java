package io.jenkins.plugins.reporters;

import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import io.jenkins.plugins.devopsportal.models.*;
import io.jenkins.plugins.devopsportal.reporters.ArtifactReleaseActivityReporter;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

public class ArtifactReleaseActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.7";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ArtifactReleaseActivityReporter reporter = new ArtifactReleaseActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setRepositoryName("registry.myserver.com");
        reporter.setArtifactName("my-application/1.0.3");
        reporter.setArtifactURL("https://registry.myserver.com/projects/my-application/tags?1.0.3");
        reporter.setTags("docker,harbor");
        project.getBuildersList().add(reporter);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                reporter,
                project.getBuildersList().get(0)
        );
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportArtifactRelease(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       repositoryName: 'registry.myserver.com',\n" +
                "       artifactName: 'my-application/1.0.3',\n" +
                "       artifactURL: 'https://registry.myserver.com/projects/my-application/tags?1.0.3',\n" +
                "       tags: 'docker,harbor'\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'ARTIFACT_RELEASE' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.ARTIFACT_RELEASE, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof ArtifactReleaseActivity);

        ArtifactReleaseActivity release = (ArtifactReleaseActivity) activity;

        assertEquals("registry.myserver.com", release.getRepositoryName());
        assertEquals("my-application/1.0.3", release.getArtifactName());
        assertTrue(release.isUrlPresent());
        assertEquals("https://registry.myserver.com/projects/my-application/tags?1.0.3", release.getArtifactURL());
        assertNotNull(release.getTags());
        assertEquals(2, release.getTags().size());

    }

}