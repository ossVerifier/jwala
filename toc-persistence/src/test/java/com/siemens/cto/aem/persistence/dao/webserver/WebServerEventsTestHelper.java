package com.siemens.cto.aem.persistence.dao.webserver;

import com.siemens.cto.aem.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

import java.util.Collection;

public class WebServerEventsTestHelper {

	public static Event<CreateWebServerRequest> createCreateWebServerEvent(final Collection<Identifier<Group>> someGroupIds,
                                                                           final String aNewWebServerName,
                                                                           final String aNewWebServerHost,
                                                                           final Integer aNewWebServerPort,
                                                                           final Integer aNewWebServerHttpPort,
                                                                           final String aUserId,
                                                                           final Path aStatusPath,
                                                                           final FileSystemPath aFileSystemPath,
                                                                           final Path aSvrRoot,
                                                                           final Path aDocRoot) {

		final Event<CreateWebServerRequest> createWebServer = new Event<>(
				new CreateWebServerRequest(someGroupIds,
                                           aNewWebServerName,
                                           aNewWebServerHost,
                                           aNewWebServerPort,
                                           aNewWebServerHttpPort,
                                           aStatusPath,
                                           aFileSystemPath,
                                           aSvrRoot,
                                           aDocRoot),
				createAuditEvent(aUserId));

		return createWebServer;
	}

    public static Event<UpdateWebServerRequest> createUpdateWebServerEvent(final Identifier<WebServer> id,
                                                                           final Collection<Identifier<Group>> newGroupIds,
                                                                           final String aNewWebServerName,
                                                                           final String aNewWebServerHost,
                                                                           final Integer aNewWebServerPort,
                                                                           final Integer aNewWebServerHttpsPort,
                                                                           final String aUserId,
                                                                           final Path aStatusPath,
                                                                           final FileSystemPath aHttpConfigFile,
                                                                           final Path aSvrRoot,
                                                                           final Path aDocRoot) {

		final Event<UpdateWebServerRequest> updateWebServer = new Event<>(new UpdateWebServerRequest(id,
                                                                                                     newGroupIds,
                                                                                                     aNewWebServerName,
                                                                                                     aNewWebServerHost,
                                                                                                     aNewWebServerPort,
                                                                                                     aNewWebServerHttpsPort,
                                                                                                     aStatusPath,
                                                                                                     aHttpConfigFile,
                                                                                                     aSvrRoot,
                                                                                                     aDocRoot),
                                                                          createAuditEvent(aUserId));

		return updateWebServer;
	}

	public static AuditEvent createAuditEvent(final String aUserId) {
		return AuditEvent.now(new User(aUserId));
	}
}
