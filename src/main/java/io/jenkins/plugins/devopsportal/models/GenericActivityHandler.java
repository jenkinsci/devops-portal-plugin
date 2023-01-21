package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;

/**
 * Provide a standard interface to all Activity handlers functions.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public interface GenericActivityHandler<T extends AbstractActivity> {

    Result updateActivity(
            @NonNull ApplicationBuildStatus status,
            @NonNull T activity,
            @NonNull TaskListener listener,
            @NonNull EnvVars env,
            @NonNull FilePath workspace
    );

}
