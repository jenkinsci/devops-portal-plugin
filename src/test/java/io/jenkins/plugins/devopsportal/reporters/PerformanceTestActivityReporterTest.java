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

import static org.junit.Assert.*;

public class PerformanceTestActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.4";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        PerformanceTestActivityReporter reporter = new PerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setTestCount(10);
        reporter.setSampleCount(450);
        reporter.setErrorCount(3);
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
        PerformanceTestActivityReporter reporter = new PerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setTestCount(10);
        reporter.setSampleCount(450);
        reporter.setErrorCount(0);
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.PERFORMANCE_TEST, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof PerformanceTestActivity);

        PerformanceTestActivity perf = (PerformanceTestActivity) activity;

        assertEquals(10, perf.getTestCount());
        assertEquals(450, perf.getSampleCount());
        assertEquals(0, perf.getErrorCount());
        assertTrue(perf.isQualityGatePassed());

    }

    @Test
    public void testReporterPartialFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        PerformanceTestActivityReporter reporter = new PerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setTestCount(10);
        reporter.setSampleCount(450);
        reporter.setErrorCount(3);
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.UNSTABLE, project);
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
    }

    @Test
    public void testReporterTotalFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        PerformanceTestActivityReporter reporter = new PerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setTestCount(10);
        reporter.setSampleCount(450);
        reporter.setErrorCount(450);
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportPerformanceTest(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       testCount: 10,\n" +
                "       sampleCount: 450,\n" +
                "       errorCount: 3\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
    }

}