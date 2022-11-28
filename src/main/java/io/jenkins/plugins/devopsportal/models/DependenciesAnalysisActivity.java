package io.jenkins.plugins.devopsportal.models;

import io.jenkins.plugins.devopsportal.buildmanager.DependencyUpgrade;
import io.jenkins.plugins.devopsportal.buildmanager.DependencyVulnerability;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * A persistent record of a DEPENDENCIES_ANALYSIS activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DependenciesAnalysisActivity extends AbstractActivity {

    private String manager;
    private int outdatedDependencies;
    private int vulnerabilities;
    private List<DependencyUpgrade> outdatedDependenciesList;
    private List<DependencyVulnerability> vulnerabilitiesList;

    @DataBoundConstructor
    public DependenciesAnalysisActivity(String applicationComponent) {
        super(ActivityCategory.DEPENDENCIES_ANALYSIS, applicationComponent);
        this.outdatedDependenciesList = new ArrayList<>();
        this.vulnerabilitiesList = new ArrayList<>();
    }

    public String getManager() {
        return manager;
    }

    @DataBoundSetter
    public void setManager(String manager) {
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

    @SuppressWarnings("unused")
    public List<DependencyUpgrade> getOutdatedDependenciesList() {
        return outdatedDependenciesList;
    }

    @DataBoundSetter
    public void setOutdatedDependenciesList(List<DependencyUpgrade> list) {
        this.outdatedDependenciesList = list;
    }

    @SuppressWarnings("unused")
    public List<DependencyVulnerability> getVulnerabilitiesList() {
        return vulnerabilitiesList;
    }

    @DataBoundSetter
    public void setVulnerabilitiesList(List<DependencyVulnerability> list) {
        this.vulnerabilitiesList = list;
    }

    @SuppressWarnings("unused")
    public boolean hasIssues() {
        return (outdatedDependenciesList != null && vulnerabilitiesList != null)
                && (!outdatedDependenciesList.isEmpty() || !vulnerabilitiesList.isEmpty());
    }

}
