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

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

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
            if (service.isMonitoringAvailable()) {
                MonitoringStatus serviceStatus;
                String failureReason;
                try {
                    int httpStatus = executeServiceMonitoring(service.getUrl());
                    if (HttpURLConnection.HTTP_OK == httpStatus) {
                        serviceStatus = MonitoringStatus.SUCCESS;
                        failureReason = null;
                    }
                    else {
                        serviceStatus = MonitoringStatus.FAILURE;
                        failureReason = "HTTP Status: " + httpStatus;
                    }
                }
                catch (MalformedURLException ex) {
                    serviceStatus = MonitoringStatus.INVALID_CONFIGURATION;
                    failureReason = "Invalid URL";
                }
                catch (Exception ex) {
                    serviceStatus = MonitoringStatus.FAILURE;
                    failureReason = ex.getMessage();
                }
                LOGGER.info("Monitor service: " + service.getLabel() + " (id: " + service.getId() + ", url: '"
                        + service.getUrl() + "', status: " + serviceStatus + ")");
                getMonitoringDescriptor().update(service, serviceStatus, failureReason);
            }
        }
    }

    private int executeServiceMonitoring(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.connect();
        return urlConn.getResponseCode();
    }

}
