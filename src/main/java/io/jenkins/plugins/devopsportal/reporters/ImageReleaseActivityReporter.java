package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.ImageReleaseActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record an IMAGE_RELEASE activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ImageReleaseActivityReporter extends AbstractActivityReporter<ImageReleaseActivity> {

    private String registryName;
    private String imageName;
    private String tags;

    @DataBoundConstructor
    public ImageReleaseActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getRegistryName() {
        return registryName;
    }

    @DataBoundSetter
    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    public String getImageName() {
        return imageName;
    }

    @DataBoundSetter
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTags() {
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public void updateActivity(@NonNull ApplicationBuildStatus status, @NonNull ImageReleaseActivity activity,
                               @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setRegistryName(registryName);
        activity.setImageName(imageName);
        activity.setTags(tags);
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.IMAGE_RELEASE;
    }

    @Symbol("reportImageRelease")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.ImageReleaseActivityReporter_DisplayName());
        }

    }

}
