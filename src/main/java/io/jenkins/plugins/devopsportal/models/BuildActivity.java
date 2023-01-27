package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.nio.file.Paths;

/**
 * A persistent record of a BUILD activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildActivity extends AbstractActivity {

    private String artifactFileName;
    private long artifactFileSize;
    private long artifactFileSizeDelta;
    private long artifactFileSizeLimit;

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

    public long getArtifactFileSizeDelta() {
        return artifactFileSizeDelta;
    }

    @DataBoundSetter
    public void setArtifactFileSizeDelta(long artifactFileSizeDelta) {
        this.artifactFileSizeDelta = artifactFileSizeDelta;
    }

    public long getArtifactFileSizeLimit() {
        return artifactFileSizeLimit;
    }

    @DataBoundSetter
    public void setArtifactFileSizeLimit(long artifactFileSizeLimit) {
        this.artifactFileSizeLimit = artifactFileSizeLimit;
    }

    @SuppressWarnings("unused")
    public String getArtifactFileSizeDeltaStr() {
        String sign = "-";
        if (artifactFileSizeDelta > 0) {
            sign = "+";
        }
        return sign + MiscUtils.readableFileSize(Math.abs(artifactFileSizeDelta));
    }

    @SuppressWarnings("unused")
    public String getArtifactFileNameStr() {
        if (artifactFileName != null && !artifactFileName.isEmpty()) {
            return Paths.get(artifactFileName).getFileName() + "";
        }
        return "";
    }

    @SuppressWarnings("unused")
    public String getArtifactFileSizeStr() {
        return MiscUtils.readableFileSize(artifactFileSize);
    }

}
