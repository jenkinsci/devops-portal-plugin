package io.jenkins.plugins.devopsportal.models;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ServiceConfigurationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public ServiceConfiguration.DescriptorImpl getDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

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
        getDescriptor().setServiceConfigurations(list);
    }

    @Test
    public void testDefaultSettings() {
        assertEquals(5, getDescriptor().getDefaultDelayMonitoringMinutes());
        assertTrue(getDescriptor().getDefaultEnableMonitoring());
        assertFalse(getDescriptor().getDefaultAcceptInvalidCertificate());
    }

    @Test
    public void testGetters() {
        assertNotNull(getDescriptor().getServiceConfigurations());
        assertEquals(3, getDescriptor().getServiceConfigurations().size());
        ServiceConfiguration service1 = getDescriptor().getService("Server 1").orElse(null);
        assertNotNull(service1);
        assertNotNull(service1.getId());
        assertEquals("Server 1", service1.getLabel());
        assertEquals("production", service1.getCategory());
        assertEquals("https://foo.mydomain.com/", service1.getUrl());
        assertEquals(5, service1.getDelayMonitoringMinutes());
        assertTrue(service1.isHttps());
        assertTrue(service1.isEnableMonitoring());
        assertTrue(service1.isAcceptInvalidCertificate());
        ServiceConfiguration service2 = getDescriptor().getService(service1.getId()).orElse(null);
        assertNotNull(service2);
        assertEquals(service1.getLabel(), service2.getLabel());
    }

}
