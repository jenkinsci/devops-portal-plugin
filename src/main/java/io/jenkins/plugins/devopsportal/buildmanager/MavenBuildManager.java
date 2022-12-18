package io.jenkins.plugins.devopsportal.buildmanager;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The configuration for Maven build manager
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@BuildManager(code = "MAVEN", label = "Maven")
public class MavenBuildManager extends AbstractBuildManager {

    @Override
    public String getManagerBinaryPath(String managerBinaryFile, @NonNull EnvVars env) {
        if (managerBinaryFile != null && !managerBinaryFile.isEmpty()) {
            return managerBinaryFile;
        }
        return env.getOrDefault("MAVEN_PATH", "mvn");
    }

    @Override
    public List<String> getDependencyUpdatesCommand(@NonNull File manifestFile, String managerBinaryFile,
                                                    @NonNull EnvVars env) {
        final List<String> cmd = new ArrayList<>();
        if (isWindowsOs()) {
            cmd.add("cmd");
            cmd.add("/k");
            cmd.add("\"" + getManagerBinaryPath(managerBinaryFile, env));
        }
        else {
            cmd.add(getManagerBinaryPath(managerBinaryFile, env));
        }
        cmd.add("-f");
        cmd.add(manifestFile.getAbsolutePath());
        if (isWindowsOs()) {
            cmd.add("versions:display-dependency-updates\"");
        }
        else {
            cmd.add("versions:display-dependency-updates");
        }
        return cmd;
    }

    @Override
    public List<String> getDependencyCheckCommand(@NonNull File manifestFile, String managerBinaryFile,
                                                  @NonNull EnvVars env) {
        final List<String> cmd = new ArrayList<>();
        if (isWindowsOs()) {
            cmd.add("cmd");
            cmd.add("/k");
            cmd.add("\"" + getManagerBinaryPath(managerBinaryFile, env));
        }
        else {
            cmd.add(getManagerBinaryPath(managerBinaryFile, env));
        }
        cmd.add("-f");
        cmd.add(manifestFile.getAbsolutePath());
        if (isWindowsOs()) {
            cmd.add("dependency-check:check\"");
        }
        else {
            cmd.add("dependency-check:check");
        }
        return cmd;
    }

    @Override
    public Map<String, List<DependencyUpgrade>> analyseOutdatedDependencies(List<String> lines) {
        final Pattern regex1 = Pattern.compile("(.*)display-dependency-updates(.*) @ (.*) ---");
        final Pattern regex2 = Pattern.compile("\\[INFO\\]   (.*) (\\.+) (.*) -> (.*)");
        final Map<String, List<DependencyUpgrade>> result = new HashMap<>();
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
                            new DependencyUpgrade(component, matcher2.group(1), matcher2.group(3), matcher2.group(4))
                    );
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, List<DependencyVulnerability>> analyseVulnerableDependencies(List<String> lines) {
        final Pattern regex = Pattern.compile("^(.+?)(\\\\.*?)? \\((.*?)\\) : (CVE.*)$");
        final Map<String, List<DependencyVulnerability>> result = new HashMap<>();
        result.put("ALL", new ArrayList<>());
        for (String str : lines) {
            Matcher matcher = regex.matcher(str);
            if (matcher.find()) {
                result.get("ALL").add(
                        new DependencyVulnerability(matcher.group(1), matcher.group(3), matcher.group(4))
                );
            }
        }
        return result;
    }

}
