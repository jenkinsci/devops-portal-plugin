package io.jenkins.plugins.devopsportal.utils;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Let parse result of a JMeter run from a remote file.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class RemoteFileJMeterParser extends MasterToSlaveFileCallable<PerformanceTestResult> implements Serializable {

    @Override
    public PerformanceTestResult invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
        if (file.exists()) {
            try {
                return parse(file);
            }
            catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        return null;
    }

    public static PerformanceTestResult parse(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        NodeList list = document.getElementsByTagName("api");
        // Document model 1
        if (list.getLength() > 0) {
            return parse1(list);
        }
        else {
            list = document.getElementsByTagName("httpSample");
            if (list.getLength() > 0) {
                return parse2(list);
            }
        }
        return null;
    }

    private static PerformanceTestResult parse1(NodeList list) {
        PerformanceTestResult result = new PerformanceTestResult();
        result.setTestCount(list.getLength());
        for (int i = 0, l = list.getLength(); i < l; i++) {
            Node node = list.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            result.addSampleCount(Integer.parseInt(element.getElementsByTagName("samples").item(0).getTextContent()));
            result.addErrorCount(Float.parseFloat(element.getElementsByTagName("errors").item(0).getTextContent()));
        }
        return result;
    }

    private static PerformanceTestResult parse2(NodeList list) {
        PerformanceTestResult result = new PerformanceTestResult();
        Set<String> tests = new HashSet<>();
        for (int i = 0, l = list.getLength(); i < l; i++) {
            Node node = list.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            String errorCount = element.getAttribute("ec");
            if (!errorCount.isEmpty()) {
                result.addErrorCount(Integer.parseInt(errorCount));
            }
            else {
                result.addErrorCount("true".equals(element.getAttribute("s")) ? 0 : 1);
            }
            String sampleCount = element.getAttribute("sc");
            if (!sampleCount.isEmpty()) {
                result.addSampleCount(Integer.parseInt(sampleCount));
            }
            else {
                result.addSampleCount(1);
            }
            tests.add(element.getAttribute("lb"));
        }
        result.setTestCount(tests.size());
        return result;
    }

}
