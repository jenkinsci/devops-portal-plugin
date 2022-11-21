package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.buildmanager.*;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.DependenciesAnalysisActivity;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Build step of a project used to record a DEPENDENCIES_ANALYSIS activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class DependenciesAnalysisActivityReporter extends AbstractActivityReporter<DependenciesAnalysisActivity> {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    private String manager;
    private String manifestFile;
    private int outdatedDependencies;
    private int vulnerabilities;
    private String managerCommand;

    @DataBoundConstructor
    public DependenciesAnalysisActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getManager() {
        return manager;
    }

    @DataBoundSetter
    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManifestFile() {
        return manifestFile;
    }

    @DataBoundSetter
    public void setManifestFile(String manifestFile) {
        this.manifestFile = manifestFile;
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

    public String getManagerCommand() {
        return managerCommand;
    }

    @DataBoundSetter
    public void setManagerCommand(String managerCommand) {
        this.managerCommand = managerCommand;
    }

    @Override
    public void updateActivity(@NonNull DependenciesAnalysisActivity activity,
                               @NonNull TaskListener listener,
                               @NonNull EnvVars env) {

        File manifest = checkManifestFile(env, manifestFile, listener);
        AbstractBuildManager buildManager = checkBuildManager(manager, listener);
        if (manifest == null || buildManager == null) {
            return;
        }
        activity.setManager(manager);

        // Log
        listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_AnalysisStarted()
                .replace("%file%", manifestFile)
                .replace("%manager%", manager)
        );

        // Check outdated dependencies
        Map<String, List<DependencyUpgrade>> upgrades = buildManager.getUpdateRecords(
                manifest,
                managerCommand,
                listener,
                env
        );
        if (upgrades != null && upgrades.containsKey(getApplicationComponent())) {
            List<DependencyUpgrade> list = upgrades.get(getApplicationComponent());
            activity.setOutdatedDependencies(list.size());
            for (DependencyUpgrade record : list) {
                listener.getLogger().println(" - Outdated dependency: " + record);
            }
            listener.getLogger().println(" - Total outdated dependencies: " + list.size());
        }
        else {
            activity.setOutdatedDependencies(0);
        }

        // Check vulnerable dependencies
        Map<String, List<DependencyVulnerability>> vulnerabilities = buildManager.getVulnerabilitiesRecords(
                manifest,
                managerCommand,
                listener,
                env
        );
        if (vulnerabilities != null && vulnerabilities.containsKey("ALL")) {
            List<DependencyVulnerability> list = vulnerabilities.get("ALL");
            activity.setVulnerabilities(list.size());
            for (DependencyVulnerability record : list) {
                listener.getLogger().println(" - Vulnerable dependency: " + record);
            }
            listener.getLogger().println(" - Total vulnerable dependencies: " + list.size());
        }
        else {
            activity.setVulnerabilities(0);
        }

    }

    public File checkManifestFile(@NonNull EnvVars env, String manifestFile, @NonNull TaskListener listener) {
        final File manifest = new File(env.get("WORKSPACE"), manifestFile);
        if (!manifest.exists() || !manifest.isFile()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_ManifestFileNotReadable()
                    .replace("%file%", manifest.toString()));
            return null;
        }
        return manifest;
    }

    public AbstractBuildManager checkBuildManager(String managerType, @NonNull TaskListener listener) {
        final Optional<AbstractBuildManager> buildManager =
                ((DependenciesAnalysisActivityReporter.DescriptorImpl) getDescriptor()).getManagerByCode(managerType);
        if (!buildManager.isPresent()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_UnknownManager()
                    .replace("%manager%", managerType));
            return null;
        }
        return buildManager.get();
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.DEPENDENCIES_ANALYSIS;
    }

    @Symbol("reportDependenciesAnalysis")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        private static final Map<BuildManager, AbstractBuildManager> MANAGERS = new HashMap<>();

        public DescriptorImpl() {
            super(Messages.DependenciesAnalysisActivityReporter_DisplayName());
            // TODO Automatic population with Annotation detection
            addManager(MavenBuildManager.class);
        }

        private void addManager(Class<?> managerClass) {
            if (managerClass != null && managerClass.isAnnotationPresent(BuildManager.class)
                    && AbstractBuildManager.class.isAssignableFrom(managerClass)) {
                try {
                    MANAGERS.put(
                            managerClass.getAnnotation(BuildManager.class),
                            (AbstractBuildManager) managerClass.getConstructor().newInstance()
                    );
                }
                catch (ReflectiveOperationException ex) {
                    LOGGER.warning("Unable to add manager class '" + managerClass + "': " + ex.getMessage());
                }
            }
            else {
                LOGGER.warning("Unable to add manager class '" + managerClass
                        + "': annotation missing or not assignable from AbstractBuildManager");
            }
        }

        public Optional<AbstractBuildManager> getManagerByCode(@NonNull String manager) {
            for (BuildManager item : MANAGERS.keySet()) {
                if (item.code().equals(manager)) {
                    return Optional.of(MANAGERS.get(item));
                }
            }
            return Optional.empty();
        }

        public ListBoxModel doFillManagerItems() {
            final ListBoxModel list = new ListBoxModel();
            for (BuildManager manager : MANAGERS.keySet()) {
                list.add(manager.label(), manager.code());
            }
            return list;
        }

        public FormValidation doCheckManager(@QueryParameter String manager) {
            if (manager == null || manager.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            if (getManagerByCode(manager).isPresent()) {
                return FormValidation.ok();
            }
            return FormValidation.error(Messages.FormValidation_Error_InvalidValue());
        }

    }

}
