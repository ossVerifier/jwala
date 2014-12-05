package com.siemens.cto.aem.si.ssl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpClientRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private HostnameVerifier verifier;
    
    DefaultHttpClient httpclient;
    
    public HttpClientRequestFactory() throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
        
        schemeRegistry.register(new Scheme("https", 443, new TrustingSSLSocketFactory()));
        schemeRegistry.register(new Scheme("http", 80, new PlainSocketFactory()));

        BasicClientConnectionManager connMgr = new BasicClientConnectionManager(schemeRegistry);
        httpclient = new DefaultHttpClient(connMgr);
        
        
        setHttpClient(httpclient);
    }
    
    public HostnameVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(HostnameVerifier verifier) {
        this.verifier = verifier;
    }
        
}
