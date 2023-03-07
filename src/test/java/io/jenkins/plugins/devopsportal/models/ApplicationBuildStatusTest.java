package io.jenkins.plugins.devopsportal.models;

import hudson.model.Descriptor;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ApplicationBuildStatusTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public ApplicationBuildStatus getDefaultConfiguration(String applicationName, String applicationVersion) {
        ApplicationBuildStatus status = new ApplicationBuildStatus();
        status.setApplicationName(applicationName);
        status.setApplicationVersion(applicationVersion);
        status.setBuildJob("My pipeline");
        status.setBuildURL("https://jenkins.mydomain.com/");
        status.setBuildNumber("18");
        status.setBuildCommit("f5a9d6a3");
        status.setBuildTimestamp(new Date().getTime());
        status.setBuildBranch("master");
        return status;
    }

    @Test
    public void testDefaultSettings() {
        ApplicationBuildStatus cfg1 = getDefaultConfiguration("My Application", "3.5.16");
        ApplicationBuildStatus cfg2 = getDefaultConfiguration("My Other Application", "1.0.16");
        assertEquals("ApplicationBuildStatus[My Application,3.5.16,My pipeline,18,https://jenkins.mydomain.com/,master,f5a9d6a3]", cfg1.toString());
        assertEquals(-474427335, cfg1.hashCode());
        assertNotEquals(cfg1, cfg2);
        assertEquals("icon-disabled", cfg1.getBuildStatusClass());
        assertEquals("90b89598-89e9-311b-b7e9-da5cf48e1a9e", cfg1.getUUID());
        assertEquals("master", cfg1.getBuildBranch());
        assertEquals("f5a9d6a3", cfg1.getBuildCommit());
        assertEquals("https://jenkins.mydomain.com/", cfg1.getBuildURL());
        assertTrue(cfg1.isBuildBranchPresent());
        assertTrue(cfg1.isBuildCommitPresent());
    }

    @Test
    public void testActivities() {
        ApplicationBuildStatus cfg1 = getDefaultConfiguration("My Application", "3.5.16");
        BuildActivity activity = new BuildActivity("component-x");
        cfg1.setComponentActivityByCategory(ActivityCategory.BUILD, "component-x", activity);
        assertEquals(1, cfg1.getActivitiesByCategory(ActivityCategory.BUILD).size());
        assertEquals(0, cfg1.getActivitiesByCategory(ActivityCategory.UNIT_TEST).size());
        assertEquals(activity, cfg1.getComponentActivityByCategory(ActivityCategory.BUILD, "component-x").orElse(null));
        cfg1.removeComponentActivity(ActivityCategory.BUILD, "component-x");
        assertEquals(0, cfg1.getActivitiesByCategory(ActivityCategory.BUILD).size());
        assertNull(cfg1.getComponentActivityByCategory(ActivityCategory.BUILD, "component-x").orElse(null));
    }

    @Test
    public void testDescriptor() {
        Descriptor<?> descriptor = jenkins.getInstance().getDescriptor(ApplicationBuildStatus.class);
        assertNotNull(descriptor);
        assertTrue(descriptor instanceof ApplicationBuildStatus.DescriptorImpl);
        ApplicationBuildStatus.DescriptorImpl descriptorImpl = ((ApplicationBuildStatus.DescriptorImpl) descriptor);
        AtomicReference<ApplicationBuildStatus> ref = new AtomicReference<>();
        descriptorImpl.update("My New Application", "2.4.0", status -> {
            assertNotNull(status);
            ref.set(status);
            status.setBuildJob("My New Job");
            status.setBuildNumber("20");
            assertEquals(0, status.getBuildTimestamp());
        });
        assertNotNull(ref.get());
        descriptorImpl.update("Job Name", 15, status -> fail());
        descriptorImpl.update("My New Job", 20, status -> {
            assertNotNull(status);
            assertEquals(ref.get(), status);
            assertNotEquals(0, status.getBuildTimestamp());
        });
        assertTrue(descriptorImpl.getBuildStatusByApplication("My New Application", "2.4.0").isPresent());
        descriptorImpl.deleteBuildStatusByApplicationVersion("My New Application", "2.4.0");
        assertFalse(descriptorImpl.getBuildStatusByApplication("My New Application", "2.4.0").isPresent());
        descriptorImpl.update("My New Job", 20, status -> fail());
    }

}
