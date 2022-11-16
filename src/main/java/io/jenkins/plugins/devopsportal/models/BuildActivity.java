package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class BuildActivity extends AbstractActivity {

    private String artifactFileName;
    private long artifactFileSize;
    private int dependenciesToUpdate;

    @DataBoundConstructor
    public BuildActivity(String applicationComponent) {
        super(ActivityCategory.BUILD, applicationComponent);
    }

    public String getArtifactFileName() {
        return artifactFileName;
    }

    @DataBoundSetter
    public void setArtifactFileName(String artifactFileName) {
        this.artifactFileName = artifactFileName;
    }

    public long getArtifactFileSize() {
        return artifactFileSize;
    }

    @DataBoundSetter
    public void setArtifactFileSize(long artifactFileSize) {
        this.artifactFileSize = artifactFileSize;
    }

    public int getDependenciesToUpdate() {
        return dependenciesToUpdate;
    }

    @DataBoundSetter
    public void setDependenciesToUpdate(int dependenciesToUpdate) {
        this.dependenciesToUpdate = dependenciesToUpdate;
    }

}
