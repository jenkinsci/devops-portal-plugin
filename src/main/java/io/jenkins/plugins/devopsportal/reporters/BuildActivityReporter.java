package io.jenkins.plugins.devopsportal.reporters;

import hudson.Extension;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.BuildActivity;
import org.kohsuke.stapler.DataBoundConstructor;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;

/**
 * Build step of a project used to record a BUILD activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildActivityReporter extends AbstractActivityReporter<BuildActivity> {

    private String artifactFileName;
    private int dependenciesToUpdate;

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

    public int getDependenciesToUpdate() {
        return dependenciesToUpdate;
    }

    @DataBoundSetter
    public void setDependenciesToUpdate(int dependenciesToUpdate) {
        this.dependenciesToUpdate = dependenciesToUpdate;
    }

    @Override
    public void updateActivity(BuildActivity activity) {
        activity.setArtifactFileName(artifactFileName);
        if (artifactFileName != null) {
            File file = new File(artifactFileName);
            if (file.exists()) {
                activity.setArtifactFileSize(file.length());
            }
        }
        activity.setDependenciesToUpdate(dependenciesToUpdate);
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

    }

}
