package com.cerner.jwala.service.webserver.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
public class ClientFactoryHelper {

    @Autowired
    @Qualifier("httpRequestFactory")
    private HttpComponentsClientHttpRequestFactory httpClientFactory;

    public ClientHttpResponse requestGet(URI statusUri) throws IOException {
        ClientHttpRequest request = httpClientFactory.createRequest(statusUri, HttpMethod.GET);
        return request.execute();
    }

}
