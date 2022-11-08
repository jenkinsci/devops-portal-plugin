package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.CopyOnWriteList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

public class ServiceMonitoring implements Describable<ServiceMonitoring> {

    private String serviceId;
    private long timestamp;
    private Boolean serviceMonitoringStatus;
    private Boolean hostMonitoringStatus;

    @DataBoundConstructor
    public ServiceMonitoring() {
    }

    public ServiceMonitoring(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    @DataBoundSetter
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @DataBoundSetter
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getServiceMonitoringStatus() {
        return serviceMonitoringStatus;
    }

    @DataBoundSetter
    public void setServiceMonitoringStatus(Boolean serviceMonitoringStatus) {
        this.serviceMonitoringStatus = serviceMonitoringStatus;
    }

    public Boolean getHostMonitoringStatus() {
        return hostMonitoringStatus;
    }

    @DataBoundSetter
    public void setHostMonitoringStatus(Boolean hostMonitoringStatus) {
        this.hostMonitoringStatus = hostMonitoringStatus;
    }

    public String getIcon() {
        if (serviceMonitoringStatus == null) {
            if (hostMonitoringStatus == null) {
                return "icon-disabled";
            }
            return hostMonitoringStatus ? "icon-blue" : "icon-red";
        }
        else {
            return serviceMonitoringStatus ? "icon-blue" : "icon-red";
        }
        // "icon-yellow"
    }

    @Override
    public Descriptor<ServiceMonitoring> getDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceMonitoring.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final ServiceMonitoring other = (ServiceMonitoring) that;
        return new EqualsBuilder()
                .append(serviceId, other.serviceId)
                .append(timestamp, other.timestamp)
                .append(serviceMonitoringStatus, other.serviceMonitoringStatus)
                .append(hostMonitoringStatus, other.hostMonitoringStatus)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(serviceId)
                .append(timestamp)
                .append(serviceMonitoringStatus)
                .append(hostMonitoringStatus)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(serviceId)
                .append(timestamp)
                .append(serviceMonitoringStatus)
                .append(hostMonitoringStatus)
                .toString();
    }

    @Symbol("servicemonitoring")
    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceMonitoring> {

        private final CopyOnWriteList<ServiceMonitoring> servicesMonitoring = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ServiceMonitoring.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ServiceMonitoring_DisplayName();
        }

        public void update(ServiceConfiguration service, MonitoringStatus serviceStatus, Boolean hostStatus) {
            ServiceMonitoring item = getMonitoringByService(service.getId()).orElse(null);
            boolean create = false;
            if (item == null) {
                item = new ServiceMonitoring();
                item.setServiceId(service.getId());
                create = true;
            }
            item.setTimestamp(Instant.now().getEpochSecond());
            item.setServiceMonitoringStatus(serviceStatus == null ? null : serviceStatus == MonitoringStatus.OK);
            item.setHostMonitoringStatus(hostStatus);
            if (create) {
                servicesMonitoring.add(item);
            }
            save();
        }

        public Optional<ServiceMonitoring> getMonitoringByService(String id) {
            return servicesMonitoring.getView().stream().filter(item -> id.equals(item.getServiceId()))
                    .max(Comparator.comparing(ServiceMonitoring::getTimestamp));
        }

    }

}
