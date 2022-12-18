package io.jenkins.plugins.devopsportal.reporters;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import io.jenkins.plugins.devopsportal.models.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class DeploymentOperationReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String targetService = "Server 2";
    final String applicationName = "My Application";
    final String applicationVersion = "1.0.0";

    @Before
    public void initializeConfigurations() {
        List<ServiceConfiguration> list = new ArrayList<>();
        list.add(new ServiceConfiguration(
                "Server 1",
                "production",
                "https://foo.mydomain.com/",
                true,
                5,
                true
        ));
        list.add(new ServiceConfiguration(
                "Server 2",
                "production",
                "https://bar.mydomain.com/",
                false,
                5,
                false
        ));
        list.add(new ServiceConfiguration(
                "Server 3",
                "staging",
                "https://staging.mydomain.com/",
                true,
                5,
                true
        ));
        jenkins.getInstance()
                .getDescriptorByType(ServiceConfiguration.DescriptorImpl.class)
                .setServiceConfigurations(list);
    }

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        DeploymentOperationReporter reporter = new DeploymentOperationReporter(
                targetService,
                applicationName,
                applicationVersion
        );
        reporter.setTags("ansible, ssh, docker-compose");
        project.getBuildersList().add(reporter);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                reporter,
                project.getBuildersList().get(0)
        );
    }

    @Test
    public void testReporterSuccess() throws Exception {

        int randomNumber = new Random().nextInt(90000) + 10000;

        FreeStyleProject project = jenkins.createFreeStyleProject();
        DeploymentOperationReporter reporter = new DeploymentOperationReporter(
                targetService,
                applicationName,
                applicationVersion
        );
        reporter.setTags("test, id-" + randomNumber);
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(
                "Report run operation 'DEPLOYMENT' on application '" + applicationName +
                        "' to environment 'production' (bar.mydomain.com)",
                build
        );

        ServiceConfiguration service = jenkins.getInstance()
                .getDescriptorByType(ServiceConfiguration.DescriptorImpl.class)
                .getService(targetService)
                .orElse(null);
        assertNotNull(service);

        DeploymentOperation deployment = jenkins
                .getInstance()
                .getDescriptorByType(DeploymentOperation.DescriptorImpl.class)
                .getLastDeploymentByService(service.getId())
                .orElse(null);

        assertNotNull(deployment);
        assertEquals(applicationName, deployment.getApplicationName());
        assertEquals(applicationVersion, deployment.getApplicationVersion());
        assertEquals(service.getId(), deployment.getServiceId());
        assertNotNull(deployment.getBuildJob());
        assertNotNull(deployment.getBuildNumber());
        assertNotNull(deployment.getTags());
        assertEquals(2, deployment.getTags().size());
        assertTrue(deployment.getTags().contains("test"));
        assertTrue(deployment.getTags().contains("id-" + randomNumber));

    }

    @Test
    public void testReporterFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        DeploymentOperationReporter reporter = new DeploymentOperationReporter(
                "Unknown service",
                applicationName,
                applicationVersion
        );
        project.getBuildersList().add(reporter);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(
                "Unable to report a run operation: environment not declared",
                build
        );
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" +
                "  reportDeployOperation(\n" +
                "       targetService: '" + targetService + "',\n" +
                "       applicationName: '" + applicationName + "',\n" +
                "       applicationVersion: '" + applicationVersion + "',\n" +
                "       tags: 'tag1, tag2'\n" +
                "  )\n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun build = jenkins.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));
        jenkins.assertLogContains(
                "Report run operation 'DEPLOYMENT' on application '" + applicationName +
                        "' to environment 'production' (bar.mydomain.com)",
                build
        );

        assertNotNull(jenkins.getInstance()
                .getDescriptorByType(DeploymentOperation.DescriptorImpl.class)
                .getLastDeploymentByApplication(applicationName, applicationVersion)
                .orElse(null));

        ServiceConfiguration service = jenkins.getInstance()
                .getDescriptorByType(ServiceConfiguration.DescriptorImpl.class)
                .getService(targetService)
                .orElse(null);
        assertNotNull(service);

        assertNotNull(jenkins.getInstance()
                .getDescriptorByType(DeploymentOperation.DescriptorImpl.class)
                .getDeploymentByRun(service.getId(), "test-scripted-pipeline", "1")
                .orElse(null));

    }

}
