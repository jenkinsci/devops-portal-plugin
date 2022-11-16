package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import io.jenkins.plugins.devopsportal.models.AbstractActivity;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class AbstractActivityReporter<T extends AbstractActivity> extends Builder implements SimpleBuildStep {

    private String applicationName;
    private String applicationVersion;
    private String applicationComponent;

    public AbstractActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.applicationComponent = applicationComponent;
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

    public String getApplicationComponent() {
        return applicationComponent;
    }

    @DataBoundSetter
    public void setApplicationComponent(String applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) {

        // Create or update BuildStatus
        getBuildStatusDescriptor().update(applicationName, applicationVersion, record -> {

            // Generic record data
            AbstractActivity.updateRecordFromRun(record, run, env);

            // Create or update
            record.updateActivity(applicationComponent, getActivityCategory(), this::updateActivity);

            listener.getLogger().printf(
                    "Report build activity '%s' for application '%s' version %s%n",
                    ActivityCategory.BUILD,
                    record.getApplicationName(),
                    record.getApplicationVersion()
            );

        });
    }

    public abstract void updateActivity(T activity);

    public abstract ActivityCategory getActivityCategory();

}
