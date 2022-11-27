package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.sonarqube.ws.Issues;

import java.io.Serializable;

public class QualityIssue implements Serializable {

    private String severity;
    private String file;
    private int line;
    private String rule;
    private String message;
    private String creation;

    @DataBoundConstructor
    public QualityIssue() {
    }

    public QualityIssue(@NonNull Issues.Issue issue) {
        severity = issue.getSeverity().name();
        file = issue.getComponent();
        line = issue.getLine();
        rule = issue.getRule();
        message = issue.getMessage();
        creation = issue.getCreationDate();
    }

    public String getSeverity() {
        return severity;
    }

    @DataBoundSetter
    public void setSeverity(String severity) {
        this.severity = severity;
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

    public String getRule() {
        return rule;
    }

    @DataBoundSetter
    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreation() {
        return creation;
    }

    @DataBoundSetter
    public void setCreation(String creation) {
        this.creation = creation;
    }

}
