package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.DependenciesAnalysisActivity;
import io.jenkins.plugins.devopsportal.models.DependenciesManager;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.util.function.BiConsumer;

/**
 * Build step of a project used to record a DEPENDENCIES_ANALYSIS activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DependenciesAnalysisActivityReporter extends AbstractActivityReporter<DependenciesAnalysisActivity> {

    private DependenciesManager manager;
    private String baseDirectory;
    private int outdatedDependencies;
    private int vulnerabilities;

    @DataBoundConstructor
    public DependenciesAnalysisActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public DependenciesManager getManager() {
        return manager;
    }

    @DataBoundSetter
    public void setManager(String manager) {
        this.manager = DependenciesManager.valueOf(manager);
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    @DataBoundSetter
    public void setBaseDirectory(String projectBaseDirectory) {
        this.baseDirectory = projectBaseDirectory;
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

    @Override
    public void updateActivity(@NonNull DependenciesAnalysisActivity activity, @NonNull TaskListener listener,
                               @NonNull EnvVars env) {
        File dir = new File(env.get("WORKSPACE"), baseDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_BaseDirectoryNotReadable()
                    .replace("%folder%", baseDirectory));
        }
        else {
            switch (manager) {
                case MAVEN: execute(DependenciesManager.MAVEN, dir, listener,
                        DependenciesAnalysisActivityReporter::analyseMaven);
                break;
                case NPM: execute(DependenciesManager.NPM, dir, listener,
                        DependenciesAnalysisActivityReporter::analyseNpm);
                break;
                default:
                    listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_BaseDirectoryNotReadable()
                            .replace("%folder%", baseDirectory));
            }
        }
        activity.setManager(manager);
    }

    private void execute(@NonNull DependenciesManager manager, @NonNull File baseDirectory,
                         @NonNull TaskListener listener, BiConsumer<File, TaskListener> function) {
        File manifest = new File(baseDirectory, manager.getManifestName());
        if (!manifest.exists() || !manifest.isFile()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_ManifestFileNotReadable()
                    .replace("%file%", manifest.toString()));
            return;
        }
        listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_AnalysisStarted()
                .replace("%file%", manifest.toString())
                .replace("%manager%", manager.getLabel()));
        try {
            function.accept(manifest, listener);
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_AnalysisError()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage()));
        }
    }

    private static void analyseMaven(@NonNull File manifest, @NonNull TaskListener listener) {

    }

    private static void analyseNpm(@NonNull File manifest, @NonNull TaskListener listener) {

    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.DEPENDENCIES_ANALYSIS;
    }

    @Symbol("reportDependenciesAnalysis")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.DependenciesAnalysisActivityReporter_DisplayName());
        }

        public ListBoxModel doFillManagerItems() {
            ListBoxModel list = new ListBoxModel();
            for (DependenciesManager manager : DependenciesManager.values()) {
                list.add(manager.getLabel(), manager.name());
            }
            return list;
        }

        public FormValidation doCheckManager(@QueryParameter String manager) {
            if (manager == null || manager.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            try {
                DependenciesManager.valueOf(manager);
            }
            catch (IllegalArgumentException ex) {
                return FormValidation.error(Messages.FormValidation_Error_InvalidValue());
            }
            return FormValidation.ok();
        }

    }

}
