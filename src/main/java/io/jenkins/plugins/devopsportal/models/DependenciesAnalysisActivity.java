package io.jenkins.plugins.devopsportal.models;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * A persistent record of a DEPENDENCIES_ANALYSIS activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DependenciesAnalysisActivity extends AbstractActivity {

    private DependenciesManager manager;
    private int outdatedDependencies;
    private int vulnerabilities;

    @DataBoundConstructor
    public DependenciesAnalysisActivity(String applicationComponent) {
        super(ActivityCategory.DEPENDENCIES_ANALYSIS, applicationComponent);
    }

    public DependenciesManager getManager() {
        return manager;
    }

    @DataBoundSetter
    public void setManager(DependenciesManager manager) {
        this.manager = manager;
    }

    public int getOutdatedDependencies() {
        return outdatedDependencies;
    }

    @DataBoundSetter
    public void setOutdatedDependencies(int outdatedDependencies) {
        this.outdatedDependencies = outdatedDependencies;
    }

    public int getVulnerabilities() {
        return vulnerabilities;
    }

    @DataBoundSetter
    public void setVulnerabilities(int vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

}
