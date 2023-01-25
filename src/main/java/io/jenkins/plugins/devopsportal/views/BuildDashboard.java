package io.jenkins.plugins.devopsportal.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.*;
import io.jenkins.plugins.devopsportal.utils.SummaryTitle;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Type of view displaying information on the progress of the various versions of the software developed.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildDashboard extends View {

    private transient SimpleDateFormat datetimeFormat;

    private String filter = "";

    @DataBoundConstructor
    public BuildDashboard(String name) {
        super(name);
    }

    @NonNull
    @Override
    public Collection<TopLevelItem> getItems() {
        return Collections.emptyList();
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return getItems().contains(item);
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        this.filter = req.getParameter("filter");
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return null;
    }

    public String getFilter() {
        return filter;
    }

    @DataBoundSetter
    public void setFilter(String filter) {
        this.filter = filter;
    }

    public synchronized String formatDatetimeSeconds(Long timestamp) {
        if (timestamp == null) {
            return "?";
        }
        if (datetimeFormat == null) {
            datetimeFormat = new SimpleDateFormat(io.jenkins.plugins.devopsportal.Messages.DateFormatter_DateTime());
        }
        return datetimeFormat.format(new java.util.Date(timestamp * 1000L));
    }

    public String getRootURL() {
        return Objects.requireNonNull(Jenkins.getInstanceOrNull()).getRootUrl();
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        public DescriptorImpl() {
            super(BuildDashboard.class);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildDashboard_DisplayName();
        }

        public ServiceConfiguration.DescriptorImpl getServiceConfigurationDescriptor() {
            return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
        }

        public ApplicationBuildStatus.DescriptorImpl getBuildStatusDescriptor() {
            return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
        }

        public DeploymentOperation.DescriptorImpl getServiceOperationDescriptor() {
            return Jenkins.get().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
        }

        public List<String> getApplicationNames(String filter) {
            Stream<String> stream = getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .map(ApplicationBuildStatus::getApplicationName)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .distinct()
                    .sorted();
            if (filter != null && !filter.isEmpty()) {
                try {
                    Pattern pattern = Pattern.compile(filter);
                    stream = stream.filter(name -> pattern.matcher(name).matches());
                }
                catch (PatternSyntaxException ignored) {
                    return Collections.emptyList();
                }
            }
            return stream.collect(Collectors.toList());
        }

        public List<String> getApplicationVersions(String applicationName) {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .sorted(Comparator.comparingLong(ApplicationBuildStatus::getBuildTimestamp))
                    .map(ApplicationBuildStatus::getApplicationVersion)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }

        public ApplicationBuildStatus getApplicationBuild(String applicationName, String applicationVersion) {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .filter(item -> applicationVersion.trim().equals(item.getApplicationVersion()))
                    .findFirst()
                    .orElse(null);
        }

        public List<AbstractActivity> getBuildActivities(ApplicationBuildStatus build, String category) {
            return build.getActivitiesByCategory(ActivityCategory.valueOf(category));
        }

        public DeploymentOperation getLastDeploymentByApplication(String applicationName, String applicationVersion) {
            return getServiceOperationDescriptor()
                    .getLastDeploymentByApplication(applicationName, applicationVersion).orElse(null);
        }

        public ServiceConfiguration getDeploymentTarget(DeploymentOperation operation) {
            if (operation != null) {
                return getServiceConfigurationDescriptor().getService(operation.getServiceId()).orElse(null);
            }
            return null;
        }

        public SummaryTitle getSummaryBuild(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return new SummaryTitle("warn", "help-circle-outline", Messages.BuildDashboard_SummaryStatus_Unknown());
            }
            List<AbstractActivity> builds = status.getActivitiesByCategory(ActivityCategory.BUILD);
            if (builds.isEmpty()) {
                return new SummaryTitle("warn", "help-circle-outline", Messages.BuildDashboard_SummaryStatus_NotBuilt());
            }
            long success = builds.stream().filter(activity -> activity.getScore() == ActivityScore.A).count();
            if (success < builds.size()) {
                return new SummaryTitle("bad", "skull-outline", Messages.BuildDashboard_SummaryStatus_Failure());
            }
            return new SummaryTitle("good", "heart-outline", Messages.BuildDashboard_SummaryStatus_Healthy());
        }

        public String getSummaryArtifactsCount(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return "-";
            }
            List<AbstractActivity> builds = status.getActivitiesByCategory(ActivityCategory.BUILD);
            long success = builds.stream().filter(activity -> activity.getScore() == ActivityScore.A).count();
            return success + "/" + builds.size();
        }

        public String getSummaryCoverageRate(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return "-";
            }
            Stream<Float> value1 = status
                    .getActivitiesByCategory(ActivityCategory.QUALITY_AUDIT)
                    .stream()
                    .map(activity -> (QualityAuditActivity) activity)
                    .map(QualityAuditActivity::getTestCoverage)
                    .filter(value -> value > 0);
            Stream<Float> value2 = status
                    .getActivitiesByCategory(ActivityCategory.UNIT_TEST)
                    .stream()
                    .map(activity -> (UnitTestActivity) activity)
                    .map(UnitTestActivity::getTestCoverage)
                    .filter(value -> value > 0);
            double average = Stream.concat(value1, value2)
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0);
            return String.format("%.0f", average * 100) + "%";
        }

        public SummaryTitle getSummaryQuality(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return new SummaryTitle("warn", "help-circle-outline",  Messages.BuildDashboard_SummaryStatus_Unknown());
            }
            boolean incomplete = status
                    .getActivitiesByCategory(ActivityCategory.QUALITY_AUDIT)
                    .stream()
                    .map(activity -> (QualityAuditActivity) activity)
                    .anyMatch(activity -> !activity.isComplete());
            if (incomplete) {
                return new SummaryTitle("pending", "sync-circle-outline", Messages.BuildDashboard_SummaryStatus_Updating());
            }
            if ("CRITICAL".equals(getSummaryWorstScore(applicationName, applicationVersion))) {
                return new SummaryTitle("bad", "skull-outline", Messages.BuildDashboard_SummaryStatus_DependencyFailure());
            }
            List<String> scores = Arrays.asList(
                    getLowerQualityScore(status, QualityAuditActivity::getBugScore),
                    getLowerQualityScore(status, QualityAuditActivity::getVulnerabilityScore),
                    getLowerQualityScore(status, QualityAuditActivity::getHotspotScore)
            );
            for (String score : Arrays.asList("E", "D", "C")) {
                if (scores.contains(score)) {
                    return new SummaryTitle("bad", "skull-outline", Messages.BuildDashboard_SummaryStatus_QualityFailure());
                }
            }
            return new SummaryTitle("good", "heart-outline", Messages.BuildDashboard_SummaryStatus_Healthy());
        }

        private static String getLowerQualityScore(ApplicationBuildStatus status,
                                                   java.util.function.Function<QualityAuditActivity, ActivityScore> extractor) {
            if (status == null) {
                return "-";
            }
            return status
                    .getActivitiesByCategory(ActivityCategory.QUALITY_AUDIT)
                    .stream()
                    .map(activity -> (QualityAuditActivity) activity)
                    .map(extractor)
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .max(String::compareTo)
                    .orElse("?");
        }

        public String getSummaryBugScore(String applicationName, String applicationVersion) {
            return getLowerQualityScore(
                    getApplicationBuild(applicationName, applicationVersion),
                    QualityAuditActivity::getBugScore
            );
        }

        public String getSummaryVulnerabilityScore(String applicationName, String applicationVersion) {
            return getLowerQualityScore(
                    getApplicationBuild(applicationName, applicationVersion),
                    QualityAuditActivity::getVulnerabilityScore
            );
        }

        public String getSummaryHotspotScore(String applicationName, String applicationVersion) {
            return getLowerQualityScore(
                    getApplicationBuild(applicationName, applicationVersion),
                    QualityAuditActivity::getHotspotScore
            );
        }

        public String getSummaryDependencyVulnerabilityCount(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return "-";
            }
            return "" + status
                    .getActivitiesByCategory(ActivityCategory.DEPENDENCIES_ANALYSIS)
                    .stream()
                    .map(activity -> (DependenciesAnalysisActivity) activity)
                    .mapToInt(DependenciesAnalysisActivity::getVulnerabilitiesCount)
                    .sum();
        }

        public String getSummaryWorstScore(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return "-";
            }
            List<String> scores = status
                    .getActivitiesByCategory(ActivityCategory.DEPENDENCIES_ANALYSIS)
                    .stream()
                    .map(activity -> (DependenciesAnalysisActivity) activity)
                    .flatMap(activity -> activity.getVulnerabilities().getItems().values().stream())
                    .flatMap(Collection::stream)
                    .map(DependencyVulnerability::getSeverity)
                    .collect(Collectors.toList());
            for (String severity : Arrays.asList("CRITICAL", "HIGHEST", "HIGH", "MEDIUM", "LOW", "LOWEST")) {
                if (scores.contains(severity)) {
                    return severity;
                }
            }
            return "-";
        }

        public SummaryTitle getSummaryRelease(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return new SummaryTitle("warn", "help-circle-outline", Messages.BuildDashboard_SummaryStatus_Unknown());
            }
            if (status.getActivitiesByCategory(ActivityCategory.ARTIFACT_RELEASE).isEmpty()) {
                return new SummaryTitle("bad", "skull-outline", Messages.BuildDashboard_SummaryStatus_Missing());
            }
            return new SummaryTitle("good", "heart-outline", Messages.BuildDashboard_SummaryStatus_Healthy());
        }

        public String getSummaryReleasesCount(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return "-";
            }
            return "" + status.getActivitiesByCategory(ActivityCategory.ARTIFACT_RELEASE).size();
        }

        public List<String> getSummaryReleasesTags(String applicationName, String applicationVersion) {
            ApplicationBuildStatus status = getApplicationBuild(applicationName, applicationVersion);
            if (status == null) {
                return Collections.emptyList();
            }
            return status
                    .getActivitiesByCategory(ActivityCategory.ARTIFACT_RELEASE)
                    .stream()
                    .map(activity -> (ArtifactReleaseActivity) activity)
                    .flatMap(activity -> activity.getTags().stream())
                    .distinct()
                    .collect(Collectors.toList());
        }

        public FormValidation doCheckFilter(@QueryParameter String filter) {
            if (filter != null && !filter.isEmpty()) {
                try {
                    Pattern.compile(filter);
                }
                catch (PatternSyntaxException pse) {
                    return FormValidation.error(pse.getMessage());
                }
            }
            return FormValidation.ok();
        }

    }

}
