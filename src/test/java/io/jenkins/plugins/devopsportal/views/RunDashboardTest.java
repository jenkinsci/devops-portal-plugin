package io.jenkins.plugins.devopsportal.views;

import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.DeploymentOperation;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RunDashboardTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public ApplicationBuildStatus.DescriptorImpl getApplicationDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    public DeploymentOperation.DescriptorImpl getDeploymentDescriptor() {
        return jenkins.getInstance().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
    }

    public RunDashboard.DescriptorImpl getViewDescriptor() {
        return jenkins.getInstance().getDescriptorByType(RunDashboard.DescriptorImpl.class);
    }

    @Before
    public void initializeConfigurations() {

        // Configure services
        List<ServiceConfiguration> list = new ArrayList<>();
        list.add(new ServiceConfiguration(
                "Server 1",
                "staging",
                "https://staging1.mydomain.com/",
                true,
                10,
                true
        ));
        list.add(new ServiceConfiguration(
                "Server 2",
                "staging",
                "https://staging2.mydomain.com/",
                true,
                10,
                true
        ));
        list.add(new ServiceConfiguration(
                "Server 3",
                "production",
                "https://www.mydomain.com/",
                true,
                5,
                false
        ));
        getServiceDescriptor().setServiceConfigurations(list);

        getApplicationDescriptor().update("My Application", "4.0.1", status -> {
        });

        // Staging deployment
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("4.0.1");
            deployment.setServiceId("Server 1");
            deployment.setTimestamp(Instant.parse("2022-11-30T18:35:24.00Z").getEpochSecond());
            deployment.setTags("A");
            deployment.setBuildBranch("develop");
        });
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("4.0.1");
            deployment.setServiceId("Server 2");
            deployment.setTimestamp(Instant.parse("2023-01-05T14:30:06.00Z").getEpochSecond());
            deployment.setTags("B");
            deployment.setBuildBranch("develop");
        });

        // Fix deployment
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("4.0.1");
            deployment.setServiceId("Server 2");
            deployment.setTimestamp(Instant.parse("2023-02-11T10:09:55.00Z").getEpochSecond());
            deployment.setTags("C");
            deployment.setBuildBranch("develop");
        });

        // Production deployment
        getDeploymentDescriptor().append(deployment -> {
            deployment.setApplicationName("My Application");
            deployment.setApplicationVersion("4.0.1");
            deployment.setServiceId("Server 3");
            deployment.setTimestamp(Instant.parse("2023-02-12T05:11:13.00Z").getEpochSecond());
            deployment.setTags("D");
            deployment.setBuildBranch("master");
        });

    }

    @Test
    public void testGetConfigurationCategories() {
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories(null),
                is(Arrays.asList("production", "staging"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories(""),
                is(Arrays.asList("production", "staging"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories("Inv{a]!.id"),
                is(Collections.emptyList())
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories(".*[n].*"),
                is(Arrays.asList("production", "staging"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories("prod.*"),
                is(Collections.singletonList("production"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getConfigurationCategories("dev.*"),
                is(Collections.emptyList())
        );
    }

    @Test
    public void testGetConfigurationsByCategory() {
        assertEquals(0, getViewDescriptor().getConfigurationsByCategory("development").size());
        assertEquals(2, getViewDescriptor().getConfigurationsByCategory("staging").size());
        assertEquals(1, getViewDescriptor().getConfigurationsByCategory("production").size());
    }

    @Test
    public void testGetMonitoringByService() {
        assertNull(getViewDescriptor().getLastDeploymentByService("Server 0"));
        assertNotNull(getViewDescriptor().getLastDeploymentByService("Server 1"));
        assertNotNull(getViewDescriptor().getLastDeploymentByService("Server 2"));
        assertNotNull(getViewDescriptor().getLastDeploymentByService("Server 3"));
    }

    @Test
    public void testGetLastDeploymentByService() {
        assertNull(getViewDescriptor().getLastDeploymentByService("Server 0"));
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getLastDeploymentByService("Server 1").getTags(),
                is(Collections.singletonList("A"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getLastDeploymentByService("Server 2").getTags(),
                is(Collections.singletonList("C"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                getViewDescriptor().getLastDeploymentByService("Server 3").getTags(),
                is(Collections.singletonList("D"))
        );
    }

    @Test
    public void testGetServicesConfiguration() {
        assertEquals(3, getViewDescriptor().getServicesConfiguration(null).size());
        assertEquals(3, getViewDescriptor().getServicesConfiguration("").size());
        assertEquals(0, getViewDescriptor().getServicesConfiguration("Inv{a]!.id").size());
        assertEquals(2, getViewDescriptor().getServicesConfiguration("staging").size());
        assertEquals(1, getViewDescriptor().getServicesConfiguration("production").size());
        assertEquals(3, getViewDescriptor().getServicesConfiguration(".*[n].*").size());
    }

    @Test
    public void testDeploymentsByService() {
        assertEquals(1, getViewDescriptor().getDeploymentsByService("Server 1").size());
        assertEquals(2, getViewDescriptor().getDeploymentsByService("Server 2").size());
        assertEquals(1, getViewDescriptor().getDeploymentsByService("Server 3").size());
    }

    @Test
    public void testDoCheckFilter() {
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter(null));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter(""));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("Valid"));
        assertEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("V.?lid"));
        assertNotEquals(FormValidation.ok(), getViewDescriptor().doCheckFilter("In{v}4.lid"));
    }

}
