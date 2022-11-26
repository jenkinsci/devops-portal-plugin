package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.sonarqube.ws.Issues;

import java.io.Serializable;

public class QualityIssue implements Serializable {

    public QualityIssue(@NonNull Issues.Issue issue) {
        String tmp = String.format(
                "rule=%s line=%s proj=%s pr=%s key=%s cdate=%s severity=%s msg=%s status=%s component=%s",
                issue.getRule(),
                issue.getLine(),
                issue.getProject(),
                issue.getPullRequest(),
                issue.getKey(),
                issue.getCreationDate(),
                issue.getSeverity().name(),
                issue.getMessage(),
                issue.getStatus(),
                issue.getComments()
        );
        System.out.println(tmp);
    }

}
