package io.jenkins.plugins.devopsportal;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

@Extension
public class MonitoringPeriodicWork extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.releasedashboard");

    public MonitoringPeriodicWork() {
        super("Monitoring Worker Thread");
    }

    @Override
    public long getRecurrencePeriod() {
        return MIN;
    }

    public ServiceConfiguration.DescriptorImpl getServicesDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public ServiceMonitoring.DescriptorImpl getMonitoringDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceMonitoring.DescriptorImpl.class);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return;
        }
        for (ServiceConfiguration service : getServicesDescriptor().getServiceConfigurations()) {
            // Service monitoring
            MonitoringStatus serviceStatus = null;
            if (service.isServiceMonitoringAvailable()) {
                serviceStatus = executeServiceMonitoring(service.getUrl());
                LOGGER.info("Monitor service: " + service.getLabel() + " (id: " + service.getId() + ", url: '"
                        + service.getUrl() + "', status: " + serviceStatus + ")");
            }
            // Host monitoring
            Boolean hostStatus = null;
            if (service.isHostMonitoringAvailable()) {
                hostStatus = executeHostMonitoring(service.getHostname());
                LOGGER.info("Monitor host: " + service.getLabel() + " (id: " + service.getId() + ", host: '"
                        + service.getHostname() + "', status: " + hostStatus + ")");
            }
            getMonitoringDescriptor().update(service, serviceStatus, hostStatus);
        }
    }

    private Boolean executeHostMonitoring(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return false;
        }
        try {
            return InetAddress.getByName(hostname).isReachable(500);
        }
        catch (Exception ex) {
            return false;
        }
    }

    private MonitoringStatus executeServiceMonitoring(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            if (HttpURLConnection.HTTP_OK == urlConn.getResponseCode()) {
                return MonitoringStatus.OK;
            }
            return MonitoringStatus.KO_OTHER;
        }
        catch (MalformedURLException ex) {
            return MonitoringStatus.KO_BAD_CONFIGURATION;
        }
        catch (Exception ex) {
            return MonitoringStatus.KO_NOT_REACHABLE;
        }
    }

}
