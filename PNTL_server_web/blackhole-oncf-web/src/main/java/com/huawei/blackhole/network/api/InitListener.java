package com.huawei.blackhole.network.api;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.net.Socket;
// VERY IMPORTANT. SOME OF THESE EXIST IN MORE THAN ONE PACKAGE!
import java.security.cert.X509Certificate;
import javax.net.ssl.X509ExtendedTrustManager;
import java.security.cert.CertificateException;

public class InitListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        HttpsURLConnection.setDefaultSSLSocketFactory(createSSLContext().getSocketFactory());
    }

    private SSLContext createSSLContext() {
        try {
            TrustManager[] wrapped = new TrustManager[1];
            wrapped[0] = new X509ExtendedTrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
                        throws CertificateException {
                    // 检查客户端认证内容
                }

                @Override
                public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
                        throws CertificateException {
                    // 检查服务端认证内容
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
                        Socket paramSocket) throws CertificateException {
                    // 检查客户端认证内容
                }

                @Override
                public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
                        Socket paramSocket) throws CertificateException {
                    // 检查服务端认证内容
                }

                @Override
                public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
                        SSLEngine paramSSLEngine) throws CertificateException {
                    // 检查客户端认证内容
                }

                @Override
                public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
                        SSLEngine paramSSLEngine) throws CertificateException {
                    // 检查服务端认证内容
                }
            };
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, wrapped, new SecureRandom());
            return context;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("NoSuchAlgorithmException." + e.getMessage());
        } catch (KeyManagementException e) {
            throw new IllegalArgumentException("KeyManagementException." + e.getMessage());
        }
    }
}
