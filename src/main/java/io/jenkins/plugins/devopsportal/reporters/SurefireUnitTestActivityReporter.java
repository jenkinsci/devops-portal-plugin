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
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import io.jenkins.plugins.devopsportal.utils.RemoteFileSurefireParser;
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
        int files = 0;
        try {
            if (workspace.isRemote()) {
                files = parseFilesFromRemoteWorkspace(activity, workspace, surefireReportPath);
            }
            else {
                files = parseFilesFromLocalWorkspace(activity, env);
            }
        }
        catch (Exception ex) {
            listener.getLogger().println("Error, unable to parse test files: " + ex.getClass().getSimpleName()
                    + " - " + ex.getMessage());
            ex.printStackTrace(listener.getLogger());
            return Result.FAILURE;
        }
        if (files < 1) {
            listener.getLogger().println("No test reports that matches '" + surefireReportPath
                    + "' found. Configuration error?");
        }
        return null;
    }

    private int parseFilesFromRemoteWorkspace(UnitTestActivity activity, FilePath workspace, String path)
            throws IOException, InterruptedException {
        return workspace.act(new RemoteFileSurefireParser(activity, path));
    }

    private int parseFilesFromLocalWorkspace(UnitTestActivity activity, EnvVars env) throws IOException {
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
        int i = 0;
        for (String str : ds.getIncludedFiles()) {
            i++;
            RemoteFileSurefireParser.parse(new File(workspace, str), activity);
        }
        return i;
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
