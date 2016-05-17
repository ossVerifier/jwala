package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.exception.WebServerServiceException;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.WORKERS_PROPS_TEMPLATE;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);
    private static final String INVOKE_WSBAT_TEMPLATE_TPL_PATH = "/InvokeWSBatTemplate.tpl";

    private final WebServerPersistenceService webServerPersistenceService;

    private final FileManager fileManager;

    private final String HTTPD_CONF = "httpd.conf";

    private final ResourceService resourceService;

    private final String templatePath;

    private final String defaultWebServerTemplateNames;

    public WebServerServiceImpl(final WebServerPersistenceService webServerPersistenceService, final FileManager fileManager,
                                final ResourceService resourceService, final String templatePath, final String defaultWebServerTemplateNames) {
        this.webServerPersistenceService = webServerPersistenceService;
        this.fileManager = fileManager;
        this.templatePath = templatePath;
        this.defaultWebServerTemplateNames = defaultWebServerTemplateNames;
        this.resourceService = resourceService;
    }

    @Override
    @Transactional
    public WebServer createWebServer(final CreateWebServerRequest createWebServerRequest,
                                     final User aCreatingUser) {
        createWebServerRequest.validate();

        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id : createWebServerRequest.getGroups()) {
            groups.add(new Group(id, null));
        }
        final WebServer webServer = new WebServer(null,
                groups,
                createWebServerRequest.getName(),
                createWebServerRequest.getHost(),
                createWebServerRequest.getPort(),
                createWebServerRequest.getHttpsPort(),
                createWebServerRequest.getStatusPath(),
                createWebServerRequest.getHttpConfigFile(),
                createWebServerRequest.getSvrRoot(),
                createWebServerRequest.getDocRoot(),
                createWebServerRequest.getState(),
                createWebServerRequest.getErrorStatus());

        return webServerPersistenceService.createWebServer(webServer, aCreatingUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) {
        return webServerPersistenceService.getWebServer(aWebServerId);
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final String aWebServerName) {
        return webServerPersistenceService.findWebServerByName(aWebServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> getWebServers() {
        return webServerPersistenceService.getWebServers();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<WebServer> getWebServersPropagationNew() {
        return webServerPersistenceService.getWebServers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final String aWebServerNameFragment) {
        return webServerPersistenceService.findWebServers(aWebServerNameFragment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final Identifier<Group> aGroupId) {
        return webServerPersistenceService.findWebServersBelongingTo(aGroupId);
    }

    @Override
    @Transactional
    public WebServer updateWebServer(final UpdateWebServerRequest anUpdateWebServerCommand,
                                     final User anUpdatingUser) {
        anUpdateWebServerCommand.validate();

        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id : anUpdateWebServerCommand.getNewGroupIds()) {
            groups.add(new Group(id, null));
        }
        final WebServer webServer = new WebServer(anUpdateWebServerCommand.getId(),
                groups,
                anUpdateWebServerCommand.getNewName(),
                anUpdateWebServerCommand.getNewHost(),
                anUpdateWebServerCommand.getNewPort(),
                anUpdateWebServerCommand.getNewHttpsPort(),
                anUpdateWebServerCommand.getNewStatusPath(),
                anUpdateWebServerCommand.getNewHttpConfigFile(),
                anUpdateWebServerCommand.getNewSvrRoot(),
                anUpdateWebServerCommand.getNewDocRoot(),
                anUpdateWebServerCommand.getState(),
                anUpdateWebServerCommand.getErrorStatus());

        return webServerPersistenceService.updateWebServer(webServer, anUpdatingUser.getId());
    }

    @Override
    @Transactional
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        webServerPersistenceService.removeWebServer(aWebServerId);
    }

    @Override
    public boolean isStarted(WebServer webServer) {
        final WebServerReachableState state = webServer.getState();
        return !WebServerReachableState.WS_UNREACHABLE.equals(state) && !WebServerReachableState.WS_NEW.equals(state);
    }

    @Override
    @Transactional
    public void updateErrorStatus(final Identifier<WebServer> id, final String errorStatus) {
        webServerPersistenceService.updateErrorStatus(id, errorStatus);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // We need state db persistence to succeed even if JMS fails.
    public void updateState(final Identifier<WebServer> id, final WebServerReachableState state, final String errorStatus) {
        webServerPersistenceService.updateState(id, state, errorStatus);
    }

    @Override
    @Transactional
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {
        webServerPersistenceService.removeWebServersBelongingTo(aGroupId);
    }

    @Override
    public String generateInvokeWSBat(WebServer webServer) {
        try {
            // NOTE: invokeWS.bat is internal to TOC that is why the template is not in Db.
            return resourceService.generateResourceFile(FileUtils.readFileToString(new File(templatePath + INVOKE_WSBAT_TEMPLATE_TPL_PATH)),
                    resourceService.generateResourceGroup(), webServer);
        } catch (final IOException ioe) {
            throw new WebServerServiceException("Error generating invokeWS.bat!", ioe);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateHttpdConfig(final String aWebServerName, ResourceGroup resourceGroup) {
        final WebServer server = webServerPersistenceService.findWebServerByName(aWebServerName);

        try {
            String httpdConfText = webServerPersistenceService.getResourceTemplate(aWebServerName, "httpd.conf");
            return ResourceFileGenerator.generateResourceConfig(httpdConfText, resourceGroup, server);
        } catch (NonRetrievableResourceTemplateContentException nrtce) {
            LOGGER.error("Failed to retrieve resource template from the database", nrtce);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, nrtce.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateWorkerProperties(final String aWebServerName) {
        final List<Jvm> jvms = webServerPersistenceService.findJvms(aWebServerName);
        final List<Application> apps = webServerPersistenceService.findApplications(aWebServerName);
        try {
            return ApacheWebServerConfigFileGenerator
                    .getWorkersProperties(aWebServerName, fileManager.getAbsoluteLocation(WORKERS_PROPS_TEMPLATE), jvms, apps);
        } catch (IOException e) {
            LOGGER.error("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(String webServerName) {
        return webServerPersistenceService.getResourceTemplateNames(webServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public String getResourceTemplate(final String webServerName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced, ResourceGroup resourceGroup) {
        final String template = webServerPersistenceService.getResourceTemplate(webServerName, resourceTemplateName);
        if (tokensReplaced) {
            WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
            return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, webServer);
        }
        return template;
    }

    @Override
    public String getResourceTemplateMetaData(String aWebServerName, String resourceTemplateName) {
        return webServerPersistenceService.getResourceTemplateMetaData(aWebServerName, resourceTemplateName);
    }

    @Override
    @Transactional
    public void populateWebServerConfig(List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user,
                                        boolean overwriteExisting) {
        webServerPersistenceService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
    }

    @Override
    @Transactional
    public JpaWebServerConfigTemplate uploadWebServerConfig(UploadWebServerTemplateRequest uploadWebServerTemplateRequest, User user) {
        uploadWebServerTemplateRequest.validate();
        return webServerPersistenceService.uploadWebserverConfigTemplate(uploadWebServerTemplateRequest);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        webServerPersistenceService.updateResourceTemplate(wsName, resourceTemplateName, template);
        return webServerPersistenceService.getResourceTemplate(wsName, resourceTemplateName);
    }

    @Override
    @Transactional(readOnly = true)
    public String previewResourceTemplate(final String webServerName, final String groupName, final String template) {
        return resourceService.generateResourceFile(template, resourceService.generateResourceGroup(),
                webServerPersistenceService.findWebServerByName(webServerName));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWebServerStartedCount(final String groupName) {
        return webServerPersistenceService.getWebServerStartedCount(groupName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWebServerCount(final String groupName) {
        return webServerPersistenceService.getWebServerCount(groupName);
    }

    @Override
    public Long getWebServerStoppedCount(final String groupName) {
        return webServerPersistenceService.getWebServerStoppedCount(groupName);
    }

    @Override
    @Transactional
    public void createDefaultTemplates(final String webServerName) {
        final String[] templateNameArray = defaultWebServerTemplateNames.split(",");
        for (final String templateName : templateNameArray) {
            try {
                final InputStream metaDataIn = new ByteArrayInputStream(FileUtils.readFileToByteArray(
                        new File(templatePath + "/" + templateName + "Properties.json")));
                final InputStream templateIn = new ByteArrayInputStream(FileUtils.readFileToByteArray(
                        new File(templatePath + "/" + templateName + "Template.tpl")));
                resourceService.createTemplate(metaDataIn, templateIn, webServerName);
            } catch (final IOException ioe) {
                throw new WebServerServiceException("Error creating " + templateName + " template!", ioe);
            }
        }
    }
}
