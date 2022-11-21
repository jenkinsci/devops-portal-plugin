package io.jenkins.plugins.devopsportal.buildmanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuildManager {

    public BuildManager getConfiguration() {
        return getClass().getAnnotation(BuildManager.class);
    }

    public abstract String getManagerBinaryPath(String managerBinaryFile, @NonNull EnvVars env);

    public abstract List<String> getDependencyUpdatesCommand(@NonNull File manifestFile,
                                                             String managerBinaryFile,
                                                             @NonNull EnvVars env);
    public abstract List<String> getDependencyCheckCommand(@NonNull File manifestFile,
                                                           String managerBinaryFile,
                                                           @NonNull EnvVars env);

    public Map<String, List<DependencyUpgrade>> getUpdateRecords(@NonNull File manifestFile,
                                                     String managerBinaryFile,
                                                     @NonNull TaskListener listener,
                                                     @NonNull EnvVars env) {

        // Manager binary file
        final String command = getManagerBinaryPath(managerBinaryFile, env);
        // Execute
        try {
            return MiscUtils.filterLines(
                    manifestFile.getParentFile(), // Work directory
                    getDependencyUpdatesCommand(manifestFile, command, env), // Shell command
                    isWindowsOs(), // Execution environment
                    this::analyseOutdatedDependencies // Mapper function
            );
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_AnalysisError()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage()));
        }
        return null;
    }

    public Map<String, List<DependencyVulnerability>> getVulnerabilitiesRecords(@NonNull File manifestFile,
                                                                                String managerBinaryFile,
                                                                                @NonNull TaskListener listener,
                                                                                @NonNull EnvVars env) {
        // Manager binary file
        final String command = getManagerBinaryPath(managerBinaryFile, env);
        // Execute
        try {
            return MiscUtils.filterLines(
                    manifestFile.getParentFile(), // Work directory
                    getDependencyCheckCommand(manifestFile, command, env), // Shell command
                    isWindowsOs(), // Execution environment
                    this::analyseVulnerableDependencies // Mapper function
            );
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_AnalysisError()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage()));
        }
        return null;
    }

    public boolean isWindowsOs() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public abstract Map<String, List<DependencyUpgrade>> analyseOutdatedDependencies(List<String> lines);

    public abstract Map<String, List<DependencyVulnerability>> analyseVulnerableDependencies(List<String> lines);

}