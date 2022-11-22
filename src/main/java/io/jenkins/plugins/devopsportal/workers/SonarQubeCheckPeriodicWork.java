package io.jenkins.plugins.devopsportal.workers;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import io.jenkins.plugins.devopsportal.models.ServiceMonitoring;
import jenkins.model.Jenkins;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.measures.SearchRequest;

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
    private static final List<WorkItem> ACTIONS = new ArrayList<>();

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
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return;
        }
        List<WorkItem> actions;
        synchronized (ACTIONS) {
            actions = new ArrayList<>(ACTIONS);
        }
        for (WorkItem item : actions) {
            boolean completed = execute(item);
            if (completed) {
                LOGGER.info("Completed SonarQube async task: job='" + item.jobName + "' build='" + item.buildNumber
                        + "' project='" + item.projectKey + "'");
                ACTIONS.remove(item.close());
            }
        }
    }

    private boolean execute(WorkItem item) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return false;
        }
        SearchRequest request = new SearchRequest();
        request.setProjectKeys(List.of(item.projectKey));
        request.setMetricKeys(List.of("coverage"));
        Measures.SearchWsResponse response = item.wsClient.measures().search(request);
        Measures.Measure result = response.getMeasures(0);
        return false;
    }

    public static void push(String jobName, String buildNumber, String projectKey, QualityAuditActivity activity,
                            String sonarUrl, String sonarToken) {
        // TODO Check arguments
        synchronized (ACTIONS) {
            ACTIONS.add(new WorkItem(jobName, buildNumber, projectKey, activity, sonarUrl, sonarToken));
            LOGGER.info("New SonarQube async task: job='" + jobName + "' build='" + buildNumber + "' project='"
                    + projectKey + "'");
        }
    }

    static class WorkItem {

        private final String jobName;
        private final String buildNumber;
        private final String projectKey;
        private final QualityAuditActivity activity;
        private final String sonarUrl;
        private final WsClient wsClient;

        public WorkItem(String jobName, String buildNumber, String projectKey, QualityAuditActivity activity,
                        String sonarUrl, String sonarToken) {
            this.jobName = jobName;
            this.buildNumber = buildNumber;
            this.projectKey = projectKey;
            this.activity = activity;
            this.sonarUrl = sonarUrl;
            HttpConnector httpConnector = HttpConnector
                    .newBuilder()
                    .url(sonarUrl)
                    //.credentials("?", "?")
                    .token(sonarToken)
                    .build();
            this.wsClient = WsClientFactories.getDefault().newClient(httpConnector);
        }

        public WorkItem close() {
            return this;
        }

    }

}
