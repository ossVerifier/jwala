package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupBuilder;

public class JpaWebServerBuilder {

	private JpaWebServer webServer;

	public JpaWebServerBuilder() {
	}

	public JpaWebServerBuilder(final JpaWebServer aWebServer) {
		webServer = aWebServer;
	}

	public WebServer build() {
	    List<JpaGroup> jpaGroups = webServer.getGroups();
	    List<Group> groups;
	    if(jpaGroups != null) {
	        groups = new ArrayList<>( webServer.getGroups().size());
    	    for(JpaGroup gid : webServer.getGroups()) {
    	        groups.add(new JpaGroupBuilder(gid).build());
    	    }
	    } else {
	        groups = Collections.<Group>emptyList();
	    }
        return new WebServer(
        				new Identifier<WebServer>(webServer.getId()),
        				groups,
                        webServer.getName(),
                        webServer.getHost(),
                        webServer.getPort(), webServer.getHttpsPort());
    }
}
