package io.jenkins.plugins.devopsportal.models;

import hudson.EnvVars;
import hudson.model.Run;

/**
 * Provide a standard interface to all persistent objects which are associated with a run.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public interface GenericRunModel {

    void setBuildJob(String buildJob);
    void setBuildNumber(String buildJob);
    void setBuildURL(String buildJob);
    void setBuildBranch(String buildJob);
    void setBuildCommit(String buildJob);

    @SuppressWarnings("unused")
    static void updateRecordFromRun(GenericRunModel record, Run<?,?> run, EnvVars env) {
        record.setBuildJob(env.get("JOB_NAME"));
        record.setBuildNumber(env.get("BUILD_NUMBER"));
        record.setBuildURL(env.get("RUN_DISPLAY_URL"));
        record.setBuildBranch(env.containsKey("GIT_BRANCH")
                ? env.get("GIT_BRANCH").replace("origin/", "") : "");
        record.setBuildCommit(env.getOrDefault("GIT_COMMIT", ""));
    }

}
