package com.siemens.cto.aem.persistence.dao.webserver;

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
			final Identifier<Group> aGroupId, final String aNewWebServerName,
			final String aNewWebServerHost, final Integer aNewWebServerPort,
			final String aUserId) {

		final Event<CreateWebServerCommand> createWebServer = new Event<>(
				new CreateWebServerCommand(aGroupId, aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort),
				createAuditEvent(aUserId));

		return createWebServer;
	}

	public static Event<UpdateWebServerCommand> createUpdateWebServerEvent(
			final Identifier<WebServer> id, final Identifier<Group> newGroupId,
			final String aNewWebServerName, final String aNewWebServerHost,
			final Integer aNewWebServerPort, final String aUserId) {

		final Event<UpdateWebServerCommand> updateWebServer = new Event<>(
				new UpdateWebServerCommand(id, newGroupId, aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort),
				createAuditEvent(aUserId));

		return updateWebServer;
	}

	public static AuditEvent createAuditEvent(final String aUserId) {
		return AuditEvent.now(new User(aUserId));
	}

}
