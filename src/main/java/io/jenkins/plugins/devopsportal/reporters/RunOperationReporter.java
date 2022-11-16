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
import hudson.util.ListBoxModel;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.RunOperations;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import io.jenkins.plugins.devopsportal.models.ServiceOperation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.time.Instant;

/**
 * Build step of a project used to record the execution of an exploitation operation.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RunOperationReporter extends Builder implements SimpleBuildStep {

    private String targetService;
    private String applicationName;
    private String applicationVersion;
    private RunOperations operation;
    private boolean success;
    private String tags;

    @DataBoundConstructor
    public RunOperationReporter(String targetService, String applicationName, String applicationVersion,
                                RunOperations operation, boolean success) {
        this.targetService = targetService;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.operation = operation;
        this.success = success;
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

    public RunOperations getOperation() {
        return operation;
    }

    public void setOperation(RunOperations operation) {
        this.operation = operation;
    }

    @DataBoundSetter
    public void setOperation(String operation) {
        this.operation = (operation == null || operation.isEmpty()) ? null : RunOperations.valueOf(operation);
    }

    public boolean isSuccess() {
        return success;
    }

    @DataBoundSetter
    public void setSuccess(boolean success) {
        this.success = success;
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
                        "Unable to report a run operation: application not declared (%s)",
                        applicationName
                );
                return;
            }

            record.setServiceId(service.getId());
            record.setOperation(operation);
            record.setSuccess(success);
            record.setTimestamp(Instant.now().getEpochSecond());
            record.setApplicationName(applicationName);
            record.setApplicationVersion(applicationVersion);
            record.setBuildJob(env.get("JOB_NAME"));
            record.setBuildNumber(env.get("BUILD_NUMBER"));
            record.setBuildURL(env.get("RUN_DISPLAY_URL")); // TODO Verify this URL
            record.setBuildBranch("");// TODO
            record.setBuildCommit("");//TODO
            record.setTags(tags);
            listener.getLogger().printf(
                    "Report run operation '%s' on application '%s' to environment '%s' (%s) : %s",
                    operation,
                    applicationName,
                    service.getCategory(),
                    service.getHostname(),
                    success
            );
        });
    }

    public static ServiceConfiguration.DescriptorImpl getServiceDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public static ServiceOperation.DescriptorImpl getServiceOperationDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceOperation.DescriptorImpl.class);
    }

    public static BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Symbol("reportRunOperation")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckTargetService(@QueryParameter String targetService) {
            if (targetService.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            if (!getServiceDescriptor().getService(targetService).isPresent()) {
                return FormValidation.error(Messages.FormValidation_Error_ServiceNotFound());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApplicationName(@QueryParameter String applicationName) {
            if (applicationName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            if (!getBuildStatusDescriptor().isApplicationExists(applicationName)) {
                return FormValidation.error(Messages.FormValidation_Error_ApplicationNotFound());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApplicationVersion(@QueryParameter String applicationVersion) {
            if (applicationVersion.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckOperation(@QueryParameter String operation) {
            if (operation.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillOperationItems() {
            ListBoxModel list = new ListBoxModel();
            for (RunOperations activity : RunOperations.values()) {
                list.add(activity.name(), activity.name());
            }
            return list;
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
