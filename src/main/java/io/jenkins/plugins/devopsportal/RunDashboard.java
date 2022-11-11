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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Type of view displaying information on application deployments and the status of monitored services.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RunDashboard extends View {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.DateFormatter_Date());
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(Messages.DateFormatter_DateTime());

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
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return null;
    }

    public String formatDateMs(long timestamp) {
        return dateFormat.format(new java.util.Date(timestamp));
    }

    public String formatDatetimeSec(long timestamp) {
        return datetimeFormat.format(new java.util.Date(timestamp * 1000L));
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

        public List<String> getConfigurationCategories() {
            return getServiceDescriptor().getServiceConfigurations().stream().map(ServiceConfiguration::getCategory)
                    .map(String::trim).distinct().sorted().collect(Collectors.toList());
        }

        public List<ServiceConfiguration> getConfigurationsByCategory(@NonNull String category) {
            return getServiceDescriptor().getServiceConfigurations().stream()
                    .filter(item -> category.trim().equals(item.getCategory().trim()))
                    .sorted(Comparator.comparing(ServiceConfiguration::getLabel)).collect(Collectors.toList());
        }

        public ServiceMonitoring getMonitoringByService(String serviceId) {
            return getMonitoringDescriptor().getMonitoringByService(serviceId).orElse(new ServiceMonitoring(serviceId));
        }

        public ServiceOperation getLastDeploymentByService(String serviceId) {
            return getOperationDescriptor()
                    .getRunOperations()
                    .stream()
                    .filter(item -> serviceId.equals(item.getServiceId()))
                    .filter(item -> item.getOperation() == RunOperations.DEPLOYMENT)
                    .min(Comparator.comparingLong(ServiceOperation::getTimestamp))
                    .orElse(null);
        }
        
    }

}
