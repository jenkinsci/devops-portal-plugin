package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.sonarqube.ws.Issues;

import java.io.Serializable;

public class QualityIssue implements Serializable {

    public final String severity;
    public final String file;
    public final int line;
    public final String rule;
    public final String message;
    public final String creation;

    public QualityIssue(@NonNull Issues.Issue issue) {
        severity = issue.getSeverity().name();
        file = issue.getComponent();
        line = issue.getLine();
        rule = issue.getRule();
        message = issue.getMessage();
        creation = issue.getCreationDate();
    }

}
