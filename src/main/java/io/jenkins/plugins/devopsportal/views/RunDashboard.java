package io.jenkins.plugins.devopsportal.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import io.jenkins.plugins.devopsportal.models.ServiceMonitoring;
import io.jenkins.plugins.devopsportal.models.ServiceOperation;
import io.jenkins.plugins.devopsportal.utils.TimeAgoUtils;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Type of view displaying information on application deployments and the status of monitored services.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RunDashboard extends View {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(io.jenkins.plugins.devopsportal.Messages.DateFormatter_Date());
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(io.jenkins.plugins.devopsportal.Messages.DateFormatter_DateTime());

    private String filter = "";

    @DataBoundConstructor
    public RunDashboard(String name) {
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

    public String formatDateMs(long timestamp) {
        return dateFormat.format(new java.util.Date(timestamp));
    }
    public String formatDatetimeSeconds(long timestamp) {
        return datetimeFormat.format(new java.util.Date(timestamp * 1000L));
    }

    public String formatUptime(long timestamp) {
        if (timestamp <= 0) {
            return io.jenkins.plugins.devopsportal.Messages.TimeAgo_Never();
        }
        return TimeAgoUtils.toDuration((Instant.now().getEpochSecond() - timestamp) * 1000L);
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        public DescriptorImpl() {
            super(RunDashboard.class);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.RunDashboard_DisplayName();
        }

        public ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
            return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
        }

        public ServiceMonitoring.DescriptorImpl getMonitoringDescriptor() {
            return Jenkins.get().getDescriptorByType(ServiceMonitoring.DescriptorImpl.class);
        }

        public ServiceOperation.DescriptorImpl getOperationDescriptor() {
            return Jenkins.get().getDescriptorByType(ServiceOperation.DescriptorImpl.class);
        }

        public List<String> getConfigurationCategories(String filter) {
            Stream<String> stream = getServiceDescriptor()
                    .getServiceConfigurations()
                    .stream().map(ServiceConfiguration::getCategory)
                    .map(String::trim)
                    .filter(category -> !category.isEmpty())
                    .distinct()
                    .sorted();
            if (filter != null && !filter.isEmpty()) {
                try {
                    Pattern pattern = Pattern.compile(filter);
                    stream = stream.filter(category -> pattern.matcher(category).matches());
                }
                catch (PatternSyntaxException ignored) { }
            }
            return stream.collect(Collectors.toList());
        }

        public List<ServiceConfiguration> getConfigurationsByCategory(@NonNull String category) {
            return getServiceDescriptor()
                    .getServiceConfigurations()
                    .stream()
                    .filter(item -> category.trim().equals(item.getCategory().trim()))
                    .sorted(Comparator.comparing(ServiceConfiguration::getLabel))
                    .collect(Collectors.toList());
        }

        public ServiceMonitoring getMonitoringByService(String serviceId) {
            return getMonitoringDescriptor().getMonitoringByService(serviceId).orElse(new ServiceMonitoring(serviceId));
        }

        public ServiceOperation getLastDeploymentByService(String serviceId) {
            return getOperationDescriptor().getLastDeploymentByService(serviceId).orElse(null);
        }

        public List<ServiceConfiguration> getServicesConfiguration(String filter) {
            Stream<ServiceConfiguration> stream = getServiceDescriptor()
                    .getServiceConfigurations()
                    .stream()
                    .distinct()
                    .sorted(Comparator.comparing(ServiceConfiguration::getLabel));
            if (filter != null && !filter.isEmpty()) {
                try {
                    Pattern pattern = Pattern.compile(filter);
                    stream = stream.filter(service -> pattern.matcher(service.getCategory()).matches());
                }
                catch (PatternSyntaxException ignored) { }
            }
            return stream.collect(Collectors.toList());
        }

        public List<ServiceOperation> getDeploymentsByService(String serviceId) {
            return getOperationDescriptor().getDeploymentsByService(serviceId);
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
