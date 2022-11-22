package io.jenkins.plugins.devopsportal.workers;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import io.jenkins.plugins.devopsportal.models.ServiceMonitoring;
import jenkins.model.Jenkins;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Scheduled task that monitor a SonarQube server.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@Extension
public class SonarQubeCheckPeriodicWork extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");
    private static final List<WorkItem> STACK = new ArrayList<>();

    public SonarQubeCheckPeriodicWork() {
        super("SonarQube Worker Thread");
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
        synchronized (STACK) {
            for (WorkItem item : new ArrayList<>(STACK)) {
                LOGGER.info("Completed SonarQube async task: job='" + item.jobName + "' build='" + item.buildNumber
                        + "' project='" + item.projectKey + "'");
                STACK.remove(item);
            }
        }
    }

    public static void push(String jobName, String buildNumber, String projectKey, QualityAuditActivity activity) {
        // TODO Check arguments
        synchronized (STACK) {
            STACK.add(new WorkItem(jobName, buildNumber, projectKey, activity));
            LOGGER.info("New SonarQube async task: job='" + jobName + "' build='" + buildNumber + "' project='" + projectKey + "'");
        }
    }

    static class WorkItem {

        private final String jobName;
        private final String buildNumber;
        private final String projectKey;
        private final QualityAuditActivity activity;

        public WorkItem(String jobName, String buildNumber, String projectKey, QualityAuditActivity activity) {
            this.jobName = jobName;
            this.buildNumber = buildNumber;
            this.projectKey = projectKey;
            this.activity = activity;
        }

    }

}
