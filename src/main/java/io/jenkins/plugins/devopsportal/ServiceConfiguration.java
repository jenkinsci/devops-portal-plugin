package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.CopyOnWriteList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ServiceConfiguration implements Describable<ServiceConfiguration> {

    private String id;
    private String label;
    private String category;
    private String url;
    private boolean enableServiceMonitoring;
    private boolean enableHostMonitoring;

    @DataBoundConstructor
    public ServiceConfiguration(String label, String category, String url, boolean enableServiceMonitoring,
                                boolean enableHostMonitoring) {
        this.id = UUID.randomUUID().toString();
        this.label = label;
        this.category = category;
        this.url = url;
        this.enableServiceMonitoring = enableServiceMonitoring;
        this.enableHostMonitoring = enableHostMonitoring;
    }

    public String getId() {
        return id;
    }

    @DataBoundSetter
    public void setId(String id) {
        this.id = id;
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

    public boolean isEnableServiceMonitoring() {
        return enableServiceMonitoring;
    }

    @DataBoundSetter
    public void setEnableServiceMonitoring(boolean enableServiceMonitoring) {
        this.enableServiceMonitoring = enableServiceMonitoring;
    }

    public boolean isEnableHostMonitoring() {
        return enableHostMonitoring;
    }

    @DataBoundSetter
    public void setEnableHostMonitoring(boolean enableHostMonitoring) {
        this.enableHostMonitoring = enableHostMonitoring;
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
                .append(enableServiceMonitoring, other.enableServiceMonitoring)
                .append(enableHostMonitoring, other.enableHostMonitoring)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(label)
                .append(category)
                .append(url)
                .append(enableServiceMonitoring)
                .append(enableHostMonitoring)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(label)
                .append(category)
                .append(url)
                .append(enableServiceMonitoring)
                .append(enableHostMonitoring)
                .toString();
    }

    public boolean isServiceMonitoringAvailable() {
        return enableServiceMonitoring && !url.trim().isEmpty();
    }

    public boolean isHostMonitoringAvailable() {
        return enableHostMonitoring && !url.trim().isEmpty();
    }

    @Symbol("serviceconfiguration")
    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceConfiguration> {

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

        public boolean getDefaultEnableServiceMonitoring() {
            return true;
        }

        public boolean getDefaultEnableHostMonitoring() {
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

    }

}
