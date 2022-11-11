package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.CopyOnWriteList;
import io.jenkins.plugins.devopsportal.utils.JenkinsUtils;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A persisted record of an exploitation operation performed on a run platform.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ServiceOperation implements Describable<ServiceOperation>, Serializable {

    private String serviceId;

    private RunOperations operation;
    private boolean success;
    private long timestamp; // seconds

    private String applicationName;
    private String applicationVersion;

    private String buildJob;
    private String buildNumber;
    private String buildURL;
    private String buildBranch;
    private String buildCommit;

    private final List<String> tags;

    @DataBoundConstructor
    public ServiceOperation() {
        tags = new ArrayList<>();
    }

    public String getServiceId() {
        return serviceId;
    }

    @DataBoundSetter
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public RunOperations getOperation() {
        return operation;
    }

    @DataBoundSetter
    public void setOperation(RunOperations operation) {
        this.operation = operation;
    }

    public boolean isSuccess() {
        return success;
    }

    @DataBoundSetter
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @DataBoundSetter
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @DataBoundSetter
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getBuildJob() {
        return buildJob;
    }

    @DataBoundSetter
    public void setBuildJob(String buildJob) {
        this.buildJob = buildJob;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    @DataBoundSetter
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildURL() {
        return buildURL;
    }

    @DataBoundSetter
    public void setBuildURL(String buildURL) {
        this.buildURL = buildURL;
    }

    public String getBuildBranch() {
        return buildBranch;
    }

    @DataBoundSetter
    public void setBuildBranch(String buildBranch) {
        this.buildBranch = buildBranch;
    }

    public String getBuildCommit() {
        return buildCommit;
    }

    @DataBoundSetter
    public void setBuildCommit(String buildCommit) {
        this.buildCommit = buildCommit;
    }

    public List<String> getTags() {
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        if (tags != null && !tags.isEmpty()) {
            this.tags.addAll(Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public Descriptor<ServiceOperation> getDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceOperation.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final ServiceOperation other = (ServiceOperation) that;
        return new EqualsBuilder()
                .append(applicationName, other.applicationName)
                .append(applicationVersion, other.applicationVersion)
                .append(buildBranch, other.buildBranch)
                .append(buildCommit, other.buildCommit)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(applicationName)
                .append(applicationVersion)
                .append(buildJob)
                .append(buildNumber)
                .append(buildURL)
                .append(buildBranch)
                .append(buildCommit)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(applicationName)
                .append(applicationVersion)
                .append(buildJob)
                .append(buildNumber)
                .append(buildURL)
                .append(buildBranch)
                .append(buildCommit)
                .toString();
    }

    public String getBuildStatusClass() {
        Run<?, ?> job = JenkinsUtils.getBuild(buildJob, buildBranch, buildNumber).orElse(null);
        if (job != null) {
            return job.getBuildStatusIconClassName();
        }
        return "icon-disabled";
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceOperation> implements Serializable {

        private final CopyOnWriteList<ServiceOperation> runOperations = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ServiceOperation.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildStatus_DisplayName();
        }

        public List<ServiceOperation> getRunOperations() {
            List<ServiceOperation> retVal = new ArrayList<>(runOperations.getView());
            retVal.sort(Comparator.comparing(ServiceOperation::getApplicationName));
            return retVal;
        }

        public void append(Consumer<ServiceOperation> updater) {
            ServiceOperation record = new ServiceOperation();
            runOperations.add(record);
            updater.accept(record);
            save();
        }

        public Optional<ServiceOperation> getLastDeploymentByService(String serviceId) {
            return getRunOperations()
                    .stream()
                    .filter(item -> serviceId.equals(item.getServiceId()))
                    .filter(item -> item.getOperation() == RunOperations.DEPLOYMENT)
                    .max(Comparator.comparingLong(ServiceOperation::getTimestamp));
        }

        public Optional<ServiceOperation> getLastDeploymentByApplication(String applicationName, String applicationVersion) {
            return getRunOperations()
                    .stream()
                    .filter(item -> item.getApplicationName().equals(applicationName))
                    .filter(item -> item.getApplicationVersion().equals(applicationVersion))
                    .filter(item -> item.getOperation() == RunOperations.DEPLOYMENT)
                    .max(Comparator.comparingLong(ServiceOperation::getTimestamp));
        }

    }

}
