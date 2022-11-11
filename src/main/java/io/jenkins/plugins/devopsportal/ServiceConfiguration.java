package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A persistent record of an application service.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class ServiceConfiguration implements Describable<ServiceConfiguration>, Serializable {

    private String id;
    private String label;
    private String category;
    private String url;
    private boolean enableMonitoring;
    private int delayMonitoringMinutes;
    private boolean acceptInvalidCertificate;

    @DataBoundConstructor
    public ServiceConfiguration(String label, String category, String url, boolean enableMonitoring,
                                int delayMonitoringMinutes, boolean acceptInvalidCertificate) {
        this.id = UUID.randomUUID().toString();
        this.label = label;
        this.category = category;
        this.url = url;
        this.enableMonitoring = enableMonitoring;
        this.delayMonitoringMinutes = delayMonitoringMinutes;
        this.acceptInvalidCertificate = acceptInvalidCertificate;
    }

    public String getId() {
        return id;
    }

    @DataBoundSetter
    public void setId(String id) {
        if (id != null && !id.isEmpty()) {
            this.id = id;
        }
    }

    public String getLabel() {
        return StringEscapeUtils.escapeJavaScript(label);
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getCategory() {
        return category;
    }

    @DataBoundSetter
    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnableMonitoring() {
        return enableMonitoring;
    }

    @DataBoundSetter
    public void setEnableMonitoring(boolean enableMonitoring) {
        this.enableMonitoring = enableMonitoring;
    }

    public int getDelayMonitoringMinutes() {
        return delayMonitoringMinutes;
    }

    @DataBoundSetter
    public void setDelayMonitoringMinutes(int delayMonitoringMinutes) {
        this.delayMonitoringMinutes = delayMonitoringMinutes;
    }

    public boolean isAcceptInvalidCertificate() {
        return acceptInvalidCertificate;
    }

    @DataBoundSetter
    public void setAcceptInvalidCertificate(boolean acceptInvalidCertificate) {
        this.acceptInvalidCertificate = acceptInvalidCertificate;
    }

    public boolean isHttps() {
        try {
            URI uri = new URI(url);
            return uri.getScheme().equalsIgnoreCase("https");
        }
        catch (URISyntaxException ex) {
            return false;
        }
    }

    public String getHostname() {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        }
        catch (URISyntaxException ex) {
            return "";
        }
    }

    @Override
    public Descriptor<ServiceConfiguration> getDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final ServiceConfiguration other = (ServiceConfiguration) that;
        return new EqualsBuilder()
                .append(label, other.label)
                .append(category, other.category)
                .append(url, other.url)
                .append(enableMonitoring, other.enableMonitoring)
                .append(delayMonitoringMinutes, other.delayMonitoringMinutes)
                .append(acceptInvalidCertificate, other.acceptInvalidCertificate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(label)
                .append(category)
                .append(url)
                .append(enableMonitoring)
                .append(delayMonitoringMinutes)
                .append(acceptInvalidCertificate)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(label)
                .append(category)
                .append(url)
                .append(enableMonitoring)
                .append(delayMonitoringMinutes)
                .append(acceptInvalidCertificate)
                .toString();
    }

    public boolean isMonitoringAvailable() {
        return enableMonitoring && !url.trim().isEmpty();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceConfiguration> implements Serializable {

        private final CopyOnWriteList<ServiceConfiguration> serviceConfigurations = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ServiceConfiguration.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ServiceConfiguration_DisplayName();
        }

        public boolean getDefaultEnableMonitoring() {
            return true;
        }

        public int getDefaultDelayMonitoringMinutes() {
            return 5;
        }

        public boolean getDefaultAcceptInvalidCertificate() {
            return false;
        }

        public List<ServiceConfiguration> getServiceConfigurations() {
            List<ServiceConfiguration> retVal = new ArrayList<>(serviceConfigurations.getView());
            retVal.sort(Comparator.comparing(ServiceConfiguration::getLabel));
            return retVal;
        }

        public void setServiceConfigurations(List<ServiceConfiguration> services) {
            serviceConfigurations.clear();
            serviceConfigurations.addAll(services);
            save();
        }

        public Optional<ServiceConfiguration> getService(String labelOrId) {
            if (labelOrId == null || labelOrId.trim().isEmpty()) {
                return Optional.empty();
            }
            return getServiceConfigurations()
                    .stream()
                    .filter(item -> item.getId().equals(labelOrId.trim()) || item.getLabel().equals(labelOrId.trim()))
                    .findFirst();
        }

        public FormValidation doCheckLabel(@QueryParameter String label, @QueryParameter String id) {
            if (label == null || label.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            ServiceConfiguration config = getService(id).orElse(null);
            if (config != null) {
                if (config.getLabel().equals(label)) {
                    return FormValidation.ok();
                }
            }
            if (getService(label).isPresent()) {
                return FormValidation.error(Messages.FormValidation_Error_UniqueValueAlreadyExists());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckDelayMonitoringMinutes(@QueryParameter int delayMonitoringMinutes) {
            if (delayMonitoringMinutes <= 0) {
                return FormValidation.error(Messages.FormValidation_Error_InvalidValue());
            }
            return FormValidation.ok();
        }

    }

}
