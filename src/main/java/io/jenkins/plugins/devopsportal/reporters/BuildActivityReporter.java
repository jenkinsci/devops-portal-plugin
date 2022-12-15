package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.BuildActivity;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;

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
                                 @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setArtifactFileName(artifactFileName);
        activity.setArtifactFileSizeLimit(artifactFileSizeLimit);
        long previousSize = activity.getArtifactFileSize();
        activity.setArtifactFileSize(0);
        activity.setArtifactFileSizeDelta(0);
        if (artifactFileName != null) {
            final File file = MiscUtils.checkFilePathIllegalAccess(
                    env.get("WORKSPACE", null),
                    artifactFileName
            );
            if (file != null) {
                try {
                    activity.setArtifactFileSize(file.length());
                    activity.setArtifactFileSizeDelta(activity.getArtifactFileSize() - previousSize);
                    listener.getLogger().println("Current artifact file size: " + activity.getArtifactFileSize());
                    listener.getLogger().println("Previous artifact file size: " + previousSize);
                    listener.getLogger().println("Delta: " + activity.getArtifactFileSizeDelta());
                }
                catch (Exception ignored) { }
            }
            else {
                listener.getLogger().println("Warning, artifact file not found: " + artifactFileName);
            }
        }
        final boolean failure = artifactFileSizeLimit > 0 && activity.getArtifactFileSize() > 0
                && activity.getArtifactFileSize() > artifactFileSizeLimit;
        if (failure) {
            listener.getLogger().println("Current artifact file size exceed limit: " + artifactFileSizeLimit);
        }
        return failure ? Result.FAILURE : null;
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
