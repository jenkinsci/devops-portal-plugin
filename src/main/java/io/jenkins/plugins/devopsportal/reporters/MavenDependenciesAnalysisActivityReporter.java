package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.*;
import io.jenkins.plugins.devopsportal.utils.*;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build step of a project used to record a DEPENDENCIES_ANALYSIS activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class MavenDependenciesAnalysisActivityReporter extends AbstractActivityReporter<DependenciesAnalysisActivity> {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    private String reportPath;

    @DataBoundConstructor
    public MavenDependenciesAnalysisActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getReportPath() {
        return reportPath;
    }

    @DataBoundSetter
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull DependenciesAnalysisActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env, @NonNull FilePath workspace) {

        // Log
        listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_AnalysisStarted()
                .replace("%manager%", "MAVEN")
                .replace("%file%", reportPath)
        );

        VulnerabilityAnalysisResult result;
        try {
            if (workspace.isRemote()) {
                result = parseFilesFromRemoteWorkspace(new FilePath(workspace, reportPath));
            }
            else {
                result = parseFilesFromLocalWorkspace(env);
            }
        }
        catch (InterruptedException ex) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            return null;
        }
        catch (Exception ex) {
            listener.getLogger().println("Error, unable to parse report file: " + ex.getClass().getSimpleName()
                    + " - " + ex.getMessage());
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Error reading analysis report file: " + reportPath, ex);
            }
            return Result.FAILURE;
        }

        if (result == null) {
            listener.getLogger().println("No dependency analysis report '" + reportPath
                    + "' found. Configuration error?");
            return Result.UNSTABLE;
        }

        listener.getLogger().println("Dependencies: " + result.getDependenciesCount());
        listener.getLogger().println("Vulnerabilities: " + result.getVulnerabilitiesCount());

        activity.setVulnerabilities(result);
        return null;
    }

    private VulnerabilityAnalysisResult parseFilesFromLocalWorkspace(EnvVars env) throws Exception {
        final File file = MiscUtils.checkFilePathIllegalAccess(
                env.get("WORKSPACE", null),
                reportPath
        );
        if (file == null) {
            return null;
        }
        VulnerabilityAnalysisResult result = new VulnerabilityAnalysisResult();
        RemoteFileDependencyAnalysisParser.parse(file, result);
        return result;
    }

    private VulnerabilityAnalysisResult parseFilesFromRemoteWorkspace(FilePath target)
            throws IOException, InterruptedException {
        return target.act(new RemoteFileDependencyAnalysisParser());
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.DEPENDENCIES_ANALYSIS;
    }

    @Symbol("reportMavenDependenciesAnalysis")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.DependenciesAnalysisActivityReporter_DisplayName());
        }

    }

}
