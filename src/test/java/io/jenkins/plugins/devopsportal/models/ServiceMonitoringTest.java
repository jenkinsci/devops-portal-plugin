package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.Messages;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ServiceMonitoringTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    public ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public ServiceMonitoring.DescriptorImpl getMonitoringDescriptor() {
        return jenkins.getInstance().getDescriptorByType(ServiceMonitoring.DescriptorImpl.class);
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
        getServiceDescriptor().setServiceConfigurations(list);
    }

    public ServiceMonitoring getDefaultConfiguration() {
        ServiceMonitoring m = new ServiceMonitoring();
        m.setServiceId("1ba56-15a9e1-f1b5a9");
        m.setFailure(MonitoringStatus.INVALID_HTTPS, "Handshake failure");
        m.setCertificateExpiration(1678195335);
        m.setFailureCount(3);
        m.setLastCertificateCheckTimestamp(1678193330);
        m.setLastFailureTimestamp(1678193331);
        m.setLastSuccessTimestamp(1678193332);
        return m;
    }

    @Test
    public void testInstance() {
        ServiceMonitoring m = getDefaultConfiguration();
        assertEquals("1ba56-15a9e1-f1b5a9", m.getServiceId());
        assertEquals(MonitoringStatus.INVALID_HTTPS, m.getCurrentMonitoringStatus());
        assertEquals("Handshake failure", m.getLastFailureReason());
        assertEquals(1678195335, m.getCertificateExpiration());
        assertEquals(1678193330, m.getLastCertificateCheckTimestamp());
        assertEquals(1678193331, m.getLastFailureTimestamp());
        assertEquals(1678193332, m.getLastSuccessTimestamp());
        assertEquals(1678193332, m.getLastTimestamp());
        assertEquals(1678193332, m.getSinceTimestamp());
        assertEquals(1678193332, m.getLastUpdateTimestamp());
        assertTrue(m.isAvailabilityUpdateRequired(5));
        assertTrue(m.isCertificateUpdateRequired());
        assertTrue(m.isCertificateExpired());
        assertEquals("icon-yellow", m.getIcon());
        assertTrue(m.isFailure());
        assertNotNull(m.getDescriptor());
        assertEquals("ServiceMonitoring[1ba56-15a9e1-f1b5a9,INVALID_HTTPS,1678193332,1678193331,Handshake failure,3,1678193330,1678195335]", m.toString());
    }

    @Test
    public void testDescriptor() {
        assertEquals(Messages.ServiceMonitoring_DisplayName(), getMonitoringDescriptor().getDisplayName());
        assertFalse(getMonitoringDescriptor().getMonitoringByService("1ba56-15a9e1-f1b5a9").isPresent());
        assertFalse(getMonitoringDescriptor().getMonitoringByService("Server 1").isPresent());
        getMonitoringDescriptor().update("Server 1", item -> {
            item.setLastSuccessTimestamp(new Date().getTime());
            item.setCurrentMonitoringStatus(MonitoringStatus.SUCCESS);
        });
        assertTrue(getMonitoringDescriptor().getMonitoringByService("Server 1").isPresent());
    }

}
