package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.PerformanceTestActivity;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import io.jenkins.plugins.devopsportal.utils.PerformanceTestResult;
import io.jenkins.plugins.devopsportal.utils.RemoteFileJMeterParser;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build step of a project used to record a PERFORMANCE_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class JMeterPerformanceTestActivityReporter extends AbstractActivityReporter<PerformanceTestActivity> {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    private String jmeterReportPath;

    @DataBoundConstructor
    public JMeterPerformanceTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getJmeterReportPath() {
        return jmeterReportPath;
    }

    @DataBoundSetter
    public void setJmeterReportPath(String jmeterReportPath) {
        this.jmeterReportPath = jmeterReportPath;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull PerformanceTestActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env, @NonNull FilePath workspace) {

        activity.setTestCount(0);
        activity.setSampleCount(0);
        activity.setErrorCount(0);

        PerformanceTestResult result;
        try {
            if (workspace.isRemote()) {
                result = parseFilesFromRemoteWorkspace(new FilePath(workspace, jmeterReportPath));
            }
            else {
                result = parseFilesFromLocalWorkspace(env);
            }
        }
        catch (InterruptedException ex) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            return null;
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.JMeterPerformanceTestActivityReporter_Error_XmlParserError()
                    .replace("%exception%", ex.getClass().getName())
                    .replace("%message%", ex.getMessage()));
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Error reading performance report file: " + jmeterReportPath, ex);
            }
            return Result.FAILURE;
        }

        if (result == null) {
            listener.getLogger().println("No performance report '" + jmeterReportPath
                    + "' found. Configuration error?");
            return Result.UNSTABLE;
        }

        activity.setTestCount(result.getTestCount());
        activity.setSampleCount(result.getSampleCount());
        activity.setErrorCount(result.getErrorCount());

        return PerformanceTestActivityReporter.handleActivityResult(activity);
    }

    private PerformanceTestResult parseFilesFromLocalWorkspace(EnvVars env) throws Exception {
        final File file = MiscUtils.checkFilePathIllegalAccess(
                env.get("WORKSPACE", null),
                jmeterReportPath
        );
        if (file == null) {
            return null;
        }
        return RemoteFileJMeterParser.parse(file);
    }

    private PerformanceTestResult parseFilesFromRemoteWorkspace(FilePath target)
            throws IOException, InterruptedException {
        return target.act(new RemoteFileJMeterParser());
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.PERFORMANCE_TEST;
    }

    @Symbol("reportJMeterPerformanceTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.JMeterPerformanceTestActivityReporter_DisplayName());
        }

    }

}
