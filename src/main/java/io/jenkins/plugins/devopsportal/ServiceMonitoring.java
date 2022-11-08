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
    private MonitoringStatus currentMonitoringStatus;
    private long lastSuccessTimestamp;
    private long lastFailureTimestamp;
    private String lastFailureReason;
    private int failureCount;

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

    public MonitoringStatus getCurrentMonitoringStatus() {
        return currentMonitoringStatus;
    }

    @DataBoundSetter
    public void setCurrentMonitoringStatus(MonitoringStatus currentMonitoringStatus) {
        this.currentMonitoringStatus = currentMonitoringStatus;
    }

    public long getLastSuccessTimestamp() {
        return lastSuccessTimestamp;
    }

    @DataBoundSetter
    public void setLastSuccessTimestamp(long lastSuccessTimestamp) {
        this.lastSuccessTimestamp = lastSuccessTimestamp;
    }

    public long getLastFailureTimestamp() {
        return lastFailureTimestamp;
    }

    @DataBoundSetter
    public void setLastFailureTimestamp(long lastFailureTimestamp) {
        this.lastFailureTimestamp = lastFailureTimestamp;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    @DataBoundSetter
    public void setLastFailureReason(String lastFailureReason) {
        this.lastFailureReason = lastFailureReason;
    }

    public int getFailureCount() {
        return failureCount;
    }

    @DataBoundSetter
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getIcon() {
        if (currentMonitoringStatus == null) {
            return MonitoringStatus.defaultIcon();
        }
        return currentMonitoringStatus.getIcon();
    }

    public long getLastTimestamp() {
        return Math.max(lastSuccessTimestamp, lastFailureTimestamp);
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
                .append(currentMonitoringStatus, other.currentMonitoringStatus)
                .append(lastSuccessTimestamp, other.lastSuccessTimestamp)
                .append(lastFailureTimestamp, other.lastFailureTimestamp)
                .append(lastFailureReason, other.lastFailureReason)
                .append(failureCount, other.failureCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(serviceId)
                .append(currentMonitoringStatus)
                .append(lastSuccessTimestamp)
                .append(lastFailureTimestamp)
                .append(lastFailureReason)
                .append(failureCount)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(serviceId)
                .append(currentMonitoringStatus)
                .append(lastSuccessTimestamp)
                .append(lastFailureTimestamp)
                .append(lastFailureReason)
                .append(failureCount)
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

        public void update(@NonNull ServiceConfiguration service, @NonNull MonitoringStatus status, String failureReason) {
            ServiceMonitoring item = getMonitoringByService(service.getId()).orElse(null);
            boolean create = false;
            if (item == null) {
                item = new ServiceMonitoring();
                item.setServiceId(service.getId());
                create = true;
            }
            if (status == MonitoringStatus.SUCCESS) {
                item.setLastSuccessTimestamp(Instant.now().getEpochSecond());
                item.setFailureCount(0);
            }
            else {
                item.setLastFailureTimestamp(Instant.now().getEpochSecond());
                item.setFailureCount(item.getFailureCount() + 1);
            }
            item.setCurrentMonitoringStatus(status);
            item.setLastFailureReason(failureReason);
            if (create) {
                servicesMonitoring.add(item);
            }
            save();
        }

        public Optional<ServiceMonitoring> getMonitoringByService(String id) {
            return servicesMonitoring.getView().stream().filter(item -> id.equals(item.getServiceId()))
                    .max(Comparator.comparing(ServiceMonitoring::getLastTimestamp));
        }

    }

}
