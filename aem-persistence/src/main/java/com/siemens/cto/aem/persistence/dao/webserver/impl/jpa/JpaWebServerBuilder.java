package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.domain.JpaWebServer;

public class JpaWebServerBuilder {

    private JpaWebServer webServer;

    public JpaWebServerBuilder() {
    }

    public JpaWebServerBuilder(final JpaWebServer aWebServer) {
        webServer = aWebServer;
    }

    public WebServer build() {
        return new WebServer(new Identifier<WebServer>(webServer.getId()),
                         webServer.getName(), 
                         webServer.getHost(),
                         webServer.getPort());
    }
}
