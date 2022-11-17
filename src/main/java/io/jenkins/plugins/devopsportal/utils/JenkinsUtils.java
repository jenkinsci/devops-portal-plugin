package io.jenkins.plugins.devopsportal.utils;

import hudson.model.*;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Optional;

/**
 * Utility class to read data from Jenkins.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public final class JenkinsUtils {

    public static Optional<Run<?, ?>> getBuild(String jobName, String branchName, String buildNumber) {
        if (Jenkins.getInstanceOrNull() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getBuild(jobName, branchName, buildNumber, Jenkins.get().getItems()));
    }

    private static Run<?, ?> getBuild(String jobName, String branchName, String buildNumber,
                                      Collection<? extends TopLevelItem> items) {
        // TODO Implements branch name for Multibranch Pipeline
        // TODO Check consistency with folder items
        for (TopLevelItem item : items) {
            if (item.getName().equals(jobName)) {
                if (item instanceof Job) {
                    Run<?, ?> build = ((Job<?, ?>) item).getBuild(buildNumber);
                    if (build != null) {
                        return build;
                    }
                }
            }
            else if (item instanceof ViewGroup) {
                Run<?, ?> build = getBuild(jobName, branchName, buildNumber, ((ViewGroup) item).getItemGroup().getItems());
                if (build != null) {
                    return build;
                }
            }
        }
        return null;
    }

}
