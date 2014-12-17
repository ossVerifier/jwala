package com.siemens.cto.aem.si.ssl.jsse;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * For use with self-signed certificates.
 * @see com.siemens.cto.aem.si.ssl.jsse.SslClientHttpRequestFactory
 */
@Deprecated
public class TrustingX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
