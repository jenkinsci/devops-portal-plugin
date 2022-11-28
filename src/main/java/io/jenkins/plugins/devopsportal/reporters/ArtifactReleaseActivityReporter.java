package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.ArtifactReleaseActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record an IMAGE_RELEASE activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ArtifactReleaseActivityReporter extends AbstractActivityReporter<ArtifactReleaseActivity> {

    private String repositoryName;
    private String artifactName;
    private String artifactURL;
    private String tags;

    @DataBoundConstructor
    public ArtifactReleaseActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    @DataBoundSetter
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    @DataBoundSetter
    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactURL() {
        return artifactURL;
    }

    @DataBoundSetter
    public void setArtifactURL(String artifactURL) {
        this.artifactURL = artifactURL;
    }

    public String getTags() {
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull ArtifactReleaseActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env) {
        activity.setRepositoryName(repositoryName);
        activity.setArtifactName(artifactName);
        activity.setArtifactURL(artifactURL);
        activity.setTags(tags);
        return null;
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.ARTIFACT_RELEASE;
    }

    @Symbol("reportArtifactRelease")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.ArtifactReleaseActivityReporter_DisplayName());
        }

    }

}
