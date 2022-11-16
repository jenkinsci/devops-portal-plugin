package io.jenkins.plugins.devopsportal;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import io.jenkins.plugins.devopsportal.reporters.BuildActivityReporter;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildActivityReporterTest {
/*
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.3";
    final BuildActivities activity = BuildActivities.BUILD;

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new BuildActivityReporter(applicationName, applicationVersion, activity));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
            new BuildActivityReporter(applicationName, applicationVersion, activity),
            project.getBuildersList().get(0)
        );
    }

    @Test
    public void testConfigRoundtripStatus() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter builder = new BuildActivityReporter(applicationName, applicationVersion, activity);
        builder.setStatus(BuildActivityStatus.FAIL);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        BuildActivityReporter lhs = new BuildActivityReporter(applicationName, applicationVersion, activity);
        lhs.setStatus(BuildActivityStatus.FAIL);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuildStatusAuto() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter builder = new BuildActivityReporter(applicationName, applicationVersion, activity);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("OK TODO", build);
    }

    @Test
    public void testBuildStatusManual() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter builder = new BuildActivityReporter(applicationName, applicationVersion, activity);
        builder.setStatus(BuildActivityStatus.UNSTABLE);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("OK TODO", build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  reportBuildActivity '" + applicationName + "', '" + applicationVersion + "', '" + activity + "'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "OK TODO";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
*/
}