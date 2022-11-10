package io.jenkins.plugins.devopsportal;

import java.io.Serializable;

/**
 * A persisted record of an exploitation operation performed on a run platform.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ServiceOperation implements Serializable {

    private String serviceId;

    private RunOperations operation;
    private boolean success;
    private long timestamp;

    private String applicationName;
    private String applicationVersion;

    private String buildJob;
    private String buildNumber;
    private String buildURL;
    private String buildBranch;
    private String buildCommit;

}
