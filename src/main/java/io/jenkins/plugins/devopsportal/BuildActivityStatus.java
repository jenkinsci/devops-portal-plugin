package io.jenkins.plugins.devopsportal;

/**
 * List of the different states of build activities.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public enum BuildActivityStatus {

    PENDING("Pending"),
    DONE("Done"),
    UNSTABLE("Unstable"),
    FAIL("Fail");

    private final String label;

    BuildActivityStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static BuildActivityStatus getDefault() {
        return PENDING;
    }

}
