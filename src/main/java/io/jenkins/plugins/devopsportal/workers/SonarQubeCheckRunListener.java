package io.jenkins.plugins.devopsportal.workers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

/**
 * Listener on run completion, not used, actually.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@Extension
public class SonarQubeCheckRunListener extends RunListener<AbstractBuild<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    public ApplicationBuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @Override
    public void onCompleted(AbstractBuild build, @NonNull TaskListener listener) {
        /*Result result = build.getResult();
        String projectName = build.getProject().getDisplayName();
        int buildNumber = build.getNumber();
        if (projectName != null && buildNumber > 0 && result != null && getBuildStatusDescriptor() != null) {
            getBuildStatusDescriptor().update(projectName, buildNumber, record -> {
                System.out.println("Completed: " + projectName + " #" + buildNumber);
            });
        }*/
    }

    @Override
    public void onFinalized(AbstractBuild r) {
    }

}
