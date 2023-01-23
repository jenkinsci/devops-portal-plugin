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

import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class JMeterPerformanceTestActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.8";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JMeterPerformanceTestActivityReporter reporter = new JMeterPerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setJmeterReportPath("report.xml");
        project.getBuildersList().add(reporter);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                reporter,
                project.getBuildersList().get(0)
        );
    }

    @Test
    public void testReporterReportFileNotFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JMeterPerformanceTestActivityReporter reporter = new JMeterPerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setJmeterReportPath("report.xml");
        project.getBuildersList().add(reporter);

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogContains(
                "The file is unreadable: report.xml",
                build
        );
    }

    @Test
    public void testReporterSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JMeterPerformanceTestActivityReporter reporter = new JMeterPerformanceTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setJmeterReportPath("jmeter-report.xml");
        project.getBuildersList().add(reporter);
        project.setScm(new SingleFileSCM(
                "jmeter-report.xml",
                new File("src/test/resources/jmeter-report.xml").toURI().toURL())
        );
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.UNSTABLE, project);
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

        PerformanceTestActivity tests = (PerformanceTestActivity) activity;

        assertEquals(10, tests.getTestCount());
        assertEquals(80, tests.getSampleCount());
        assertEquals(6, tests.getErrorCount());

    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportJMeterPerformanceTest(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       jmeterReportPath: 'report.xml'\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'PERFORMANCE_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
    }

}
