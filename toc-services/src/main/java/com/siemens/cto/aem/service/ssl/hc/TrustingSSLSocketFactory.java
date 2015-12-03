package com.siemens.cto.aem.service.ssl.hc;

import com.siemens.cto.aem.service.ssl.jsse.NullHostNameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Provides SSL Sockets to HTTP Client that do not do verification
 */
public class TrustingSSLSocketFactory extends SSLSocketFactory {

    public TrustingSSLSocketFactory() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        super(new TrustStrategy() {
            
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }, new NullHostNameVerifier());
    }
    
}
