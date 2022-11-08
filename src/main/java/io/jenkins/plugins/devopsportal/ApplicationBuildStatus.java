package io.jenkins.plugins.devopsportal;

import java.util.Map;

public class ApplicationBuildStatus {

    private String applicationName;
    private String applicationVersion;

    private String buildJob;
    private String buildNumber;
    private String buildURL;
    private String buildBranch;
    private String buildCommit;

    private Map<BuildActivities, BuildActivityStatus> activitiesStatus;

}
