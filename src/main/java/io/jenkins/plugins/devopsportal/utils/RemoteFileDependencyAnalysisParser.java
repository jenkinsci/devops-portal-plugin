package io.jenkins.plugins.devopsportal.utils;

import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.devopsportal.models.DependencyVulnerability;
import io.jenkins.plugins.devopsportal.models.VulnerabilityAnalysisResult;
import jenkins.MasterToSlaveFileCallable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Let parse dependency analysis report on remote workspace.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RemoteFileDependencyAnalysisParser extends MasterToSlaveFileCallable<VulnerabilityAnalysisResult> implements Serializable {

    public RemoteFileDependencyAnalysisParser() {
    }

    @Override
    public VulnerabilityAnalysisResult invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
        if (!file.exists()) {
            return null;
        }
        try {
            VulnerabilityAnalysisResult result = new VulnerabilityAnalysisResult();
            parse(file, result);
            return result;
        }
        catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static void parse(File file, VulnerabilityAnalysisResult result) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList dependencies = (NodeList) xPath.compile("/analysis/dependencies/dependency").evaluate(document, XPathConstants.NODESET);
        for (int i = 0, l = dependencies.getLength(); i < l; i++) {
            NodeList vulnerabilities = (NodeList) xPath.compile("vulnerabilities/vulnerability").evaluate(dependencies.item(i), XPathConstants.NODESET);
            if (vulnerabilities.getLength() < 1) {
                continue;
            }
            String dependency = (String) xPath.compile("fileName").evaluate(dependencies.item(i), XPathConstants.STRING);
            String project = (String) xPath.compile("projectReferences/projectReference").evaluate(dependencies.item(i), XPathConstants.STRING);
            List<DependencyVulnerability> items = result.add(project + "::::" + dependency);
            for (int j = 0, k = vulnerabilities.getLength(); j < k; j++) {
                String name = (String) xPath.compile("name").evaluate(vulnerabilities.item(j), XPathConstants.STRING);
                String severity = (String) xPath.compile("severity").evaluate(vulnerabilities.item(j), XPathConstants.STRING);
                List<String> tags = new ArrayList<>();
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/attackVector", value -> true, "{{value}}", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/userInteraction", value -> !"NONE".equals(value), "userinteraction", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/privilegesRequired", value -> !"NONE".equals(value), "rootprivilege", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/attackComplexity", "LOW"::equals, "easycomplexity", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/attackComplexity", "HIGH"::equals, "hard", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/confidentialityImpact", "HIGH"::equals, "confidentiality", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/integrityImpact", "HIGH"::equals, "integrity", tags);
                addTagIf(xPath, vulnerabilities.item(j), "cvssV3/availabilityImpact", "HIGH"::equals, "availability", tags);
                items.add(new DependencyVulnerability(
                        name,
                        severity,
                        tags
                ));
            }
        }
    }

    private static void addTagIf(XPath xPath, Node node, String path, Function<String, Boolean> filter, String tag,
                                 List<String> list) throws XPathExpressionException {
        String value = (String) xPath.compile(path).evaluate(node, XPathConstants.STRING);
        if (value != null && !value.isEmpty() && filter.apply(value)) {
            list.add(tag.replace("{{value}}", value).toLowerCase());
        }
    }

}
