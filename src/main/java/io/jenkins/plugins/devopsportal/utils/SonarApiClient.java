package io.jenkins.plugins.devopsportal.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiConsumer;

public class SonarApiClient {

    private final String url;
    private final String authenticationToken;
    private final HttpClientBuilder builder;

    public SonarApiClient(String url, String authenticationToken, boolean acceptInvalidCertificate) {
        this.url = url;
        this.authenticationToken = Base64.getEncoder().withoutPadding().encodeToString((authenticationToken+":").getBytes());
        this.builder = HttpClients.custom();
        if (acceptInvalidCertificate) {
            try {
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, s) -> true).build();
                builder
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
            catch (GeneralSecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void getMetrics(String projectKey, String... metricKeys) {
        try {
            URIBuilder builder = new URIBuilder(url + "/api/measures/search");
            builder.setParameter("projectKeys", projectKey);
            builder.setParameter("metricKeys", String.join(",", metricKeys));
            execute(new HttpGet(builder.build()), (status, response) -> {
                System.out.println("Status:" + status);
                System.out.println("Response: " + response);
            });
        }
        catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void execute(HttpUriRequest request, BiConsumer<StatusLine, Map<String, Object>> consumer) {
        request.setHeader("Authorization", "Basic " + authenticationToken);
        try (CloseableHttpClient httpClient = builder.build()) {
            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                consumer.accept(response.getStatusLine(), null);
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> value = mapper.readValue(response.getEntity().getContent(), Map.class);
                consumer.accept(response.getStatusLine(), value);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        var client = new SonarApiClient(
                "https://registry.trustingenierie.com:9000/",
                "squ_f9206dc2876419bd70a3cbe0a64d5e3b09de9938",
                true
        );
        client.getMetrics("io.jenkins.plugins.devops-portal:1.0", "alert_status", "quality_gate_details");

    }

}
