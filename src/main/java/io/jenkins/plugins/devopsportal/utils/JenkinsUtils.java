package io.jenkins.plugins.devopsportal.utils;

import hudson.model.*;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to read data from Jenkins.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public final class JenkinsUtils {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    private JenkinsUtils() {
    }

    public static Optional<Run<?, ?>> getBuild(String jobName, String branchName, String buildNumber) {
        if (Jenkins.getInstanceOrNull() == null) {
            return Optional.empty();
        }
        if (branchName != null && branchName.isEmpty()) {
            branchName = null;
        }
        Job<?, ?> job = findJobByName(jobName, branchName, Jenkins.get().getItems());
        if (job == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(job.getBuild(buildNumber));
    }

    public static Job<?, ?> findJobByName(String jobName, String itemName, Collection<? extends TopLevelItem> items) {
        return findJobByName(jobName, itemName, items, "");
    }

    /**
     * This function is used to retrieve a job instance from Jenkins persistent data.
     * It has two behaviour:
     * - It is used to retrieve a simple element (job) by designating it by its name.
     *   In this case, only the jobName is required and itemName must be null.
     * - But it also allows to get the sub-job in the case of a complex object like a
     *   Multi-Branch pipeline. In this case, itemName must be given.
     */
    private static Job<?, ?> findJobByName(String jobName, String itemName, Collection<? extends TopLevelItem> items, String path) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Find: " + jobName + " / " + itemName + " path=" + path);
        }
        for (TopLevelItem item : items) {
            // Item groups (WorkflowMultiBranchProject)
            if (itemName != null && item instanceof ItemGroup && item.getName().equals(jobName)) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(" - ItemGroup: " + item.getName() + " path=" + path);
                }
                try {
                    Object job = ((ItemGroup<?>) item).getItem(itemName);
                    if (job != null) {
                        return (Job<?, ?>) job;
                    }
                }
                catch (Exception ex) {
                    LOGGER.warning("Unable to find job '" + jobName + "/" + itemName + "': "
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                }
            }
            // View groups (Folders)
            else if (item instanceof ViewGroup) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(" - ViewGroup: " + path + "/" + item.getName());
                }
                for (View view : ((ViewGroup) item).getAllViews()) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("   - View: " + path + "/" + item.getName() + "/" + view.getViewName());
                    }
                    Job<?, ?> job = findJobByName(jobName, itemName, view.getItems(), path + "/" + item.getName() + "/" + view.getViewName());
                    if (job != null) {
                        return job;
                    }
                }
            }
            // Jobs (FreeStyleProject, WorkflowJob, ...)
            else if (itemName == null && item instanceof Job) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(" - Job: " + path + "/" + item.getName());
                }
                if (item.getName().equals(jobName)) {
                    return (Job<?, ?>) item;
                }
            }
            else if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(" - Unknown: " + path + "/" + item.getName() + " (" + item.getClass() + ")");
            }
        }
        return null;
    }

}
