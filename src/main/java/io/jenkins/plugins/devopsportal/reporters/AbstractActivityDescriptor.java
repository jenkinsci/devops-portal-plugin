package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.devopsportal.Messages;
import org.kohsuke.stapler.QueryParameter;

public abstract class AbstractActivityDescriptor extends BuildStepDescriptor<Builder> {

    private final String displayName;

    protected AbstractActivityDescriptor(String displayName) {
        super();
        this.displayName = displayName;
    }

    public FormValidation doCheckApplicationName(@QueryParameter String applicationName) {
        if (applicationName.trim().isEmpty()) {
            return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckApplicationVersion(@QueryParameter String applicationVersion) {
        if (applicationVersion.trim().isEmpty()) {
            return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckApplicationComponent(@QueryParameter String applicationComponent) {
        if (applicationComponent.trim().isEmpty()) {
            return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
        }
        return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

}
