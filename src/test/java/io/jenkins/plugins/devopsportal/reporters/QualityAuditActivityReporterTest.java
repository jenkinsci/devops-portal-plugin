package io.jenkins.plugins.devopsportal.reporters;

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
        reporter.setDuplicationRate(.32f);
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

        ApplicationBuildStatus status = jenkins
                .getInstance()
                .getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class)
                .getBuildStatusByApplication(applicationName, applicationVersion)
                .orElse(null);

        assertNotNull(status);

        AbstractActivity activity = status.getComponentActivityByCategory(ActivityCategory.QUALITY_AUDIT, applicationComponent)
                .orElse(null);

        assertNotNull(activity);
        assertTrue(activity instanceof QualityAuditActivity);

        QualityAuditActivity qa = (QualityAuditActivity) activity;

        assertEquals(4, qa.getBugCount());
        assertEquals(ActivityScore.B, qa.getBugScore());
        assertEquals(6, qa.getVulnerabilityCount());
        assertEquals(ActivityScore.C, qa.getVulnerabilityScore());
        assertEquals(2, qa.getHotspotCount());
        assertEquals(ActivityScore.D, qa.getHotspotScore());
        assertEquals(0.12, qa.getDuplicationRate(), 0.01);
        assertEquals(0.65, qa.getTestCoverage(), 0.01);
        assertEquals(23000, qa.getLinesCount());
        assertFalse(qa.isQualityGatePassed());
        assertTrue(qa.isComplete());

    }

}