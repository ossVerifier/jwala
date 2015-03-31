package com.siemens.cto.deploy.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.conn.ssl.*
import org.apache.http.impl.client.HttpClients

/**
 * Created by z002xuvs on 8/22/2014.
 */
public class TocContext {

    protected HttpClient httpClient;
    protected HttpContext httpContext = new BasicHttpContext();

    protected Urls urls;
    protected String username;
    protected String password;
    protected String groupId;

    public TocContext(String protocol, String host, String port, String username, String password) {
        urls = new Urls(protocol, host, port);
        this.username = username;
        this.password = password;

	SSLContextBuilder builder = new SSLContextBuilder();
	builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
	SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    	
	httpClient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

    }
}
