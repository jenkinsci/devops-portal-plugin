package io.jenkins.plugins.devopsportal.reporters;

import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SonarQualityAuditActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.3";
    final String applicationComponent = "backend";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SonarQualityAuditActivityReporter reporter = new SonarQualityAuditActivityReporter(
                applicationName, applicationVersion, applicationComponent);
        reporter.setProjectKey("project-key");
        reporter.setAcceptInvalidCertificate(true);
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
                "  reportSonarQubeAudit(\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       applicationComponent: '" + applicationComponent + "',\n" +
                "       projectKey: 'project-key',\n" +
                "       acceptInvalidCertificate: false\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report build activity 'QUALITY_AUDIT' for application '" + applicationName + "' version "
                        + applicationVersion + " component '" + applicationComponent + "'",
                completedBuild
        );

    }

}