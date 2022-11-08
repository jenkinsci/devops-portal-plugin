package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a change of state of a development activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildActivityReporter extends Builder implements SimpleBuildStep {

    private String applicationName;
    private String applicationVersion;
    private BuildActivities activity;
    private BuildActivityStatus status;

    @DataBoundConstructor
    public BuildActivityReporter(String applicationName, String applicationVersion, BuildActivities activity) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.activity = activity;
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

    public BuildActivities getActivity() {
        return activity;
    }

    @DataBoundSetter
    public void setActivity(BuildActivities activity) {
        this.activity = activity;
    }

    public BuildActivityStatus getStatus() {
        return status;
    }

    public void setStatus(BuildActivityStatus status) {
        this.status = status;
    }

    @DataBoundSetter
    public void setStatus(String status) {
        this.status = (status == null || status.isEmpty()) ? null : BuildActivityStatus.valueOf(status);
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, TaskListener listener) throws InterruptedException, IOException {


        getBuildStatusDescriptor().update(applicationName, applicationVersion, item -> {
            item.setBuildJob(env.get("JOB_NAME"));
            item.setBuildNumber(env.get("BUILD_NUMBER"));
            item.setBuildURL(env.get("RUN_DISPLAY_URL"));
            item.setBuildBranch("?");// TODO
            item.setBuildCommit("?");//TODO
            if (status != null) {
                // Manually
                item.setActivityStatus(activity, status);
            }
            else {
                // Automatic
                item.setActivityStatus(activity, getActivityStatus(activity, run, env));
            }
            listener.getLogger().printf(
                    "Update build activity '%s' to '%s' for application '%s' version %s%n",
                    activity,
                    item.getActivityStatus(activity),
                    item.getApplicationName(),
                    item.getApplicationVersion()
            );
        });
    }

    private BuildActivityStatus getActivityStatus(BuildActivities activity, Run<?,?> run, EnvVars env) {
        return BuildActivityStatus.PENDING; // TODO
    }

    public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Symbol("reportBuildActivity")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckApplicationName(@QueryParameter String applicationName) {
            if (applicationName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApplicationVersion(@QueryParameter String applicationVersion) {
            if (applicationVersion.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillActivityItems() {
            ListBoxModel list = new ListBoxModel();
            for (BuildActivities activity : BuildActivities.values()) {
                list.add(activity.getLabel(), activity.name());
            }
            return list;
        }

        public ListBoxModel doFillStatusItems() {
            ListBoxModel list = new ListBoxModel();
            list.add("", "");
            for (BuildActivityStatus status : BuildActivityStatus.values()) {
                list.add(status.getLabel(), status.name());
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
            return Messages.BuildPublisher_DisplayName();
        }

    }

}
