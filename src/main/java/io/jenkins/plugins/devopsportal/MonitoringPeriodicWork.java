package io.jenkins.plugins.devopsportal;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import nl.altindag.ssl.util.CertificateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * Scheduled task that performs the monitoring of application services.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@Extension
public class MonitoringPeriodicWork extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger("io.jenkins.plugins.devopsportal");

    public MonitoringPeriodicWork() {
        super("Monitoring Worker Thread");
    }

    @Override
    public long getRecurrencePeriod() {
        return MIN;
    }

    public ServiceConfiguration.DescriptorImpl getServicesDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceConfiguration.DescriptorImpl.class);
    }

    public ServiceMonitoring.DescriptorImpl getMonitoringDescriptor() {
        return Jenkins.get().getDescriptorByType(ServiceMonitoring.DescriptorImpl.class);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return;
        }
        for (ServiceConfiguration service : getServicesDescriptor().getServiceConfigurations()) {
            // Service monitoring
            if (service.isMonitoringAvailable()) {
                MonitoringStatus serviceStatus;
                String failureReason;
                try {
                    int httpStatus = executeServiceMonitoring(service.getUrl(), service.isAcceptInvalidCertificate());
                    if (HttpURLConnection.HTTP_OK == httpStatus) {
                        serviceStatus = MonitoringStatus.SUCCESS;
                        failureReason = null;
                    }
                    else {
                        serviceStatus = MonitoringStatus.FAILURE;
                        failureReason = "HTTP Status: " + httpStatus;
                    }
                }
                catch (MalformedURLException ex) {
                    serviceStatus = MonitoringStatus.INVALID_CONFIGURATION;
                    failureReason = "Invalid URL";
                }
                catch (Exception ex) {
                    serviceStatus = MonitoringStatus.FAILURE;
                    failureReason = ex.getMessage();
                }
                LOGGER.info("Monitor service: " + service.getLabel() + " (id: " + service.getId() + ", url: '"
                        + service.getUrl() + "', status: " + serviceStatus + ")");
                getMonitoringDescriptor().update(service, serviceStatus, failureReason);
            }
        }
    }

    private int executeServiceMonitoring(String urlString, boolean acceptInvalidCertificate)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        URL url = new URL(urlString);
        if (url.getProtocol().equalsIgnoreCase("https")) {
            LOGGER.info("List certificates for URL: " + urlString);
            for (Certificate cert : CertificateUtils.getCertificate(urlString).get(urlString)) {
                LOGGER.info(" -> " + cert);
            }
            LOGGER.info("URL ping: " + urlString);
            org.apache.http.conn.ssl.TrustStrategy strategy = (x509Certificates, s) -> true;
            HttpClientBuilder builder = HttpClients
                    .custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, strategy).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            try (CloseableHttpClient httpClient = builder.build()) {
                HttpUriRequest request = new HttpGet(urlString);
                HttpResponse response = httpClient.execute(request);
                return response.getStatusLine().getStatusCode();
            }
        }
        else {
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            return urlConn.getResponseCode();
        }
    }

}
