package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Map;

/**
 * Model for a security hotspot in a quality audit activity
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class SecurityHotspot implements Serializable {

    private String category;
    private String file;
    private int line;
    private String message;
    private String probability;
    private String creation;

    @DataBoundConstructor
    public SecurityHotspot() {
    }

    public SecurityHotspot(@NonNull Map<String, Object> issue) {
        category = MiscUtils.getStringOrEmpty(issue, "securityCategory");
        file = MiscUtils.getStringOrEmpty(issue, "component");
        line = MiscUtils.getIntOrZero(issue, "line");
        message = MiscUtils.getStringOrEmpty(issue, "message");
        probability = MiscUtils.getStringOrEmpty(issue, "vulnerabilityProbability");
        creation = MiscUtils.getStringOrEmpty(issue, "creationDate");
    }

    public String getCategory() {
        return category;
    }

    @DataBoundSetter
    public void setCategory(String category) {
        this.category = category;
    }

    public String getFile() {
        return file;
    }

    @DataBoundSetter
    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    @DataBoundSetter
    public void setLine(int line) {
        this.line = line;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    @SuppressWarnings("unused")
    public String getProbability() {
        return probability;
    }

    @SuppressWarnings("unused")
    @DataBoundSetter
    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getCreation() {
        return creation;
    }

    @DataBoundSetter
    public void setCreation(String creation) {
        this.creation = creation;
    }

}
