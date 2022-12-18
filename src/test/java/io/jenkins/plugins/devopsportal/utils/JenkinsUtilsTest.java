package io.jenkins.plugins.devopsportal.utils;

import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.*;

public class JenkinsUtilsTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testFindSimpleJob() throws Exception {
        jenkins.createFreeStyleProject("Project A");
        assertNotNull(JenkinsUtils.findJobByName("Project A", null, jenkins.getInstance().getItems()));
        assertNull(JenkinsUtils.findJobByName("Project B", null, jenkins.getInstance().getItems()));
    }

    @Test
    public void testFindNestedJob() throws Exception {
        MockFolder folder = jenkins.createFolder("Folder");
        folder.createProject(FreeStyleProject.class, "Project C");
        jenkins.createFreeStyleProject("Project D");
        assertNotNull(JenkinsUtils.findJobByName("Project C", null, jenkins.getInstance().getItems()));
    }

    @Test
    public void testFindWorkflowJob() throws Exception {
        jenkins.createProject(WorkflowJob.class, "Project E");
        assertNotNull(JenkinsUtils.findJobByName("Project E", null, jenkins.getInstance().getItems()));
    }

    @Test
    public void testGetBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("Project F");
        jenkins.buildAndAssertSuccess(project);
        assertTrue(JenkinsUtils.getBuild("Project F", null, "1").isPresent());
        assertFalse(JenkinsUtils.getBuild("Project F", null, "2").isPresent());
        assertFalse(JenkinsUtils.getBuild("Project G", null, "1").isPresent());
    }

}
