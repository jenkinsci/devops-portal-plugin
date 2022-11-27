package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.sonarqube.ws.Hotspots;

import java.io.Serializable;

public class SecurityHotspot implements Serializable {

    public final String category;
    public final String file;
    public final int line;
    public final String message;
    public final String probability;
    public final String creation;

    public SecurityHotspot(@NonNull Hotspots.SearchWsResponse.Hotspot issue) {
        category = issue.getSecurityCategory();
        file = issue.getComponent();
        line = issue.getLine();
        message = issue.getMessage();
        probability = issue.getVulnerabilityProbability();
        creation = issue.getCreationDate();
    }

}
