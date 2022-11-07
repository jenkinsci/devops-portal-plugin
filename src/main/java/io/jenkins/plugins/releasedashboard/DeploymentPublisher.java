package io.jenkins.plugins.releasedashboard;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class DeploymentPublisher implements Serializable, Describable<DeploymentPublisher> {

    private final String parameterName;

    @DataBoundConstructor
    public DeploymentPublisher(final String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    @Override
    public DeploymentPublisher.DescriptorImpl getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DeploymentPublisher.DescriptorImpl.class);
    }

    @Symbol("deploymentpublisher")
    @Extension
    public static final class DescriptorImpl extends Descriptor<DeploymentPublisher> {

        public DescriptorImpl() {
            super(DeploymentPublisher.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.DeploymentPublisher_DisplayName();
        }

    }

}
