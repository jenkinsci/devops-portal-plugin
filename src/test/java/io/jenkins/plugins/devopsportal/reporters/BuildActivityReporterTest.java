package io.jenkins.plugins.devopsportal.reporters;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import io.jenkins.plugins.devopsportal.models.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import static org.junit.Assert.*;

public class BuildActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.6";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter reporter = new BuildActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setArtifactFileName("artifact.jar");
        project.getBuildersList().add(reporter);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                reporter,
                project.getBuildersList().get(0)
        );
    }

    @Test
    public void testReporterSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter reporter = new BuildActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setArtifactFileName("build.jar");
        reporter.setArtifactFileSizeLimit(1024);
        project.getBuildersList().add(reporter);
        project.setScm(new SingleFileSCM("build.jar", "hello"));

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(
                "Report build activity 'BUILD' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogNotContains(
                "Warning, artifact file not found",
                build
        );

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.BUILD, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof BuildActivity);

        BuildActivity artifact = (BuildActivity) activity;

        assertEquals("build.jar", artifact.getArtifactFileName());
        assertEquals(1024, artifact.getArtifactFileSizeLimit());
        assertEquals(5, artifact.getArtifactFileSize());

        assertEquals(applicationComponent, activity.getApplicationComponent());
        assertEquals("b43fdd98-b1fd-305a-a4c3-a10cf25aad8a", activity.getUUID());
    }

    @Test
    public void testReporterArtifactNotFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter reporter = new BuildActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setArtifactFileName("nofile.jar");
        reporter.setArtifactFileSizeLimit(1024);
        project.getBuildersList().add(reporter);

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.UNSTABLE, project);
        jenkins.assertLogContains(
                "Report build activity 'BUILD' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogContains(
                "Warning, artifact file not found",
                build
        );
    }

    @Test
    public void testReporterArtifactFileExceeded() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter reporter = new BuildActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setArtifactFileName("build.jar");
        reporter.setArtifactFileSizeLimit(3);
        project.getBuildersList().add(reporter);
        project.setScm(new SingleFileSCM("build.jar", "hello"));

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains(
                "Report build activity 'BUILD' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogContains(
                "Current artifact file size exceed limit: 3",
                build
        );
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportBuild(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       artifactFileName: '',\n" +
                "       artifactFileSizeLimit: 650\n" +
                "  )" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'BUILD' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
    }

}