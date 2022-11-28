package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.CopyOnWriteList;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.utils.JenkinsUtils;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
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
 * A persisted record of a DEPLOYMENT operation performed on a run environment.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DeploymentOperation implements Describable<DeploymentOperation>, Serializable, GenericRunModel {

    private String serviceId;

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
    public DeploymentOperation() {
        tags = new ArrayList<>();
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
        this.tags.clear();
        if (tags != null && !tags.trim().isEmpty()) {
            this.tags.addAll(MiscUtils.split(tags, ","));
        }
    }

    @SuppressWarnings("unused")
    public boolean isBranchProvided() {
        return buildBranch != null && !buildBranch.isEmpty();
    }

    @Override
    public Descriptor<DeploymentOperation> getDescriptor() {
        return Jenkins.get().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final DeploymentOperation other = (DeploymentOperation) that;
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
    public static final class DescriptorImpl extends Descriptor<DeploymentOperation> implements Serializable {

        private final CopyOnWriteList<DeploymentOperation> runOperations = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(DeploymentOperation.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.DeploymentOperation_DisplayName();
        }

        public synchronized List<DeploymentOperation> getRunOperations() {
            List<DeploymentOperation> retVal = new ArrayList<>(runOperations.getView());
            retVal.sort(Comparator.comparing(DeploymentOperation::getApplicationName));
            return retVal;
        }

        public synchronized void append(Consumer<DeploymentOperation> updater) {
            DeploymentOperation record = new DeploymentOperation();
            runOperations.add(record);
            updater.accept(record);
            save();
        }

        public Optional<DeploymentOperation> getLastDeploymentByService(String serviceId) {
            return getRunOperations()
                    .stream()
                    .filter(item -> serviceId.equals(item.getServiceId()))
                    .max(Comparator.comparingLong(DeploymentOperation::getTimestamp));
        }

        public Optional<DeploymentOperation> getLastDeploymentByApplication(String applicationName, String applicationVersion) {
            return getRunOperations()
                    .stream()
                    .filter(item -> item.getApplicationName().equals(applicationName))
                    .filter(item -> item.getApplicationVersion().equals(applicationVersion))
                    .max(Comparator.comparingLong(DeploymentOperation::getTimestamp));
        }

        public List<DeploymentOperation> getDeploymentsByService(String serviceId) {
            return getRunOperations()
                    .stream()
                    .filter(item -> serviceId.equals(item.getServiceId()))
                    .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                    .collect(Collectors.toList());
        }

        public Optional<DeploymentOperation> getDeploymentByRun(String environmentId, String jobName, String runNumber) {
            return getRunOperations()
                    .stream()
                    .filter(item -> environmentId.equals(item.getServiceId()))
                    .filter(item -> jobName.equals(item.getBuildJob()))
                    .filter(item -> runNumber.equals(item.getBuildNumber()))
                    .max((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }

        public boolean deleteDeploymentByRun(String environmentId, String jobName, String runNumber) {
            final DeploymentOperation operation = getDeploymentByRun(environmentId, jobName, runNumber)
                    .orElse(null);
            if (operation != null) {
                synchronized (this) {
                    if (runOperations.remove(operation)) {
                        save();
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
