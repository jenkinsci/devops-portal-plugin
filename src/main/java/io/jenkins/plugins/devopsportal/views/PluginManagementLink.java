package io.jenkins.plugins.devopsportal.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Plugin settings screen, used to define the list of application services.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@Extension
public class PluginManagementLink extends ManagementLink {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    @Override
    public String getIconFileName() {
        return "symbol-cube plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return Messages.PluginManagement_DisplayName();
    }

    @Override
    public String getUrlName() {
        return "releaseEnvironments";
    }

    @Override
    public String getDescription() {
        return Messages.PluginManagement_Description();
    }

    public Descriptor<ServiceConfiguration> getServiceConfigurationDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    @NonNull
    @Override
    public Category getCategory() {
        return Category.CONFIGURATION;
    }

    @SuppressWarnings("unused")
    public void doSaveSettings(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return;
        }
        jenkins.checkPermission(Jenkins.ADMINISTER);
        LOGGER.info("Plugin settings saved");

        // Extract list of services from request
        JSONObject request = JSONObject.fromObject(req.getParameter("json"));
        List<ServiceConfiguration> services = req.bindJSONToList(ServiceConfiguration.class, request.get("services"));

        // Sanity and uniqueness check
        List<String> distinctLabels = new ArrayList<>();
        for (ServiceConfiguration service : services) {
            service.setLabel(service.getLabel().trim());
            if (distinctLabels.contains(service.getLabel())) {
                rsp.sendError(400);
                return;
            }
            distinctLabels.add(service.getLabel());
            service.setCategory(service.getCategory().trim());
            service.setUrl(service.getUrl().trim());
        }

        // Store services
        ((ServiceConfiguration.DescriptorImpl) getServiceConfigurationDescriptor()).setServiceConfigurations(services);

        // Redirect to same page
        rsp.sendRedirect(req.getContextPath() + "/" + getUrlName());

    }

}
