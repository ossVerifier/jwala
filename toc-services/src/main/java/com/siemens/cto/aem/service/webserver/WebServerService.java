package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerConfigTemplate;

import java.util.List;

public interface WebServerService {

    WebServer createWebServer(final CreateWebServerRequest aCreateWebServerCommand,
            final User aCreatingUser);

	WebServer getWebServer(final Identifier<WebServer> aWebServerId);

	WebServer getWebServer(final String aWebServerName);
	
	List<WebServer> getWebServers();
	
	List<WebServer> findWebServers(final String aWebServerNameFragment);
	
	List<WebServer> findWebServers(final Identifier<Group> aGroupId);
	
	WebServer updateWebServer(final UpdateWebServerRequest anUpdateWebServerCommand,
	            final User anUpdatingUser);
	
	void removeWebServer(final Identifier<WebServer> aWebServerId);
	
	void removeWebServersBelongingTo(final Identifier<Group> aGroupId);

    String generateHttpdConfig(final String aWebServerName, final Boolean withSsl);

    String generateWorkerProperties(final String aWebServerName);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName, final boolean tokensReplaced);

	void populateWebServerConfig(List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting);

	JpaWebServerConfigTemplate uploadWebServerConfig(UploadWebServerTemplateRequest uploadWebServerTemplateCommand, User user);

    String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    String previewResourceTemplate(String webServerName, String groupName, String template);

	JpaWebServer getJpaWebServer(long webServerId, boolean fetchGroups);
}
