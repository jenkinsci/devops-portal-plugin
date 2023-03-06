package io.jenkins.plugins.devopsportal.reporters;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import io.jenkins.plugins.devopsportal.models.*;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for BUILD activity reporters.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public abstract class AbstractActivityReporter<T extends AbstractActivity> extends Builder
        implements SimpleBuildStep, GenericActivityHandler<T> {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

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

    public ApplicationBuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) {

        // Check if identifiers are missing
        if (Strings.isNullOrEmpty(applicationName) || Strings.isNullOrEmpty(applicationVersion) || Strings.isNullOrEmpty(applicationComponent)) {
            // Log
            listener.getLogger().printf(
                    "Unable to report build activity '%s': missing identifier (name='%s' version='%s' component='%s')",
                    getActivityCategory(),
                    applicationName,
                    applicationVersion,
                    applicationComponent
            );
            return;
        }

        // Create or update BuildStatus
        getBuildStatusDescriptor().update(applicationName, applicationVersion, record -> {

            // Generic record data
            GenericRunModel.updateRecordFromRun(record, run, env);

            // Log
            listener.getLogger().printf(
                    "Report build activity '%s' for application '%s' version %s component '%s'%n",
                    getActivityCategory(),
                    record.getApplicationName(),
                    record.getApplicationVersion(),
                    applicationComponent
            );

            // Create or update AbstractActivity
            try {
                final Result result = record.updateActivity(
                        applicationComponent,
                        getActivityCategory(),
                        listener,
                        env,
                        this,
                        workspace
                );
                // In case of failure
                if (result != null && run.getResult() != Result.FAILURE && run.getResult() != Result.ABORTED) {
                    listener.getLogger().printf("Build activity '%s' changed run result to: %s%n", getActivityCategory(), result);
                    run.setResult(result);
                }
            }
            catch (Exception ex) {
                listener.getLogger().printf(
                        "Build activity '%s' changed run result to: %s due to an %s : %s%n",
                        getActivityCategory(),
                        Result.FAILURE,
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                );
                LOGGER.log(Level.INFO, "Failed to execute: " + getClass().getName(), ex);
                run.setResult(Result.FAILURE);
            }

        });
    }

    public abstract ActivityCategory getActivityCategory();

}
