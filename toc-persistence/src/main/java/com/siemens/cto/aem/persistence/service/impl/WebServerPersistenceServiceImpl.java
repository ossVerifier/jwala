package com.siemens.cto.aem.persistence.service.impl;

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
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;

import java.util.List;

/**
 * {@link WebServerPersistenceService} implementation.
 * <p/>
 * Created by JC043760 on 12/17/2015.
 */
public class WebServerPersistenceServiceImpl implements WebServerPersistenceService {

    private final GroupCrudService groupCrudService;
    private final WebServerCrudService webServerCrudService;

    public WebServerPersistenceServiceImpl(final GroupCrudService groupCrudService,
                                           final WebServerCrudService webServerCrudService) {
        this.groupCrudService = groupCrudService;
        this.webServerCrudService = webServerCrudService;
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
        final WebServer createdWebServer = webServerCrudService.createWebServer(webServer, createdBy);
        groupCrudService.linkWebServer(createdWebServer.getId(), webServer);
        return webServerCrudService.getWebServer(createdWebServer.getId());
    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String updatedBy) {
        webServerCrudService.updateWebServer(webServer, updatedBy);
        groupCrudService.linkWebServer(webServer);
        return webServerCrudService.getWebServer(webServer.getId());
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerCrudService.getWebServer(aWebServerId);
    }

    @Override
    public List<WebServer> getWebServers() {
        return webServerCrudService.getWebServers();
    }

    @Override
    public List<WebServer> findWebServers(final String aWebServerNameFragment) {
        return webServerCrudService.findWebServers(aWebServerNameFragment);
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        webServerCrudService.removeWebServer(aWebServerId);
    }

    @Override
    public List<WebServer> findWebServersBelongingTo(final Identifier<Group> aGroupId) {
        return webServerCrudService.findWebServersBelongingTo(aGroupId);
    }

    @Override
    public List<Application> findApplications(final String aWebServerName) {
        return webServerCrudService.findApplications(aWebServerName);
    }

    @Override
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {
        webServerCrudService.removeWebServersBelongingTo(aGroupId);
    }

    @Override
    public WebServer findWebServerByName(final String aWebServerName) {
        return webServerCrudService.findWebServerByName(aWebServerName);
    }

    @Override
    public List<Jvm> findJvms(final String aWebServerName) {
        return webServerCrudService.findJvms(aWebServerName);
    }

    @Override
    public List<String> getResourceTemplateNames(final String webServerName) {
        return webServerCrudService.getResourceTemplateNames(webServerName);
    }

    @Override
    public String getResourceTemplate(final String webServerName, final String resourceTemplateName) {
        return webServerCrudService.getResourceTemplate(webServerName, resourceTemplateName);
    }

    @Override
    public void populateWebServerConfig(final List<UploadWebServerTemplateRequest> uploadWSTemplateCommands,
                                        final User user, boolean overwriteExisting) {
        webServerCrudService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
    }

    @Override
    public JpaWebServerConfigTemplate uploadWebServerConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest, String absoluteDeployPath, String userId) {

//        if (absoluteDeployPath.endsWith("/httpd.conf")) {
            // check for an existing httpd.conf
//            WebServer webServer = uploadWebServerTemplateRequest.getWebServer();
//            final String webServerName = webServer.getName();
//            for (String resourceTemplateName : getResourceTemplateNames(webServerName)) {
//                if ("httpd.conf".equals(resourceTemplateName)) {
////                            LOGGER.error("Tried to upload httpd.conf template to {} with already existing httpd.conf template", webServerName);
//                    // TODO need to log this error
//                    throw new InternalErrorException(AemFaultType.HTTPD_CONF_TEMPLATE_ALREADY_EXISTS, webServerName + " already has a template for the httpd.conf. Please delete the existing httpd.conf template and try again.");
//                }
//            }
//
//            // update the web server httpd config path
//            WebServer updateWebServer = new WebServer(
//                    webServer.getId(),
//                    webServer.getGroups(),
//                    webServerName,
//                    webServer.getHost(),
//                    webServer.getPort(),
//                    webServer.getHttpsPort(),
//                    webServer.getStatusPath(),
//                    new FileSystemPath(absoluteDeployPath),
//                    webServer.getSvrRoot(),
//                    webServer.getDocRoot(),
//                    webServer.getState(),
//                    webServer.getErrorStatus());
//            updateWebServer(updateWebServer, userId);
//        }

        return webServerCrudService.uploadWebserverConfigTemplate(uploadWebServerTemplateRequest);
    }

    @Override
    public void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        webServerCrudService.updateResourceTemplate(wsName, resourceTemplateName, template);
    }

    @Override
    public void updateState(final Identifier<WebServer> id, final WebServerReachableState state) {
        webServerCrudService.updateState(id, state);
    }

    @Override
    public void updateErrorStatus(final Identifier<WebServer> id, final String errorStatus) {
        webServerCrudService.updateErrorStatus(id, errorStatus);
    }

    @Override
    public void updateState(final Identifier<WebServer> id, final WebServerReachableState state, final String errorStatus) {
        webServerCrudService.updateState(id, state, errorStatus);
    }

    @Override
    public Long getWebServerStartedCount(final String groupName) {
        return webServerCrudService.getWebServerStartedCount(groupName);
    }

    @Override
    public Long getWebServerCount(final String groupName) {
        return webServerCrudService.getWebServerCount(groupName);
    }

    @Override
    public Long getWebServerStoppedCount(final String groupName) {
        return webServerCrudService.getWebServerStoppedCount(groupName);
    }

    @Override
    public int removeTemplate(final String name) {
        return webServerCrudService.removeTemplate(name);
    }

    @Override
    public int removeTemplate(final String webServerName, final String templateName) {
        return webServerCrudService.removeTemplate(webServerName, templateName);
    }

    @Override
    public List<JpaWebServerConfigTemplate> getJpaWebServerConfigTemplates(final String webServerName) {
        return webServerCrudService.getJpaWebServerConfigTemplates(webServerName);
    }

    @Override
    public String getResourceTemplateMetaData(String webServerName, String resourceTemplateName) {
        return webServerCrudService.getResourceTemplateMetaData(webServerName, resourceTemplateName);
    }

    @Override
    public List<WebServer> getWebServersByGroupName(String groupName) {
        return webServerCrudService.getWebServersByGroupName(groupName);
    }

    @Override
    public boolean checkWebServerResourceFileName(final String groupName, final String webServerName, final String fileName) {
        return webServerCrudService.checkWebServerResourceFileName(groupName, webServerName, fileName);
    }
}
