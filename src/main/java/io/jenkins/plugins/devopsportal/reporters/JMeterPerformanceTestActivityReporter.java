package io.jenkins.plugins.devopsportal.reporters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.models.ActivityCategory;
import io.jenkins.plugins.devopsportal.models.ApplicationBuildStatus;
import io.jenkins.plugins.devopsportal.models.PerformanceTestActivity;
import io.jenkins.plugins.devopsportal.utils.MiscUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Build step of a project used to record a PERFORMANCE_TEST activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class JMeterPerformanceTestActivityReporter extends AbstractActivityReporter<PerformanceTestActivity> {

    private String jmeterReportPath;

    @DataBoundConstructor
    public JMeterPerformanceTestActivityReporter(String applicationName, String applicationVersion, String applicationComponent) {
        super(applicationName, applicationVersion, applicationComponent);
    }

    public String getJmeterReportPath() {
        return jmeterReportPath;
    }

    @DataBoundSetter
    public void setJmeterReportPath(String jmeterReportPath) {
        this.jmeterReportPath = jmeterReportPath;
    }

    @Override
    public Result updateActivity(@NonNull ApplicationBuildStatus status, @NonNull PerformanceTestActivity activity,
                                 @NonNull TaskListener listener, @NonNull EnvVars env) {

        final File file = MiscUtils.checkFilePathIllegalAccess(
                env.get("WORKSPACE", ""), jmeterReportPath);

        if (file == null || !file.exists() || !file.canRead()) {
            listener.getLogger().println(Messages.FormValidation_Error_FileNotReadable()
                    .replace("%file%", jmeterReportPath));
            return Result.FAILURE;
        }

        activity.setTestCount(0);
        activity.setSampleCount(0);
        activity.setErrorCount(0);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            NodeList list = document.getElementsByTagName("api");
            int samples = 0;
            float errors = 0;
            for (int i = 0, l = list.getLength(); i < l; i++) {
                Node node = list.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                samples += Integer.parseInt(element.getElementsByTagName("samples").item(0).getTextContent());
                errors += Float.parseFloat(element.getElementsByTagName("errors").item(0).getTextContent());
            }
            activity.setTestCount(list.getLength());
            activity.setSampleCount(samples);
            activity.setErrorCount((long) errors);
        }
        catch (Exception ex) {
            listener.getLogger().println(Messages.JMeterPerformanceTestActivityReporter_Error_XmlParserError()
                    .replace("%exception%", ex.getClass().getName())
                    .replace("%message%", ex.getMessage()));
            return Result.FAILURE;
        }

        return PerformanceTestActivityReporter.handleActivityResult(activity);
    }

    @Override
    public ActivityCategory getActivityCategory() {
        return ActivityCategory.PERFORMANCE_TEST;
    }

    @Symbol("reportJMeterPerformanceTest")
    @Extension
    public static final class DescriptorImpl extends AbstractActivityDescriptor {

        public DescriptorImpl() {
            super(Messages.JMeterPerformanceTestActivityReporter_DisplayName());
        }

    }

}
