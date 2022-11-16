package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.nio.file.Paths;

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

    public String getArtifactFileNameStr() {
        if (artifactFileName != null && !artifactFileName.isEmpty()) {
            return Paths.get(artifactFileName).getFileName().toString();
        }
        return "";
    }

    public String getArtifactFileSizeStr() {
        return MiscUtils.readableFileSize(artifactFileSize);
    }

}
