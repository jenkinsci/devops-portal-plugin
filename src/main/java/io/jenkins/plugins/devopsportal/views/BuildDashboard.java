package io.jenkins.plugins.devopsportal.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.*;
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

    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat(io.jenkins.plugins.devopsportal.Messages.DateFormatter_DateTime());

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

    public String formatDatetimeSeconds(Long timestamp) {
        if (timestamp == null) {
            return "?";
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

        @SuppressWarnings("unused")
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
                catch (PatternSyntaxException ignored) { }
            }
            return stream.collect(Collectors.toList());
        }

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        public ApplicationBuildStatus getApplicationBuild(String applicationName, String applicationVersion) {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .filter(item -> applicationVersion.trim().equals(item.getApplicationVersion()))
                    .findFirst()
                    .orElse(null);
        }

        @SuppressWarnings("unused")
        public List<AbstractActivity> getBuildActivities(ApplicationBuildStatus build, String category) {
            return build.getActivitiesByCategory(ActivityCategory.valueOf(category));
        }

        public DeploymentOperation getLastDeploymentByApplication(String applicationName, String applicationVersion) {
            return getServiceOperationDescriptor()
                    .getLastDeploymentByApplication(applicationName, applicationVersion).orElse(null);
        }

        @SuppressWarnings("unused")
        public ServiceConfiguration getDeploymentTarget(DeploymentOperation operation) {
            if (operation != null) {
                return getServiceConfigurationDescriptor().getService(operation.getServiceId()).orElse(null);
            }
            return null;
        }

        @SuppressWarnings("unused")
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
