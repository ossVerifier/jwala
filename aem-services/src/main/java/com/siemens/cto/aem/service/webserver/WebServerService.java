package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.command.UploadWebServerTemplateCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerConfigTemplate;

import java.util.List;

public interface WebServerService {

    WebServer createWebServer(final CreateWebServerCommand aCreateWebServerCommand,
            final User aCreatingUser);

	WebServer getWebServer(final Identifier<WebServer> aWebServerId);

	WebServer getWebServer(final String aWebServerName);
	
	List<WebServer> getWebServers();
	
	List<WebServer> findWebServers(final String aWebServerNameFragment);
	
	List<WebServer> findWebServers(final Identifier<Group> aGroupId);
	
	WebServer updateWebServer(final UpdateWebServerCommand anUpdateWebServerCommand,
	            final User anUpdatingUser);
	
	void removeWebServer(final Identifier<WebServer> aWebServerId);
	
	void removeWebServersBelongingTo(final Identifier<Group> aGroupId);

    String generateHttpdConfig(final String aWebServerName, final Boolean withSsl);

    String generateWorkerProperties(final String aWebServerName);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName, final boolean tokensReplaced);

	void populateWebServerConfig(List<UploadWebServerTemplateCommand> uploadWSTemplateCommands, User user, boolean overwriteExisting);

	JpaWebServerConfigTemplate uploadWebServerConfig(UploadWebServerTemplateCommand uploadWebServerTemplateCommand, User user);

    String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    String previewResourceTemplate(String webServerName, String groupName, String template);

}
