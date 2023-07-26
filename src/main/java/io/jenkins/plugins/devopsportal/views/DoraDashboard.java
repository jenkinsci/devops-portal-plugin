package io.jenkins.plugins.devopsportal.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.DeploymentOperation;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Type of view displaying DORA metrics for an application and a set of environments.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DoraDashboard extends View {

    private String filterApplication = "";
    private String filterEnvironments = "";
    private String filterDeploymentTags = "";

    @DataBoundConstructor
    public DoraDashboard(String name) {
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
        this.filterApplication = req.getParameter("filterApplication");
        this.filterEnvironments = req.getParameter("filterEnvironments");
        this.filterDeploymentTags = req.getParameter("filterDeploymentTags");
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return null;
    }

    public String getFilterApplication() {
        return filterApplication;
    }

    @DataBoundSetter
    public void setFilterApplication(String filterApplication) {
        this.filterApplication = filterApplication;
    }

    public String getFilterEnvironments() {
        return filterEnvironments;
    }

    @DataBoundSetter
    public void setFilterEnvironments(String filterEnvironments) {
        this.filterEnvironments = filterEnvironments;
    }

    public String getFilterDeploymentTags() {
        return filterDeploymentTags;
    }

    @DataBoundSetter
    public void setFilterDeploymentTags(String filterDeploymentTags) {
        this.filterDeploymentTags = filterDeploymentTags;
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        public DescriptorImpl() {
            super(DoraDashboard.class);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.DoraDashboard_DisplayName();
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

        public String getApplication(String filterApplication) {
            return null;
        }

        public List<String> getEnvironments(String filterEnvironments, String filterDeploymentTags) {
            return null;
        }

        public FormValidation doCheckFilterApplication(@QueryParameter String filter) {
            return FormValidation.ok();
        }

        public FormValidation doCheckFilterEnvironments(@QueryParameter String filter) {
            return FormValidation.ok();
        }

        public FormValidation doCheckFilterDeploymentTags(@QueryParameter String filter) {
            return FormValidation.ok();
        }

    }

}
