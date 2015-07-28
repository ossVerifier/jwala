package com.siemens.cto.aem.service.webserver.component;

import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
public class ClientFactoryHelper {

    @Autowired
    @Qualifier("webServerHttpRequestFactory")
    private HttpClientRequestFactory httpClientFactory;

    public ClientHttpResponse requestGet(URI statusUri) throws IOException {
        ClientHttpRequest request = httpClientFactory.createRequest(statusUri, HttpMethod.GET);
        return request.execute();
    }
}
