package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Type of view displaying information on the progress of the various versions of the software developed.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildDashboard extends View {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.DateFormatter_Date());
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(Messages.DateFormatter_DateTime());

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
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return null;
    }

    public String formatDatetimeSec(long timestamp) {
        return datetimeFormat.format(new java.util.Date(timestamp * 1000L));
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

        public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
            return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
        }

        public ServiceOperation.DescriptorImpl getServiceOperationDescriptor() {
            return Jenkins.get().getDescriptorByType(ServiceOperation.DescriptorImpl.class);
        }

        public List<String> getApplicationNames() {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .map(BuildStatus::getApplicationName)
                    .map(String::trim)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        public List<String> getApplicationVersions(String applicationName) {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .sorted(Comparator.comparingLong(BuildStatus::getBuildTimestamp))
                    .map(BuildStatus::getApplicationVersion)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }

        public BuildStatus getApplicationBuild(String applicationName, String applicationVersion) {
            return getBuildStatusDescriptor()
                    .getBuildStatus()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .filter(item -> applicationVersion.trim().equals(item.getApplicationVersion()))
                    .findFirst()
                    .orElse(null);
        }

        public ServiceOperation getLastDeploymentByApplication(String applicationName, String applicationVersion) {
            return getServiceOperationDescriptor()
                    .getLastDeploymentByApplication(applicationName, applicationVersion).orElse(null);
        }

        public ServiceConfiguration getDeploymentService(ServiceOperation operation) {
            if (operation != null) {
                return getServiceConfigurationDescriptor().getService(operation.getServiceId()).orElse(null);
            }
            return null;
        }

    }

}
