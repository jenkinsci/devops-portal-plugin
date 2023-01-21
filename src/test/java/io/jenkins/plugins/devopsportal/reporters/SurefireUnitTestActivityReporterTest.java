package io.jenkins.plugins.devopsportal.reporters;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import io.jenkins.plugins.devopsportal.models.AbstractActivity;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.UnitTestActivity;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import static org.junit.Assert.*;

public class SurefireUnitTestActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.2";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SurefireUnitTestActivityReporter reporter = new SurefireUnitTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setSurefireReportPath("report-surefire.xml");
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
        SurefireUnitTestActivityReporter reporter = new SurefireUnitTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setSurefireReportPath("report-surefire.xml");
        project.getBuildersList().add(reporter);

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains(
                "Report build activity 'UNIT_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogContains(
                "No test reports that matches 'report-surefire.xml' found. Configuration error?",
                build
        );
    }

    @Test
    public void testReporterSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SurefireUnitTestActivityReporter reporter = new SurefireUnitTestActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setSurefireReportPath("report-*.xml");
        project.getBuildersList().add(reporter);
        project.setScm(new SingleFileSCM("report-surefire.xml", createReportContent()));

        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains(
                "Report build activity 'UNIT_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.UNIT_TEST, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof UnitTestActivity);

        UnitTestActivity tests = (UnitTestActivity) activity;

        assertEquals(4, tests.getTestsFailed());
        assertEquals(2, tests.getTestsIgnored());
        assertEquals(51, tests.getTestsPassed());
        assertEquals(0, tests.getTestCoverage(), 0);

    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportSurefireTest(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       surefireReportPath: 'report-surefire.xml'\n" +
                "  )" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'UNIT_TEST' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
        jenkins.assertLogContains(
                "No test reports that matches 'report-surefire.xml' found. Configuration error?",
                completedBuild
        );
    }

    private String createReportContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<testsuite xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report-3.0.xsd\" version=\"3.0\" name=\"InjectedTest\" time=\"34.738\" tests=\"51\" errors=\"3\" skipped=\"2\" failures=\"1\">\n");
        sb.append("  <!-- content -->\n");
        sb.append("</testsuite>");
        return sb.toString();
    }

}
