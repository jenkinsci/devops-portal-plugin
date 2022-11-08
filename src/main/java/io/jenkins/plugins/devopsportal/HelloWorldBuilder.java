package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private String applicationName;
    private String applicationVersion;
    private BuildActivities activity;
    private BuildActivityStatus status;

    @DataBoundConstructor
    public HelloWorldBuilder(String applicationName, String applicationVersion, BuildActivities activity) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.activity = activity;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @DataBoundSetter
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public BuildActivities getActivity() {
        return activity;
    }

    @DataBoundSetter
    public void setActivity(BuildActivities activity) {
        this.activity = activity;
    }

    public BuildActivityStatus getStatus() {
        return status;
    }

    @DataBoundSetter
    public void setStatus(BuildActivityStatus status) {
        this.status = status;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("OK TODO");
    }

    @Symbol("buildActivity")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String applicationName,
                                          @QueryParameter boolean applicationVersion, @QueryParameter boolean activity)
                throws IOException, ServletException {
            // FormValidation.error
            // FormValidation.warning
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }


        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildPublisher_DisplayName();
        }

    }

}
