package com.siemens.cto.aem.persistence.dao.webserver;

import java.util.Collection;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

public class WebServerEventsTestHelper {

	public static Event<CreateWebServerCommand> createCreateWebServerEvent(
			final Collection<Identifier<Group>> someGroupIds, final String aNewWebServerName,
			final String aNewWebServerHost, final Integer aNewWebServerPort, final Integer aNewWebServerHttpPort,
			final String aUserId,
            final String aStatusPath) {

		final Event<CreateWebServerCommand> createWebServer = new Event<>(
				new CreateWebServerCommand(someGroupIds, aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort, aNewWebServerHttpPort, aStatusPath),
				createAuditEvent(aUserId));

		return createWebServer;
	}

	public static Event<UpdateWebServerCommand> createUpdateWebServerEvent(
			final Identifier<WebServer> id, final Collection<Identifier<Group>> newGroupIds,
			final String aNewWebServerName, final String aNewWebServerHost,
			final Integer aNewWebServerPort,
            final Integer aNewWebServerHttpsPort,
            final String aUserId,
            final String aStatusPath) {

		final Event<UpdateWebServerCommand> updateWebServer = new Event<>(
				new UpdateWebServerCommand(id, newGroupIds, aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort, aNewWebServerHttpsPort, aStatusPath),
				createAuditEvent(aUserId));

		return updateWebServer;
	}

	public static AuditEvent createAuditEvent(final String aUserId) {
		return AuditEvent.now(new User(aUserId));
	}

}
