package ssl.jsse;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Deprecated
/**
 * Ping using JSSE and HttpUrlConnection
 * <code>
 *     <bean id="webServerHttpRequestFactory" class="SslClientHttpRequestFactory">
 *       <property name="verifier" value="#{nullHostNameVerifier}"/>
 *       <property name="connectTimeout" value="${ping.jvm.connectTimeout}"/>
 *       <property name="readTimeout" value="${ping.jvm.readTimeout}"/>
 *     </bean>
 * </code>
 */
public class SslClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private HostnameVerifier verifier;
    private final SSLContext context;
    private final SSLSocketFactory socketFactory;
    
    public SslClientHttpRequestFactory() throws KeyManagementException, NoSuchAlgorithmException {
        
        context = SSLContext.getInstance("TLSv1.2");

        TrustManager[] tm = { new TrustingX509TrustManager() };
        context.init(null,  tm, new SecureRandom());

        socketFactory = context.getSocketFactory(); 
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        
        if(connection instanceof HttpsURLConnection) { 
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            httpsConnection.setHostnameVerifier(verifier);
            httpsConnection.setSSLSocketFactory(socketFactory);
        }
        
        super.prepareConnection(connection, httpMethod);
    }

    public HostnameVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(HostnameVerifier verifier) {
        this.verifier = verifier;
    }
}
