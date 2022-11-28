package io.jenkins.plugins.devopsportal.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.model.Failure;
import hudson.model.RootAction;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.DeploymentOperation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

@Extension
public class RunApi implements RootAction {

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "run-api";
    }

    public DeploymentOperation.DescriptorImpl getDescriptor() {
        return Jenkins.get().getDescriptorByType(DeploymentOperation.DescriptorImpl.class);
    }

    @GET
    @WebMethod(name = "delete-operation")
    public HttpResponse deleteRunOperation(@QueryParameter(required = true) String environment,
                                           @QueryParameter(required = true) String job,
                                           @QueryParameter(required = true) String number,
                                           @QueryParameter(required = true) String origin) {
        boolean admin = Jenkins.get().hasPermission(Jenkins.ADMINISTER);
        if (admin && getDescriptor().deleteDeploymentByRun(environment, job, number)) {
            return new HttpRedirect(origin);
        }
        return new Failure(Messages.FormValidation_Error_Unauthorized());
    }

}
