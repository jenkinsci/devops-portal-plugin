package io.jenkins.plugins.devopsportal.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.model.Failure;
import hudson.model.RootAction;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

@Extension
public class BuildApi implements RootAction {

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
        return "build-api";
    }

    public ApplicationBuildStatus.DescriptorImpl getDescriptor() {
        return Jenkins.get().getDescriptorByType(ApplicationBuildStatus.DescriptorImpl.class);
    }

    @GET
    @WebMethod(name = "delete-build-status")
    public HttpResponse deleteBuildStatusByVersion(@QueryParameter(required = true) String application,
                                                   @QueryParameter(required = true) String version,
                                                   @QueryParameter(required = true) String origin) {
        boolean admin = Jenkins.get().hasPermission(Jenkins.ADMINISTER);
        if (admin && getDescriptor().deleteBuildStatusByApplicationVersion(application, version)) {
            return new HttpRedirect(origin);
        }
        return new Failure(Messages.FormValidation_Error_Unauthorized());
    }

}
