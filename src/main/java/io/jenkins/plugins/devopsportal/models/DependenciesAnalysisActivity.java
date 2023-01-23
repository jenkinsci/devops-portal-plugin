package io.jenkins.plugins.devopsportal.models;

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

    private List<DependencyUpgrade> outdatedDependenciesList;
    private VulnerabilityAnalysisResult vulnerabilities;

    @DataBoundConstructor
    public DependenciesAnalysisActivity(String applicationComponent) {
        super(ActivityCategory.DEPENDENCIES_ANALYSIS, applicationComponent);
        this.outdatedDependenciesList = new ArrayList<>();
        this.vulnerabilities = new VulnerabilityAnalysisResult();
    }

    public List<DependencyUpgrade> getOutdatedDependenciesList() {
        return outdatedDependenciesList;
    }

    @DataBoundSetter
    public void setOutdatedDependenciesList(List<DependencyUpgrade> outdatedDependenciesList) {
        this.outdatedDependenciesList = outdatedDependenciesList;
    }

    public VulnerabilityAnalysisResult getVulnerabilities() {
        return vulnerabilities;
    }

    @DataBoundSetter
    public void setVulnerabilities(VulnerabilityAnalysisResult vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    @SuppressWarnings("unused")
    public boolean hasIssues() {
        return outdatedDependenciesList != null && !vulnerabilities.isEmpty();
    }

}
