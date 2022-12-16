package io.jenkins.plugins.reporters;

import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import io.jenkins.plugins.devopsportal.reporters.QualityAuditActivityReporter;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class QualityAuditActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.3";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        QualityAuditActivityReporter reporter = new QualityAuditActivityReporter(applicationName, applicationVersion, applicationComponent);
        reporter.setBugCount(4);
        reporter.setBugScore("B");
        reporter.setVulnerabilityCount(0);
        reporter.setVulnerabilityScore("A");
        reporter.setHotspotCount(2);
        reporter.setHotspotScore("D");
        reporter.setDuplicationRate(.324f);
        reporter.setTestCoverage(.78f);
        reporter.setLinesCount(16500);
        reporter.setQualityGatePassed(true);
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
                "  reportQualityAudit(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       bugCount: 4,\n" +
                "       bugScore: 'B',\n" +
                "       vulnerabilityCount: 6,\n" +
                "       vulnerabilityScore: 'C',\n" +
                "       hotspotCount: 2,\n" +
                "       hotspotScore: 'D',\n" +
                "       duplicationRate: 0.12,\n" +
                "       testCoverage: 0.65,\n" +
                "       linesCount: 23000,\n" +
                "       qualityGatePassed: false\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'QUALITY_AUDIT' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );
    }

}