package com.cerner.jwala.ws.rest.v1.provider;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.webserver.WebServer;

import javax.ws.rs.QueryParam;
import java.util.HashSet;
import java.util.Set;

public class WebServerIdsParameterProvider extends AbstractIdsParameterProvider<WebServer> {

    @QueryParam("webServerId")
    private Set<String> webServerIds;

    public WebServerIdsParameterProvider(final Set<String> someWebServerIds) {
        this();
        webServerIds = new HashSet<>(someWebServerIds);
    }

    public WebServerIdsParameterProvider() {
        super("Invalid WebServer Identifier specified");
    }

    @Override
    protected Set<String> getIds() {
        return webServerIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("webServerIds", webServerIds)
                .toString();
    }
}
