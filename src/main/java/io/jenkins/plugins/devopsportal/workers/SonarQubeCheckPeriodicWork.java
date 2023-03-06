package io.jenkins.plugins.devopsportal.workers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import io.jenkins.plugins.devopsportal.utils.SonarApiClient;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.jenkins.plugins.devopsportal.utils.MiscUtils.checkNotEmpty;

/**
 * Scheduled task that monitor a SonarQube server.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@Extension
public class SonarQubeCheckPeriodicWork extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");
    private static final int MAX_FAILURE = 3;
    private static final List<WorkItem> ACTIONS = new ArrayList<>();

    public SonarQubeCheckPeriodicWork() {
        super("SonarQube Worker Thread");
    }

    @Override
    public long getRecurrencePeriod() {
        return MIN;
    }

    public ApplicationBuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @Override
    protected void execute(@NonNull TaskListener listener) throws IOException, InterruptedException {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            LOGGER.severe("Unable to run MonitoringPeriodicWork: Jenkins instance is null");
            return;
        }
        if (getBuildStatusDescriptor() == null) {
            LOGGER.severe("Unable to run MonitoringPeriodicWork: unable to get ApplicationBuildStatus descriptor");
            return;
        }
        List<WorkItem> actions;
        synchronized (ACTIONS) {
            actions = new ArrayList<>(ACTIONS);
        }
        for (WorkItem item : actions) {
            if (item.activity.isComplete()) {
                // In case of parallel treatments
                continue;
            }
            boolean completed = false;
            try {
                completed = execute(item);
            }
            catch (Exception ex) {
                LOGGER.severe(
                        "Unable to complete SonarQube async task: job='" + item.jobName + "' build='" + item.buildNumber
                        + "' project='" + item.applicationName + ":" + item.applicationVersion + "/" + item.projectKey + "' => "
                        + ex.getClass().getSimpleName() + " : " + ex.getMessage()
                );
                item.failure++;
                if (item.failure >= MAX_FAILURE) {
                    item.activity.setComplete(true);
                    LOGGER.info("Canceled SonarQube async task: job='" + item.jobName + "' build='" + item.buildNumber
                            + "' project='" + item.applicationName + ":" + item.applicationVersion + "/" + item.projectKey + "'");
                    synchronized (ACTIONS) {
                        ACTIONS.remove(item.close());
                    }
                }
            }
            if (completed) {
                item.activity.setComplete(true);
                LOGGER.info("Completed SonarQube async task: job='" + item.jobName + "' build='" + item.buildNumber
                        + "' project='" + item.applicationName + ":" + item.applicationVersion + "/" + item.projectKey + "'");
                synchronized (ACTIONS) {
                    ACTIONS.remove(item.close());
                }
                getBuildStatusDescriptor().getBuildStatusByApplication(
                        item.applicationName,
                        item.applicationVersion
                ).ifPresent(status -> {
                    status.setComponentActivityByCategory(
                            ActivityCategory.QUALITY_AUDIT,
                            item.applicationComponent,
                            item.activity
                    );
                });
            }
        }
    }

    private boolean execute(@NonNull WorkItem item) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return false;
        }
        // METRICS
        item.activity.setMetrics(item.wsClient.getMetrics(item.projectKey));
        // ISSUES
        item.activity.setIssues(item.wsClient.getIssues(item.projectKey));
        // HOTSPOTS
        item.activity.setHotSpots(item.wsClient.getHotspots(item.projectKey));
        return true;
    }

    public static void push(@NonNull String jobName, @NonNull String buildNumber, @NonNull String projectKey,
                            @NonNull QualityAuditActivity activity, @NonNull String sonarUrl,
                            @NonNull String sonarToken, @NonNull String applicationName,
                            @NonNull String applicationVersion, @NonNull String applicationComponent,
                            boolean acceptInvalidCertificate) {

        // Check arguments
        checkNotEmpty(jobName, buildNumber, projectKey, sonarUrl, sonarToken, applicationName,
                applicationVersion, applicationComponent);



        synchronized (ACTIONS) {

            // Remove older actions for same applicationName/applicationVersion/applicationComponent
            List<WorkItem> existing = ACTIONS.stream()
                    .filter(item -> item.applicationName.equals(applicationName))
                    .filter(item -> item.applicationVersion.equals(applicationVersion))
                    .filter(item -> item.applicationComponent.equals(applicationComponent))
                    .collect(Collectors.toList());
            if (!existing.isEmpty()) {
                for (WorkItem item : existing) {
                    item.activity.setComplete(true);
                    ACTIONS.remove(item);
                }
            }

            // Push new action to do
            ACTIONS.add(new WorkItem(
                    jobName, buildNumber, projectKey, activity, sonarUrl,
                    sonarToken, applicationName, applicationVersion, applicationComponent,
                    acceptInvalidCertificate
            ));
            LOGGER.info("New SonarQube async task: job='" + jobName + "' build='" + buildNumber + "' project='"
                    + applicationName + ":" + applicationVersion + "/" + applicationComponent
                    + "' unsafe=" + acceptInvalidCertificate);

        }
    }

    static class WorkItem {

        private final String jobName;
        private final String buildNumber;
        private final String projectKey;
        private final QualityAuditActivity activity;
        private final SonarApiClient wsClient;
        private final String applicationName;
        private final String applicationVersion;
        private final String applicationComponent;
        public int failure = 0;

        public WorkItem(@NonNull String jobName, @NonNull String buildNumber, @NonNull String projectKey,
                        @NonNull QualityAuditActivity activity, @NonNull String sonarUrl, @NonNull String sonarToken,
                        @NonNull String applicationName, @NonNull String applicationVersion,
                        @NonNull String applicationComponent, boolean acceptInvalidCertificate) {
            this.jobName = jobName;
            this.buildNumber = buildNumber;
            this.projectKey = projectKey;
            this.activity = activity;
            this.applicationName = applicationName;
            this.applicationVersion = applicationVersion;
            this.applicationComponent = applicationComponent;
            this.wsClient = new SonarApiClient(sonarUrl, sonarToken, acceptInvalidCertificate);
        }

        public WorkItem close() {
            return this;
        }

    }

}
