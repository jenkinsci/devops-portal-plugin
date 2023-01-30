package io.jenkins.plugins.devopsportal.models;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
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
        assertTrue(service1.isValidURL());
        assertTrue(service1.isEnableMonitoring());
        assertTrue(service1.isAcceptInvalidCertificate());
        ServiceConfiguration service2 = getDescriptor().getService(service1.getId()).orElse(null);
        assertNotNull(service2);
        assertEquals(service1.getLabel(), service2.getLabel());
    }

    @Test
    public void testProperties() {
        ServiceConfiguration service = getDescriptor().getService("Server 1").orElse(null);
        assertNotNull(service);
        assertEquals("ServiceConfiguration[Server 1,production,https://foo.mydomain.com/,true,5,true]", service.toString());
        assertEquals(-37982208, service.hashCode());
        assertEquals(service, getDescriptor().getService("Server 1").orElse(null));
        assertNotEquals(service, getDescriptor().getService("Server 2").orElse(null));
        assertEquals("foo.mydomain.com", service.getHostname());
        service.setUrl("inval!d://URL");
        assertFalse(service.isHttps());
        assertFalse(service.isValidURL());
        assertEquals("", service.getHostname());
    }

    @Test
    public void testDescriptor() {
        Descriptor<?> descriptor = jenkins.getInstance().getDescriptor(ServiceConfiguration.class);
        assertNotNull(descriptor);
        assertTrue(descriptor instanceof ServiceConfiguration.DescriptorImpl);
        ServiceConfiguration.DescriptorImpl descriptorImpl = ((ServiceConfiguration.DescriptorImpl) descriptor);
        assertTrue(descriptorImpl.getService("Server 1").isPresent());
        assertNotNull(descriptorImpl.doCheckLabel("", "Server 1"));
        assertNotNull(descriptorImpl.doCheckLabel("Server 1", "Server 1"));
        assertEquals(FormValidation.ok(), descriptorImpl.doCheckLabel("Server X", "Server 1"));
    }

}
