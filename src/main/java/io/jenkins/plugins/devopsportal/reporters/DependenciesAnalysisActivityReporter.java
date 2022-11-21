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
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void setManager(Object manager) {
        if (manager instanceof String) {
            this.manager = DependenciesManager.valueOf((String) manager);
        }
        else if (manager instanceof DependenciesManager) {
            this.manager = (DependenciesManager) manager;
        }
        else {
            throw new IllegalArgumentException();
        }
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
        final File dir = new File(env.get("WORKSPACE"), baseDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_BaseDirectoryNotReadable()
                    .replace("%folder%", baseDirectory));
        }
        else {
            Map<String, List<Record>> result = null;
            switch (manager) {
                case MAVEN:
                    result = executeManager(
                            DependenciesManager.MAVEN,
                            env.getOrDefault(DependenciesManager.MAVEN.getHomeDirectoryEnvVar(), ""),
                            dir,
                            listener,
                            DependenciesAnalysisActivityReporter::analyseMaven
                    );
                    break;
                case NPM:
                    result = executeManager(
                            DependenciesManager.NPM,
                            env.getOrDefault(DependenciesManager.NPM.getHomeDirectoryEnvVar(), ""),
                            dir,
                            listener,
                            DependenciesAnalysisActivityReporter::analyseNpm
                    );
                    break;
                default:
                    listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_BaseDirectoryNotReadable()
                            .replace("%folder%", baseDirectory));
            }
            if (result != null) {
                int outdated = 0;
                int vulnerability = 0;
                for (String component : result.keySet()) {
                    listener.getLogger().println("Component: " + component);
                    for (Record record : result.get(component)) {
                        listener.getLogger().println(" - Dependency: " + record);
                        if ("$VULNERABILITY".equals(component)) {
                            vulnerability++;
                        }
                        else {
                            outdated++;
                        }
                    }
                }
                activity.setOutdatedDependencies(outdated);
                activity.setVulnerabilities(vulnerability);
            }
        }
        activity.setManager(manager);
    }

    private Map<String, List<Record>> executeManager(@NonNull DependenciesManager manager, @NonNull String managerHomeDir,
                                                     @NonNull File baseDirectory, @NonNull TaskListener listener,
                                                     BiFunction<File, File, Map<String, List<Record>>> function) {
        final File manifest = new File(baseDirectory, manager.getManifestName());
        if (!manifest.exists() || !manifest.isFile()) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_ManifestFileNotReadable()
                    .replace("%file%", manifest.toString()));
            return null;
        }
        listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_AnalysisStarted()
                .replace("%file%", manifest.toString())
                .replace("%manager%", manager.getLabel()));
        try {
            return function.apply(manifest, new File(managerHomeDir));
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.DependenciesAnalysisActivityReporter_Error_AnalysisError()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage()));
        }
        return null;
    }

    private static Map<String, List<Record>> analyseMaven(@NonNull File manifestFile, @NonNull File managerDir) {
        final List<String> params = new ArrayList<>();
        final boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        if (windows) {
            params.add("cmd");
            params.add("/k");
            params.add("\"" + new File(managerDir, "bin/mvn").getAbsolutePath());
        }
        else {
            params.add("mvn");
        }
        params.add("-f");
        params.add(manifestFile.getAbsolutePath());
        if (windows) {
            params.add("versions:display-dependency-updates\"");
        }
        else {
            params.add("versions:display-dependency-updates");
        }
        return MiscUtils.filterLines(
                manifestFile.getParentFile(),
                params.toArray(new String[0]),
                windows,
                (lines) -> {
                    final Pattern regex1 = Pattern.compile("(.*)display-dependency-updates(.*) @ (.*) ---");
                    final Pattern regex2 = Pattern.compile("\\[INFO\\]   (.*) (\\.+) (.*) -> (.*)");
                    final Map<String, List<Record>> result = new HashMap<>();
                    String component = null;
                    for (String str : lines) {
                        Matcher matcher1 = regex1.matcher(str);
                        if (matcher1.find()) {
                            component = matcher1.group(3);
                            result.put(component, new ArrayList<>());
                        }
                        else if (component != null) {
                            Matcher matcher2 = regex2.matcher(str);
                            if (matcher2.find()) {
                                result.get(component).add(
                                        new Record(component, matcher2.group(1), matcher2.group(3), matcher2.group(4))
                                );
                            }
                        }
                    }
                    return result;
                }
        );
    }

    private static Map<String, List<Record>> analyseNpm(@NonNull File manifestFile, @NonNull File managerDir) {
        return null;
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
            final ListBoxModel list = new ListBoxModel();
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

    public static class Record {

        public final String component;
        public final String dependency;
        public final String currentVersion;
        public final String updateVersion;

        public Record(String component, String dependency, String currentVersion, String updateVersion) {
            this.component = component;
            this.dependency = dependency;
            this.currentVersion = currentVersion;
            this.updateVersion = updateVersion;
        }

        public String toString() {
            return String.format("%s/%s [ %s => %s ]", component, dependency, currentVersion, updateVersion);
        }

    }

}
