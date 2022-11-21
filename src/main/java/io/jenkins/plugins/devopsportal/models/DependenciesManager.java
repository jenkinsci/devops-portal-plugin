package io.jenkins.plugins.devopsportal.models;

public enum DependenciesManager {

    MAVEN("Maven", "pom.xml", "MAVEN_HOME"),
    NPM("NPM", "packages.json", "NODE_PATH");

    private final String label;
    private final String manifestName;
    private final String homeDirectoryEnvVar;

    DependenciesManager(String name, String manifestName, String homeDirectoryEnvVar) {
        this.label = name;
        this.manifestName = manifestName;
        this.homeDirectoryEnvVar = homeDirectoryEnvVar;
    }

    public String getLabel() {
        return label;
    }

    public String getManifestName() {
        return manifestName;
    }

    public String getHomeDirectoryEnvVar() {
        return homeDirectoryEnvVar;
    }

}
