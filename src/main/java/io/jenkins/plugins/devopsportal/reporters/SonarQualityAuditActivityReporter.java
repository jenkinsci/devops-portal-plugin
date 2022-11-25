package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.QualityAuditActivity;
import io.jenkins.plugins.devopsportal.workers.SonarQubeCheckPeriodicWork;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a QUALITY_AUDIT activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class SonarQualityAuditActivityReporter extends AbstractActivityReporter<QualityAuditActivity> {

    private String projectKey;

    @DataBoundConstructor
    public SonarQualityAuditActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getProjectKey() {
        return projectKey;
    }

    @DataBoundSetter
    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull QualityAuditActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env) {

        if (!env.containsKey("SONAR_AUTH_TOKEN") || !env.containsKey("SONAR_HOST_URL")) {
            listener.getLogger().println(Messages.SonarQualityAuditActivityReporter_Error_MissingEnvVar());
            return null;
        }

        activity.setComplete(false);

        SonarQubeCheckPeriodicWork.push(
                env.get("JOB_NAME"),
                env.get("BUILD_NUMBER"),
                projectKey,
                activity,
                env.get("SONAR_HOST_URL"),
                env.get("SONAR_AUTH_TOKEN"),
                status.getApplicationName(),
                status.getApplicationVersion(),
                getApplicationComponent()
        );

        return null;

    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.QUALITY_AUDIT;
    }

    @Symbol("reportSonarQubeAudit")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.SonarQualityAuditActivityReporter_DisplayName());
        }

    }

}
