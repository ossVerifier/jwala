package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;

import java.util.List;

/**
 * Web server persistence service.
 *
 * Created by JC043760 on 12/17/2015.
 */
public interface WebServerPersistenceService {

    WebServer createWebServer(WebServer webServer, String createdBy);

    WebServer updateWebServer(WebServer webServer, String updatedBy);

    WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException;

    List<WebServer> getWebServers();

    List<WebServer> findWebServers(final String aWebServerNameFragment);

    void removeWebServer(final Identifier<WebServer> aWebServerId);

    List<WebServer> findWebServersBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplications(final String aWebServerName);

    void removeWebServersBelongingTo(final Identifier<Group> aGroupId);

    WebServer findWebServerByName(final String aWebServerName);

    List<Jvm> findJvms(final String aWebServerName);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName);

    void populateWebServerConfig(List<UploadWebServerTemplateRequest> uploadWSTemplateCommands,
                                 User user, boolean overwriteExisting);

    JpaWebServerConfigTemplate uploadWebserverConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest);

    void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    void updateState(Identifier<WebServer> id, WebServerReachableState state);

    void updateErrorStatus(Identifier<WebServer> id, String errorSatus);

    void updateState(Identifier<WebServer> id, WebServerReachableState state, String errorStatus);

    Long getWebServerStartedCount(String groupName);

    Long getWebServerCount(String groupName);

    Long getWebServerStoppedCount(String groupName);

    int removeTemplate(String name);

    int removeTemplate(String webServerName, String templateName);

    List<JpaWebServerConfigTemplate> getJpaWebServerConfigTemplates(String webServerName);
}
