package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * A persistent record of a IMAGE_RELEASE activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ImageReleaseActivity extends AbstractActivity {

    private String registryName;
    private String imageName;
    private final List<String> tags;

    @DataBoundConstructor
    public ImageReleaseActivity(String applicationComponent) {
        super(ActivityCategory.IMAGE_RELEASE, applicationComponent);
        this.tags = new ArrayList<>();
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

    public List<String> getTags() {
        return tags;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags.clear();
        this.tags.addAll(MiscUtils.split(tags, ","));
    }

}
