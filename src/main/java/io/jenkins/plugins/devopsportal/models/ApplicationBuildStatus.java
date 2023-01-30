package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.util.CopyOnWriteList;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.utils.JenkinsUtils;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A persistent record of the progress of build activities for a software release.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ApplicationBuildStatus implements Describable<ApplicationBuildStatus>, Serializable, GenericRunModel {

    private String applicationName;
    private String applicationVersion;

    private String buildJob;
    private String buildNumber;
    private String buildURL;
    private String buildBranch;
    private String buildCommit;
    private long buildTimestamp; // seconds

    private final Map<ActivityCategory, List<AbstractActivity>> activities;

    @DataBoundConstructor
    public ApplicationBuildStatus() {
        activities = new HashMap<>();
        for (ActivityCategory category : ActivityCategory.values()) {
            activities.put(category, new ArrayList<>());
        }
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

    public long getBuildTimestamp() {
        return buildTimestamp;
    }

    @DataBoundSetter
    @Override
    public void setBuildTimestamp(long buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public Map<ActivityCategory, List<AbstractActivity>> getActivities() {
        return activities;
    }

    @SuppressWarnings("unused")
    public boolean isBuildBranchPresent() {
        return buildBranch != null && !buildBranch.isEmpty();
    }

    @SuppressWarnings("unused")
    public boolean isBuildCommitPresent() {
        return buildCommit != null && !buildCommit.isEmpty();
    }

    @Override
    public Descriptor<ApplicationBuildStatus> getDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final ApplicationBuildStatus other = (ApplicationBuildStatus) that;
        return new EqualsBuilder()
                .append(applicationName, other.applicationName)
                .append(applicationVersion, other.applicationVersion)
                .append(buildBranch, other.buildBranch)
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

    @SuppressWarnings("unchecked")
    public <T extends AbstractActivity> Result updateActivity(@NonNull String applicationComponent,
                                                              @NonNull ActivityCategory category,
                                                              @NonNull TaskListener listener,
                                                              @NonNull EnvVars env,
                                                              @NonNull GenericActivityHandler<T> updater,
                                                              @NonNull FilePath workspace) {
        T activity;
        synchronized (activities) {
            activity = (T) activities.getOrDefault(category, new ArrayList<>())
                    .stream()
                    .filter(item -> item.getApplicationComponent().equals(applicationComponent))
                    .findFirst()
                    .orElse(null);
            if (activity == null) {
                activity = createActivity(category, applicationComponent);
                if (!activities.containsKey(category)) {
                    activities.put(category, new ArrayList<>());
                }
                activities.get(category).add(activity);
            }
        }
        final Result result = updater.updateActivity(this, activity, listener, env, workspace);
        synchronized (activities) {
            getDescriptor().save();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractActivity> T createActivity(@NonNull ActivityCategory category,
                                                          @NonNull String applicationComponent) {
        try {
            assert category.getClazz() != null;
            return (T) category.getClazz().getConstructor(String.class).newInstance(applicationComponent);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AbstractActivity> getActivitiesByCategory(@NonNull ActivityCategory category) {
        synchronized (activities) {
            return this.activities.get(category);
        }
    }

    public Optional<AbstractActivity> getComponentActivityByCategory(@NonNull ActivityCategory category,
                                                                     @NonNull String applicationComponent) {
        synchronized (activities) {
            if (!activities.containsKey(category)) {
                return Optional.empty();
            }
            return activities
                    .get(category)
                    .stream()
                    .filter(item -> applicationComponent.equals(item.getApplicationComponent()))
                    .findFirst();
        }
    }


    public boolean removeComponentActivity(@NonNull ActivityCategory category, @NonNull String applicationComponent) {
        Optional<AbstractActivity> activity = getComponentActivityByCategory(category, applicationComponent);
        if (activity.isPresent()) {
            synchronized (activities) {
                return this.activities.get(category).remove(activity.get());
            }
        }
        return false;
    }

    public void setComponentActivityByCategory(@NonNull ActivityCategory category,
                                               @NonNull String applicationComponent,
                                               @NonNull AbstractActivity activity) {
        synchronized (activities) {
            // Create category
            if (!activities.containsKey(category)) {
                activities.put(category, new ArrayList<>());
            }
            // Remove old activity
            removeComponentActivity(category, applicationComponent);
            // Add new one
            getActivitiesByCategory(category).add(activity);
            getDescriptor().save();
        }
    }

    @SuppressWarnings("unused")
    public String getUUID() {
        return UUID.nameUUIDFromBytes((applicationName + applicationVersion).getBytes(StandardCharsets.UTF_8)).toString();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ApplicationBuildStatus> implements Serializable {

        private final CopyOnWriteList<ApplicationBuildStatus> buildStatus = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ApplicationBuildStatus.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildStatus_DisplayName();
        }

        public synchronized List<ApplicationBuildStatus> getBuildStatus() {
            List<ApplicationBuildStatus> retVal = new ArrayList<>(buildStatus.getView());
            retVal.sort(Comparator.comparing(ApplicationBuildStatus::getApplicationName));
            return retVal;
        }

        public synchronized Optional<ApplicationBuildStatus> getBuildStatusByApplication(@NonNull String applicationName,
                                                                                         @NonNull String applicationVersion) {
            return buildStatus
                    .getView()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .filter(item -> applicationVersion.trim().equals(item.getApplicationVersion()))
                    .findFirst();
        }

        public synchronized void update(@NonNull String applicationName, @NonNull String applicationVersion,
                                        @NonNull Consumer<ApplicationBuildStatus> updater) {
            ApplicationBuildStatus status = getBuildStatusByApplication(applicationName, applicationVersion).orElse(null);
            if (status == null) {
                status = new ApplicationBuildStatus();
                status.setApplicationName(applicationName.trim());
                status.setApplicationVersion(applicationVersion.trim());
                buildStatus.add(status);
            }
            updater.accept(status);
            status.setBuildTimestamp(Instant.now().getEpochSecond());
            save();
        }

        public synchronized void update(@NonNull String jobName, int buildNumber,
                                        @NonNull Consumer<ApplicationBuildStatus> updater) {
            List<ApplicationBuildStatus> status = buildStatus
                    .getView()
                    .stream()
                    .filter(item -> jobName.equals(item.getBuildJob()))
                    .filter(item -> String.valueOf(buildNumber).equals(item.getBuildNumber()))
                    .collect(Collectors.toList());
            for (ApplicationBuildStatus record : status) {
                updater.accept(record);
            }
            if (!status.isEmpty()) {
                save();
            }
        }

        public boolean isApplicationExists(@NonNull String applicationName) {
            return getBuildStatus().stream().anyMatch(item -> applicationName.equals(item.getApplicationName()));
        }

        public boolean deleteBuildStatusByApplicationVersion(String applicationName, String applicationVersion) {
            final ApplicationBuildStatus status = getBuildStatusByApplication(applicationName, applicationVersion)
                    .orElse(null);
            if (status != null) {
                synchronized (this) {
                    if (buildStatus.remove(status)) {
                        save();
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
