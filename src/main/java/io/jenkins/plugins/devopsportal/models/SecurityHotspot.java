package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.sonarqube.ws.Hotspots;

import java.io.Serializable;

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

    public SecurityHotspot(@NonNull Hotspots.SearchWsResponse.Hotspot issue) {
        category = issue.getSecurityCategory();
        file = issue.getComponent();
        line = issue.getLine();
        message = issue.getMessage();
        probability = issue.getVulnerabilityProbability();
        creation = issue.getCreationDate();
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
