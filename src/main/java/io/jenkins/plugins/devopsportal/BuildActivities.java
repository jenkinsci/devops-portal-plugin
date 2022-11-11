package io.jenkins.plugins.devopsportal;

/**
 * List of all development activities leading to a finalized version.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public enum BuildActivities {

    //INTEGRATION("Features integration"),
    BUILD("Build"),
    UT("Unit Tests"),
    QA("Quality Audit"),
    SECU("Security Audit"),
    DEPLOY("Deployment"),
    PERF("Performance Tests");
    //QUALIF("Qualification Tests"),
    //UAT("User Acceptance Tests");

    private final String label;

    BuildActivities(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
