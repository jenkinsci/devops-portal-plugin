package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.UnitTestActivity;
import io.jenkins.plugins.devopsportal.utils.RemoteFileSurefireParser;
import io.jenkins.plugins.devopsportal.utils.TestSuiteResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.*;

/**
 * Build step of a project used to record a UNIT_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class SurefireUnitTestActivityReporter extends AbstractActivityReporter<UnitTestActivity> {

    private String surefireReportPath;

    @DataBoundConstructor
    public SurefireUnitTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getSurefireReportPath() {
        return surefireReportPath;
    }

    @DataBoundSetter
    public void setSurefireReportPath(String surefireReportPath) {
        this.surefireReportPath = surefireReportPath;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull UnitTestActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env, @NonNull FilePath workspace) {

        activity.resetCounters();
        TestSuiteResult result = null;
        try {
            if (workspace.isRemote()) {
                result = parseFilesFromRemoteWorkspace(workspace, surefireReportPath);
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
            listener.getLogger().println("Error, unable to parse test files: " + ex.getClass().getSimpleName()
                    + " - " + ex.getMessage());
            ex.printStackTrace(listener.getLogger());
            return Result.FAILURE;
        }
        if (result.files.isEmpty()) {
            listener.getLogger().println("No test reports that matches '" + surefireReportPath
                    + "' found. Configuration error?");
        }
        else {
            listener.getLogger().println(result.files.size() + " test reports where collected:");
            listener.getLogger().println(String.join(", ", result.files));
            activity.setTestsPassed(result.testsPassed);
            activity.setTestsIgnored(result.testsIgnored);
            activity.setTestsFailed(result.testsFailed);

        }
        activity.updateScore();
        return null;
    }

    private TestSuiteResult parseFilesFromRemoteWorkspace(FilePath workspace, String path)
            throws IOException, InterruptedException {
        return workspace.act(new RemoteFileSurefireParser(path));
    }

    private TestSuiteResult parseFilesFromLocalWorkspace(EnvVars env) throws IOException {
        File workspace = new File(env.get("WORKSPACE", ""));
        FileSet fs = Util.createFileSet(
                workspace,
                surefireReportPath,
                null
        );
        DirectoryScanner ds;
        try {
            ds = fs.getDirectoryScanner(new Project());
        }
        catch (BuildException ex) {
            throw new IOException(ex.getMessage());
        }
        TestSuiteResult result = new TestSuiteResult();
        for (String str : ds.getIncludedFiles()) {
            RemoteFileSurefireParser.parse(new File(workspace, str), result);
        }
        return result;
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.UNIT_TEST;
    }

    @Symbol("reportSurefireTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.SurefireUnitTestActivityReporter_DisplayName());
        }

    }

}
