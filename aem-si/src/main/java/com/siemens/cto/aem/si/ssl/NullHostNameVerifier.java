package com.siemens.cto.aem.si.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }
}