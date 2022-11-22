package io.jenkins.plugins.devopsportal;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.plugins.devopsportal.models.MonitoringStatus;
import io.jenkins.plugins.devopsportal.models.ServiceConfiguration;
import io.jenkins.plugins.devopsportal.models.ServiceMonitoring;
import jenkins.model.Jenkins;
import nl.altindag.ssl.util.CertificateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
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
            getMonitoringDescriptor().update(service.getId(), record -> {
                // Service monitoring must be enabled
                if (!service.isMonitoringAvailable()) {
                    if (record.getCurrentMonitoringStatus() != MonitoringStatus.DISABLED) {
                        record.setCurrentMonitoringStatus(MonitoringStatus.DISABLED);
                    }
                    return;
                }
                // Update URL availability
                if (record.isAvailabilityUpdateRequired(service.getDelayMonitoringMinutes())) {
                    updateAvailabilityState(service, record);
                }
                // Update certificate expiration
                if (record.isCertificateUpdateRequired()) {
                    updateCertificateExpiration(service, record);
                }
            });
        }
    }

    private void updateAvailabilityState(ServiceConfiguration service, ServiceMonitoring record) {
        try {
            int httpStatus = getHttpResponseCode(service.getUrl(), service.isAcceptInvalidCertificate());
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                record.setCurrentMonitoringStatus(MonitoringStatus.SUCCESS);
                record.setLastSuccessTimestamp(Instant.now().getEpochSecond());
                record.setLastFailureReason(null);
                record.setFailureCount(0);
            }
            else {
                final String message = Messages.ServiceMonitoring_Error_InvalidHttpResponse()
                    .replace("%status%", "" + httpStatus);
                record.setFailure(MonitoringStatus.FAILURE, message);
            }
        }
        catch (MalformedURLException ex) {
            final String message = Messages.ServiceMonitoring_Error_InvalidConfigurationURL()
                .replace("%url%", service.getUrl());
            record.setFailure(MonitoringStatus.INVALID_CONFIGURATION, message);
        }
        catch (SSLHandshakeException ex) {
            final String message = Messages.ServiceMonitoring_Error_InvalidHttpsConfiguration()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage());
            record.setFailure(MonitoringStatus.INVALID_HTTPS, message);
        }
        catch (Exception ex) {
            final String message = Messages.ServiceMonitoring_Error_OtherException()
                    .replace("%exception%", ex.getClass().getSimpleName())
                    .replace("%message%", ex.getMessage());
            record.setFailure(MonitoringStatus.FAILURE, message);
        }
    }

    private void updateCertificateExpiration(ServiceConfiguration service, ServiceMonitoring record) {
        try {
            URL url = new URL(service.getUrl());
            if (url.getProtocol().equalsIgnoreCase("https")) {
                record.setLastCertificateCheckTimestamp(Instant.now().getEpochSecond());
                Date lastExpiration = null;
                for (X509Certificate cert : CertificateUtils.getCertificate(service.getUrl()).get(service.getUrl())) {
                    LOGGER.fine(url.getHost() + " > " + cert.getSubjectX500Principal() + " > " + cert.getNotAfter());
                    if (lastExpiration == null || cert.getNotAfter().before(lastExpiration)) {
                        lastExpiration = cert.getNotAfter();
                    }
                }
                if (lastExpiration != null) {
                    record.setCertificateExpiration(lastExpiration.getTime());
                }
            }
        }
        catch (Throwable ex) {
            LOGGER.warning("Unable to fetch certificates for: " + service.getUrl());
        }
    }

    private int getHttpResponseCode(String urlString, boolean acceptInvalidCertificate)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        URL url = new URL(urlString);
        // HTTP
        if (url.getProtocol().equalsIgnoreCase("http")) {
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            return urlConn.getResponseCode();
        }
        // Unsafe HTTPS
        else if (acceptInvalidCertificate) {
            HttpClientBuilder builder = HttpClients
                    .custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(
                            null, (x509Certificates, s) -> true).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            try (CloseableHttpClient httpClient = builder.build()) {
                HttpUriRequest request = new HttpGet(urlString);
                HttpResponse response = httpClient.execute(request);
                return response.getStatusLine().getStatusCode();
            }
        }
        // Safe HTTPS
        else {
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.connect();
            return urlConn.getResponseCode();
        }
    }

}
