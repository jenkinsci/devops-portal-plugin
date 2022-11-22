package io.jenkins.plugins.devopsportal.workers;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import io.jenkins.plugins.devopsportal.utils.SSLUtils;
import jenkins.model.Jenkins;
import org.sonarqube.ws.Hotspots;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.sonarqube.ws.Common.RuleType.*;

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

    public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
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
                synchronized (ACTIONS) {
                    ACTIONS.remove(item.close());
                }
            }
        }
    }

    private boolean execute(WorkItem item) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return false;
        }
        org.sonarqube.ws.client.measures.SearchRequest request1 = new org.sonarqube.ws.client.measures.SearchRequest();
        // COVERAGE
        request1.setProjectKeys(List.of(item.projectKey));
        request1.setMetricKeys(List.of("coverage"));
        Measures.SearchWsResponse response1 = item.wsClient.measures().search(request1);
        Measures.Measure result1 = response1.getMeasures(0);
        item.activity.setTestCoverage(response1.getMeasuresCount() == 0 ? 0 : Float.parseFloat(response1.getMeasures(0).getValue()));
        // ISSUES
        org.sonarqube.ws.client.issues.SearchRequest request2 = new org.sonarqube.ws.client.issues.SearchRequest();
        request2.setComponentKeys(List.of(item.projectKey));
        request2.setTypes(List.of("BUG", "VULNERABILITY", "CODE_SMELL"));
        request2.setSeverities(List.of("MAJOR", "CRITICAL", "BLOCKER"));
        request2.setResolved("no");
        request2.setPs("500");
        Issues.SearchWsResponse response2 = item.wsClient.issues().search(request2);
        item.activity.setBugCount(0);
        for (Issues.Issue issue : response2.getIssuesList()) {
            if ("java:S1135".equals(issue.getRule())) {
                // Ignore TODOs
                continue;
            }
            if (issue.getType() == BUG) {
                item.activity.addBug(issue);
            }
            else if (issue.getType() == VULNERABILITY || issue.getType() == SECURITY_HOTSPOT) {
                item.activity.addVulnerability(issue);
            }
        }
        // HOTSPOTS
        org.sonarqube.ws.client.hotspots.SearchRequest request3 = new org.sonarqube.ws.client.hotspots.SearchRequest();
        request3.setProjectKey(item.projectKey);
        request3.setStatus("TO_REVIEW");
        Hotspots.SearchWsResponse response3 = item.wsClient.hotspots().search(request3);
        item.activity.setVulnerabilityCount(0);
        for (Hotspots.SearchWsResponse.Hotspot hotspot : response3.getHotspotsList()) {
            item.activity.addHotSpot(hotspot);
        }
        // TODO duplicationRate, linesCount, qualityGatePassed
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
            X509TrustManager manager = SSLUtils.getUntrustedManager();
            SSLContext context = SSLUtils.getSSLContext(manager);
            HttpConnector httpConnector = HttpConnector
                    .newBuilder()
                    .url(sonarUrl)
                    .token(sonarToken)
                    .setSSLSocketFactory(context.getSocketFactory())
                    .setTrustManager(manager)
                    .build();
            this.wsClient = WsClientFactories.getDefault().newClient(httpConnector);
        }

        public WorkItem close() {
            return this;
        }

    }

}
