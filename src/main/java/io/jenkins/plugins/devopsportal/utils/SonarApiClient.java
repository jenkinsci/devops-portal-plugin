package io.jenkins.plugins.devopsportal.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Consumer;

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

    public List<Map<String, Object>> getIssues(String projectKey) {
        return execute("/api/issues/search", "issues", request -> {
            request.setParameter("componentKeys", projectKey);
            request.setParameter("ps", "500");
            request.setParameter("resolved", "no");
            request.setParameter("severities", "MAJOR,CRITICAL,BLOCKER");
            request.setParameter("statuses", "OPEN");
            request.setParameter("types", "BUG,VULNERABILITY,CODE_SMELL");
        });
    }

    public List<Map<String, Object>> getMetrics(String projectKey) {
        return getMetrics(
                projectKey,
                // Quality Gate
        "alert_status",
                // Scores
                "sqale_rating", // Maintainability (code smells)
                "reliability_rating", // Reliability (bugs)
                "security_rating", // Security (vulnerabilities)
                "security_review_rating", // Security review (hotspots)
                // Metrics
                "coverage", // Test coverage
                "duplicated_lines_density", // Duplication
                "ncloc" // Lines of code
        );
    }

    public List<Map<String, Object>> getMetrics(String projectKey, String... metricKeys) {
        return execute("/api/measures/search", "measures", request -> {
            request.setParameter("projectKeys", projectKey);
            request.setParameter("metricKeys", String.join(",", metricKeys));
        });
    }

    public List<Map<String, Object>> getHotspots(String projectKey) {
        return execute("/api/hotspots/search", "hotspots", request -> {
            request.setParameter("projectKey", projectKey);
            //request.setParameter("status", "TO_REVIEW");
            request.setParameter("ps", "500");
        });
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> execute(String path, String listAttribute, Consumer<URIBuilder> consumer) {
        HttpGet request;
        try {
            URIBuilder builder = new URIBuilder(url + path);
            consumer.accept(builder);
            request = new HttpGet(builder.build());
        }
        catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        request.setHeader("Authorization", "Basic " + authenticationToken);
        try (CloseableHttpClient httpClient = this.builder.build()) {
            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                Map<?, ?> map = mapper.readValue(response.getEntity().getContent(), Map.class);
                if (map.containsKey(listAttribute)) {
                    return (List<Map<String, Object>>) map.get(listAttribute);
                }
                return Collections.emptyList();
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
        debug("metrics", client.getMetrics("io.jenkins.plugins.devops-portal:1.0"));
        debug("issues", client.getIssues("io.jenkins.plugins.devops-portal:1.0"));
        debug("hotspots", client.getHotspots("io.jenkins.plugins.devops-portal:1.0"));
    }

    private static void debug(String data, List<Map<String, Object>> values) {
        System.out.println(" " + data +" >>> " + values.size());
        System.out.println(values);
        switch (data) {
            case "metrics":
                for (Map<String, Object> metric : values) {
                    System.out.println(" - " + metric.get("metric") + " = " + metric.get("value"));
                }
                break;
            case "issues":
                for (Map<String, Object> metric : values) {
                    System.out.println(
                            " - type="+ metric.get("type")
                                    + " rule=" + metric.get("rule")
                                    + " severity=" + metric.get("severity")
                                    + " message='" + metric.get("message")
                                    + "' cdate=" + metric.get("creationDate")
                                    + " file=" + metric.get("component")
                                    + " line=" + metric.get("line"));
                }
                break;
            case "hotspots":
                for (Map<String, Object> metric : values) {
                    System.out.println(
                            " - category="+ metric.get("securityCategory")
                                    + " probability=" + metric.get("probability")
                                    + " message='" + metric.get("message")
                                    + "' cdate=" + metric.get("creationDate")
                                    + " file=" + metric.get("component")
                                    + " line=" + metric.get("line"));
                }
                break;
        }
    }

}
