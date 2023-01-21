package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.BuildActivity;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import jenkins.MasterToSlaveFileCallable;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;

/**
 * Build step of a project used to record a BUILD activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildActivityReporter extends AbstractActivityReporter<BuildActivity> {

    private String artifactFileName;

    private int artifactFileSizeLimit;

    @DataBoundConstructor
    public BuildActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getArtifactFileName() {
        return artifactFileName;
    }

    @DataBoundSetter
    public void setArtifactFileName(String artifactFileName) {
        this.artifactFileName = artifactFileName;
    }

    public int getArtifactFileSizeLimit() {
        return artifactFileSizeLimit;
    }

    @DataBoundSetter
    public void setArtifactFileSizeLimit(int artifactFileSizeLimit) {
        this.artifactFileSizeLimit = artifactFileSizeLimit;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull BuildActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env, @NonNull FilePath workspace) {
        activity.setArtifactFileName(artifactFileName);
        activity.setArtifactFileSizeLimit(artifactFileSizeLimit);
        long previousSize = activity.getArtifactFileSize();
        activity.setArtifactFileSize(0);
        activity.setArtifactFileSizeDelta(0);
        if (artifactFileName == null) {
            return null;
        }
        // Get file size
        try {
            if (workspace.isRemote()) {
                getFileSizeFromRemoteWorkspace(activity, new FilePath(workspace, artifactFileName), listener);
            } else {
                getFileSizeFromLocalWorkspace(activity, env);
            }
        }
        // Unable to get file size
        catch (Exception ex) {
            listener.getLogger().println("Error, unable to get file size: " + ex.getClass().getSimpleName()
                + " - " + ex.getMessage());
            return Result.FAILURE;
        }
        // File size comparison
        if (activity.getArtifactFileSize() > 0) {
            activity.setArtifactFileSizeDelta(activity.getArtifactFileSize() - previousSize);
            listener.getLogger().println("Current artifact file size: " + activity.getArtifactFileSize());
            listener.getLogger().println("Previous artifact file size: " + previousSize);
            listener.getLogger().println("Delta: " + activity.getArtifactFileSizeDelta());
            // File size limit
            if (artifactFileSizeLimit > 0 && activity.getArtifactFileSize() > artifactFileSizeLimit) {
                listener.getLogger().println("Current artifact file size exceed limit: " + artifactFileSizeLimit);
                return Result.FAILURE;
            }
        }
        else {
            listener.getLogger().println("Error, artifact file not found: " + artifactFileName);
            return Result.FAILURE;
        }
        return null;
    }

    private void getFileSizeFromRemoteWorkspace(BuildActivity activity, FilePath target, @NonNull TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Fetch remote file: " + target);
        long size = target.act(new MasterToSlaveFileCallable<Long>() {
            @Override
            public Long invoke(File file, VirtualChannel channel) {
                listener.getLogger().println("Invoke on slave file 1: " + file);
                File report = new File(target.getRemote());
                listener.getLogger().println("Invoke on slave file 2: " + report);
                return file.length();
            }
        });
        listener.getLogger().println("Fetch remote size: " + size);
        activity.setArtifactFileSize(size);
    }

    private void getFileSizeFromLocalWorkspace(@NotNull BuildActivity activity, @NotNull EnvVars env) {
        final File file = MiscUtils.checkFilePathIllegalAccess(
                env.get("WORKSPACE", null),
                artifactFileName
        );
        if (file != null) {
                activity.setArtifactFileSize(file.length());
        }
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.BUILD;
    }

    @Symbol("reportBuild")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.BuildActivityReporter_DisplayName());
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckArtifactFileName(@QueryParameter String artifactFileName) {
            if (artifactFileName == null || artifactFileName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

    }

}
