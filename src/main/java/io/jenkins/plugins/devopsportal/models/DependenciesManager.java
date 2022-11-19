package io.jenkins.plugins.devopsportal.models;

public enum DependenciesManager {

    MAVEN("Maven", "pom.xml"),
    NPM("NPM", "packages.json");

    private final String label;
    private final String manifestName;

    DependenciesManager(String name, String manifestName) {
        this.label = name;
        this.manifestName = manifestName;
    }

    public String getLabel() {
        return label;
    }

    public String getManifestName() {
        return manifestName;
    }

}
