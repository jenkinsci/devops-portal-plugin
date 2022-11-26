package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.sonarqube.ws.Hotspots;

import java.io.Serializable;

public class SecurityHotspot implements Serializable {

    public SecurityHotspot(@NonNull Hotspots.SearchWsResponse.Hotspot issue) {
        String tmp = String.format(
                "status=%s line=%s proj=%s rule=%s key=%s cdate=%s component=%s msg=%s",
                issue.getStatus(),
                issue.getLine(),
                issue.getProject(),
                issue.getRuleKey(),
                issue.getKey(),
                issue.getCreationDate(),
                issue.getComponent(),
                issue.getMessage()
        );
        System.out.println(tmp);
    }

}
