package io.jenkins.plugins.releasedashboard;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class HostConfiguration implements Describable<HostConfiguration> {

    private String label;
    private String hostname;
    private boolean enabled;

    @DataBoundConstructor
    public HostConfiguration(String label, String hostname, boolean enabled) {
        this.label = label;
        this.hostname = hostname;
        this.enabled = enabled;
    }

    public String getLabel() {
        return StringEscapeUtils.escapeJavaScript(label);
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getHostname() {
        return hostname;
    }

    @DataBoundSetter
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @DataBoundSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Descriptor<HostConfiguration> getDescriptor() {
        return Jenkins.get().getDescriptorByType(HostConfiguration.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final HostConfiguration other = (HostConfiguration) that;
        return new EqualsBuilder()
                .append(label, other.label)
                .append(hostname, other.hostname)
                .append(enabled, other.enabled)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(label)
                .append(hostname)
                .append(enabled)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(label)
                .append(hostname)
                .append(enabled)
                .toString();
    }

    @Symbol("hostconfiguration")
    @Extension
    public static final class DescriptorImpl extends Descriptor<HostConfiguration> {

        public DescriptorImpl() {
            super(HostConfiguration.class);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.HostConfiguration_DisplayName();
        }

        public boolean getDefaultEnabled() {
            return true;
        }

    }

}
