package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import java.util.ArrayList;
import java.util.List;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupBuilder;
import com.siemens.cto.aem.persistence.domain.JpaGroup;
import com.siemens.cto.aem.persistence.domain.JpaWebServer;

public class JpaWebServerBuilder {

	private JpaWebServer webServer;

	public JpaWebServerBuilder() {
	}

	public JpaWebServerBuilder(final JpaWebServer aWebServer) {
		webServer = aWebServer;
	}

	public WebServer build() {
	    List<Group> groups = new ArrayList<>( webServer.getGroups().size());
	    for(JpaGroup gid : webServer.getGroups()) {
	        groups.add(new JpaGroupBuilder(gid).build());
	    }
        return new WebServer(
        				new Identifier<WebServer>(webServer.getId()),
        				groups,
                        webServer.getName(), 
                        webServer.getHost(),
                        webServer.getPort());
    }
}
