package com.siemens.cto.aem.si.ssl.jsse;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * Do not do host name verification for ping
 */
public class NullHostNameVerifier implements HostnameVerifier, X509HostnameVerifier {

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
    }
}