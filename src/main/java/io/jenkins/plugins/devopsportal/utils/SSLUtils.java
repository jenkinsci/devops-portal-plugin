package io.jenkins.plugins.devopsportal.utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLUtils {

    public static SSLContext getSSLContext(X509TrustManager manager) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(
                    null,
                    new X509TrustManager[]{manager},
                    new SecureRandom()
            );
            return context;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static X509TrustManager getUntrustedManager() {
        return new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

        };
    }

}