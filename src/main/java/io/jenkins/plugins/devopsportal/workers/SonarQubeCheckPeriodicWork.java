package io.jenkins.plugins.devopsportal.workers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ActivityScore;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.jenkins.plugins.devopsportal.utils.MiscUtils.checkNotEmpty;
import static org.sonarqube.ws.Common.RuleType.*;

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
        handleMetrics(item);
        // ISSUES
        handleIssues(item);
        // HOTSPOTS
        handleHotspots(item);
        return true;
    }

    private static void handleHotspots(@NonNull WorkItem item) {
        org.sonarqube.ws.client.hotspots.SearchRequest request = new org.sonarqube.ws.client.hotspots.SearchRequest();
        request.setProjectKey(item.projectKey);
        request.setStatus("TO_REVIEW");
        Hotspots.SearchWsResponse response = item.wsClient.hotspots().search(request);
        item.activity.setHotspotCount(0);
        for (Hotspots.SearchWsResponse.Hotspot hotspot : response.getHotspotsList()) {
            item.activity.addHotSpot(hotspot);
        }
    }

    private static void handleIssues(@NonNull WorkItem item) {
        org.sonarqube.ws.client.issues.SearchRequest request = new org.sonarqube.ws.client.issues.SearchRequest();
        request.setComponentKeys(Collections.singletonList(item.projectKey));
        request.setTypes(Arrays.asList("BUG", "VULNERABILITY", "CODE_SMELL"));
        request.setSeverities(Arrays.asList("MAJOR", "CRITICAL", "BLOCKER"));
        request.setStatuses(Collections.singletonList("OPEN"));
        request.setResolved("no");
        request.setPs("500");
        Issues.SearchWsResponse response = item.wsClient.issues().search(request);
        item.activity.setBugCount(0);
        item.activity.setVulnerabilityCount(0);
        for (Issues.Issue issue : response.getIssuesList()) {
            if ("java:S1135".equals(issue.getRule())) {
                // Ignore TODOs
                continue;
            }
            if (issue.getType() == BUG) {
                item.activity.addBug(issue);
            }
            else if (issue.getType() == VULNERABILITY) {
                item.activity.addVulnerability(issue);
            }
        }
    }

    private static void handleMetrics(@NonNull WorkItem item) {
        org.sonarqube.ws.client.measures.SearchRequest request = new org.sonarqube.ws.client.measures.SearchRequest();
        request.setProjectKeys(Collections.singletonList(item.projectKey));
        request.setMetricKeys(Arrays.asList(
                // Quality Gate
                "alert_status",
                "quality_gate_details",
                // Scores
                "sqale_rating", // Maintainability (code smells)
                "reliability_rating", // Reliability (bugs)
                "security_rating", // Security (vulnerabilities)
                "security_review_rating", // Security review (hotspots)
                // Metrics
                "coverage", // Test coverage
                "duplicated_lines_density", // Duplication
                "ncloc" // Lines of code
        ));
        Measures.SearchWsResponse response = item.wsClient.measures().search(request);
        item.activity.setQualityGatePassed(!"ERROR".equals(getMeasure(response, "alert_status", String.class)));
        item.activity.setBugScore(getMeasure(response, "reliability_rating", ActivityScore.class));
        item.activity.setVulnerabilityScore(getMeasure(response, "security_rating", ActivityScore.class));
        item.activity.setHotspotScore(getMeasure(response, "security_review_rating", ActivityScore.class));
        item.activity.setTestCoverage(getMeasure(response, "coverage", Float.class) / 100f); //NOSONAR
        item.activity.setDuplicationRate(getMeasure(response, "duplicated_lines_density", Float.class) / 100f); //NOSONAR
        item.activity.setLinesCount(getMeasure(response, "ncloc", Integer.class)); //NOSONAR
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMeasure(Measures.SearchWsResponse response, String name, Class<T> type) {
        for (int i = 0, l = response.getMeasuresCount(); i < l; i++) {
            Measures.Measure measure = response.getMeasures(i);
            if (!name.equals(measure.getMetric().toLowerCase())) {
                continue;
            }
            if (type == String.class) {
                return (T) measure.getValue();
            }
            if (type == Integer.class) {
                return (T) Integer.valueOf(Integer.parseInt(measure.getValue()));
            }
            if (type == Float.class) {
                return (T) Float.valueOf(Float.parseFloat(measure.getValue()));
            }
            if (type == Boolean.class) {
                return (T) Boolean.valueOf(measure.getValue());
            }
            if (type == ActivityScore.class) {
                return (T) ActivityScore.parseString(measure.getValue());
            }
            throw new IllegalArgumentException("Unable to convert measure '" + name + "' into " + type.getSimpleName());
        }
        LOGGER.warning("Unable to get measure '" + name + "' from response");
        if (type == Integer.class) {
            return (T) Integer.valueOf(-1);
        }
        if (type == Float.class) {
            return (T) Float.valueOf(-1f);
        }
        if (type == Boolean.class) {
            return (T) Boolean.valueOf(false);
        }
        return (T) null;
    }

    public static void push(@NonNull String jobName, @NonNull String buildNumber, @NonNull String projectKey,
                            @NonNull QualityAuditActivity activity, @NonNull String sonarUrl,
                            @NonNull String sonarToken, @NonNull String applicationName,
                            @NonNull String applicationVersion, @NonNull String applicationComponent) {

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
                    sonarToken, applicationName, applicationVersion, applicationComponent
            ));
            LOGGER.info("New SonarQube async task: job='" + jobName + "' build='" + buildNumber + "' project='"
                    + applicationName + ":" + applicationVersion + "/" + applicationComponent + "'");

        }
    }

    static class WorkItem {

        private final String jobName;
        private final String buildNumber;
        private final String projectKey;
        private final QualityAuditActivity activity;
        private final WsClient wsClient;
        private final String applicationName;
        private final String applicationVersion;
        private final String applicationComponent;
        public int failure = 0;

        public WorkItem(@NonNull String jobName, @NonNull String buildNumber, @NonNull String projectKey,
                        @NonNull QualityAuditActivity activity, @NonNull String sonarUrl, @NonNull String sonarToken,
                        @NonNull String applicationName, @NonNull String applicationVersion,
                        @NonNull String applicationComponent) {
            this.jobName = jobName;
            this.buildNumber = buildNumber;
            this.projectKey = projectKey;
            this.activity = activity;
            this.applicationName = applicationName;
            this.applicationVersion = applicationVersion;
            this.applicationComponent = applicationComponent;
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
