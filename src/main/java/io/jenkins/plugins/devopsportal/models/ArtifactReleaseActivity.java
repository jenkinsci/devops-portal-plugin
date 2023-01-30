package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A persistent record of a ARTIFACT_RELEASE activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ArtifactReleaseActivity extends AbstractActivity {

    private String repositoryName;
    private String artifactName;
    private String artifactURL;
    private final List<String> tags;

    @DataBoundConstructor
    public ArtifactReleaseActivity(String applicationComponent) {
        super(ActivityCategory.ARTIFACT_RELEASE, applicationComponent);
        this.tags = new ArrayList<>();
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

    @SuppressWarnings("unused")
    public boolean isUrlPresent() {
        if (artifactURL == null || artifactURL.trim().isEmpty()) {
            return false;
        }
        try {
            // JENSEC-1938 Restrict href protocol to only allow some https / http schemes
            final String scheme = new URL(artifactURL).getProtocol();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        }
        catch (MalformedURLException ex) {
            return false;
        }
    }

    public List<String> getTags() {
        if (tags == null) {
            return Collections.emptyList();
        }
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags.clear();
        if (tags != null && !tags.isEmpty()) {
            this.tags.addAll(MiscUtils.split(tags, ","));
        }
    }

}
