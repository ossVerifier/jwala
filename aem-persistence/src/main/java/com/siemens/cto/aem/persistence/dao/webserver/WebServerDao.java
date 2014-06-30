package com.siemens.cto.aem.persistence.dao.webserver;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

public interface WebServerDao {

	WebServer createWebServer(
			final Event<CreateWebServerCommand> aWebServerToCreate);

	WebServer updateWebServer(
			final Event<UpdateWebServerCommand> aWebServerToUpdate);

	WebServer getWebServer(final Identifier<WebServer> aWebServerId)
			throws NotFoundException;

	List<WebServer> getWebServers(final PaginationParameter somePagination);

	List<WebServer> findWebServers(final String aName,
			final PaginationParameter somePagination);

	void removeWebServer(final Identifier<WebServer> aWebServerId);

	List<WebServer> findWebServersBelongingTo(Identifier<Group> aGroupId,
			PaginationParameter aPaginationParam);

	void removeWebServersBelongingTo(final Identifier<Group> aGroupId);

    List<Application> findApplications(final String aWebServerName,
                                       final PaginationParameter somePagination);

    WebServer findWebServerByName(final String aWebServerName);

    List<Jvm> findJvms(final String aWebServerName, final PaginationParameter somePagination);

}
