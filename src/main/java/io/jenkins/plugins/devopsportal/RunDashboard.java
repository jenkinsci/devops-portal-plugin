package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RunDashboard extends View {

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

    @Symbol("rundashboard")
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

        public List<String> getConfigurationCategories() {
            return getServiceDescriptor().getServiceConfigurations().stream().map(ServiceConfiguration::getCategory)
                    .map(String::trim).distinct().sorted().collect(Collectors.toList());
        }

        public List<ServiceConfiguration> getConfigurationsByCategory(@NonNull String category) {
            return getServiceDescriptor().getServiceConfigurations().stream()
                    .filter(item -> category.trim().equals(item.getCategory().trim()))
                    .sorted(Comparator.comparing(ServiceConfiguration::getLabel)).collect(Collectors.toList());
        }

        public ServiceMonitoring getMonitoringByService(String id) {
            return getMonitoringDescriptor().getMonitoringByService(id).orElse(new ServiceMonitoring(id));
        }
        
    }

}
