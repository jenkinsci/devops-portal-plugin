package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.DeploymentOperation;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.GenericRunModel;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.time.Instant;

/**
 * Build step of a project used to record the execution of a DEPLOYMENT operation.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DeploymentOperationReporter extends Builder implements SimpleBuildStep {

    private String targetService;
    private String applicationName;
    private String applicationVersion;
    private String tags;

    @DataBoundConstructor
    public DeploymentOperationReporter(String targetService, String applicationName, String applicationVersion) {
        this.targetService = targetService;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    public String getTargetService() {
        return targetService;
    }

    @DataBoundSetter
    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @DataBoundSetter
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getTags() {
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {

        getServiceOperationDescriptor().append(record -> {

            ServiceConfiguration service = getServiceDescriptor().getService(targetService).orElse(null);
            if (service == null) {
                listener.getLogger().printf(
                        "Unable to report a run operation: environment not declared (%s)",
                        applicationName
                );
                return;
            }

            GenericRunModel.updateRecordFromRun(record, run, env);
            record.setServiceId(service.getId());
            record.setTimestamp(Instant.now().getEpochSecond());
            record.setApplicationName(applicationName);
            record.setApplicationVersion(applicationVersion);
            record.setTags(tags);

            listener.getLogger().printf(
                    "Report run operation 'DEPLOYMENT' on application '%s' to environment '%s' (%s)\n",
                    applicationName,
                    service.getCategory(),
                    service.getHostname()
            );
        });
    }

    public static ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public static DeploymentOperation.DescriptorImpl getServiceOperationDescriptor() {
        return Jenkins.get().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
    }

    public static ApplicationBuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @Symbol("reportDeployOperation")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @SuppressWarnings("unused")
        public FormValidation doCheckTargetService(@QueryParameter String targetService) {
            if (targetService == null || targetService.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            if (!getServiceDescriptor().getService(targetService).isPresent()) {
                return FormValidation.error(Messages.FormValidation_Error_ServiceNotFound());
            }
            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckApplicationName(@QueryParameter String applicationName) {
            if (applicationName == null || applicationName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            if (!getBuildStatusDescriptor().isApplicationExists(applicationName)) {
                return FormValidation.error(Messages.FormValidation_Error_ApplicationNotFound());
            }
            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckApplicationVersion(@QueryParameter String applicationVersion) {
            if (applicationVersion == null || applicationVersion.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.RunOperationReporter_DisplayName();
        }

    }

}
