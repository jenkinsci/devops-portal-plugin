package io.jenkins.plugins.devopsportal.models;

import java.io.Serializable;

/**
 * A record for a dependency to upgrade
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DependencyUpgrade implements Serializable {

    public final String component;
    public final String dependency;
    public final String currentVersion;
    public final String updateVersion;

    public DependencyUpgrade(String component, String dependency, String currentVersion, String updateVersion) {
        this.component = component;
        this.dependency = dependency;
        this.currentVersion = currentVersion;
        this.updateVersion = updateVersion;
    }

    public String toString() {
        return String.format("%s/%s [ %s => %s ]", component, dependency, currentVersion, updateVersion);
    }

}
