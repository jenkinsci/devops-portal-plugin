package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.BuildActivity;
import io.jenkins.plugins.devopsportal.models.BuildStatus;
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

    @Override
    public void updateActivity(@NonNull BuildStatus status, @NonNull BuildActivity activity,
                               @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setArtifactFileName(artifactFileName);
        if (artifactFileName != null) {
            // TODO According to workspace
            File file = new File(artifactFileName);
            if (file.exists()) {
                try {
                    activity.setArtifactFileSize(file.length());
                }
                catch (Exception ignored) {}
            }
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

        public FormValidation doCheckArtifactFileName(@QueryParameter String artifactFileName) {
            if (artifactFileName == null || artifactFileName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

    }

}
