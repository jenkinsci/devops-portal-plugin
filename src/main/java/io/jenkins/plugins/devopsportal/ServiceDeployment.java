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

public class ServiceDeployment implements Describable<ServiceDeployment> {
/*
    private String serviceId;
    private String jobName;
    private String buildNumber;
    private String buildURL;
 */
    private String label;
    private String category;
    private String url;
    private boolean enableServiceMonitoring;
    private boolean enableHostMonitoring;

    @DataBoundConstructor
    public ServiceDeployment(String label, String category, String url, boolean enableServiceMonitoring,
                             boolean enableHostMonitoring) {
        this.label = label;
        this.category = category;
        this.url = url;
        this.enableServiceMonitoring = enableServiceMonitoring;
        this.enableHostMonitoring = enableHostMonitoring;
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
    public Descriptor<ServiceDeployment> getDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceDeployment.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final ServiceDeployment other = (ServiceDeployment) that;
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

    @Symbol("servicedeployment")
    @Extension
    public static final class DescriptorImpl extends Descriptor<ServiceDeployment> {

        private final CopyOnWriteList<ServiceDeployment> serviceDeployments = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(ServiceDeployment.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.DeploymentPublisher_DisplayName();
        }

    }

}
