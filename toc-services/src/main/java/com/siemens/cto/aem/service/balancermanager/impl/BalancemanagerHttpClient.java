package com.siemens.cto.aem.service.balancermanager.impl;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BalancemanagerHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancemanagerHttpClient.class);

    public int doHttpClientPost(final String uri, final Map<String, String> map) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());
        } catch (KeyManagementException e) {
            LOGGER.error(e.getMessage(), e);
        }
        X509HostnameVerifier verifier = new AbstractVerifier() {
            @Override
            public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
            }

        };
        CloseableHttpClient httpclient;
        HttpPost httppost = new HttpPost(uri);
        CloseableHttpResponse res;
        int returnCode = 0;
        try {
            httpclient = HttpClients.custom().setSslcontext(sslContext).setHostnameVerifier(verifier).build();
            List<NameValuePair> nvps = new ArrayList<>();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                nvps.add(new BasicNameValuePair(pair.getKey().toString(), pair.getValue().toString()));
                System.out.println(pair.getKey().toString() + " " + pair.getValue().toString());
                it.remove();
            }
            httppost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            res = httpclient.execute(httppost);
            returnCode = res.getStatusLine().getStatusCode();
            res.close();
        } catch (ClientProtocolException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return returnCode;
    }

}
