package com.siemens.cto.aem.service.ssl.hc;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.NullBackoffStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.HostnameVerifier;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Use a pooling connection Manager for HTTP and HTTPS ping connections
 * <code>
 *   <bean id="webServerHttpRequestFactory" class="HttpClientRequestFactory">
 *     <property name="connectTimeout" value="${ping.jvm.connectTimeout}"/>
 *     <property name="readTimeout" value="${ping.jvm.readTimeout}"/>
 *     <property name="periodMillis" value="${ping.jvm.period.millis}"/>
 *     <property name="maxHttpConnections" value="${ping.jvm.maxHttpConnections}" />        
 *   </bean>
 * </code>
 */
public class HttpClientRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private HostnameVerifier verifier;    
    private DefaultHttpClient httpclient;   
    private long periodMillis = 0;
    private final PoolingClientConnectionManager poolMgr; 
    
    public HttpClientRequestFactory() throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
        
        schemeRegistry.register(new Scheme("https", 443, new TrustingSSLSocketFactory()));
        schemeRegistry.register(new Scheme("http", 80, new PlainSocketFactory()));

        poolMgr = new PoolingClientConnectionManager(schemeRegistry);
        
        httpclient = new DefaultHttpClient(poolMgr);
                
        httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                return periodMillis * 2; 
            }
        });
        
        httpclient.setReuseStrategy(new DefaultConnectionReuseStrategy());        
        httpclient.setConnectionBackoffStrategy(new NullBackoffStrategy());
        
        setHttpClient(httpclient);        
    }
    
    public HostnameVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(HostnameVerifier verifier) {
        this.verifier = verifier;
    }
    
    public void setPeriodMillis(final long millis) { 
        periodMillis = millis;
    }
    
    public void setMaxHttpConnections(final int count) { 
        poolMgr.setMaxTotal(count);
    }
        
}
