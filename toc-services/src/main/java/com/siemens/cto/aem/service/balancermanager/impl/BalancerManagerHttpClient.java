package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.service.ssl.jsse.NullHostNameVerifier;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

//TODO: Discuss with team to use local trust store to verify the hostname, but in our local trust store,
//TODO: it has fully qualify domain name, our hostname do not have domain name, and it will not match
public class BalancerManagerHttpClient {

    public CloseableHttpResponse doHttpClientPost(final String uri, final List<NameValuePair> nvps) throws KeyManagementException, IOException, NoSuchAlgorithmException {
        SSLContext sslContext;
        CloseableHttpClient httpclient;
        HttpPost httppost = new HttpPost(uri);
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, new SecureRandom());
        httpclient = HttpClients.custom().setSslcontext(sslContext).setHostnameVerifier(new NullHostNameVerifier()).build();
        httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        return httpclient.execute(httppost);
    }
}
