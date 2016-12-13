package com.cerner.jwala.service.ssl.jsse;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

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
//        DO NOT throw exception: will break the Balancer Manager Service
//        Address comments in BalancerManagerHttpClient in order to implement this correctly
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
//        DO NOT throw exception: will break the Balancer Manager Service
//        Address comments in BalancerManagerHttpClient in order to implement this correctly
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
//        DO NOT throw exception: will break the Balancer Manager Service
//        Address comments in BalancerManagerHttpClient in order to implement this correctly
    }
}