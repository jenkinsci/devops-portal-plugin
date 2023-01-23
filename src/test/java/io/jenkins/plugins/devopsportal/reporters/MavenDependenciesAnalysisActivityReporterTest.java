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

public class MavenDependenciesAnalysisActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.5";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        MavenDependenciesAnalysisActivityReporter reporter = new MavenDependenciesAnalysisActivityReporter(
                applicationName, applicationVersion, applicationComponent);
        reporter.setReportPath("target/dependency-check-report.xml");
        project.getBuildersList().add(reporter);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                reporter,
                project.getBuildersList().get(0)
        );
    }

    @Test
    public void testFileNotFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        MavenDependenciesAnalysisActivityReporter reporter = new MavenDependenciesAnalysisActivityReporter(
                applicationName, applicationVersion, applicationComponent);
        reporter.setReportPath("dependency-check-report.xml");
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.UNSTABLE, project);
        jenkins.assertLogContains(
                "Report build activity 'DEPENDENCIES_ANALYSIS' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );
        jenkins.assertLogContains(
                "No dependency analysis report 'dependency-check-report.xml' found. Configuration error?",
                build
        );
    }

    @Test
    public void testSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        MavenDependenciesAnalysisActivityReporter reporter = new MavenDependenciesAnalysisActivityReporter(
                applicationName, applicationVersion, applicationComponent);
        reporter.setReportPath("dependency-check-report.xml");
        project.getBuildersList().add(reporter);
        project.setScm(new SingleFileSCM(
                "dependency-check-report.xml",
                new File("src/test/resources/dependency-check-report.xml").toURI().toURL())
        );
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains(
                "Report build activity 'DEPENDENCIES_ANALYSIS' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                build
        );

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.DEPENDENCIES_ANALYSIS, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof DependenciesAnalysisActivity);

        DependenciesAnalysisActivity perf = (DependenciesAnalysisActivity) activity;

        assertEquals(30, perf.getVulnerabilities().getDependenciesCount());
        assertEquals(62, perf.getVulnerabilities().getVulnerabilitiesCount());

    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportMavenDependenciesAnalysis(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       reportPath: 'dependency-check-report.xml'\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'DEPENDENCIES_ANALYSIS' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
        jenkins.assertLogContains(
                "No dependency analysis report 'dependency-check-report.xml' found. Configuration error?",
                completedBuild
        );
    }

}