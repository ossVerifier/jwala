package com.siemens.cto.deploy.http

import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.impl.client.HttpClients

/**
 * Created by z002xuvs on 8/22/2014.
 */
public class TocContext {

    protected HttpClient httpClient;
    protected HttpContext httpContext = new BasicHttpContext();

    protected String v1BaseUrl;
    protected String username;
    protected String password;
    protected String groupId;

    public TocContext(String protocol, String host, String port, String username, String password) {
        this.username = username;
        this.password = password;

	    SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
	    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    	
	    httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        this.v1BaseUrl = "${protocol}://${host}:${port}/aem/v1.0";

    }
    public String getV1BaseUrl() {
        return this.v1BaseUrl;
    }
    public String getLoginUrl() {
        return this.v1BaseUrl + "/user/login";
    }
}
