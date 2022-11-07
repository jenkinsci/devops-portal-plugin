package io.jenkins.plugins.releasedashboard;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServiceConfiguration implements Describable<ServiceConfiguration> {

    private String label;
    private String category;
    private String url;
    private boolean enabled;

    @DataBoundConstructor
    public ServiceConfiguration(String label, String category, String url, boolean enabled) {
        this.label = label;
        this.category = category;
        this.url = url;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }

    @DataBoundSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
                .append(enabled, other.enabled)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(label)
                .append(category)
                .append(url)
                .append(enabled)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(label)
                .append(category)
                .append(url)
                .append(enabled)
                .toString();
    }

    @Symbol("serviceconfiguration")
    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceConfiguration> {

        private final CopyOnWriteList<ServiceConfiguration> serviceConfigurations = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ServiceConfiguration.class);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ServiceConfiguration_DisplayName();
        }

        public boolean getDefaultEnabled() {
            return true;
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
